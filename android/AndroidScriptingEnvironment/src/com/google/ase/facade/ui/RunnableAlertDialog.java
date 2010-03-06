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

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.ase.exception.AseRuntimeException;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureIntent;

/**
 * Wrapper class for alert dialog running in separate thread.
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog extends FutureActivityTask implements RunnableDialog {
  private AlertDialog mDialog;
  private final String mTitle;
  private final String mMessage;
  private FutureIntent mResult;
  private Activity mActivity;
  private CharSequence[] mItems;
  private String mPositiveButtonText;
  private String mNegativeButtonText;
  private String mNeutralButtonText;

  public RunnableAlertDialog(String title, String message) {
    mTitle = title;
    mMessage = message;
  }

  public void setPositiveButtonText(String text) {
    mPositiveButtonText = text;
  }

  public void setNegativeButtonText(String text) {
    mNegativeButtonText = text;
  }

  public void setNeutralButtonText(String text) {
    mNeutralButtonText = text;
  }

  /**
   * Set list items.
   *
   * @param Items
   */
  public void setItems(JSONArray items) {
    if (mItems == null) {
      mItems = new CharSequence[items.length()];
      for (int i = 0; i < items.length(); i++) {
        try {
          mItems[i] = items.getString(i);
        } catch (JSONException e) {
          throw new AseRuntimeException(e);
        }
      }
    }
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void run(final Activity activity, FutureIntent result) {
    mActivity = activity;
    mResult = result;
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    if (mTitle != null) {
      builder.setTitle(mTitle);
    }
    // Can't display both a message and items. We'll elect to show the items instead.
    if (mMessage != null && mItems == null) {
      builder.setMessage(mMessage);
    }
    if (mItems != null) {
      builder.setItems(mItems, new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int item) {
          Intent intent = new Intent();
          intent.putExtra("item", item);
          mResult.set(intent);
          // TODO(damonkohler): This leaves the dialog in the UiFacade map of dialogs. Memory leak.
          dialog.dismiss();
          activity.finish();
        }
      });
    }
    configureButtons(builder, activity);
    addOnCancelListener(builder, activity);
    mDialog = builder.show();
  }

  private Builder addOnCancelListener(final AlertDialog.Builder builder, final Activity activity) {
    return builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("canceled", true);
        mResult.set(intent);
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
        Intent intent = new Intent();
        intent.putExtra("which", which);
        mResult.set(intent);
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
    if (mNeutralButtonText != null) {
      builder.setNeutralButton(mNeutralButtonText, buttonListener);
    }
  }

  @Override
  public void dismissDialog() {
    mDialog.dismiss();
    mActivity.finish();
  }
}
