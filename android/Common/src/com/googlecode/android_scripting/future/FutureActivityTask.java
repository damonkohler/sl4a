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

package com.googlecode.android_scripting.future;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;

import com.googlecode.android_scripting.activity.Sl4aServiceHelper;

/**
 * Encapsulates an {@link Activity} and a {@link FutureObject}.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class FutureActivityTask<T> {
  private final static AtomicInteger mNextFutureTaskId = new AtomicInteger(0);
  private final FutureResult<T> mResult = new FutureResult<T>();
  private final int myTaskId = mNextFutureTaskId.incrementAndGet();

  public abstract void run(final Sl4aServiceHelper activity, final FutureResult<T> result);

  public Runnable getRunnable(final Sl4aServiceHelper activity) {
    return new Runnable() {
      @Override
      public void run() {
        FutureActivityTask.this.run(activity, mResult);
      }
    };
  }

  public FutureResult<T> getFutureResult() {
    return mResult;
  }

  public int getTaskId() {
    return myTaskId;
  }
}
