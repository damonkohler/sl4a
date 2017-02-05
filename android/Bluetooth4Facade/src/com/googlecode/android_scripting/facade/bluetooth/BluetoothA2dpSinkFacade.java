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
import java.util.ArrayList;

import android.app.Service;
// import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
// import android.bluetooth.BluetoothUuid;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.bluetooth.BluetoothNonpublicApi;
import com.googlecode.android_scripting.bluetooth.BluetoothUuid;
import com.googlecode.android_scripting.facade.Bluetooth4Facade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothA2dpSinkFacade extends RpcReceiver {
  static final ParcelUuid[] SOURCE_UUIDS = {
    BluetoothUuid.AudioSource,
  };

  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  // private static BluetoothA2dpSink sA2dpSinkProfile = null;
    private static BluetoothProfile sA2dpSinkProfile = null;

  public BluetoothA2dpSinkFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new A2dpSinkServiceListener(),
        BluetoothNonpublicApi.A2DP_SINK);
  }

  class A2dpSinkServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        // sA2dpSinkProfile = (BluetoothA2dpSink) proxy;
        sA2dpSinkProfile = proxy;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sA2dpSinkProfile = null;
    }
  }

  public Boolean a2dpSinkConnect(BluetoothDevice device) {
      return BluetoothNonpublicApi.connectProfile(sA2dpSinkProfile, device);
  }

  public Boolean a2dpSinkDisconnect(BluetoothDevice device) {
      return BluetoothNonpublicApi.disconnectProfile(sA2dpSinkProfile, device);
  }

  @Rpc(description = "Set priority of the profile")
  public void bluetoothA2dpSinkSetPriority(
      @RpcParameter(name = "device", description = "Mac address of a BT device.")
      String deviceStr,
      @RpcParameter(name = "priority", description = "Priority that needs to be set.")
      Integer priority)
      throws Exception {
    if (sA2dpSinkProfile == null) return;
    BluetoothDevice device =
            Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(), deviceStr);
        // Log.d("Changing priority of device " + device.getAliasName() + " p: " + priority);
        BluetoothNonpublicApi.setPriorityProfile(
            sA2dpSinkProfile, device, priority);
  }

  @Rpc(description = "get priority of the profile")
  public Integer bluetoothA2dpSinkGetPriority(
      @RpcParameter(name = "device", description = "Mac address of a BT device.")
      String deviceStr)
      throws Exception {
    if (sA2dpSinkProfile == null) return BluetoothProfile.PRIORITY_UNDEFINED;
    BluetoothDevice device =
            Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(), deviceStr);
        return BluetoothNonpublicApi.getPriorityProfile(sA2dpSinkProfile, device);
  }

  @Rpc(description = "Is A2dpSink profile ready.")
  public Boolean bluetoothA2dpSinkIsReady() {
    return (sA2dpSinkProfile != null);
  }

  @Rpc(description = "Connect to an A2DP Sink device.")
  public Boolean bluetoothA2dpSinkConnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a bluetooth device.")
      String deviceStr)
      throws Exception {
    if (sA2dpSinkProfile == null) return false;
    BluetoothDevice device =
        Bluetooth4Facade.getDevice(Bluetooth4Facade.DiscoveredDevices, deviceStr);
    // Log.d("Connecting to device " + device.getAliasName());
    return a2dpSinkConnect(device);
  }

  @Rpc(description = "Disconnect an A2DP Sink device.")
  public Boolean bluetoothA2dpSinkDisconnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a device.")
      String deviceStr) {
    if (sA2dpSinkProfile == null) return false;
    Log.d("Connected devices: " + sA2dpSinkProfile.getConnectedDevices());
    BluetoothDevice device = null;
    try {
      device = Bluetooth4Facade.getDevice(sA2dpSinkProfile.getConnectedDevices(), deviceStr);
      return a2dpSinkDisconnect(device);
    } catch (Exception e) {
        // Do nothing here. Since it is disconnect this function should force shutdown anyways.
        Log.d("bluetoothA2dpSinkDisconnect error while getDevice " + e);
    }
    return false;
  }

  @Rpc(description = "Get all the devices connected through A2DP Sink.")
  public List<BluetoothDevice> bluetoothA2dpSinkGetConnectedDevices() {
    if (sA2dpSinkProfile == null) return new ArrayList<BluetoothDevice>();
    return sA2dpSinkProfile.getConnectedDevices();
  }

  @Rpc(description = "Get the connection status of a device.")
  public Integer bluetoothA2dpSinkGetConnectionStatus(
      @RpcParameter(name = "deviceID",
                    description = "Name or MAC address of a bluetooth device.")
        String deviceID) {
    if (sA2dpSinkProfile == null) {
      return BluetoothProfile.STATE_DISCONNECTED;
    }
    List<BluetoothDevice> deviceList = sA2dpSinkProfile.getConnectedDevices();
    BluetoothDevice device;
    try {
      device = Bluetooth4Facade.getDevice(deviceList, deviceID);
    } catch (Exception e) {
      Log.e(e);
      return BluetoothProfile.STATE_DISCONNECTED;
    }
    return sA2dpSinkProfile.getConnectionState(device);
  }

  @Override
  public void shutdown() {
  }
}
