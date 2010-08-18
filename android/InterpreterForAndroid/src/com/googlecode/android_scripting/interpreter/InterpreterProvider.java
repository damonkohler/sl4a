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

package com.googlecode.android_scripting.interpreter;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A provider that can be queried to obtain execution-related interpreter info.
 * 
 * <p>
 * To create an interpreter APK, please extend this content provider and implement getDescriptor()
 * and getEnvironmentSettings().<br>
 * Please declare the provider in the android manifest xml (the authority values has to be set to
 * your_package_name.provider_name).
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class InterpreterProvider extends ContentProvider {

  private static final int PROPERTIES = 1;
  private static final int ENVIRONMENT_VARIABLES = 2;
  private static final int ARGUMENTS = 3;

  private UriMatcher mUriMatcher;
  private SharedPreferences mPreferences;

  private InterpreterDescriptor mDescriptor;
  private Context mContext;

  public static final String MIME = "vnd.android.cursor.item/vnd.googlecode.interpreter";

  public InterpreterProvider() {
    mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    String auth = this.getClass().getName().toLowerCase();
    mUriMatcher.addURI(auth, InterpreterConstants.PROVIDER_PROPERTIES, PROPERTIES);
    mUriMatcher.addURI(auth, InterpreterConstants.PROVIDER_ENVIRONMENT_VARIABLES,
        ENVIRONMENT_VARIABLES);
    mUriMatcher.addURI(auth, InterpreterConstants.PROVIDER_ARGUMENTS, ARGUMENTS);
  }

  /**
   * Returns an instance of the class that implements the desired {@link InterpreterDescriptor}.
   */
  protected abstract InterpreterDescriptor getDescriptor();

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return MIME;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public boolean onCreate() {
    mDescriptor = getDescriptor();
    mContext = getContext();
    mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    return false;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    if (!isInterpreterInstalled()) {
      return null;
    }
    Map<String, String> map;
    switch (mUriMatcher.match(uri)) {
    case PROPERTIES:
      map = getProperties();
      break;
    case ENVIRONMENT_VARIABLES:
      map = getEnvironmentVariables();
      break;
    case ARGUMENTS:
      map = getArguments();
      break;
    default:
      map = null;
    }
    return buildCursorFromMap(map);
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  private boolean isInterpreterInstalled() {
    return mPreferences.getBoolean(InterpreterConstants.INSTALLED_PREFERENCE_KEY, false);
  }

  private Cursor buildCursorFromMap(Map<String, String> map) {
    if (map == null) {
      return null;
    }
    MatrixCursor cursor = new MatrixCursor(map.keySet().toArray(new String[map.size()]));
    cursor.addRow(map.values());
    return cursor;
  }

  private Map<String, String> getProperties() {
    Map<String, String> values = new HashMap<String, String>();
    values.put(InterpreterPropertyNames.NAME, mDescriptor.getName());
    values.put(InterpreterPropertyNames.NICE_NAME, mDescriptor.getNiceName());
    values.put(InterpreterPropertyNames.EXTENSION, mDescriptor.getExtension());
    values.put(InterpreterPropertyNames.BINARY, mDescriptor.getBinary(mContext).getAbsolutePath());
    values.put(InterpreterPropertyNames.INTERACTIVE_COMMAND, mDescriptor
        .getInteractiveCommand(mContext));
    values.put(InterpreterPropertyNames.SCRIPT_COMMAND, mDescriptor.getScriptCommand(mContext));
    values.put(InterpreterPropertyNames.HAS_INTERACTIVE_MODE, Boolean.toString(mDescriptor
        .hasInteractiveMode()));
    return values;
  }

  private Map<String, String> getEnvironmentVariables() {
    Map<String, String> values = new HashMap<String, String>();
    values.putAll(mDescriptor.getEnvironmentVariables(mContext));
    return values;
  }

  private Map<String, String> getArguments() {
    Map<String, String> values = new LinkedHashMap<String, String>();
    int column = 0;
    for (String argument : mDescriptor.getArguments(mContext)) {
      values.put(Integer.toString(column), argument);
      column++;
    }
    return values;
  }
}
