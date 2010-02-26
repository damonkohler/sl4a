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

import android.app.ProgressDialog;
import android.app.Service;

import com.google.ase.AseLog;

/**
 * Wrapper class for progress dialog running in separate thread
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableProgressDialog implements Runnable {
  private ProgressDialog mDialog;
  private final Service mService;
  private final CountDownLatch mLatch;
  private final CountDownLatch mShowLatch;

  private Integer mType = ProgressDialog.STYLE_SPINNER;
  private final String mTitle;
  private final String mMessage;
  private final Boolean mCancelable;

  public RunnableProgressDialog(final Service service, final CountDownLatch latch,
      final CountDownLatch show_latch, final Integer dialog_type, final String title,
      final String message, final Boolean cancelable) {
    // Set local variables.
    mType = dialog_type;
    mService = service;
    mLatch = latch;
    mShowLatch = show_latch;
    mDialog = null;
    mTitle = title;
    mMessage = message;
    mCancelable = cancelable;
  }

  /**
   * Returns created dialog
   *
   * @return Object
   */
  public Object getDialog() {
    return mDialog;
  }

  @Override
  public void run() {
    mDialog = new ProgressDialog(mService);
    mDialog.setProgressStyle(mType);
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
