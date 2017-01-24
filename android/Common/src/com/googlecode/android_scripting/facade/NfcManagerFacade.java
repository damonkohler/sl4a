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

package com.googlecode.android_scripting.facade;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;

/**
 * Access NFC functions.
 */
public class NfcManagerFacade extends RpcReceiver {

    private final Service mService;
    private final NfcManager mNfcManager;
    private final NfcAdapter mNfc;
    private final EventFacade mEventFacade;
    private final IntentFilter mStateChangeFilter;
    private boolean mTrackingStateChange;

    public NfcManagerFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mNfcManager = (NfcManager) mService.getSystemService(Context.NFC_SERVICE);
        mNfc = mNfcManager.getDefaultAdapter();
        mEventFacade = manager.getReceiver(EventFacade.class);
        mStateChangeFilter = new IntentFilter(NfcAdapter.ACTION_ADAPTER_STATE_CHANGED);
    }

    private final BroadcastReceiver mNfcStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_ADAPTER_STATE_CHANGED.equals(action)) {
                int nfcState = intent.getIntExtra(NfcAdapter.EXTRA_ADAPTER_STATE,
                        NfcAdapter.STATE_OFF);
                if (nfcState == NfcAdapter.STATE_ON) {
                    mEventFacade.postEvent("NfcStateOn", null);
                } else if (nfcState == NfcAdapter.STATE_OFF) {
                    mEventFacade.postEvent("NfcStateOff", null);
                }
            }
        }
    };

    @Rpc(description = "Check if NFC hardware is enabled.")
    public Boolean nfcIsEnabled() {
        return mNfc.isEnabled();
    }

    @Rpc(description = "Asynchronous call to enable NFC hardware.")
    public Boolean nfcEnable() {
        return mNfc.enable();
    }

    @Rpc(description = "Asynchronous call to disable NFC hardware.")
    public Boolean nfcDisable() {
        return mNfc.disable();
    }

    @Rpc(description = "Start tracking NFC hardware state changes.")
    public void nfcStartTrackingStateChange() {
        mService.registerReceiver(mNfcStateReceiver, mStateChangeFilter);
        mTrackingStateChange = true;
    }

    @Rpc(description = "Stop tracking NFC hardware state changes.")
    public void nfcStopTrackingStateChange() {
        mService.unregisterReceiver(mNfcStateReceiver);
        mTrackingStateChange = false;
    }

    @Override
    public void shutdown() {
        if (mTrackingStateChange == true) {
            nfcStopTrackingStateChange();
        }
    }

}
