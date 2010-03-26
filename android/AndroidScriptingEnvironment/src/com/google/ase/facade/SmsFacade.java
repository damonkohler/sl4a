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

package com.google.ase.facade;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import android.app.Service;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import com.google.ase.exception.AseRuntimeException;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

/**
 * SMSFacade
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class SmsFacade implements RpcReceiver {
  private final Service mService;
  private final ContentResolver mContentResolver;

  public SmsFacade(Service service) {
    mService = service;
    mContentResolver = mService.getContentResolver();
  }

  @Rpc(description = "Return number of messages")
  public Integer getMessageCount(@RpcParameter(name = "unread_only") Boolean unread_only,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri mURI = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    Integer result = 0;
    String cond = "";
    if (unread_only)
      cond = "read = 0";
    try {
      Cursor cursor = mContentResolver.query(mURI, null, cond, null, null);
      result = cursor.getCount();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message count.");
    }
    return result;
  }

  @Rpc(description = "Return list containing message id's")
  public List<Integer> getMessageList(@RpcParameter(name = "unread_only") Boolean unread_only,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri mURI = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    List<Integer> result = new ArrayList<Integer>();
    String cond = "";
    if (unread_only)
      cond = "read = 0";
    try {
      Cursor cursor = mContentResolver.query(mURI, null, cond, null, null);
      while (cursor.moveToNext())
        result.add(cursor.getInt(0));
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message list.");
    }
    return result;
  }

  public JSONArray getMessage(Integer id) {
    return null;
  }

  @Override
  public void shutdown() {
  }
}