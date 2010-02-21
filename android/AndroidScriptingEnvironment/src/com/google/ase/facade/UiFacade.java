/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

import com.google.ase.AseLog;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultBoolean;
import com.google.ase.jsonrpc.RpcDefaultInteger;
import com.google.ase.jsonrpc.RpcDefaultString;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * UiFacade
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 * @version 1.0
 */
public class UiFacade implements RpcReceiver {
  private final Service mService;
  private final Handler mHandler;
  private final Map<String, Object> mObjectMap = new HashMap<String, Object>();
  private final Map<String, CountDownLatch> mLockMap = new HashMap<String, CountDownLatch>();

  public UiFacade(Service service) {
    mService = service;
    mHandler = new Handler();
    AseLog.v("UI Facade started!");
  }

  /**
   * Returns universally unique identifier which is used in object addressing
   */
  private String getUUID() {
    return String.valueOf(UUID.randomUUID());
  }

  /**
   * Returns object corresponding to a UUID.
   *
   * @param id
   *          UUID of object to return
   * @return Object
   */
  private Object getObjectById(String id) {
    Object result = null;
    Object object = null;
    if (mObjectMap.containsKey(id)) {
      object = mObjectMap.get(id);
      // Progress dialog.
      if (object instanceof RunnableProgressDialog) {
        result = ((RunnableProgressDialog) object).getDialog();
      } else
      // Alert dialog.
      if (object instanceof RunnableAlertDialog) {
        result = ((RunnableAlertDialog) object).getDialog();
      }
    }
    return result;
  }

  /**
   * Add object by UUID
   *
   * @param id
   *          UUID of object to add
   * @param obj
   *          object to add
   */
  private void addObject(String id, Object obj) {
    mObjectMap.put(id, obj);
  }

  @Rpc(description = "Create progress dialog of specified type and return its ID.\nTypes: 0 - spinner, 1 - horizontal\n", returns = "UUID as String")
  public String dialogCreateProgress(
      @RpcParameter("dialog_type") Integer dialog_type,
      @RpcDefaultString(description = "Dialog title", defaultValue = "") String title,
      @RpcDefaultString(description = "Dialog message", defaultValue = "") String message,
      @RpcDefaultBoolean(description = "Can dialog be canceled", defaultValue = false) Boolean cancelable) {
    String id = getUUID();
    RunnableProgressDialog thread_obj;
    CountDownLatch latch = new CountDownLatch(1);

    // Show signal.
    CountDownLatch show_latch = new CountDownLatch(1);
    mLockMap.put(id, show_latch);

    // Create new thread and register dialog.
    thread_obj =
        new RunnableProgressDialog(mService, latch, show_latch, dialog_type, title, message,
            cancelable);

    addObject(id, thread_obj);
    mHandler.post(thread_obj);

    // Wait for dialog to be created.
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      AseLog.e("Interrupted while waiting for handler to complete.", e);
    }

