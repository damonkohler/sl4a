/*
 * Copyright (C) 2016 Google Inc.
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

package com.googlecode.android_scripting;

import android.content.Context;
import android.os.Handler;

import com.googlecode.android_scripting.future.FutureResult;

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
          Log.e(e);
          result.set(null);
        }
      }
    });
    try {
      return result.get();
    } catch (InterruptedException e) {
      Log.e(e);
    }
    return null;
  }

  public static void run(Context context, final Runnable task) {
    Handler handler = new Handler(context.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        task.run();
      }
    });
  }
}
