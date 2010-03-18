/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class AseLog {
  private AseLog() {
    // Utility class.
  }

  private static String getTag() {
    StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    String fullClassName = stackTraceElements[4].getClassName();
    String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
    int lineNumber = stackTraceElements[4].getLineNumber();
    return "ase." + className + ":" + lineNumber;
  }

  private static void toast(Context context, String message) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
  }

  public static void v(String message) {
    Log.v(getTag(), message);
  }

  public static void v(String message, Throwable e) {
    Log.v(getTag(), message, e);
  }

  public static void v(Context context, String message) {
    toast(context, message);
    Log.v(getTag(), message);
  }

  public static void v(Context context, String message, Throwable e) {
    toast(context, message);
    Log.v(getTag(), message, e);
  }

  public static void e(Throwable e) {
    Log.e(getTag(), "Error", e);
  }

  public static void e(String message) {
    Log.e(getTag(), message);
  }

  public static void e(String message, Throwable e) {
    Log.e(getTag(), message, e);
  }

  public static void e(Context context, String message) {
    toast(context, message);
    Log.e(getTag(), message);
  }

  public static void e(Context context, String message, Throwable e) {
    toast(context, message);
    Log.e(getTag(), message, e);
  }

  public static void w(Throwable e) {
    Log.w(getTag(), "Warning", e);
  }

  public static void w(String message) {
    Log.w(getTag(), message);
  }

  public static void w(String message, Throwable e) {
    Log.w(getTag(), message, e);
  }

  public static void w(Context context, String message) {
    toast(context, message);
    Log.w(getTag(), message);
  }

  public static void w(Context context, String message, Throwable e) {
    toast(context, message);
    Log.w(getTag(), message, e);
  }
}
