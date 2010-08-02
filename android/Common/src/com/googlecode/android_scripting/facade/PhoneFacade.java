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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts.People;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcEvent;
import com.googlecode.android_scripting.rpc.RpcParameter;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

/**
 * Exposes TelephonyManager funcitonality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com) Felix Arends (felix.arends@gmail.com)
 */
public class PhoneFacade extends RpcReceiver {
  private final AndroidFacade mAndroidFacade;
  private final EventFacade mEventFacade;
  private final TelephonyManager mTelephonyManager;
  private final Bundle mPhoneState;
  private final Service mService;
  private PhoneStateListener mPhoneStateListener;

  public PhoneFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mTelephonyManager = (TelephonyManager) mService.getSystemService(Context.TELEPHONY_SERVICE);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mPhoneState = new Bundle();
    mPhoneStateListener = MainThread.run(mService, new Callable<PhoneStateListener>() {
      @Override
      public PhoneStateListener call() throws Exception {
        return new PhoneStateListener() {
          @Override
          public void onCallStateChanged(int state, String incomingNumber) {
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
            mEventFacade.postEvent("phone", mPhoneState);
          }
        };
      }
    });
  }

  @Override
  public void shutdown() {
    stopTrackingPhoneState();
  }

  @Rpc(description = "Starts tracking phone state.")
  @RpcEvent("phone")
  public void startTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Rpc(description = "Returns the current phone state and incoming number.", returns = "A Map of \"state\" and \"incomingNumber\"")
  public Bundle readPhoneState() {
    return mPhoneState;
  }

  @Rpc(description = "Stops tracking phone state.")
  public void stopTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  @Rpc(description = "Calls a contact/phone number by URI.")
  public void phoneCall(@RpcParameter(name = "uri") final String uriString) throws Exception {
    Uri uri = Uri.parse(uriString);
    if (uri.getScheme().equals("content")) {
      String phoneNumberColumn = People.NUMBER;
      String selectWhere = null;
      if ((FacadeManager.class.cast(mManager)).getSdkLevel() >= 5) {
        Class<?> contactsContract_Data_class =
            Class.forName("android.provider.ContactsContract$Data");
        Field RAW_CONTACT_ID_field = contactsContract_Data_class.getField("RAW_CONTACT_ID");
        selectWhere = RAW_CONTACT_ID_field.get(null).toString() + "=" + uri.getLastPathSegment();
        Field CONTENT_URI_field = contactsContract_Data_class.getField("CONTENT_URI");
        uri = Uri.parse(CONTENT_URI_field.get(null).toString());
        Class<?> ContactsContract_CommonDataKinds_Phone_class =
            Class.forName("android.provider.ContactsContract$CommonDataKinds$Phone");
        Field NUMBER_field = ContactsContract_CommonDataKinds_Phone_class.getField("NUMBER");
        phoneNumberColumn = NUMBER_field.get(null).toString();
      }
      ContentResolver resolver = mService.getContentResolver();
      Cursor c = resolver.query(uri, new String[] { phoneNumberColumn }, selectWhere, null, null);
      String number = "";
      if (c.moveToFirst()) {
        number = c.getString(c.getColumnIndexOrThrow(phoneNumberColumn));
      }
      c.close();
      phoneCallNumber(number);
    } else {
      mAndroidFacade.startActivity(Intent.ACTION_CALL, uriString, null, null);
    }
  }

  @Rpc(description = "Calls a phone number.")
  public void phoneCallNumber(@RpcParameter(name = "phone number") final String number)
      throws Exception {
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
