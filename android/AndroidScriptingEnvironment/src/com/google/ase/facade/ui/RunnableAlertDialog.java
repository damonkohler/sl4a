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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

import com.google.ase.AseLog;

/**
 * Wrapper class for alert dialog running in separate thread
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog implements Runnable {
  private AlertDialog mDialog;
  private final Context mActivity;
  private final OnClickListener mListener;
  private final CountDownLatch mLatch;
  private final CountDownLatch mShowLatch;
  private final String mTitle;
  private final String mMessage;
  private final Boolean mCancelable;
  public int mResponse = 0;

  // TODO(damonkohler): This needs to accept a service not a context.
  public RunnableAlertDialog(Context activity, final CountDownLatch latch,
      final CountDownLatch showLatch, final String title, final String message,
      final Boolean cancelable) {
    mActivity = activity;
    mLatch = latch;
    mShowLatch = showLatch;
    mDialog = null;
    mTitle = title;
    mMessage = message;
    mCancelable = cancelable;
    // Event listener for dialog buttons.
    mListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        mResponse = which;
      }
    };
  }

  /**
   * Returns created dialog.
   */
  public Object getDialog() {
    return mDialog;
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
    switch (buttonNumber) {
      case 0:
        mDialog.setButton(text, mListener);
        break;
      case 1:
        mDialog.setButton2(text, mListener);
        break;
      case 2:
        mDialog.setButton3(text, mListener);
        break;
    }
  }

  @Override
  public void run() {
    mDialog = new AlertDialog.Builder(mActivity).create();
    mDialog.setCancelable(mCancelable);
    mDialog.setTitle(mTitle);
    mDialog.setMessage(mMessage);
    // Allow main thread to continue and wait for show signal.
    mLatch.countDown();
    try {
      mShowLatch.await();
    } catch (InterruptedException e) {
      AseLog.e("Interrupted while waiting for handler to complete.", e);
    }
    mDialog.show();
  }
}
