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

import com.google.ase.ActivityRunnable;
import com.google.ase.FutureIntent;

/**
 * Wrapper class for alert dialog running in separate thread.
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog extends ActivityRunnable implements RunnableDialog {
  private AlertDialog mDialog;
  private final String mTitle;
  private final String mMessage;
  private final boolean mCancelable;
  private FutureIntent mResult;

  public RunnableAlertDialog(String title, String message, boolean cancelable) {
    mTitle = title;
    mMessage = message;
    mCancelable = cancelable;
  }

  /**
   * Set button text
   *
   * @param buttonNumber
   *          button number
   * @param text
   *          button text
   */
  public void setButton(Integer buttonNumber, String text) {
    DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent();
        intent.putExtra("which", which);
        mResult.set(intent);
      }
    };
    switch (buttonNumber) {
      case 0:
        mDialog.setButton(text, listener);
        break;
      case 1:
        mDialog.setButton2(text, listener);
        break;
      case 2:
        mDialog.setButton3(text, listener);
        break;
    }
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void setMessage(String message) {
    mDialog.setMessage(message);
  }

  @Override
  public void run(Activity activity, FutureIntent result) {
    mResult = result;
    mDialog = new AlertDialog.Builder(activity).create();
    mDialog.setCancelable(mCancelable);
    mDialog.setTitle(mTitle);
    mDialog.setMessage(mMessage);
    mDialog.show();
  }

}
