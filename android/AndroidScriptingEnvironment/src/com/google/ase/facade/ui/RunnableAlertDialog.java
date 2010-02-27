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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Wrapper class for alert dialog running in separate thread
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog implements RunnableDialog {
  private final AlertDialog mDialog;
  private final Context mActivity;
  private final String mTitle;
  private final String mMessage;
  private final Boolean mCancelable;
  private int mResponse = 0;

  // TODO(damonkohler): This needs to accept a service not a context.
  public RunnableAlertDialog(Context activity, String title, String message, boolean cancelable) {
    mActivity = activity;
    mTitle = title;
    mMessage = message;
    mCancelable = cancelable;
    mDialog = new AlertDialog.Builder(mActivity).create();
  }

  public int getResponse() {
    return mResponse;
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
        mResponse = which;
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
  public void run() {
    mDialog.setCancelable(mCancelable);
    mDialog.setTitle(mTitle);
    mDialog.setMessage(mMessage);
    mDialog.show();
  }

}
