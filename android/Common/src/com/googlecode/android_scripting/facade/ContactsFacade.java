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
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.PhonesColumns;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Provides access to contacts related functionality.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com
 */
public class ContactsFacade extends RpcReceiver {
  private static final Uri CONTACTS_URI = Uri.parse("content://contacts/people");
  private final ContentResolver mContentResolver;
  private final Service mService;
  private final CommonIntentsFacade mCommonIntentsFacade;

  public ContactsFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mContentResolver = mService.getContentResolver();
    mCommonIntentsFacade = manager.getReceiver(CommonIntentsFacade.class);
  }

  private Uri buildUri(Integer id) {
    Uri uri = ContentUris.withAppendedId(People.CONTENT_URI, id);
    return uri;
  }

  @Rpc(description = "Displays a list of contacts to pick from.", returns = "A map of result values.")
  public Intent pickContact() throws JSONException {
    return mCommonIntentsFacade.pick("content://contacts/people");
  }

  @Rpc(description = "Displays a list of phone numbers to pick from.", returns = "The selected phone number.")
  public String pickPhone() throws JSONException {
    String result = null;
    Intent data = mCommonIntentsFacade.pick("content://contacts/phones");
    if (data != null) {
      Uri phoneData = data.getData();
      Cursor cursor = mService.getContentResolver().query(phoneData, null, null, null, null);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          result = cursor.getString(cursor.getColumnIndexOrThrow(PhonesColumns.NUMBER));
        }
        cursor.close();
      }
    }
    return result;
  }

  @Rpc(description = "Returns a List of all possible attributes for contacts.")
  public List<String> contactsGetAttributes() {
    List<String> result = new ArrayList<String>();
    Cursor cursor = mContentResolver.query(CONTACTS_URI, null, null, null, null);
    if (cursor != null) {
      String[] columns = cursor.getColumnNames();
      for (int i = 0; i < columns.length; i++) {
        result.add(columns[i]);
      }
      cursor.close();
    }
    return result;
  }

  // TODO(MeanEYE.rcf): Add ability to narrow selection by providing named pairs of attributes.
  @Rpc(description = "Returns a List of all contact IDs.")
  public List<Integer> contactsGetIds() {
    List<Integer> result = new ArrayList<Integer>();
    String[] columns = { "_id" };
    Cursor cursor = mContentResolver.query(CONTACTS_URI, columns, null, null, null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        result.add(cursor.getInt(0));
      }
      cursor.close();
    }
    return result;
  }

  @Rpc(description = "Returns a List of all contacts.", returns = "a List of contacts as Maps")
  public List<JSONObject> contactsGet(
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    List<JSONObject> result = new ArrayList<JSONObject>();
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "name", "primary_phone", "primary_email", "type" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    Cursor cursor = mContentResolver.query(CONTACTS_URI, columns, null, null, null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        JSONObject message = new JSONObject();
        for (int i = 0; i < columns.length; i++) {
          message.put(columns[i], cursor.getString(i));
        }
        result.add(message);
      }
      cursor.close();
    }
    return result;
  }

  @Rpc(description = "Returns contacts by ID.")
  public JSONObject contactsGetById(@RpcParameter(name = "id") Integer id,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    JSONObject result = null;
    Uri uri = buildUri(id);
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] { "_id", "name", "primary_phone", "primary_email", "type" };
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    Cursor cursor = mContentResolver.query(uri, columns, null, null, null);
    if (cursor != null) {
      result = new JSONObject();
      cursor.moveToFirst();
      for (int i = 0; i < columns.length; i++) {
        result.put(columns[i], cursor.getString(i));
      }
      cursor.close();
    }
    return result;
  }

  // TODO(MeanEYE.rcf): Add ability to narrow selection by providing named pairs of attributes.
  @Rpc(description = "Returns the number of contacts.")
  public Integer contactsGetCount() {
    Integer result = 0;
    Cursor cursor = mContentResolver.query(CONTACTS_URI, null, null, null, null);
    if (cursor != null) {
      result = cursor.getCount();
      cursor.close();
    }
    return result;
  }

  @Override
  public void shutdown() {
  }
}
