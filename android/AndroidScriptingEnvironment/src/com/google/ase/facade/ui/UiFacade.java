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

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import org.json.JSONArray;

import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.util.AndroidRuntimeException;

import com.google.ase.AseApplication;
import com.google.ase.activity.AseServiceHelper;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultBoolean;
import com.google.ase.jsonrpc.RpcDefaultInteger;
import com.google.ase.jsonrpc.RpcOptionalString;
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
  private final Map<String, RunnableDialog> mDialogMap;
  private final Queue<FutureActivityTask> mTaskQueue;

  public UiFacade(Service service) {
    mService = service;
    mTaskQueue = ((AseApplication) mService.getApplication()).getTaskQueue();
    mDialogMap = new HashMap<String, RunnableDialog>();
  }

  private void launchHelper() {
    Intent helper = new Intent(mService, AseServiceHelper.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mService.startActivity(helper);
  }

  /**
   * Returns {@link RunnableDialog} corresponding to a UUID.
   */
  private RunnableDialog getDialogById(String id) {
    if (mDialogMap.containsKey(id)) {
      return mDialogMap.get(id);
    }
    return null;
  }

  /**
   * Adds {@link RunnableDialog} and returns its ID.
   *
   * @return dialog ID
   */
  private String addDialog(RunnableDialog dialog) {
    String id = String.valueOf(UUID.randomUUID());
    mDialogMap.put(id, dialog);
    return id;
  }

  @Rpc(description = "Create a spinner progress dialog.", returns = "Dialog ID as String")
  public String dialogCreateSpinnerProgress(@RpcOptionalString(description = "Title") String title,
      @RpcOptionalString(description = "Message") String message,
      @RpcDefaultInteger(description = "Maximum progress", defaultValue = 100) Integer max,
      @RpcDefaultBoolean(description = "Cancelable", defaultValue = false) Boolean cancelable) {
    return addDialog(new RunnableProgressDialog(ProgressDialog.STYLE_SPINNER, max, title, message,
        cancelable));
  }

  @Rpc(description = "Create a horizontal progress dialog.", returns = "Dialog ID as String")
  public String dialogCreateHorizontalProgress(
      @RpcOptionalString(description = "Title") String title,
      @RpcOptionalString(description = "Message") String message,
      @RpcDefaultInteger(description = "Maximum progress", defaultValue = 100) Integer max,
      @RpcDefaultBoolean(description = "Cancelable", defaultValue = false) Boolean cancelable) {
    return addDialog(new RunnableProgressDialog(ProgressDialog.STYLE_HORIZONTAL, max, title,
        message, cancelable));
  }

  @Rpc(description = "Create alert dialog.", returns = "Dialog ID as String")
  public String dialogCreateAlert(@RpcOptionalString(description = "Title") String title,
      @RpcOptionalString(description = "Message") String message) {
    return addDialog(new RunnableAlertDialog(title, message));
  }

  @Rpc(description = "Dismiss dialog.")
  public void dialogDismiss(@RpcParameter("id") String id) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null) {
      dialog.dismissDialog();
      mDialogMap.remove(id);
    }
  }

  @Rpc(description = "Show dialog.")
  public void dialogShow(@RpcParameter("id") String id) {
    FutureActivityTask task = (FutureActivityTask) getDialogById(id);
    if (task != null) {
      mTaskQueue.offer(task);
      launchHelper();
    }
  }

  @Rpc(description = "Set progress dialog current value.")
  public void dialogSetCurrentProgress(@RpcParameter("id") String id,
      @RpcParameter("current") Integer current) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog.getDialog() instanceof ProgressDialog) {
      ((ProgressDialog) dialog.getDialog()).setProgress(current);
    }
  }

  @Rpc(description = "Set progress dialog maximum value.")
  public void dialogSetMaxProgress(@RpcParameter("id") String id, @RpcParameter("max") Integer max) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog.getDialog() instanceof ProgressDialog) {
      ((ProgressDialog) dialog.getDialog()).setMax(max);
    }
  }

  @Rpc(description = "Set alert dialog positive button text.")
  public void dialogSetPositiveButtonText(@RpcParameter("id") String id,
      @RpcParameter("text") String text) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) dialog).setPositiveButtonText(text);
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNegativeButtonText(@RpcParameter("id") String id,
      @RpcParameter("text") String text) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) dialog).setNegativeButtonText(text);
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNeutralButtonText(@RpcParameter("id") String id,
      @RpcParameter("text") String text) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) dialog).setNeutralButtonText(text);
    }
  }

  // TODO(damonkohler): Make RPC layer translate between JSONArray and List<Object>.
  @Rpc(description = "Set alert dialog list items.")
  public void dialogSetItems(@RpcParameter("id") String id, @RpcParameter("items") JSONArray items) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) dialog).setItems(items);
    }
  }

  @Rpc(description = "Returns dialog response.", returns = "Button number")
  public Intent dialogGetResponse(@RpcParameter("id") String id) {
    FutureActivityTask task = (FutureActivityTask) getDialogById(id);
    try {
      return task.getResult().get();
    } catch (Exception e) {
      throw new AndroidRuntimeException(e);
    }
  }

  @Override
  public void shutdown() {
  }
}
