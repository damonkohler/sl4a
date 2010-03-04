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
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.os.SystemClock;

import com.google.ase.IntentBuilders;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class AlarmManagerFacade implements RpcReceiver {
  /** A random value that is used to identify pending intents. */
  private static final int EXECUTE_SCRIPT_REQUEST_CODE = 0x12f412a;

  final AlarmManager mAlarmManager;
  final Service mService;
  
  public AlarmManagerFacade(Service service, EventFacade eventFacade) {
    mAlarmManager = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
    mService = service;
  }

  @Rpc(description = "scheudles a script for regular execution")
  public void scheduleRepeating(
      @RpcParameter("interval") Long interval,
      @RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mService,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime(), interval, pendingIntent);
  }

  @Rpc(description = "cancels the regular execution of a given script")
  public void cancelRepeating(@RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mService,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.cancel(pendingIntent);
  }

  @Override
  public void shutdown() {
  }
}
