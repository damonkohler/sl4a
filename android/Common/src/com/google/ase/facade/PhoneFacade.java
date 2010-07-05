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
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.ase.AseLog;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.concurrent.CountDownLatch;

/**
 * Exposes TelephonyManager funcitonality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com) Felix Arends (felix.arends@gmail.com)
 */
public class PhoneFacade extends RpcReceiver {
  private final AndroidFacade mAndroidFacade;
  private final EventFacade mEventFacade;
  private final TelephonyManager mTelephonyManager;
  private Bundle mPhoneState;
  private final Handler mHandler;
  private PhoneStateListener mPhoneStateListener;
  private final CountDownLatch mLatch = new CountDownLatch(1);

  public PhoneFacade(FacadeManager manager) {
    super(manager);
    Service service = manager.getService();
    mTelephonyManager = (TelephonyManager) service.getSystemService(Context.TELEPHONY_SERVICE);
    mAndroidFacade = manager.getFacade(AndroidFacade.class);
    mEventFacade = manager.getFacade(EventFacade.class);
    mHandler = new Handler(service.getMainLooper());
    mHandler.post(new Runnable() {
      @Override
      public void run() {
        createPhoneStateListener();
      }
    });
  }

  private void createPhoneStateListener(){
    mPhoneStateListener = new PhoneStateListener() {
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

    mLatch.countDown();
  }

  @Override
  public void shutdown() {
    stopTrackingPhoneState();
  }

  @Rpc(description = "Starts tracking phone state.")
  public void startTrackingPhoneState() {
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      AseLog.e(e);
    }
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Rpc(description = "Returns the current phone state and incoming number.", returns = "A Map of \"state\" and \"incomingNumber\"")
  public Bundle readPhoneState() {
    return mPhoneState;
  }

  @Rpc(description = "Stops tracking phone state.")
  public void stopTrackingPhoneState() {
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      AseLog.e(e);
    }
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  @Rpc(description = "Calls a contact/phone number by URI.")
  public void phoneCall(@RpcParameter(name = "uri") final String uri) throws JSONException {
    mAndroidFacade.startActivity(Intent.ACTION_CALL, uri, null, null);
  }

  @Rpc(description = "Calls a phone number.")
  public void phoneCallNumber(@RpcParameter(name = "phone number") final String number)
      throws UnsupportedEncodingException, JSONException {
    phoneCall("tel:" + URLEncoder.encode(number, "ASCII"));
  }

  @Rpc(description = "Dials a contact/phone number by URI.")
  public void phoneDial(@RpcParameter(name = "uri") final String uri) throws JSONException {
    mAndroidFacade.startActivity(Intent.ACTION_DIAL, uri, null, null);
  }

  @Rpc(description = "Dials a phone number.")
  public void phoneDialNumber(@RpcParameter(name = "phone number") final String number)
      throws JSONException, UnsupportedEncodingException {
    phoneDial("tel:" + URLEncoder.encode(number, "ASCII"));
  }

  @Rpc(description = "Returns the current cell location.")
  public CellLocation getCellLocation() {
    return mTelephonyManager.getCellLocation();
  }
}
