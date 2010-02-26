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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Activity;
import android.app.Application;
import android.os.Handler;

public class AseApplication extends Application {

  private Activity mHelperActivity;
  private Handler mHelperHandler;

  private final Queue<ActivityRunnable> mTaskQueue = new ConcurrentLinkedQueue<ActivityRunnable>();

  public synchronized void setHelperActivity(Activity activity) {
    mHelperActivity = activity;
  }

  public synchronized Activity getHelperActivity() {
    return mHelperActivity;
  }

  public synchronized void setHelperHandler(Handler mHelperHandler) {
    this.mHelperHandler = mHelperHandler;
  }

  public synchronized Handler getHelperHandler() {
    return mHelperHandler;
  }

  public FutureIntent offerTask(ActivityRunnable task) {
    mTaskQueue.offer(task);
    return task.getFutureResult();
  }

  public ActivityRunnable pollTaskQueue() {
    return mTaskQueue.poll();
  }

  @Override
  public void onCreate() {
    super.onCreate();
    AseAnalytics.start(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    AseAnalytics.stop();
  }
}
