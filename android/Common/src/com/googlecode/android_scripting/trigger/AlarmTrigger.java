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

package com.googlecode.android_scripting.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;

import com.googlecode.android_scripting.AseApplication;
import com.googlecode.android_scripting.IntentBuilders;

/**
 * A trigger that fires at a specific fixed time.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class AlarmTrigger extends Trigger {
  private static final long serialVersionUID = 3175281973854075190L;
  private final long mExecutionTimeMs;
  private final boolean mWakeup;

  private transient AlarmManager mAlarmManager;
  private transient Service mService;

  /**
   * @param scriptName
   *          name of the script to execute
   * @param executionTime
   *          execution time in seconds since epoch
   */
  public AlarmTrigger(String scriptName, Context context, long executionTimeMs, boolean wakeup) {
    super(scriptName);
    mExecutionTimeMs = executionTimeMs;
    mWakeup = wakeup;
  }

  @Override
  public void beforeTrigger(Service service) {
    super.beforeTrigger(service);

    // This trigger will only fire once: remove it from the repository.
    AseApplication application = (AseApplication) service.getApplication();
    application.getTriggerRepository().removeTrigger(getId());
  }

  /**
   * Returns the execution time in seconds since epoch.
   */
  public long getExecutionTimeMs() {
    return mExecutionTimeMs;
  }

  @Override
  public void install(Service service) {
    final int alarmType = mWakeup ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;
    final PendingIntent pendingIntent = IntentBuilders.buildTriggerIntent(service, this);
    mService = service;
    mAlarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);

    if (!isDeserializing()) {
      mAlarmManager.set(alarmType, mExecutionTimeMs, pendingIntent);
    }
  }

  @Override
  public void remove() {
    final PendingIntent pendingIntent = IntentBuilders.buildTriggerIntent(mService, this);
    mAlarmManager.cancel(pendingIntent);
  }
}
