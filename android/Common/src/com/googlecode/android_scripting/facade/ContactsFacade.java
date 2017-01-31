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
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts.People;
import android.provider.Contacts.PhonesColumns;
import android.util.Log;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
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
  private static final String TAG = "ContactsFacade";
  private static final Uri CONTACTS_URI = Uri.parse("content://contacts/people");
  private final ContentResolver mContentResolver;
  private final Service mService;
  private final CommonIntentsFacade mCommonIntentsFacade;
  private final ContactsStatusReceiver mContactsStatusReceiver;
  private final EventFacade mEventFacade;

  private Uri mPhoneContent = null;
  private String mContactId;
  private String mPrimary;
  private String mPhoneNumber;
  private String mHasPhoneNumber;

  public ContactsFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mContentResolver = mService.getContentResolver();
    mCommonIntentsFacade = manager.getReceiver(CommonIntentsFacade.class);
    mContactsStatusReceiver = new ContactsStatusReceiver();
    mContentResolver.registerContentObserver(
        ContactsContract.Contacts.CONTENT_URI, true, mContactsStatusReceiver);
    mEventFacade = manager.getReceiver(EventFacade.class);
    try {
      // Backward compatibility... get contract stuff using reflection
      Class<?> phone = Class.forName("android.provider.ContactsContract$CommonDataKinds$Phone");
      mPhoneContent = (Uri) phone.getField("CONTENT_URI").get(null);
      mContactId = (String) phone.getField("CONTACT_ID").get(null);
      mPrimary = (String) phone.getField("IS_PRIMARY").get(null);
      mPhoneNumber = (String) phone.getField("NUMBER").get(null);
      mHasPhoneNumber = (String) phone.getField("HAS_PHONE_NUMBER").get(null);
    } catch (Exception e) {
        Log.e(TAG, "Unable to get field from Contacts Database");
    }
  }

  private Uri getUri(Integer id) {
      return ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
    return uri;
  }

  @Rpc(
    description = "Displays a list of contacts to pick from.",
    returns = "A map of result values."
  )
  public Intent contactsDisplayContactPickList() throws JSONException {
    return mCommonIntentsFacade.pick("content://contacts/people");
  }

  @Rpc(
    description = "Displays a list of phone numbers to pick from.",
    returns = "The selected phone number."
  )
  public String contactsDisplayPhonePickList() throws JSONException {
    String phoneNumber = null;
    String result = null;
    Intent data = mCommonIntentsFacade.pick("content://contacts/phones");
    if (data != null) {
      Uri phoneData = data.getData();
      Cursor cursor = mService.getContentResolver().query(phoneData, null, null, null, null);
      if (cursor != null) {
        if (cursor.moveToFirst()) {
          phoneNumber =
              cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.NUMBER));
        }
        cursor.close();
      }
    }
    return phoneNumber;
  }

  @Rpc(description = "Returns a List of all possible attributes for contacts.")
  public List<String> contactsGetAttributes() {
    List<String> attributes = new ArrayList<String>();
    Cursor cursor = mContentResolver.query(CONTACTS_URI, null, null, null, null);
    if (cursor != null) {
      String[] columns = cursor.getColumnNames();
      for (int i = 0; i < columns.length; i++) {
        attributes.add(columns[i]);
      }
      cursor.close();
    }
    return attributes;
  }

  @Rpc(description = "Returns a List of all contact IDs.")
  public List<Integer> contactsGetContactIds() {
    List<Integer> ids = new ArrayList<Integer>();
    String[] columns = {"_id"};
    Cursor cursor = mContentResolver.query(CONTACTS_URI, columns, null, null, null);
    if (cursor != null) {
      while (cursor.moveToNext()) {
        ids.add(cursor.getInt(0));
      }
      cursor.close();
    }
    return ids;
  }

  @Rpc(description = "Returns a List of all contacts.", returns = "a List of contacts as Maps")
  public List<JSONObject> contactsGetAllContacts(
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes) throws JSONException {
    List<JSONObject> contacts = new ArrayList<JSONObject>();
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] {"_id", "name", "primary_phone", "primary_email", "type"};
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    List<String> queryList = new ArrayList<String>();
    for (String s : columns) {
      queryList.add(s);
    }
    if (!queryList.contains("_id")) {
      queryList.add("_id");
    }

    String[] query = queryList.toArray(new String[queryList.size()]);
    Cursor cursor = mContentResolver.query(CONTACTS_URI, query, null, null, null);
    if (cursor != null) {
      int idIndex = cursor.getColumnIndex("_id");
      while (cursor.moveToNext()) {
        String id = cursor.getString(idIndex);
        JSONObject message = new JSONObject();
        for (int i = 0; i < columns.length; i++) {
          String key = columns[i];
          String value = cursor.getString(cursor.getColumnIndex(key));
          if (mPhoneNumber != null) {
            if (key.equals("primary_phone")) {
              value = findPhone(id);
            }
          }
          message.put(key, value);
        }
        contacts.add(message);
      }
      cursor.close();
    }
    return contacts;
  }

  private String findPhone(String id) {
    String phoneNumber = null;
    if (id == null || id.equals("")) {
      return phoneNumber;
    }
    try {
      if (Integer.parseInt(id) > 0) {
        Cursor pCur =
            mContentResolver.query(
                mPhoneContent,
                new String[] {mPhoneNumber},
                mContactId + " = ? and " + mPrimary + "=1",
                new String[] {id},
                null);
        if (pCur != null) {
          pCur.getColumnNames();
          while (pCur.moveToNext()) {
            phoneNumber = pCur.getString(0);
            break;
          }
        }
        pCur.close();
      }
    } catch (Exception e) {
      return null;
    }
    return phoneNumber;
  }

  @Rpc(description = "Returns contacts by ID.")
  public JSONObject contactsGetContactById(
      @RpcParameter(name = "id") Integer id,
      @RpcParameter(name = "attributes") @RpcOptional JSONArray attributes)
      throws JSONException {
    JSONObject contact = null;
    Uri uri = getUri(id);
    String[] columns;
    if (attributes == null || attributes.length() == 0) {
      // In case no attributes are specified we set the default ones.
      columns = new String[] {"_id", "name", "primary_phone", "primary_email", "type"};
    } else {
      // Convert selected attributes list into usable string list.
      columns = new String[attributes.length()];
      for (int i = 0; i < attributes.length(); i++) {
        columns[i] = attributes.getString(i);
      }
    }
    Cursor cursor = mContentResolver.query(uri, columns, null, null, null);
    if (cursor != null) {
      contact = new JSONObject();
      cursor.moveToFirst();
      for (int i = 0; i < columns.length; i++) {
        contact.put(columns[i], cursor.getString(i));
      }
      cursor.close();
    }
    return contact;
  }

  @Rpc(description = "Returns the number of contacts.")
  public Integer contactsGetCount() {
    Integer count = 0;
    Cursor cursor = mContentResolver.query(CONTACTS_URI, null, null, null, null);
    if (cursor != null) {
      count = cursor.getCount();
      cursor.close();
    }
    return count;
  }

  private String[] jsonToArray(JSONArray array) throws JSONException {
    String[] resultingArray = null;
    if (array != null && array.length() > 0) {
      resultingArray = new String[array.length()];
      for (int i = 0; i < array.length(); i++) {
          resultingArray[i] = array.getString(i);
      }
    }
    return resultingArray;
  }

  private Uri getAllContactsVcardUri() {
    Cursor cursor =
        mContentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            new String[] {ContactsContract.Contacts.LOOKUP_KEY},
            null,
            null,
            null);
    if (cursor == null) {
      return null;
    }
    try {
      StringBuilder uriListBuilder = new StringBuilder();
      int index = 0;
      while (cursor.moveToNext()) {
        if (index != 0) {
          uriListBuilder.append(':');
        }
        uriListBuilder.append(cursor.getString(0));
        index++;
      }
      return Uri.withAppendedPath(
          ContactsContract.Contacts.CONTENT_MULTI_VCARD_URI, Uri.encode(uriListBuilder.toString()));
    } finally {
      cursor.close();
    }
  }

  @Rpc(description = "Erase all contacts in phone book.")
  public void contactsEraseAll() {
    Cursor cursor =
        mContentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            new String[] {ContactsContract.Contacts.LOOKUP_KEY},
            null,
            null,
            null);
    if (cursor == null) {
      return;
    }
    while (cursor.moveToNext()) {
      Uri uri =
          Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, cursor.getString(0));
      mContentResolver.delete(uri, null, null);
    }
    return;
  }

  /**
   * Exactly as per <a href=
   * "http://developer.android.com/reference/android/content/ContentResolver.html#query%28android.net.Uri,%20java.lang.String[],%20java.lang.String,%20java.lang.String[],%20java.lang.String%29"
   * >ContentResolver.query</a>
   */
  @Rpc(description = "Content Resolver Query", returns = "result of query as Maps")
  public List<JSONObject> contactsQueryContent(
      @RpcParameter(
            name = "uri",
            description = "The URI, using the content:// scheme, for the content to retrieve."
          )
          String uri,
      @RpcParameter(
            name = "attributes",
            description = "A list of which columns to return. Passing null will return all columns"
          )
          @RpcOptional
          JSONArray attributes,
      @RpcParameter(name = "selection", description = "A filter declaring which rows to return")
          @RpcOptional
          String selection,
      @RpcParameter(
            name = "selectionArgs",
            description =
                "You may include ?s in selection, which will be replaced by the values from selectionArgs"
          )
          @RpcOptional
          JSONArray selectionArgs,
      @RpcParameter(name = "order", description = "How to order the rows") @RpcOptional
          String order)
      throws JSONException {
    List<JSONObject> queryResults = new ArrayList<JSONObject>();
    String[] columns = jsonToArray(attributes);
    String[] args = jsonToArray(selectionArgs);
    Cursor cursor = mContentResolver.query(Uri.parse(uri), columns, selection, args, order);
    if (cursor != null) {
      String[] names = cursor.getColumnNames();
      while (cursor.moveToNext()) {
        JSONObject message = new JSONObject();
        for (int i = 0; i < cursor.getColumnCount(); i++) {
          String key = names[i];
          String value = cursor.getString(i);
          message.put(key, value);
        }
        queryResults.add(message);
      }
      cursor.close();
    }
    return queryResults;
  }

  @Rpc(
    description = "Content Resolver Query Attributes",
    returns = "a list of available columns for a given content uri"
  )
  public JSONArray queryAttributes(
      @RpcParameter(
            name = "uri",
            description = "The URI, using the content:// scheme, for the content to retrieve."
          )
          String uri)
      throws JSONException {
    JSONArray columns = new JSONArray();
    Cursor cursor = mContentResolver.query(Uri.parse(uri), null, "1=0", null, null);
    if (cursor != null) {
      String[] names = cursor.getColumnNames();
      for (String name : names) {
        columns.put(name);
      }
      cursor.close();
    }
    return columns;
  }

  @Rpc(description = "Launches VCF import.")
  public void importVcf(
      @RpcParameter(
            name = "uri",
            description = "The URI, using the file:/// scheme, for the content to retrieve."
          )
          String uri) {
    Intent intent = new Intent();
    intent.setComponent(
        new ComponentName(
            "com.google.android.contacts",
            "com.android.contacts.common.vcard.ImportVCardActivity"));
    intent.setData(Uri.parse(uri));
    mService.startActivity(intent);
  }

  @Rpc(description = "Launches VCF export.")
  public void exportVcf(
      @RpcParameter(
            name = "path",
            description = "The file path, using the / scheme, for the content to save to."
          )
          String path) {
    OutputStream out = null;
    StringBuilder string = new StringBuilder();
    try {
      AssetFileDescriptor fd =
          mContentResolver.openAssetFileDescriptor(getAllContactsVcardUri(), "r");
      FileInputStream inputStream = fd.createInputStream();
      PrintWriter writer = new PrintWriter(path, "UTF-8");
      int character;
      while ((character = inputStream.read()) != -1) {
        if ((char) character != '\r') {
          string.append((char) character);
        }
      }
      writer.append(string);
      writer.close();
    } catch (IOException e) {
      Log.w(TAG, "Failed to export VCF.");
    }
  }

  private class ContactsStatusReceiver extends ContentObserver {
    public ContactsStatusReceiver() {
      super(null);
    }

    public void onChange(boolean updated) {
      mEventFacade.postEvent("ContactsChanged", null);
    }
  }

  @Override
  public void shutdown() {
      mContentResolver.unregisterContentObserver(mContactsStatusReceiver);
  }
}
