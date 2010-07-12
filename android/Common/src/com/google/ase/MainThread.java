// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ase;

import android.content.Context;
import android.os.Handler;

import com.google.ase.future.FutureResult;

import java.util.concurrent.Callable;

public class MainThread {

  private MainThread() {
    // Utility class.
  }

  /**
   * Executed in the main thread, returns the result of an execution. Anything that runs here should
   * finish quickly to avoid hanging the UI thread.
   */
  public static <T> T run(Context context, final Callable<T> task) {
    final FutureResult<T> result = new FutureResult<T>();
    Handler handler = new Handler(context.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          result.set(task.call());
        } catch (Exception e) {
          AseLog.e(e);
          result.set(null);
        }
      }
    });
    try {
      return result.get();
    } catch (InterruptedException e) {
      AseLog.e(e);
    }
    return null;
  }
}
