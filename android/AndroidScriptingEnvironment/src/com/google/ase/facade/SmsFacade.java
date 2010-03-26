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

import android.app.Service;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.gsm.SmsManager;

import com.google.ase.exception.AseRuntimeException;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

/**
 * Provides access to SMS related functionality.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class SmsFacade implements RpcReceiver {

  private final Service mService;
  private final ContentResolver mContentResolver;
  private final SmsManager mSms;

  public SmsFacade(Service service) {
    mService = service;
    mContentResolver = mService.getContentResolver();
    mSms = SmsManager.getDefault();
  }

  @Rpc(description = "Sends an SMS to the given recipient.")
  public void sendSms(@RpcParameter(name = "destinationAddress") String destinationAddress,
      @RpcParameter(name = "text") String text) {
    mSms.sendTextMessage(destinationAddress, null, text, null, null);
  }

  @Rpc(description = "Get the number of messages.", returns = "The number of messages.")
  public Integer getSmsMessageCount(@RpcParameter(name = "unread_only") Boolean unread_only,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri uri = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    Integer result = 0;
    String selection = "";
    if (unread_only) {
      selection = "read = 0";
    }
    try {
      Cursor cursor = mContentResolver.query(uri, null, selection, null, null);
      result = cursor.getCount();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message count.");
    }
    return result;
  }

  // TODO(damonkohler): This should probably just return the messages as a list of maps and by-pass
  // the use of IDs. Maps would be {fromAddress=..., message=...}
  @Rpc(description = "Get the list of messages.", returns = "The list of message IDs.")
  public List<Integer> getSmsMessageList(@RpcParameter(name = "unread_only") Boolean unread_only,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri uri = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    List<Integer> result = new ArrayList<Integer>();
    String selection = "";
    if (unread_only) {
      selection = "read = 0";
    }
    try {
      Cursor cursor = mContentResolver.query(uri, null, selection, null, null);
      while (cursor.moveToNext())
        result.add(cursor.getInt(0));
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message list.");
    }
    return result;
  }

  @Override
  public void shutdown() {
  }
}