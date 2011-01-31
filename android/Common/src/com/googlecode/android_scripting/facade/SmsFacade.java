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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.gsm.SmsManager;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides access to SMS related functionality.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class SmsFacade extends RpcReceiver {

  private final Service mService;
  private final ContentResolver mContentResolver;
  private final SmsManager mSms;

  public SmsFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mContentResolver = mService.getContentResolver();
    mSms = SmsManager.getDefault();
  }

  String buildSelectionClause(boolean unreadOnly) {
    if (unreadOnly) {
      return "read = 0";
    }
    return "";
  }

  private Uri buildFolderUri(String folder) {
    Uri.Builder builder = Uri.parse("content://sms").buildUpon();
    builder.appendPath(folder);
    Uri uri = builder.build();
    Log.v("Built SMS URI: " + uri);
    return uri;
  }

  private Uri buildMessageUri(Integer id) {
    Uri.Builder builder = Uri.parse("content://sms").buildUpon();
    ContentUris.appendId(builder, id);
    Uri uri = builder.build();
    return uri;
  }

  @Rpc(description = "Sends an SMS.")
  public void smsSend(
      @RpcParameter(name = "destinationAddress", description = "typically a phone number") String destinationAddress,
      @RpcParameter(name = "text") String text) {
    mSms.sendTextMessage(destinationAddress, null, text, null, null);
  }

  @Rpc(description = "Returns the number of messages.")
  public Integer smsGetMessageCount(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcDefault("inbox") String folder) {
    Uri uri = buildFolderUri(folder);
    Integer result = 0;
    String selection = buildSelectionClause(unreadOnly);
    Cursor cursor = mContentResolver.query(uri, null, selection, null, null);
    if (cursor != null) {
      result = cursor.getCount();
      cursor.close();
    } else {
      result = 0;
    }
    return result;
  }

  @Rpc(description = "Returns a List of all message IDs.")
  public List<Integer> smsGetMessageIds(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcDefault("inbox") String folder) {
    Uri uri = buildFolderUri(folder);
    List<Integer> result = new ArrayList<Integer>();
    String selection = buildSelectionClause(unreadOnly);
    String[] columns = { "_id" };
    Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
    while (cursor != null && cursor.moveToNext()) {
      result.add(cursor.getInt(0));
    }
    cursor.close();
    return result;
  }

  @Rpc(description = "Returns a List of all messages.", returns = "a List of messages as Maps")
  public List<JSONObject> smsGetMessages(@RpcParameter(name = "unreadOnly") Boolean unreadOnly,
      @RpcParameter(name = "folder") @RpcDefault("inbox") String folder,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    List<JSONObject> result = new ArrayList<JSONObject>();
    Uri uri = buildFolderUri(folder);
    String selection = buildSelectionClause(unreadOnly);
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "address", "date", "body", "read" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    Cursor cursor = mContentResolver.query(uri, columns, selection, null, null);
    while (cursor != null && cursor.moveToNext()) {
      JSONObject message = new JSONObject();
      for (int i = 0; i < columns.length; i++) {
        message.put(columns[i], cursor.getString(i));
      }
      result.add(message);
    }
    cursor.close();
    return result;
  }

  @Rpc(description = "Returns message attributes.")
  public JSONObject smsGetMessageById(
      @RpcParameter(name = "id", description = "message ID") Integer id,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    JSONObject result = new JSONObject();
    Uri uri = buildMessageUri(id);
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "address", "date", "body", "read" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    Cursor cursor = mContentResolver.query(uri, columns, null, null, null);
    if (cursor != null) {
      cursor.moveToFirst();
      for (int i = 0; i < columns.length; i++) {
        result.put(columns[i], cursor.getString(i));
      }
      cursor.close();
    } else {
      result = null;
    }
    return result;
  }

  @Rpc(description = "Returns a List of all possible message attributes.")
  public List<String> smsGetAttributes() {
    List<String> result = new ArrayList<String>();
    Cursor cursor = mContentResolver.query(Uri.parse("content://sms"), null, null, null, null);
    if (cursor != null) {
      String[] columns = cursor.getColumnNames();
      for (int i = 0; i < columns.length; i++) {
        result.add(columns[i]);
      }
      cursor.close();
    } else {
      result = null;
    }
    return result;
  }

  @Rpc(description = "Deletes a message.", returns = "True if the message was deleted")
  public Boolean smsDeleteMessage(@RpcParameter(name = "id") Integer id) {
    Uri uri = buildMessageUri(id);
    Boolean result = false;
    result = mContentResolver.delete(uri, null, null) > 0;
    return result;
  }

  @Rpc(description = "Marks messages as read.", returns = "number of messages marked read")
  public Integer smsMarkMessageRead(
      @RpcParameter(name = "ids", description = "List of message IDs to mark as read.") JSONArray ids,
      @RpcParameter(name = "read") Boolean read) throws JSONException {
    Integer result = 0;
    ContentValues values = new ContentValues();
    values.put("read", read);
    for (int i = 0; i < ids.length(); i++) {
      Uri uri = buildMessageUri(ids.getInt(i));
      result += mContentResolver.update(uri, values, null, null);
    }
    return result;
  }

  @Override
  public void shutdown() {
  }
}