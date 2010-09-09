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
import android.content.Intent;
import android.content.UriMatcher;
import android.content.Intent.ShortcutIconResource;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.LiveFolders;

import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;

import java.io.File;

public class ScriptProvider extends ContentProvider {

  public static final String SINGLE_MIME = "vnd.android.cursor.item/vnd.sl4a.script";
  public static final String MULTIPLE_MIME = "vnd.android.cursor.dir/vnd.sl4a.script";

  private static final int LIVEFOLDER_ID = 1;
  private static final int SUGGESTIONS_ID = 2;

  public static final String AUTHORITY = ScriptProvider.class.getName().toLowerCase();
  public static final String LIVEFOLDER = "liveFolder";
  public static final String SUGGESTIONS = "searchSuggestions/*/*";

  private final UriMatcher mUriMatcher;

  private Context mContext;
  private InterpreterConfiguration mConfiguration;

  public ScriptProvider() {
    mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    mUriMatcher.addURI(AUTHORITY, LIVEFOLDER, LIVEFOLDER_ID);
    mUriMatcher.addURI(AUTHORITY, SUGGESTIONS, SUGGESTIONS_ID);
  }

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
    switch (mUriMatcher.match(uri)) {
    case LIVEFOLDER_ID:
      return queryLiveFolder();
    case SUGGESTIONS_ID:
      String query = uri.getLastPathSegment().toLowerCase();
      return querySearchSuggestions(query);
    default:
      return null;
    }
  }

  private Cursor querySearchSuggestions(String query) {
    String[] columns =
        { BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
          SearchManager.SUGGEST_COLUMN_TEXT_2, SearchManager.SUGGEST_COLUMN_ICON_2,
          SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };
    MatrixCursor cursor = new MatrixCursor(columns);
    int index = 0;
    for (File script : ScriptStorageAdapter.listExecutableScripts(null, mConfiguration)) {
      String scriptName = script.getName().toLowerCase();
      if (!scriptName.contains(query)) {
        continue;
      }
      Interpreter interpreter = mConfiguration.getInterpreterForScript(scriptName);
      String secondLine = interpreter.getNiceName();
      int icon = FeaturedInterpreters.getInterpreterIcon(mContext, interpreter.getExtension());
      Object[] row =
          { index, scriptName, secondLine, icon, scriptName,
            SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
      cursor.addRow(row);
      ++index;
    }
    return cursor;
  }

  private Cursor queryLiveFolder() {
    String[] columns =
        { BaseColumns._ID, LiveFolders.NAME, LiveFolders.INTENT, LiveFolders.ICON_RESOURCE,
          LiveFolders.ICON_PACKAGE, LiveFolders.DESCRIPTION };
    MatrixCursor cursor = new MatrixCursor(columns);
    int index = 0;
    for (File script : ScriptStorageAdapter.listExecutableScriptsRecursively(null, mConfiguration)) {
      int iconId = 0;
      if (script.isDirectory()) {
        iconId = R.drawable.folder;
      } else {
        iconId = FeaturedInterpreters.getInterpreterIcon(mContext, script.getName());
        if (iconId == 0) {
          iconId = R.drawable.sl4a_logo_32;
        }
      }
      ShortcutIconResource icon = ShortcutIconResource.fromContext(mContext, iconId);
      Intent intent = IntentBuilders.buildStartInBackgroundIntent(script);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      String description = script.getAbsolutePath();
      if (description.startsWith(InterpreterConstants.SCRIPTS_ROOT)) {
        description = description.replaceAll(InterpreterConstants.SCRIPTS_ROOT, "scripts/");
      }
      Object[] row =
          { index, script.getName(), intent.toURI(), icon.resourceName, icon.packageName,
            description };
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
