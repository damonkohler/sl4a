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

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;

import com.google.ase.AseLog;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.jsonrpc.RpcDefaultInteger;
import com.google.ase.jsonrpc.RpcDefaultString;


/**
 * UIFacade
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 * @version 1.0
 */
public class UIFacade implements RpcReceiver {
  private final Context mContext;
  private final Handler mHandler;
  private final Map<String, Object> mObjectMap = new HashMap<String, Object>();
  
  public UIFacade(Context context) {
    mContext = context;
    mHandler = new Handler();
    
    AseLog.v("UI Facade started!");
  }
  
  /**
   * Generates universaly unique identifier
   * which is used in object addressing
   * @return String
   */
  private String getUUID() {
    return String.valueOf(UUID.randomUUID());
  }
    
  /**
   * Get object by UUID
   * @param id
   * @return Object
   */
  private Object getObjectById(String id) {
    Object result = null;
    Object object = null;
    
    if (mObjectMap.containsKey(id)) {
      object = mObjectMap.get(id);
    
      if (object instanceof RunnableProgressDialog) {
        // get ProgressDialog
        result = ((RunnableProgressDialog)object).getDialog();
      }
    }
    
    return result;
  }

  /**
   * Add object by UUID
   * @param id
   * @param obj
   */
  private void addObject(String id, Object obj) {
    mObjectMap.put(id, obj);
  }

  @Rpc(
      description="Create progress dialog of specified type and return its ID.\n" +
                  "Types: (0 - spinner, 1 - horizontal)\n",
      returns="UUID as String"
      )
  public String dialogCreateProgress(
      @RpcParameter("dialog_type") Integer dialog_type,
      @RpcParameter("message") String message,
      @RpcDefaultString(description="Dialog title", defaultValue="") String title
  ) {
    String id = getUUID();
    RunnableProgressDialog thread_obj;

    // create new thread and register dialog
    thread_obj = new RunnableProgressDialog(mContext, dialog_type, title, message);
    
    addObject(id, thread_obj);
    mHandler.post(thread_obj);

    return id;
  }
  
  /**
   * Cancel dialog with specified ID
   * @param id
   */
  @Rpc(description="Cancel dialog with specified ID")
  public void dialogCancel(@RpcParameter("id") String id) {
    Object dialog = getObjectById(id);
    
    if (dialog != null) {
      if (dialog instanceof ProgressDialog)
        ((ProgressDialog) dialog).cancel();
    }
  }
  
  @Override
  public void shutdown() {
    // TODO Auto-generated method stub

  }
}

/**
 * Wrapper class for progress dialog running in separate thread
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
class RunnableProgressDialog implements Runnable {
  private Integer mType;
  private Dialog mDialog;
  private Context mContext;
  
  private String mTitle;
  private String mMessage;
  
  public RunnableProgressDialog(
      final Context context, 
      final Integer dialog_type,
      final String title, 
      final String message
  ) {
    // set local variables
    mType = dialog_type;
    mContext = context;
    mDialog = null;
    mTitle = title;
    mMessage = message;
  }

  /**
   * Returns created dialog
   * @return Dialog
   */
  public Dialog getDialog() {
    return mDialog;
  }
  
  @Override
  public void run() {
    // create dialog object based on type
    switch (mType) {
      case 0:
        // spinner progress
        mDialog = ProgressDialog.show(mContext, mTitle, mMessage);
        break;
        
      case 1:
        // horizontal progress
        break;
    }
  }
}

