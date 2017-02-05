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

import java.util.List;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothPan;
import android.bluetooth.BluetoothProfile;
// import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.bluetooth.BluetoothNonpublicApi;
import com.googlecode.android_scripting.bluetooth.BluetoothUuid;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothPanFacade extends RpcReceiver {

    static final ParcelUuid[] UUIDS = {
        BluetoothUuid.NAP,
        BluetoothUuid.PANU,
    };

    private final Service mService;
    private final BluetoothAdapter mBluetoothAdapter;

    private static boolean sIsPanReady = false;
    // private static BluetoothPan sPanProfile = null;
    private static BluetoothProfile sPanProfile = null;

    public BluetoothPanFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.getProfileProxy(mService, new PanServiceListener(),
            BluetoothNonpublicApi.PAN);
    }

    class PanServiceListener implements BluetoothProfile.ServiceListener {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            // sPanProfile = (BluetoothPan) proxy;
            sPanProfile = proxy;
            sIsPanReady = true;
        }

        @Override
        public void onServiceDisconnected(int profile) {
            sIsPanReady = false;
        }
    }

    @Rpc(description = "Set Bluetooth Tethering")
    public void bluetoothPanSetBluetoothTethering(
            @RpcParameter(name = "enable") Boolean enable) {
        Log.e("setBluetoothTegthering wont run in no-system app.");
        // sPanProfile.setBluetoothTethering(enable);
    }

    public Boolean panConnect(BluetoothDevice device) {
        return BluetoothNonpublicApi.connectProfile(sPanProfile, device);
    }

    public Boolean panDisconnect(BluetoothDevice device) {
        return BluetoothNonpublicApi.disconnectProfile(sPanProfile, device);
    }

    @Rpc(description = "Is Pan profile ready.")
    public Boolean bluetoothPanIsReady() {
        return sIsPanReady;
    }

    @Rpc(description = "Get all the devices connected through PAN")
    public List<BluetoothDevice> bluetoothPanGetConnectedDevices() {
        return sPanProfile.getConnectedDevices();
    }

    @Rpc(description = "Is tethering on.")
    public Boolean bluetoothPanIsTetheringOn() {
        if (!sIsPanReady || sPanProfile == null) {
            return false;
        }
        Log.e("isTetheringOn won't work in no-system app.");
        return false;
        // TODO: try to implement.
        // return sPanProfile.isTetheringOn();
    }

    public void shutdown() {
    }
}
