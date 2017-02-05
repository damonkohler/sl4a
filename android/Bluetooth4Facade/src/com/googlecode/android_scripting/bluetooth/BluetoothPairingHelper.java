/*
 * Copyright (C) 2016 Google Inc.
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

package com.googlecode.android_scripting.facade.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.event.Event;

public class BluetoothPairingHelper extends BroadcastReceiver {
  private final EventFacade mEventFacade;
  private static final int DEFAULT_TIMEOUT_MS = 5000;
  private boolean mAutoConfirm = true;

  public BluetoothPairingHelper(EventFacade eventFacade) {
    super();
    mEventFacade = eventFacade;
    Log.d("Pairing helper created.");
  }
  /**
   *  Confirms bluetooth connection/bonding requests.
   */
  @Override
  public void onReceive(Context c, Intent intent) {
    String action = intent.getAction();
    Bundle result = new Bundle();
    Log.d("Bluetooth pairing intent received: " + action);
    BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    if(action.equals(BluetoothDevice.ACTION_PAIRING_REQUEST)) {
      mDevice.setMessageAccessPermission(BluetoothDevice.ACCESS_ALLOWED);
      mDevice.setPhonebookAccessPermission(BluetoothDevice.ACCESS_ALLOWED);
      int type = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
      Log.d("Processing Action Paring Request with type " + type);
      int pin = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_KEY,0);
      String deviceAddress = mDevice.getAddress();
      result.putInt("Pin", pin);
      result.putInt("PairingVariant", type);
      result.putString("DeviceAddress", deviceAddress);
      mEventFacade.postEvent("BluetoothActionPairingRequest", result.clone());
      result.clear();
      if(type == BluetoothDevice.PAIRING_VARIANT_CONSENT) {
        mDevice.setPairingConfirmation(true);
        Log.d("Connection auto-confirmed by consent");
        abortBroadcast(); // Abort the broadcast so Settings app doesn't get it.
      } else if (type == BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION) {
        if (mAutoConfirm) {
           mDevice.setPairingConfirmation(true);
          Log.d("Connection auto-confirmed");
        } else {
          // Wait for confirmation
          Event userConfirmEvent = null;
          try {
            userConfirmEvent = mEventFacade.eventWaitFor(
              "BluetoothActionPairingRequestUserConfirm",
              true, DEFAULT_TIMEOUT_MS);
          } catch (InterruptedException e) {
            Log.d("Connection interrupted");
            userConfirmEvent = null;
          }
          if (userConfirmEvent == null) {
            Log.d("Null response received from test server or timeout");
            mDevice.setPairingConfirmation(false);
          } else {
            String userConfirmEventData = (String) userConfirmEvent.getData();
            if (userConfirmEventData.equalsIgnoreCase("True")) {
              mDevice.setPairingConfirmation(true);
              Log.d("Connection confirmed");
            } else {
              mDevice.setPairingConfirmation(false);
              Log.d("Connection rejected");
            }
          }
        }
        abortBroadcast(); // Abort the broadcast so Settings app doesn't get it.
      }
    }
    else if(action.equals(BluetoothDevice.ACTION_CONNECTION_ACCESS_REQUEST)) {
      int type = intent.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE, BluetoothDevice.ERROR);
      Log.d("Processing Action Connection Access Request type " + type);
      if(type == BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS ||
         type == BluetoothDevice.REQUEST_TYPE_PHONEBOOK_ACCESS ||
         type == BluetoothDevice.REQUEST_TYPE_PROFILE_CONNECTION) {
    	  Intent newIntent = new Intent(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);
    	  String mReturnPackage = intent.getStringExtra(BluetoothDevice.EXTRA_PACKAGE_NAME);
          String mReturnClass = intent.getStringExtra(BluetoothDevice.EXTRA_CLASS_NAME);
          int mRequestType = intent.getIntExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE,
                  BluetoothDevice.REQUEST_TYPE_MESSAGE_ACCESS);
          if (mReturnPackage != null && mReturnClass != null) {
              newIntent.setClassName(mReturnPackage, mReturnClass);
          }
    	  newIntent.putExtra(BluetoothDevice.EXTRA_CONNECTION_ACCESS_RESULT,
    			             BluetoothDevice.CONNECTION_ACCESS_YES);
          newIntent.putExtra(BluetoothDevice.EXTRA_ALWAYS_ALLOWED, true);
    	  newIntent.putExtra(BluetoothDevice.EXTRA_DEVICE, mDevice);
    	  newIntent.putExtra(BluetoothDevice.EXTRA_ACCESS_REQUEST_TYPE, mRequestType);
          Log.d("Sending connection access acceptance intent.");
          abortBroadcast();
          c.sendBroadcast(newIntent, android.Manifest.permission.BLUETOOTH_ADMIN);
      }
    }
  }

  /**
   * Set autoConfirm flag to Value
   */
  public synchronized void setAutoConfirm(boolean value) {
    mAutoConfirm = value;
  }
}
