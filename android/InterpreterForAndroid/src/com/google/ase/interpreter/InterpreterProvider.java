package com.google.ase.interpreter;

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
import java.util.Map;

public abstract class InterpreterProvider extends ContentProvider {

  protected static final int BASE = 1;
  protected static final int ENVVARS = 2;

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
  }

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
    values.put(InterpreterStrings.EMPTY_PARAMS, mDescriptor.getEmptyParams());
    values.put(InterpreterStrings.EXECUTE_PARAMS, mDescriptor.getExecuteParams());
    values.put(InterpreterStrings.EXECUTE, mDescriptor.getExecuteCommand());
    values.put(InterpreterStrings.PATH, mDescriptor.getPath(mContext));
    values.put(InterpreterStrings.BIN, mDescriptor.getBinary());

    return values;
  }

  protected abstract InterpreterDescriptor getDescriptor();

  protected abstract Map<String, String> getEnvironmentSettings();
}
