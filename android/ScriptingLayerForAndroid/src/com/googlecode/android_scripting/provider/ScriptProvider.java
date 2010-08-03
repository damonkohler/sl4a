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

package com.googlecode.android_scripting.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;

import java.io.File;

public class ScriptProvider extends ContentProvider {

  public static final Uri CONTENT_URI =
      Uri.parse("content://com.googlecode.android_scripting.scriptprovider");
  public static final String SINGLE_MIME = "vnd.android.cursor.item/vnd.sl4a.script";
  public static final String MULTIPLE_MIME = "vnd.android.cursor.dir/vnd.sl4a.script";

  private Context mContext;
  private InterpreterConfiguration mConfiguration;

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    if (uri.getLastPathSegment().equals("scripts")) {
      return MULTIPLE_MIME;
    }
    return SINGLE_MIME;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public boolean onCreate() {
    mContext = getContext();
    mConfiguration = new InterpreterConfiguration(mContext);
    mConfiguration.startDiscovering();
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    String[] columns = { BaseColumns._ID, LiveFolders.NAME, LiveFolders.INTENT };
    MatrixCursor cursor = new MatrixCursor(columns);
    int index = 0;
    for (File script : ScriptStorageAdapter.listExecutableScripts(mConfiguration)) {
      String scriptName = script.getName();
      Intent intent = IntentBuilders.buildStartInBackgroundIntent(scriptName);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      Object[] row = { index, scriptName, intent.toURI() };
      cursor.addRow(row);
      ++index;
    }
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

}
