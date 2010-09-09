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

import com.googlecode.android_scripting.IntentBuilders;

/**
 * A trigger that fires repeatedly with a fixed interval and starting time.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class ExactRepeatingAlarmTrigger extends RepeatingAlarmTrigger {
  private static final long serialVersionUID = -9125118724160624255L;

  private final long mFirstExecutionTimeMs;

  public ExactRepeatingAlarmTrigger(String script, long intervalMs, long firstExecutionTimeMs,
      boolean wakeUp) {
    super(script, intervalMs, wakeUp);
    mFirstExecutionTimeMs = firstExecutionTimeMs;
  }

  @Override
  protected final void installAlarm() {
    final int alarmType = mWakeUp ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;
    final PendingIntent pendingIntent = IntentBuilders.buildTriggerIntent(mService, this);
    mAlarmManager.setRepeating(alarmType, mFirstExecutionTimeMs, mIntervalMs, pendingIntent);
  }
}
