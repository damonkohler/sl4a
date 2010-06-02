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

import java.util.UUID;

import android.app.AlarmManager;
import android.content.Context;

/**
 * A class keeping track of currently scheduled alarms in cooperation with the
 * {@link TriggerRepository}.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class AlarmTriggerManager {
  final AlarmManager mAlarmManager;
  final Context mContext;
  final TriggerRepository mTriggerRepository;

  public AlarmTriggerManager(Context context, TriggerRepository triggerRepository) {
    mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    mContext = context;
    mTriggerRepository = triggerRepository;
  }

  /**
   * Schedules the repeated execution of a script. The intervals may vary slightly. This is more
   * power efficient than scheduleRepeating.
   * 
   * @param interval
   *          interval between script invocations, in seconds
   * @param script
   *          script to execute
   * @param wakeUp
   *          wake up the device even when asleep
   */
  public void scheduleInexactRepeating(double interval, String script, boolean wakeUp) {
    mTriggerRepository.addTrigger(new InexactRepeatingAlarmTrigger(script, mContext,
        convertSecondsToMilliseconds(interval), wakeUp));
  }

  /**
   * Schedules the repeated execution of a script.
   * 
   * @param interval
   *          interval between executions, in seconds
   * @param script
   *          script to execute
   * @param wakeup
   *          if true then the phone will wake up when the alarm goes off
   */
  public void scheduleRepeating(Double interval, String script, boolean wakeup) {
    long firstExecutionTime = System.currentTimeMillis() + convertSecondsToMilliseconds(interval);
    mTriggerRepository.addTrigger(new ExactRepeatingAlarmTrigger(script, mContext,
        convertSecondsToMilliseconds(interval), firstExecutionTime, wakeup));
  }

  /**
   * Schedules the execution of a script at a specific point of time.
   * 
   * @param executionTimeS
   *          time of execution, in seconds since epoch
   * @param script
   *          script to execute
   * @param wakeup
   *          whether or not to wakeup the phone if its asleep
   */
  public void schedule(Double executionTimeS, String script, boolean wakeup) {
    mTriggerRepository.addTrigger(new AlarmTrigger(script, mContext,
        convertSecondsToMilliseconds(executionTimeS), wakeup));
  }

  /**
   * Cancels the scheduled execution of a script.
   * 
   * @param script
   *          name of the script whose scheduled invocation to cancel
   */
  public void cancelByScriptName(final String script) {
    mTriggerRepository.removeTriggers(new TriggerRepository.TriggerFilter() {
      @Override
      public boolean matches(Trigger trigger) {
        if (trigger.getScriptName().compareToIgnoreCase(script) == 0) {
          return true;
        } else {
          return false;
        }
      }
    });
  }

  /**
   * Cancels the scheduled execution of the trigger with the given id.
   * 
   * @param triggerId
   *          id of the trigger to cancel
   */
  public void cancelById(final UUID triggerId) {
    mTriggerRepository.removeTrigger(triggerId);
  }

  private long convertSecondsToMilliseconds(double seconds) {
    return (long) (seconds * 1000L);
  }
}
