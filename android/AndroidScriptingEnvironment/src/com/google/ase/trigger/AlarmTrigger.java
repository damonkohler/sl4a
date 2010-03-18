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

public abstract class AlarmTrigger implements Trigger {
  private static final long serialVersionUID = 7610406773988708932L;

  /** name of the script to run */
  private final String mScriptName;
  /** interval between executions of the alarm, in seconds */
  private final Double mIntervalS;
  /** whether or not to wake up the device */
  private final boolean mWakeUp;

  public AlarmTrigger(String scriptName, Double interval, boolean wakeUp) {
    this.mScriptName = scriptName;
    this.mIntervalS = interval;
    this.mWakeUp = wakeUp;
  }

  /** Returns the name of the script to execute */
  public String getScriptName() {
    return mScriptName;
  }
  
  /** Returns the interval between executions in seconds */
  public Double getIntervalS() {
    return mIntervalS;
  }
  
  /** Returns whether or not the device should be woken up by the alarm. */
  public boolean shouldWakeUp() {
    return mWakeUp;
  }
}
