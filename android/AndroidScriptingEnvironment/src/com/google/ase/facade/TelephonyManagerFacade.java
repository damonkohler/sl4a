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
import android.content.Context;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * Exposes TelephonyManager funcitonality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 *         Felix Arends (felix.arends@gmail.com)
 */
public class TelephonyManagerFacade implements RpcReceiver {
  private final EventFacade mEventFacade;

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
      mEventFacade.postEvent("phone_state", mPhoneState);
    }
  };

  public TelephonyManagerFacade(Service service, EventFacade eventFacade) {
    this.mEventFacade = eventFacade;
    mTelephonyManager = (TelephonyManager) service.getSystemService(Context.TELEPHONY_SERVICE);
  }

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
