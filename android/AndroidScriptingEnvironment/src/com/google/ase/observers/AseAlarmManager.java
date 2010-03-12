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

package com.google.ase.observers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;

import com.google.ase.IntentBuilders;

public class AseAlarmManager {
  final AlarmManager mAlarmManager;
  final Service mService;

  public AseAlarmManager(Service service) {
    mAlarmManager = (AlarmManager) service.getSystemService(Context.ALARM_SERVICE);
    mService = service;
  }
  
  /**
   * Schedules the repeated execution of a script.  The intervals may vary slightly.  This is
   * more power efficient than scheduleRepeating.
   * 
   * @param interval interval between script invocations, in seconds
   * @param script script to execute
   * @param wakeUp wake up the device even when asleep
   */
  public void scheduleInexactRepeating(Double interval, String script, boolean wakeUp) {
    final PendingIntent pendingIntent = IntentBuilders.buildPendingIntent(mService, script);
    final int alarmType = wakeUp ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;

    mAlarmManager.setInexactRepeating(alarmType, System.currentTimeMillis(),
        convertSecondsToMilliseconds(interval), pendingIntent);
  }

  /**
   * Schedules the repeated execution of a script.
   * 
   * @param intervalS interval between executions, in seconds
   * @param script script to execute
   * @param firstExecutionTimeS time stamp of first time to execute the script, in seconds
   * @param wakeUp if true then the phone will wake up when the alarm goes off
   */
  public void scheduleRepeating(Double intervalS, String script, Double firstExecutionTimeS,
      boolean wakeUp) {
    final PendingIntent pendingIntent = IntentBuilders.buildPendingIntent(mService, script);
    final int alarmType = wakeUp ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;

    mAlarmManager.setRepeating(alarmType, convertSecondsToMilliseconds(firstExecutionTimeS),
        convertSecondsToMilliseconds(intervalS), pendingIntent);
  }

  /**
   * Cancels the scheduled execution of a script.
   * 
   * @param script name of the script whose scheduled invocation to cancel
   */
  public void cancelRepeating(String script) {
    final PendingIntent pendingIntent = IntentBuilders.buildPendingIntent(mService, script);

    mAlarmManager.cancel(pendingIntent);
  }

  private long convertSecondsToMilliseconds(Double seconds) {
    return (long)(seconds * 1000L);
  }
}
