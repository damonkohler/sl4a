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
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothHspFacade extends RpcReceiver {
  static final ParcelUuid[] UUIDS = {
    BluetoothUuid.HSP, BluetoothUuid.Handsfree
  };

  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  private static boolean sIsHspReady = false;
  private static BluetoothHeadset sHspProfile = null;

  public BluetoothHspFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new HspServiceListener(),
        BluetoothProfile.HEADSET);
  }

  class HspServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
      sHspProfile = (BluetoothHeadset) proxy;
      sIsHspReady = true;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sIsHspReady = false;
    }
  }

  public Boolean hspConnect(BluetoothDevice device) {
    if (sHspProfile == null) return false;
    return sHspProfile.connect(device);
  }

  public Boolean hspDisconnect(BluetoothDevice device) {
    if (sHspProfile == null) return false;
    return sHspProfile.disconnect(device);
  }

  @Rpc(description = "Is Hsp profile ready.")
  public Boolean bluetoothHspIsReady() {
    return sIsHspReady;
  }

  @Rpc(description = "Connect to an HSP device.")
  public Boolean bluetoothHspConnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a bluetooth device.")
      String device)
      throws Exception {
    if (sHspProfile == null)
      return false;
    BluetoothDevice mDevice = BluetoothFacade.getDevice(BluetoothFacade.DiscoveredDevices, device);
    Log.d("Connecting to device " + mDevice.getAliasName());
    return hspConnect(mDevice);
  }

  @Rpc(description = "Disconnect an HSP device.")
  public Boolean bluetoothHspDisconnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a device.")
      String device)
      throws Exception {
    if (sHspProfile == null)
      return false;
    Log.d("Connected devices: " + sHspProfile.getConnectedDevices());
    BluetoothDevice mDevice = BluetoothFacade.getDevice(sHspProfile.getConnectedDevices(),
                                                        device);
    return hspDisconnect(mDevice);
  }

  @Rpc(description = "Get all the devices connected through HSP.")
  public List<BluetoothDevice> bluetoothHspGetConnectedDevices() {
    while (!sIsHspReady);
    return sHspProfile.getConnectedDevices();
  }

  @Rpc(description = "Get the connection status of a device.")
  public Integer bluetoothHspGetConnectionStatus(
          @RpcParameter(name = "deviceID",
                        description = "Name or MAC address of a bluetooth device.")
          String deviceID) {
      if (sHspProfile == null) {
          return BluetoothProfile.STATE_DISCONNECTED;
      }
      List<BluetoothDevice> deviceList = sHspProfile.getConnectedDevices();
      BluetoothDevice device;
      try {
          device = BluetoothFacade.getDevice(deviceList, deviceID);
      } catch (Exception e) {
          return BluetoothProfile.STATE_DISCONNECTED;
      }
      return sHspProfile.getConnectionState(device);
  }

  @Override
  public void shutdown() {
  }
}
