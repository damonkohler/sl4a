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
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
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

  String buildSelectionClause(boolean unreadOnly) {
    if (unreadOnly) {
      return "read = 0";
    }
    return "";
  }

  @Rpc(description = "Sends an SMS to the given recipient.")
  public void smsSend(@RpcParameter(name = "destinationAddress") String destinationAddress,
      @RpcParameter(name = "text") String text) {
    mSms.sendTextMessage(destinationAddress, null, text, null, null);
  }

  @Rpc(description = "Get the number of messages.", returns = "The number of messages.")
  public Integer smsGetMessageCount(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri uri = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    Integer result = 0;
    String selection = buildSelectionClause(unreadOnly);
    try {
      Cursor cursor = mContentResolver.query(uri, null, selection, null, null);
      result = cursor.getCount();
      cursor.close();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message count.");
    }
    return result;
  }

  @Rpc(description = "Get list of message IDs.", returns = "The list of message IDs.")
  public List<Integer> smsGetMessageIds(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcOptional String folder) {
    Uri uri = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    List<Integer> result = new ArrayList<Integer>();
    String selection = buildSelectionClause(unreadOnly);
    String[] columns = { "_id" };
    try {
      Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
      while (cursor.moveToNext()) {
        result.add(cursor.getInt(0));
      }
      cursor.close();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message list.");
    }
    return result;
  }

  @Rpc(description = "Get list of messages.", returns = "List containing messages.")
  public List<JSONObject> smsGetMessages(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcOptional String folder,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    List<JSONObject> result = new ArrayList<JSONObject>();
    Uri uri = Uri.parse("content://sms" + (folder != "" ? "/" + folder : ""));
    String selection = buildSelectionClause(unreadOnly);
    String[] columns;
    if (attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "address", "date", "body", "read" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    try {
      Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
      while (cursor.moveToNext()) {
        JSONObject message = new JSONObject();
        for (int i = 0; i < columns.length; i++) {
          message.put(columns[i], cursor.getString(i));
        }
        result.add(message);
      }
      cursor.close();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving messages.");
    }
    return result;
  }

  @Rpc(description = "Return attributes for message with specified ID.", returns = "Message attributes.")
  public JSONObject smsGetMessageById(@RpcParameter(name = "id") Integer id,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    JSONObject result = new JSONObject();
    Uri uri = Uri.parse("content://sms/" + id);
    String[] columns;
    if (attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "address", "date", "body", "read" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    try {
      Cursor cursor = mContentResolver.query(uri, columns, null, null, null);
      if (cursor.getCount() == 1) {
        cursor.moveToFirst();
        for (int i = 0; i < columns.length; i++) {
          result.put(columns[i], cursor.getString(i));
        }
      }
      cursor.close();
    } catch (Exception e) {
      throw new AseRuntimeException("Error retrieving message with ID: " + id);
    }
    return result;
  }

  @Rpc(description = "Retrieve SMS list possible attributes", returns = "List of attributes")
  public List<String> smsGetAttributes() {
    List<String> result = new ArrayList<String>();
    Cursor cursor = mContentResolver.query(Uri.parse("content://sms"), null, null, null, null);
    String[] columns = cursor.getColumnNames();
    for (int i = 0; i < columns.length; i++) {
      result.add(columns[i]);
    }
    cursor.close();
    return result;
  }

  @Rpc(description = "Delete specified SMS message.", returns = "True if message was deleted.")
  public Boolean smsDeleteMessage(@RpcParameter(name = "id") Integer id) {
    Uri uri = Uri.parse("content://sms/" + id);
    Boolean result = false;
    result = mContentResolver.delete(uri, null, null) > 0;
    return result;
  }

  @Rpc(description = "Mark messages with IDs in list as read.", returns = "Number of updated messages.")
  public Integer smsMarkMessageRead(@RpcParameter(name = "list") JSONArray list,
      @RpcParameter(name = "read") Boolean read) throws JSONException {
    Integer result = 0;
    ContentValues values = new ContentValues();
    values.put("read", read);
    for (int i = 0; i < list.length(); i++) {
      int message_id = list.getInt(i);
      Uri uri = Uri.parse("content://sms/" + message_id);
      result += mContentResolver.update(uri, values, null, null);
    }
    return result;
  }

  @Override
  public void shutdown() {
  }
}