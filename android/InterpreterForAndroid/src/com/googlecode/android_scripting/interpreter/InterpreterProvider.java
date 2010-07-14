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

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterStrings;

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
 * To create an interpreter APK, please extend this content provider and
 * implement getDescriptor() and getEnvironmentSettings().<br>
 * Please declare the provider in the android manifest xml (the authority values
 * has to be set to your_package_name.provider_name).
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class InterpreterProvider extends ContentProvider {

  protected static final int BASE = 1;
  protected static final int ENVVARS = 2;
  protected static final int ARGS = 3;

  protected InterpreterDescriptor mDescriptor;
  protected Context mContext;
  protected UriMatcher matcher;
  private SharedPreferences mPreferences;

  public static final String MIME = "vnd.android.cursor.item/vnd.googlecode.interpreter";

  protected InterpreterProvider() {
    matcher = new UriMatcher(UriMatcher.NO_MATCH);
    String auth = this.getClass().getName().toLowerCase();
    matcher.addURI(auth, InterpreterConstants.PROVIDER_BASE, BASE);
    matcher.addURI(auth, InterpreterConstants.PROVIDER_ENV, ENVVARS);
    matcher.addURI(auth, InterpreterConstants.PROVIDER_ARGS, ARGS);
  }

  /**
   * Should return an instance instance of a class that implements interpreter
   * descriptor.
   */
  protected abstract InterpreterDescriptor getDescriptor();

  /**
   * Should return a map of environment variables names and their values (or
   * null if interpreter does not require any environment variables).
   */
  protected abstract Map<String, String> getEnvironmentSettings();

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

    Map<String, ? extends Object> map;

    switch (matcher.match(uri)) {
      case BASE:
        map = getSettings();
        break;
      case ENVVARS:
        map = getEnvironmentSettings();
        break;
      case ARGS:
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

  protected boolean isInterpreterInstalled() {
    return mPreferences.getBoolean(InterpreterConstants.INSTALL_PREF, false);
  }

  protected Cursor buildCursorFromMap(Map<String, ? extends Object> map) {
    if (map == null) {
      return null;
    }
    MatrixCursor cursor = new MatrixCursor(map.keySet().toArray(new String[map.size()]));
    cursor.addRow(map.values());
    return cursor;
  }

  protected Map<String, Object> getSettings() {
    Map<String, Object> values = new HashMap<String, Object>();

    values.put(InterpreterStrings.NAME, mDescriptor.getName());
    values.put(InterpreterStrings.NICE_NAME, mDescriptor.getNiceName());
    values.put(InterpreterStrings.EXTENSION, mDescriptor.getExtension());
    values.put(InterpreterStrings.PATH, mDescriptor.getPath(mContext));
    values.put(InterpreterStrings.BIN, mDescriptor.getBinary());
    values.put(InterpreterStrings.EXECUTE, mDescriptor.getExecuteCommand(mContext));
    values.put(InterpreterStrings.EMPTY_PARAMS, mDescriptor.getEmptyParams(mContext));
    values.put(InterpreterStrings.EXECUTE_PARAMS, mDescriptor.getExecuteParams(mContext));

    return values;
  }

  protected Map<String, Object> getArguments() {
    String[] arguments = mDescriptor.getExecuteArgs(mContext);
    if (arguments == null) {
      return null;
    }
    Map<String, Object> values = new LinkedHashMap<String, Object>();
    for (String argument : arguments) {
      values.put(argument, argument);
    }
    return values;
  }
}
