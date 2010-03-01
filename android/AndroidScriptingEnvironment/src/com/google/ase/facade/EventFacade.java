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

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;

import com.google.ase.IntentBuilders;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class EventFacade implements RpcReceiver {
  private static final int EXECUTE_SCRIPT_REQUEST_CODE = 1;

  final Queue<Bundle> mEventQueue = new ConcurrentLinkedQueue<Bundle>();
  final Context mContext;
  final AlarmManager mAlarmManager;
  
  public EventFacade(final Context context) {
    mContext = context;
    mAlarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
  }
  
  @Rpc(description = "scheudles a script for regular execution")
  public void scheduleRepeating(
      @RpcParameter("interval") Long interval,
      @RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mContext,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime(), interval, pendingIntent);
  }
  
  @Rpc(description = "cancels the regular execution of a given script")
  public void cancelRepeating(@RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mContext,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.cancel(pendingIntent);
  }

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.", returns = "Map of event properties.")
  public Bundle receiveEvent() {
    return mEventQueue.poll();
  }

  @Override
  public void shutdown() {
  }

}
