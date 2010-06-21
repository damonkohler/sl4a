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

import android.app.Application;

import com.google.ase.activity.NotificationIdFactory;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.trigger.TriggerRepository;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AseApplication extends Application {

  private final Queue<FutureActivityTask> mTaskQueue =
      new ConcurrentLinkedQueue<FutureActivityTask>();

  private TriggerRepository mTriggerRepository;

  private final NotificationIdFactory mNotificaitonIdFactory = NotificationIdFactory.INSTANCE;

  private InterpreterConfiguration mConfiguration;

  public Queue<FutureActivityTask> getTaskQueue() {
    return mTaskQueue;
  }

  public TriggerRepository getTriggerRepository() {
    return mTriggerRepository;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mTriggerRepository = new TriggerRepository(this);
    mConfiguration = new InterpreterConfiguration(this);
    Analytics.start(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    Analytics.stop();
  }

  public int getNewNotificationId() {
    return mNotificaitonIdFactory.createId();
  }

  public InterpreterConfiguration getInterpreterConfiguration() {
    return mConfiguration;
  }
}
