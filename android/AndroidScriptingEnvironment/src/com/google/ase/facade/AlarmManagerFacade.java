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

import android.app.Service;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultBoolean;
import com.google.ase.jsonrpc.RpcOptionalDouble;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.observers.AseAlarmManager;

public class AlarmManagerFacade implements RpcReceiver {
  final AseAlarmManager mAlarmManager;

  public AlarmManagerFacade(Service service, EventFacade eventFacade) {
    mAlarmManager = new AseAlarmManager(service);
  }

  @Rpc(description = "schedules a script for (inexact) regular execution - saves battery in "
      + "comparison to scheduleRepeating")
  public void scheduleInexactRepeating(
      @RpcParameter(name = "interval", description = "the interval between invocations, in seconds")
      Double interval,
      @RpcParameter(name = "script", description = "the script to execute")
      String script,
      @RpcDefaultBoolean(name = "wakeUp", description = "whether or not to wakeup the device if asleep", defaultValue = true) 
      Boolean wakeUp) {
    mAlarmManager.scheduleInexactRepeating(interval, script, wakeUp);
  }

  @Rpc(description = "scheudles a script for (exact) regular execution")
  public void scheduleRepeating(
      @RpcParameter(name = "interval", description = "interval between invocations, in seconds")
      Double interval,
      @RpcParameter(name = "script", description = "script to execute")
      String script,
      @RpcOptionalDouble(name = "firstExecutionTime", description = "first time to execute script, in seconds since epoch")
      Double firstExecutionTime,
      @RpcDefaultBoolean(name = "wakeUp", description = "whether or not to wake up the device if asleep", defaultValue = true)
      Boolean wakeUp) {
    if (firstExecutionTime == null) {
      // If the default value is passed, the current time is used.
      firstExecutionTime = currentTime();
    }

    mAlarmManager.scheduleRepeating(interval, script, firstExecutionTime, wakeUp);
  }

  @Rpc(description = "cancels all scheduled regular executions of a given script")
  public void cancelRepeating(@RpcParameter(name = "script") String script) {
    mAlarmManager.cancelRepeating(script);
  }

  private Double currentTime() {
    return (1.0 * System.currentTimeMillis()) / 1000;
  }

  @Override
  public void shutdown() {
  }
}
