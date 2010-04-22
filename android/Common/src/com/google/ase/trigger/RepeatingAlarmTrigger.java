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

public abstract class RepeatingAlarmTrigger extends Trigger {
  private static final long serialVersionUID = 7610406773988708932L;

  /** Interval between executions of the alarm, in seconds. */
  private final double mInterval;

  /** Whether or not to wake up the device. */
  private final boolean mWakeUp;

  public RepeatingAlarmTrigger(String scriptName, double interval, boolean wakeUp) {
    super(scriptName);
    mInterval = interval;
    mWakeUp = wakeUp;
  }

  /** Returns the interval between executions in seconds. */
  public double getInterval() {
    return mInterval;
  }

  /** Returns whether or not the device should be woken up by the alarm. */
  public boolean shouldWakeUp() {
    return mWakeUp;
  }
}
