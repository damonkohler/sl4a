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

import com.google.ase.IntentBuilders;

import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public abstract class RepeatingAlarmTrigger extends Trigger {
  private static final String WAKE_LOCK_TAG = "com.google.ase.trigger.RepeatingAlarmTrigger";

  /** The {@link WakeLock} held during the execution of the trigger. */
  private transient WakeLock mWakeLock = null;
  protected transient AlarmManager mAlarmManager;
  protected transient Context mContext;

  /** Interval between executions of the alarm, in seconds. */
  protected final long mIntervalMs;

  /** Whether or not to wake up the device. */
  protected final boolean mWakeUp;

  public RepeatingAlarmTrigger(String scriptName, Context context, long intervalMs, boolean wakeUp) {
    super(scriptName);
    mIntervalMs = intervalMs;
    mWakeUp = wakeUp;
    initializeTransients(context);
  }

  /** Obtains the wake lock. */
  @Override
  public void afterTrigger(Service service) {
    super.afterTrigger(service);
    mWakeLock.release();
  }

  /** Releases the wake lock. */
  @Override
  public void beforeTrigger(Service service) {
    super.beforeTrigger(service);
    PowerManager powerManager = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
    mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
    mWakeLock.acquire();
  }

  private static final long serialVersionUID = 7610406773988708932L;

  /** Returns the interval between executions in milli-seconds. */
  public long getInterval() {
    return mIntervalMs;
  }

  /** Returns whether or not the device should be woken up by the alarm. */
  public boolean shouldWakeUp() {
    return mWakeUp;
  }

  @Override
  public void initializeTransients(Context context) {
    mContext = context;
    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  }

  @Override
  public void remove() {
    mAlarmManager.cancel(IntentBuilders.buildTriggerIntent(mContext, this));
  }
}
