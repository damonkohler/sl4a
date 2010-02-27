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
import java.util.UUID;

import android.app.ProgressDialog;
import android.app.Service;
import android.os.Handler;

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
  private final Map<String, RunnableDialog> mDialogMap = new HashMap<String, RunnableDialog>();

  public UiFacade(Service service, Handler handler) {
    mService = service;
    mHandler = handler;
  }

  /**
   * Returns universally unique identifier which is used in object addressing
   */
  private String getUuid() {
    return String.valueOf(UUID.randomUUID());
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
   * Add {@link RunnableDialog} by UUID.
   */
  private void addDialog(String id, RunnableDialog dialog) {
    mDialogMap.put(id, dialog);
  }

  @Rpc(description = "Create a spinner progress dialog.", returns = "Dialog ID as String")
  public String dialogCreateSpinnerProgress(
      @RpcDefaultString(description = "Title", defaultValue = "") String title,
      @RpcDefaultString(description = "Message", defaultValue = "") String message,
      @RpcDefaultBoolean(description = "Cancelable", defaultValue = false) boolean cancelable) {
    String id = getUuid();
    addDialog(id, new RunnableProgressDialog(mService, ProgressDialog.STYLE_SPINNER, title,
        message, cancelable));
    return id;
  }

  @Rpc(description = "Create a horizontal progress dialog.", returns = "Dialog ID as String")
  public String dialogCreateProgress(
      @RpcDefaultString(description = "Title", defaultValue = "") String title,
      @RpcDefaultString(description = "Message", defaultValue = "") String message,
      @RpcDefaultBoolean(description = "Cancelable", defaultValue = false) boolean cancelable) {
    String id = getUuid();
    addDialog(id, new RunnableProgressDialog(mService, ProgressDialog.STYLE_HORIZONTAL, title,
        message, cancelable));
    return id;
  }

  @Rpc(description = "Create alert dialog.", returns = "Dialog ID as String")
  public String dialogCreateAlert(
      @RpcDefaultString(description = "Title", defaultValue = "") String title,
      @RpcDefaultString(description = "Message", defaultValue = "") String message,
      @RpcDefaultBoolean(description = "Cancelable", defaultValue = false) boolean cancelable) {
    String id = getUuid();
    addDialog(id, new RunnableAlertDialog(mService, title, message, cancelable));
    return id;
  }

  @Rpc(description = "Dismiss dialog")
  public void dialogDismiss(@RpcParameter("id") String id) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null) {
      dialog.getDialog().dismiss();
      mDialogMap.remove(id);
    }
  }

  @Rpc(description = "Show dialog.")
  public void dialogShow(@RpcParameter("id") String id) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null) {
      mHandler.post(dialog);
    }
  }

  @Rpc(description = "Set progress dialog maximum value.")
  public void dialogProgressSetMax(@RpcParameter("id") String id, @RpcParameter("max") int max) {
    Object dialog = getDialogById(id);
    if (dialog != null) {
      if (dialog instanceof ProgressDialog)
        ((ProgressDialog) dialog).setMax(max);
    }
  }

  @Rpc(description = "Set progress dialog current value.")
  public void dialogProgressSetCurrent(@RpcParameter("id") String id,
      @RpcParameter("current") int current) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog.getDialog() instanceof ProgressDialog) {
      ((ProgressDialog) dialog.getDialog()).setProgress(current);
    }
  }

  @Rpc(description = "Set dialog title.")
  public void dialogSetTitle(@RpcParameter("id") String id, @RpcParameter("title") String title) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null) {
      dialog.getDialog().setTitle(title);
    }
  }

  @Rpc(description = "Set dialog message.")
  public void dialogSetMessage(@RpcParameter("id") String id,
      @RpcParameter("message") String message) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null) {
      dialog.setMessage(message);
    }
  }

  @Rpc(description = "Set dialog button text.")
  public void dialogSetButton(@RpcParameter("id") String id,
      @RpcDefaultInteger(description = "Button number", defaultValue = 0) int button,
      @RpcParameter("text") String text) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      ((RunnableAlertDialog) dialog).setButton(button, text);
    }
  }

  @Rpc(description = "Returns dialog response.", returns = "Button number")
  public Object dialogGetResponse(@RpcParameter("id") String id) {
    RunnableDialog dialog = getDialogById(id);
    if (dialog != null && dialog instanceof RunnableAlertDialog) {
      return ((RunnableAlertDialog) dialog).getResponse();
    }
    return null;
  }

  @Override
  public void shutdown() {
  }
}
