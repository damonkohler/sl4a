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
import android.bluetooth.BluetoothA2dp;
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
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcParameter;

// @RpcMinSdk(5)
@RpcMinSdk(11)
public class BluetoothA2dpFacade extends RpcReceiver {
  static final ParcelUuid[] SINK_UUIDS = {
    BluetoothUuid.AudioSink, BluetoothUuid.AdvAudioDist,
  };
  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;

  private static boolean sIsA2dpReady = false;
  private static BluetoothA2dp sA2dpProfile = null;

  public BluetoothA2dpFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothAdapter.getProfileProxy(mService, new A2dpServiceListener(),
        BluetoothProfile.A2DP);
  }

  class A2dpServiceListener implements BluetoothProfile.ServiceListener {
    @Override
    public void onServiceConnected(int profile, BluetoothProfile proxy) {
      sA2dpProfile = (BluetoothA2dp) proxy;
      sIsA2dpReady = true;
    }

    @Override
    public void onServiceDisconnected(int profile) {
      sIsA2dpReady = false;
    }
  }

  public Boolean a2dpConnect(BluetoothDevice device) {
    List<BluetoothDevice> sinks = sA2dpProfile.getConnectedDevices();
    if (sinks != null) {
      for (BluetoothDevice sink : sinks) {
                BluetoothNonpublicApi.disconnectProfile(sA2dpProfile, sink);
      }
    }
        return BluetoothNonpublicApi.connectProfile(sA2dpProfile, device);
  }

  public Boolean a2dpDisconnect(BluetoothDevice device) {
      BluetoothNonpublicApi.priorityOnProfile(sA2dpProfile, device);
      return BluetoothNonpublicApi.disconnectProfile(sA2dpProfile, device);
  }

  /**
   * Checks to see if the A2DP profile is ready for use.
   *
   * @return Returns true if the A2DP Profile is ready.
   */
  @Rpc(description = "Is A2dp profile ready.")
  public Boolean bluetoothA2dpIsReady() {
    return sIsA2dpReady;
  }

  @Rpc(description = "Set priority of the profile")
  public void bluetoothA2dpSetPriority(
      @RpcParameter(name = "device", description = "Mac address of a BT device.")
      String deviceStr,
      @RpcParameter(name = "priority", description = "Priority that needs to be set.")
      Integer priority)
      throws Exception {
    if (sA2dpProfile == null) return;
    BluetoothDevice device =
        Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(), deviceStr);
        // Log.d("Changing priority of device " + device.getAliasName() + " p: " + priority);
        BluetoothNonpublicApi.setPriorityProfile(
              (BluetoothProfile)sA2dpProfile, device, priority);
  }


  /**
   * Connect to remote device using the A2DP profile.
   *
   * @param deviceID the name or mac address of the remote Bluetooth device.
   * @return True if connected successfully.
   * @throws Exception
   */
  @Rpc(description = "Connect to an A2DP device.")
  public Boolean bluetoothA2dpConnect(
      @RpcParameter(name = "deviceID", description = "Name or MAC address of a bluetooth device.")
      String deviceID)
      throws Exception {
    if (sA2dpProfile == null)
      return false;
    BluetoothDevice mDevice = Bluetooth4Facade.getDevice(
        Bluetooth4Facade.DiscoveredDevices, deviceID);
    // Log.d("Connecting to device " + mDevice.getAliasName());
    return a2dpConnect(mDevice);
  }

  /**
   * Disconnect a remote device using the A2DP profile.
   *
   * @param deviceID the name or mac address of the remote Bluetooth device.
   * @return True if connected successfully.
   * @throws Exception
   */
  @Rpc(description = "Disconnect an A2DP device.")
  public Boolean bluetoothA2dpDisconnect(
      @RpcParameter(name = "deviceID", description = "Name or MAC address of a device.")
      String deviceID)
      throws Exception {
    if (sA2dpProfile == null)
      return false;
    List<BluetoothDevice> connectedA2dpDevices = sA2dpProfile.getConnectedDevices();
    Log.d("Connected a2dp devices " + connectedA2dpDevices);
    BluetoothDevice mDevice = Bluetooth4Facade.getDevice(connectedA2dpDevices, deviceID);
    return a2dpDisconnect(mDevice);
  }

  /**
   * Get the list of devices connected through the A2DP profile.
   *
   * @return List of bluetooth devices that are in one of the following states:
   *   connected, connecting, and disconnecting.
   */
  @Rpc(description = "Get all the devices connected through A2DP.")
  public List<BluetoothDevice> bluetoothA2dpGetConnectedDevices() {
    while (!sIsA2dpReady);
    return sA2dpProfile.getDevicesMatchingConnectionStates(
          new int[] {BluetoothProfile.STATE_CONNECTED,
                     BluetoothProfile.STATE_CONNECTING,
                     BluetoothProfile.STATE_DISCONNECTING});
  }

  @Override
  public void shutdown() {
  }
}
