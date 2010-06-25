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

import android.app.Dialog;
import android.app.ProgressDialog;

import com.google.ase.activity.AseServiceHelper;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureResult;

/**
 * Wrapper class for progress dialog running in separate thread
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableProgressDialog extends FutureActivityTask implements RunnableDialog {

  private final CountDownLatch mShowLatch;

  private ProgressDialog mDialog;
  private AseServiceHelper mActivity;

  private final int mStyle;
  private final int mMax;
  private final String mTitle;
  private final String mMessage;
  private final Boolean mCancelable;

  public RunnableProgressDialog(int style, int max, String title, String message, boolean cancelable) {
    mShowLatch = new CountDownLatch(1);
    mStyle = style;
    mMax = max;
    mTitle = title;
    mMessage = message;
    mCancelable = cancelable;
  }

  @Override
  public void run(AseServiceHelper activity, FutureResult result) {
    mActivity = activity;
    mDialog = new ProgressDialog(activity);
    mDialog.setProgressStyle(mStyle);
    mDialog.setMax(mMax);
    mDialog.setCancelable(mCancelable);
    mDialog.setTitle(mTitle);
    mDialog.setMessage(mMessage);
    mDialog.show();
    mShowLatch.countDown();
  }

  @Override
  public Dialog getDialog() {
    return mDialog;
  }

  @Override
  public void dismissDialog() {
    mDialog.dismiss();
    mActivity.taskDone(getTaskId());
  }

  @Override
  public CountDownLatch getShowLatch() {
    return mShowLatch;
  }
}
