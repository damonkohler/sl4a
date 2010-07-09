// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.ase;

import android.app.Service;
import android.os.Handler;

import com.google.ase.future.FutureResult;

import java.util.concurrent.Callable;

public class MainThread {

  private MainThread() {
    // Utility class.
  }

  public static <T> T init(Service service, final Callable<T> task) {
    final FutureResult<T> result = new FutureResult<T>();
    Handler handler = new Handler(service.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        try {
          result.set(task.call());
        } catch (Exception e) {
          AseLog.e(e);
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
