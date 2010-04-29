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

package com.google.ase.facade;

import android.app.AlarmManager;
import android.app.Service;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;
import com.google.ase.trigger.AlarmTriggerManager;
import com.google.ase.trigger.TriggerRepository;

/**
 * A facade exposing the functionality of the {@link AlarmManager}.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class AlarmManagerFacade implements RpcReceiver {
  final AlarmTriggerManager mAlarmManager;

  public AlarmManagerFacade(Service service, EventFacade eventFacade,
      TriggerRepository triggerRepository) {
    mAlarmManager = new AlarmTriggerManager(service, triggerRepository);
  }

  @Rpc(description = "schedules a script for (inexact) regular execution - saves battery in "
      + "comparison to scheduleRepeating")
  public void scheduleInexactRepeating(
      @RpcParameter(name = "interval", description = "the interval between invocations, in seconds")
      Double interval,
      @RpcParameter(name = "script", description = "the script to execute")
      String script,
      @RpcParameter(name = "wakeup", description = "whether or not to wakeup the device if asleep")
      @RpcDefault("true") 
      Boolean wakeup) {
    mAlarmManager.scheduleInexactRepeating(interval, script, wakeup);
  }

  @Rpc(description = "scheudles a script for (exact) regular execution")
  public void scheduleRepeating(
      @RpcParameter(name = "interval", description = "interval between invocations, in seconds")
      Double interval,
      @RpcParameter(name = "script", description = "script to execute")
      String script,
      @RpcParameter(name = "firstExecutionTime", description = "first time to execute script, in seconds since epoch") @RpcOptional
      Double firstExecutionTime,
      @RpcParameter(name = "wakeup", description = "whether or not to wake up the device if asleep")
      @RpcDefault("true")
      Boolean wakeup) {
    if (firstExecutionTime == null) {
      // If the default value is passed, use the current time.
      firstExecutionTime = currentTime();
    }

    mAlarmManager.scheduleRepeating(interval, script, wakeup);
  }
  
  @Rpc(description = "schedules one-time execution of a script")
  public void scheudleAbsolute(
      @RpcParameter(name = "script", description = "script to execute")
      String script,
      @RpcParameter(name = "time", description = "time of invocation, in seconds since epoch")
      Double time,
      @RpcParameter(name = "wakeup", description = "whether or not to wake up the device if asleep")
      @RpcDefault("true")
      Boolean wakeup) {
    mAlarmManager.schedule(time, script, wakeup);
  }
  
  @Rpc(description = "schedules one-time execution of a script, a given number of seconds from now")
  public void scheduleRelative(
      @RpcParameter(name = "script", description = "script to execute")
      String script,
      @RpcParameter(name = "secondsFromNow", description = "after what time to execute the script")
      Double secondsFromNow,
      @RpcParameter(name = "wakeup", description = "whether or not to wake up the device if asleep")
      @RpcDefault("true")
      Boolean wakeup) {
    mAlarmManager.schedule(currentTime() + secondsFromNow, script, wakeup);
  }

  @Rpc(description = "cancels all scheduled regular executions of a given script")
  public void cancelRepeating(@RpcParameter(name = "script") String script) {
    mAlarmManager.cancelByScriptName(script);
  }

  /** Returns the current time, in seconds since epoch. */
  private Double currentTime() {
    return (1.0d * System.currentTimeMillis()) / 1000;
  }

  @Override
  public void shutdown() {
  }
}
