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

package com.google.ase.trigger;

import android.app.AlarmManager;
import android.app.PendingIntent;

import com.google.ase.IntentBuilders;

/**
 * A trigger that fires repeatedly with an approximate interval between events. This is a more
 * power-efficient version of {@link ExactRepeatingAlarmTrigger} with the drawback that the interval
 * is not respected as precisely.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class InexactRepeatingAlarmTrigger extends RepeatingAlarmTrigger {
  private static final long serialVersionUID = -9193318334645990578L;

  public InexactRepeatingAlarmTrigger(String scriptName, long interval,
      boolean wakeUp) {
    super(scriptName, interval, wakeUp);
  }

  @Override
  public void installAlarm() {
    final int alarmType = mWakeUp ? AlarmManager.RTC : AlarmManager.RTC_WAKEUP;
    final PendingIntent pendingIntent = IntentBuilders.buildTriggerIntent(mService, this);
    long firstExecutionTime = System.currentTimeMillis() + mIntervalMs;
    mAlarmManager.setInexactRepeating(alarmType, firstExecutionTime, mIntervalMs, pendingIntent);
  }
}
