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
import android.bluetooth.BluetoothProfile;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.Bluetooth4Facade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.bluetooth.BluetoothNonpublicApi;
import com.googlecode.android_scripting.bluetooth.BluetoothUuid;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothMapFacade extends RpcReceiver {
  static final ParcelUuid[] MAP_UUIDS = {
      BluetoothUuid.MAP,
      BluetoothUuid.MNS,
      BluetoothUuid.MAS,
  };
  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  private static boolean sIsMapReady = false;
  // private static BluetoothMap sMapProfile = null;
    private static BluetoothProfile sMapProfile = null;

  public BluetoothMapFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new MapServiceListener(),
        BluetoothNonpublicApi.MAP);
  }

  class MapServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        // sMapProfile = (BluetoothMap) proxy;
        sMapProfile = proxy;
      sIsMapReady = true;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sIsMapReady = false;
    }
  }

  public Boolean mapDisconnect(BluetoothDevice device) {
      return BluetoothNonpublicApi.disconnectProfile(sMapProfile, device);
  }

  @Rpc(description = "Is Map profile ready.")
  public Boolean bluetoothMapIsReady() {
    return sIsMapReady;
  }

  @Rpc(description = "Disconnect an MAP device.")
  public Boolean bluetoothMapDisconnect(
      @RpcParameter(name = "deviceID", description = "Name or MAC address of a device.")
      String deviceID)
      throws Exception {
    if (sMapProfile == null) return false;
    List<BluetoothDevice> connectedMapDevices = sMapProfile.getConnectedDevices();
    Log.d("Connected map devices: " + connectedMapDevices);
    BluetoothDevice mDevice = Bluetooth4Facade.getDevice(connectedMapDevices, deviceID);
    if (!connectedMapDevices.isEmpty() && connectedMapDevices.get(0).equals(mDevice)) {
        BluetoothNonpublicApi.priorityOnProfile(sMapProfile, mDevice);
        return BluetoothNonpublicApi.disconnectProfile(sMapProfile, mDevice);
    } else {
        return false;
    }
  }

  @Rpc(description = "Get all the devices connected through MAP.")
  public List<BluetoothDevice> bluetoothMapGetConnectedDevices() {
    while (!sIsMapReady);
    return sMapProfile.getDevicesMatchingConnectionStates(
          new int[] {BluetoothProfile.STATE_CONNECTED,
                     BluetoothProfile.STATE_CONNECTING,
                     BluetoothProfile.STATE_DISCONNECTING});
  }

  @Rpc(description = "Get the currently connected remote Bluetooth device (PCE).")
  public BluetoothDevice bluetoothMapGetClient() {
    if (sMapProfile == null) { return null; }
    return sMapProfile.getClient();
  }

  @Override
  public void shutdown() {
  }
}
