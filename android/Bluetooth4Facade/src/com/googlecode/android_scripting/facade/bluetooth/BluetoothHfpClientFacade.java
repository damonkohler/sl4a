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
// import android.bluetooth.BluetoothHeadsetClient;
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

public class BluetoothHfpClientFacade extends RpcReceiver {
  static final ParcelUuid[] UUIDS = {
    BluetoothUuid.Handsfree_AG,
  };

  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  private static boolean sIsHfpClientReady = false;
  // private static BluetoothHeadsetClient sHfpClientProfile = null;
    private static BluetoothProfile sHfpClientProfile = null;

  public BluetoothHfpClientFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new HfpClientServiceListener(),
        BluetoothNonpublicApi.HEADSET_CLIENT);
  }

  class HfpClientServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
        // sHfpClientProfile = (BluetoothHeadsetClient) proxy;
        sHfpClientProfile = proxy;
        sIsHfpClientReady = true;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sIsHfpClientReady = false;
    }
  }

  public Boolean hfpClientConnect(BluetoothDevice device) {
      return BluetoothNonpublicApi.connectProfile(sHfpClientProfile, device);
  }

  public Boolean hfpClientDisconnect(BluetoothDevice device) {
      return BluetoothNonpublicApi.connectProfile(sHfpClientProfile, device);
  }

  @Rpc(description = "Is HfpClient profile ready.")
  public Boolean bluetoothHfpClientIsReady() {
    return sIsHfpClientReady;
  }

  @Rpc(description = "Set priority of the profile")
  public void bluetoothHfpClientSetPriority(
      @RpcParameter(name = "device", description = "Mac address of a BT device.")
      String deviceStr,
      @RpcParameter(name = "priority", description = "Priority that needs to be set.")
      Integer priority)
      throws Exception {
        BluetoothDevice device =
            Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(), deviceStr);
        BluetoothNonpublicApi.setPriorityProfile(
            sHfpClientProfile, device, priority);
  }

  @Rpc(description = "Get priority of the profile")
  public Integer bluetoothHfpClientGetPriority(
      @RpcParameter(name = "device", description = "Mac address of a BT device.")
      String deviceStr)
      throws Exception {
    if (sHfpClientProfile == null) return BluetoothNonpublicApi.PRIORITY_UNDEFINED;
    BluetoothDevice device =
            Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(), deviceStr);
        return BluetoothNonpublicApi.getPriorityProfile(
            sHfpClientProfile, device);
  }

  @Rpc(description = "Connect to an HFP Client device.")
  public Boolean bluetoothHfpClientConnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a bluetooth device.")
      String deviceStr)
      throws Exception {
    if (sHfpClientProfile == null) return false;
    try {
      BluetoothDevice device =
          Bluetooth4Facade.getDevice(Bluetooth4Facade.DiscoveredDevices, deviceStr);
        // Log.d("Connecting to device " + device.getAliasName());
      return hfpClientConnect(device);
    } catch (Exception e) {
        Log.e("bluetoothHfpClientConnect failed on getDevice " + deviceStr + " with " + e);
        return false;
    }
  }

  @Rpc(description = "Disconnect an HFP Client device.")
  public Boolean bluetoothHfpClientDisconnect(
      @RpcParameter(name = "device", description = "Name or MAC address of a device.")
      String deviceStr) {
    if (sHfpClientProfile == null) return false;
    Log.d("Connected devices: " + sHfpClientProfile.getConnectedDevices());
    try {
        BluetoothDevice device =
            Bluetooth4Facade.getDevice(sHfpClientProfile.getConnectedDevices(), deviceStr);
        return hfpClientDisconnect(device);
    } catch (Exception e) {
        // Do nothing since it is disconnect and this function should force disconnect.
        Log.e("bluetoothHfpClientConnect getDevice failed " + e);
    }
    return false;
  }

  @Rpc(description = "Get all the devices connected through HFP Client.")
  public List<BluetoothDevice> bluetoothHfpClientGetConnectedDevices() {
    if (sHfpClientProfile == null) return new ArrayList<BluetoothDevice>();
    return sHfpClientProfile.getConnectedDevices();
  }

  @Rpc(description = "Get the connection status of a device.")
  public Integer bluetoothHfpClientGetConnectionStatus(
          @RpcParameter(name = "deviceID",
                        description = "Name or MAC address of a bluetooth device.")
          String deviceID) {
      if (sHfpClientProfile == null) {
          return BluetoothProfile.STATE_DISCONNECTED;
      }
      List<BluetoothDevice> deviceList = sHfpClientProfile.getConnectedDevices();
      BluetoothDevice device;
      try {
          device = Bluetooth4Facade.getDevice(deviceList, deviceID);
      } catch (Exception e) {
          Log.e(e);
          return BluetoothProfile.STATE_DISCONNECTED;
      }
      return sHfpClientProfile.getConnectionState(device);
  }

  @Override
  public void shutdown() {
  }
}
