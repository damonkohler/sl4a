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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;

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
  private final String[] mButtonTexts;

  public RunnableAlertDialog(String title, String message) {
    mTitle = title;
    mMessage = message;
    mButtonTexts = new String[3];
  }

  /**
   * Set button text.
   *
   * @param buttonNumber
   *          button number
   * @param text
   *          button text
   */
  public void setButton(int buttonNumber, String text) {
    mButtonTexts[buttonNumber] = text;
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void run(final Activity activity, FutureIntent result) {
    mResult = result;
    mDialog = new AlertDialog.Builder(activity).create();
    mDialog.setTitle(mTitle);
    mDialog.setMessage(mMessage);
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.putExtra("which", which);
        mResult.set(intent);
        mDialog.dismiss();
        activity.finish();
      }
    };
    if (mButtonTexts[0] != null) {
      mDialog.setButton(mButtonTexts[0], buttonListener);
    }
    if (mButtonTexts[1] != null) {
      mDialog.setButton2(mButtonTexts[1], buttonListener);
    }
    if (mButtonTexts[2] != null) {
      mDialog.setButton3(mButtonTexts[2], buttonListener);
    }
    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        Intent intent = new Intent();
        intent.putExtra("canceled", true);
        mResult.set(intent);
        activity.finish();
      }
    });
    mDialog.show();
  }
}
