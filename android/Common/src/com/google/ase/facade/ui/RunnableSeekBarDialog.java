/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.facade.ui;

import java.util.concurrent.CountDownLatch;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.util.AndroidRuntimeException;
import android.widget.SeekBar;

import com.google.ase.AseLog;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureResult;

/**
 * Wrapper class for dialog box with seek bar.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class RunnableSeekBarDialog extends FutureActivityTask implements RunnableDialog {

  private final CountDownLatch mShowLatch;

  private AlertDialog mDialog;
  private SeekBar mSeekBar;

  private Activity mActivity;
  private FutureResult mResult;

  private final int mProgress;
  private final int mMax;
  private final String mTitle;
  private final String mMessage;

  private String mPositiveButtonText;
  private String mNegativeButtonText;

  public RunnableSeekBarDialog(int progress, int max, String title, String message) {
    mShowLatch = new CountDownLatch(1);
    mProgress = progress;
    mMax = max;
    mTitle = title;
    mMessage = message;
  }

  public void setPositiveButtonText(String text) {
    mPositiveButtonText = text;
  }

  public void setNegativeButtonText(String text) {
    mNegativeButtonText = text;
  }

  @Override
  public void run(Activity activity, FutureResult result) {
    mActivity = activity;
    mResult = result;
    mSeekBar = new SeekBar(activity);
    mSeekBar.setMax(mMax);
    mSeekBar.setProgress(mProgress);
    mSeekBar.setPadding(10, 0, 10, 3);
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if (mTitle != null) {
      builder.setTitle(mTitle);
    }
    if (mMessage != null) {
      builder.setMessage(mMessage);
    }
    builder.setView(mSeekBar);
    configureButtons(builder, activity);
    addOnCancelListener(builder, activity);
    mDialog = builder.show();
    mShowLatch.countDown();
  }

  private Builder addOnCancelListener(final AlertDialog.Builder builder, final Activity activity) {
    return builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        JSONObject result = new JSONObject();
        try {
          result.put("canceled", true);
          result.put("progress", mSeekBar.getProgress());
        } catch (JSONException e) {
          AseLog.e(e);
        }
        mResult.set(result);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.finish();
      }
    });
  }

  private void configureButtons(final AlertDialog.Builder builder, final Activity activity) {
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        JSONObject result = new JSONObject();
        switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          try {
            result.put("which", "positive");
            result.put("progress", mSeekBar.getProgress());
          } catch (JSONException e) {
            throw new AndroidRuntimeException(e);
          }
          break;
        case DialogInterface.BUTTON_NEGATIVE:
          try {
            result.put("which", "negative");
            result.put("progress", mSeekBar.getProgress());
          } catch (JSONException e) {
            throw new AndroidRuntimeException(e);
          }
          break;
        }
        mResult.set(result);
        // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
        dialog.dismiss();
        activity.finish();
      }
    };
    if (mNegativeButtonText != null) {
      builder.setNegativeButton(mNegativeButtonText, buttonListener);
    }
    if (mPositiveButtonText != null) {
      builder.setPositiveButton(mPositiveButtonText, buttonListener);
    }
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void dismissDialog() {
    mDialog.dismiss();
    mActivity.finish();
  }

  @Override
  public CountDownLatch getShowLatch() {
    return mShowLatch;
  }
}
