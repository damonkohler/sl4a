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

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDepreciated;
import com.googlecode.android_scripting.rpc.RpcMinSdk;

import java.lang.reflect.Method;
import java.util.List;

public class ApiProvider extends ContentProvider {

  public static final String SINGLE_MIME = "vnd.android.cursor.item/vnd.sl4a.api";

  private static final int SUGGESTIONS_ID = 1;

  public static final String AUTHORITY = ApiProvider.class.getName().toLowerCase();
  public static final String SUGGESTIONS = "searchSuggestions/*/*";

  private final UriMatcher mUriMatcher;
  private final List<MethodDescriptor> mRpcDescriptors;

  private Context mContext;

  public ApiProvider() {
    mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    mUriMatcher.addURI(AUTHORITY, SUGGESTIONS, SUGGESTIONS_ID);
    mRpcDescriptors = FacadeConfiguration.collectRpcDescriptors();
    for (int i = mRpcDescriptors.size() - 1; i >= 0; i--) {
      MethodDescriptor descriptor = mRpcDescriptors.get(i);
      Method method = descriptor.getMethod();
      if (method.isAnnotationPresent(RpcDepreciated.class)) {
        mRpcDescriptors.remove(i);
      } else if (method.isAnnotationPresent(RpcMinSdk.class)) {
        int requiredSdkLevel = method.getAnnotation(RpcMinSdk.class).value();
        if (FacadeConfiguration.getSdkLevel() < requiredSdkLevel) {
          mRpcDescriptors.remove(i);
        }
      }
    }
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public String getType(Uri uri) {
    return SINGLE_MIME;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public boolean onCreate() {
    mContext = getContext();
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
      String sortOrder) {
    switch (mUriMatcher.match(uri)) {
    case SUGGESTIONS_ID:
      String query = uri.getLastPathSegment().toLowerCase();
      return querySearchSuggestions(query);
    }
    return null;
  }

  private Cursor querySearchSuggestions(String query) {
    String[] columns =
        { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
          SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_QUERY };
    MatrixCursor cursor = new MatrixCursor(columns);
    int index = 0;
    for (MethodDescriptor descriptor : mRpcDescriptors) {
      String name = descriptor.getMethod().getName();
      if (!name.toLowerCase().contains(query)) {
        continue;
      }
      String description = descriptor.getMethod().getAnnotation(Rpc.class).description();
      description = description.replaceAll("\\s+", " ");
      Object[] row = { index, name, description, name };
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
