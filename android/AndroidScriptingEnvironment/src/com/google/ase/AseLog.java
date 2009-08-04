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
