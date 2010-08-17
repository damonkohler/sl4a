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

package com.googlecode.android_scripting.facade.ui;

import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.ProgressDialog;
import android.app.Service;
import android.util.AndroidRuntimeException;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * UiFacade
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class UiFacade extends RpcReceiver {
  private final Service mService;
  private final FutureActivityTaskExecutor mTaskQueue;
  private DialogTask mDialogTask;

  public UiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mTaskQueue = ((BaseApplication) mService.getApplication()).getTaskQueue();
  }

  @Rpc(description = "Create a text input dialog.")
  public void dialogCreateInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Value") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message)
      throws InterruptedException {
    dialogDismiss();
    mDialogTask = new AlertDialogTask(title, message);
    ((AlertDialogTask) mDialogTask).setTextInput();
  }

  @Rpc(description = "Create a password input dialog.")
  public void dialogCreatePassword(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Password") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message) {
    dialogDismiss();
    mDialogTask = new AlertDialogTask(title, message);
    ((AlertDialogTask) mDialogTask).setPasswordInput();
  }

  @SuppressWarnings("unchecked")
  @Rpc(description = "Queries the user for a text input.")
  public String dialogGetInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Value") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message)
      throws InterruptedException {
    dialogCreateInput(title, message);
    dialogShow();
    Map<String, Object> response = (Map<String, Object>) dialogGetResponse();
    return (String) response.get("value");
  }

  @SuppressWarnings("unchecked")
  @Rpc(description = "Queries the user for a password.")
  public String dialogGetPassword(
      @RpcParameter(name = "title", description = "title of the password box") @RpcDefault("Password") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message)
      throws InterruptedException {
    dialogCreatePassword(title, message);
    dialogShow();
    Map<String, Object> response = (Map<String, Object>) dialogGetResponse();
    return (String) response.get("value");
  }

  @Rpc(description = "Create a spinner progress dialog.")
  public void dialogCreateSpinnerProgress(@RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message,
      @RpcParameter(name = "maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new ProgressDialogTask(ProgressDialog.STYLE_SPINNER, max, title, message, true);
  }

  @Rpc(description = "Create a horizontal progress dialog.")
  public void dialogCreateHorizontalProgress(
      @RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message,
      @RpcParameter(name = "maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask =
        new ProgressDialogTask(ProgressDialog.STYLE_HORIZONTAL, max, title, message, true);
  }

  @Rpc(description = "Create alert dialog.")
  public void dialogCreateAlert(@RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new AlertDialogTask(title, message);
  }

  @Rpc(description = "Create seek bar dialog.")
  public void dialogCreateSeekBar(
      @RpcParameter(name = "starting value") @RpcDefault("50") Integer progress,
      @RpcParameter(name = "maximum value") @RpcDefault("100") Integer max,
      @RpcParameter(name = "title") String title, @RpcParameter(name = "message") String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new SeekBarDialogTask(progress, max, title, message);
  }

  @Rpc(description = "Create time picker dialog.")
  public void dialogCreateTimePicker(
      @RpcParameter(name = "hour") @RpcDefault("0") Integer hour,
      @RpcParameter(name = "minute") @RpcDefault("0") Integer minute,
      @RpcParameter(name = "is24hour", description = "Use 24 hour clock") @RpcDefault("false") Boolean is24hour) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new TimePickerDialogTask(hour, minute, is24hour);
  }

  @Rpc(description = "Create date picker dialog.")
  public void dialogCreateDatePicker(@RpcParameter(name = "year") @RpcDefault("1970") Integer year,
      @RpcParameter(name = "month") @RpcDefault("1") Integer month,
      @RpcParameter(name = "day") @RpcDefault("1") Integer day) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new DatePickerDialogTask(year, month, day);
  }

  @Rpc(description = "Dismiss dialog.")
  public void dialogDismiss() {
    if (mDialogTask != null) {
      mDialogTask.dismissDialog();
      mDialogTask = null;
    }
  }

  @Rpc(description = "Show dialog.")
  public void dialogShow() throws InterruptedException {
    if (mDialogTask != null && mDialogTask.getDialog() == null) {
      mTaskQueue.execute(mDialogTask);
      mDialogTask.getShowLatch().await();
    } else {
      throw new RuntimeException("No dialog to show.");
    }
  }

  @Rpc(description = "Set progress dialog current value.")
  public void dialogSetCurrentProgress(@RpcParameter(name = "current") Integer current) {
    if (mDialogTask != null && mDialogTask instanceof ProgressDialogTask) {
      ((ProgressDialog) mDialogTask.getDialog()).setProgress(current);
    } else {
      throw new RuntimeException("No valid dialog to assign value to.");
    }
  }

  @Rpc(description = "Set progress dialog maximum value.")
  public void dialogSetMaxProgress(@RpcParameter(name = "max") Integer max) {
    if (mDialogTask != null && mDialogTask instanceof ProgressDialogTask) {
      ((ProgressDialog) mDialogTask.getDialog()).setMax(max);
    } else {
      throw new RuntimeException("No valid dialog to set maximum value of.");
    }
  }

  @Rpc(description = "Set alert dialog positive button text.")
  public void dialogSetPositiveButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setPositiveButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof SeekBarDialogTask) {
      ((SeekBarDialogTask) mDialogTask).setPositiveButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNegativeButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setNegativeButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof SeekBarDialogTask) {
      ((SeekBarDialogTask) mDialogTask).setNegativeButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNeutralButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setNeutralButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  // TODO(damonkohler): Make RPC layer translate between JSONArray and List<Object>.
  @Rpc(description = "Set alert dialog list items.")
  public void dialogSetItems(@RpcParameter(name = "items") JSONArray items) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setItems(items);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Set dialog single choice items and selected item.")
  public void dialogSetSingleChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "selected item index") @RpcDefault("0") Integer selected) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setSingleChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Set dialog multiple choice items and selection.")
  public void dialogSetMultiChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "list of selected items") @RpcOptional JSONArray selected)
      throws JSONException {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setMultiChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Returns dialog response.")
  public Object dialogGetResponse() {
    try {
      return mDialogTask.getResult();
    } catch (Exception e) {
      throw new AndroidRuntimeException(e);
    }
  }

  @Rpc(description = "This method provides list of items user selected.", returns = "Selected items")
  public Set<Integer> dialogGetSelectedItems() {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      return ((AlertDialogTask) mDialogTask).getSelectedItems();
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Display a WebView with the given URL.")
  public void webViewShow(@RpcParameter(name = "url") String url) {
    WebViewTask task = new WebViewTask(url, mManager.getReceiver(EventFacade.class));
    mTaskQueue.execute(task);
  }

  @Override
  public void shutdown() {
  }
}
