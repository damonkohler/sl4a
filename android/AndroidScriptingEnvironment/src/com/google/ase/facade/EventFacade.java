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
import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.ase.IntentBuilders;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * This facade exposes the functionality to read from the event queue as an RPC, and the
 * functionality to write to the event queue as a pure java function.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class EventFacade implements RpcReceiver {
  private static final int EXECUTE_SCRIPT_REQUEST_CODE = 1;

  final Queue<Bundle> mEventQueue = new ConcurrentLinkedQueue<Bundle>();
  final Context mService;
  final AlarmManager mAlarmManager;

  public EventFacade(final Service service) {
    mService = service;
    mAlarmManager = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
    mTelephonyManager = (TelephonyManager)service.getSystemService(Context.TELEPHONY_SERVICE);
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

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.", returns = "Map of event properties.")
  public Bundle receiveEvent() {
    return mEventQueue.poll();
  }

  /**
   * Posts an event on the event queue. This method is supposed to be used from other facades to
   * post events.
   */
  void postEvent(String name, Bundle bundle) {
    Bundle event = new Bundle(bundle);
    event.putString("name", name);
    mEventQueue.add(event);
  }
  
  private Bundle mPhoneState;
  private final TelephonyManager mTelephonyManager;
  private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      mPhoneState = new Bundle();
      mPhoneState.putString("incomingNumber", incomingNumber);
      switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
          mPhoneState.putString("state", "idle");
          break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
          mPhoneState.putString("state", "offhook");
          break;
        case TelephonyManager.CALL_STATE_RINGING:
          mPhoneState.putString("state", "ringing");
          break;
      }
      postEvent("phone_state", mPhoneState);
    }
  };

  @Override
  public void shutdown() {
    stopTrackingPhoneState();
  }

  @Rpc(description = "Starts tracking phone state.")
  public void startTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Rpc(description = "Returns the current phone state and incoming number.", returns = "A map of \"state\" and \"incomingNumber\"")
  public Bundle readPhoneState() {
    return mPhoneState;
  }

  @Rpc(description = "Stops tracking phone state.")
  public void stopTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }
}