    AseLog.v("Created progress dialog with id: " + id);
    return id;
  }

  @Rpc(description = "Create alert dialog", returns = "UUID as String")
  public String dialogCreateAlert(
      @RpcDefaultString(description = "Dialog title", defaultValue = "") String title,
      @RpcDefaultString(description = "Dialog message", defaultValue = "") String message,
      @RpcDefaultBoolean(description = "Can dialog be canceled", defaultValue = false) Boolean cancelable) {
    String id = getUUID();
    RunnableAlertDialog thread_obj;
    CountDownLatch latch = new CountDownLatch(1);

    // Show signal.
    CountDownLatch show_latch = new CountDownLatch(1);
    mLockMap.put(id, show_latch);

    // Create new thread and register dialog.
    thread_obj = new RunnableAlertDialog(mService, latch, show_latch, title, message, cancelable);

    addObject(id, thread_obj);
    mHandler.post(thread_obj);

    // Wait for dialog to be created.
    try {
      latch.await(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      AseLog.e("Interrupted while waiting for handler to complete.", e);
    }

    AseLog.v("Created alert dialog with id: " + id);
    return id;
  }

  @Rpc(description = "Dismiss dialog with specified ID")
  public void dialogDismiss(@RpcParameter("id") String id) {
    Object dialog = getObjectById(id);
    if (dialog != null) {
      // Progress dialog.
      if (dialog instanceof ProgressDialog) {
        ((ProgressDialog) dialog).dismiss();
      } else
      // Alert dialog.
      if (dialog instanceof AlertDialog) {
        ((AlertDialog) dialog).dismiss();
      }
      // Remove objects from maps so GC can deal with them.
      mObjectMap.remove(id);
      mLockMap.remove(id);
      AseLog.v("Dismissed dialog with id: " + id);
    }
  }

  @Rpc(description = "Allow selected dialog to be shown")
  public void dialogShow(@RpcParameter("id") String id) {
    CountDownLatch show_latch;
    if (mLockMap.containsKey(id)) {
      show_latch = mLockMap.get(id);
      show_latch.countDown();
    }
    AseLog.v("Allowed dialog ID: " + id + " to show himself");
  }

  @Rpc(description = "Set progress dialog maximum value")
  public void dialogProgressSetMax(@RpcParameter("id") String id, @RpcParameter("max") Integer max) {
    Object dialog = getObjectById(id);
    if (dialog != null) {
      if (dialog instanceof ProgressDialog)
        ((ProgressDialog) dialog).setMax(max);
    }
  }

  @Rpc(description = "Set progress dialog current value")
  public void dialogProgressSetCurrent(@RpcParameter("id") String id,
      @RpcParameter("current") Integer current) {
    Object dialog = getObjectById(id);
    if (dialog != null) {
      if (dialog instanceof ProgressDialog)
        ((ProgressDialog) dialog).setProgress(current);
    }
  }

  @Rpc(description = "Set dialog title")
  public void dialogSetTitle(@RpcParameter("id") String id, @RpcParameter("title") String title) {
    Object dialog = getObjectById(id);
    if (dialog != null) {
      // Progress dialog.
      if (dialog instanceof ProgressDialog) {
        ((ProgressDialog) dialog).setTitle(title);
      } else
      // Alert dialog.
      if (dialog instanceof AlertDialog) {
        ((AlertDialog) dialog).setTitle(title);
      }
    }
  }

  @Rpc(description = "Set dialog message")
  public void dialogSetMessage(@RpcParameter("id") String id,
      @RpcParameter("message") String message) {
    Object dialog = getObjectById(id);
    if (dialog != null) {
      // Progress dialog.
      if (dialog instanceof ProgressDialog) {
        ((ProgressDialog) dialog).setMessage(message);
      } else
      // Alert dialog.
      if (dialog instanceof AlertDialog) {
        ((AlertDialog) dialog).setMessage(message);
      }
    }
  }

  @Rpc(description = "Set dialog message")
  public String dialogSetButton(@RpcParameter("id") String id,
      @RpcDefaultInteger(description = "Button number", defaultValue = 0) Integer button,
      @RpcParameter("text") String text) {
    String result = null;
    if (mObjectMap.containsKey(id)) {
      Object object = mObjectMap.get(id);
      // Alert dialog.
      if (object instanceof RunnableAlertDialog) {
        ((RunnableAlertDialog) object).setButton(button, text);
        result = String.valueOf(((RunnableAlertDialog) object).getDialog());
      }
    }
    return result;
  }

  @Rpc(description = "Retrieves response from dialog", returns = "Result")
  public Object dialogGetResponse(@RpcParameter("id") String id) {
    Object result = null;
    if (mObjectMap.containsKey(id)) {
      Object object = mObjectMap.get(id);
      // Alert dialog.
      if (object instanceof RunnableAlertDialog) {
        result = ((RunnableAlertDialog) object).mResponse;
      }
    }
    return result;
  }

  @Override
  public void shutdown() {
    // Clean up.
    mObjectMap.clear();
    mLockMap.clear();
  }
}

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

/**
 * Wrapper class for alert dialog running in separate thread
 *
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableAlertDialog implements Runnable {
  private AlertDialog mDialog;
  private final Service mService;
  private final OnClickListener mListener;
  private final CountDownLatch mLatch;
  private final CountDownLatch mShowLatch;
  private final String mTitle;
  private final String mMessage;
  private final Boolean mCancelable;
  public int mResponse = 0;

  public RunnableAlertDialog(final Service context, final CountDownLatch latch,
      final CountDownLatch show_latch, final String title, final String message,
      final Boolean cancelable) {
    // Set local variables
    mService = context;
    mLatch = latch;
    mShowLatch = show_latch;
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
   * @param num
   *          button number
   * @param text
   *          button text
   */
  public void setButton(Integer num, String text) {
    switch (num) {
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
    mDialog = new AlertDialog.Builder(mService).create();
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
