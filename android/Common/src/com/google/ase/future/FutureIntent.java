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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Intent;

/**
 * FutureIntent represents an eventual Intent result object for asynchronous operations.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class FutureIntent implements Future<Intent> {

  private final CountDownLatch mLatch = new CountDownLatch(1);
  private Intent mIntent;

  public void set(Intent intent) {
    mIntent = intent;
    mLatch.countDown();
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    return false;
  }

  @Override
  public Intent get() throws InterruptedException {
    mLatch.await();
    return mIntent;
  }

  @Override
  public Intent get(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
    mLatch.await(timeout, unit);
    return mIntent;
  }

  @Override
  public boolean isCancelled() {
    return false;
  }

  @Override
  public boolean isDone() {
    return mIntent == null;
  }

}
