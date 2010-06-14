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

package com.google.ase.future;

import com.google.ase.activity.AseServiceHelper;

import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;

/**
 * Encapsulates an {@link Activity} and a {@link FutureResult}.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class FutureActivityTask {
  private final static AtomicInteger mNextFutureTaskId = new AtomicInteger(0);
  private final FutureResult mResult = new FutureResult();
  private final int myTaskId = mNextFutureTaskId.incrementAndGet();

  public abstract void run(final AseServiceHelper activity, final FutureResult result);

  public Runnable getRunnable(final AseServiceHelper activity) {
    return new Runnable() {
      @Override
      public void run() {
        FutureActivityTask.this.run(activity, mResult);
      }
    };
  }

  public FutureResult getResult() {
    return mResult;
  }

  public int getTaskId() {
    return myTaskId;
  }
}
