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

import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.ase.AseLog;
import com.google.ase.BluetoothService;
import com.google.ase.activity.BluetoothDeviceManager;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

public class BluetoothFacade implements RpcReceiver {

  // UUID for ASE.
  private static final String DEFAULT_UUID = "457807c0-4897-11df-9879-0800200c9a66";

  private final Service mService;
  private String mConnectedDeviceName;

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case BluetoothService.MESSAGE_STATE_CHANGE:
        switch (msg.arg1) {
        case BluetoothService.STATE_CONNECTED:
          AseLog.v("Bluetooth connected.");
          mEventFacade.postEvent("bluetooth-connected", new Bundle());
          break;
        case BluetoothService.STATE_CONNECTING:
          AseLog.v("Bluetooth connecting.");
          break;
        case BluetoothService.STATE_LISTEN:
          AseLog.v("Bluetooth listening.");
          break;
        case BluetoothService.STATE_NONE:
          AseLog.v("Bluetooth in null state.");
          break;
        }
        break;
      case BluetoothService.MESSAGE_WRITE:
        byte[] writeBuf = (byte[]) msg.obj;
        String writeMessage = new String(writeBuf);
        AseLog.v("Wrote: " + writeMessage);
        break;
      case BluetoothService.MESSAGE_READ:
        byte[] readBuf = (byte[]) msg.obj;
        // construct a string from the valid bytes in the buffer
        String readMessage = new String(readBuf, 0, msg.arg1);
        AseLog.v("Read: " + readMessage);
        Bundle bundle = new Bundle();
        bundle.putString("message", readMessage);
        mEventFacade.postEvent("bluetooth-read", bundle);
        break;
      case BluetoothService.MESSAGE_DEVICE_NAME:
        mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
        AseLog.v("Connected to " + mConnectedDeviceName);
        break;
      case BluetoothService.MESSAGE_TOAST:
        AseLog.e(msg.getData().getString(BluetoothService.TOAST));
        break;
      }
    }
  };

  AndroidFacade mAndroidFacade;
  BluetoothAdapter mBluetoothAdapter;
  BluetoothService mBluetoothService;
  EventFacade mEventFacade;

  public BluetoothFacade(Service service, AndroidFacade androidFacade, EventFacade eventFacade) {
    mService = service;
    mAndroidFacade = androidFacade;
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    mBluetoothService = new BluetoothService(mHandler);
    mEventFacade = eventFacade;
  }

  @Rpc(description = "Displays a dialog with discoverable devices and connects to one chosen by the user.", returns = "True if the connection was established successfully.")
  public boolean bluetoothConnect(
      @RpcParameter(name = "uuid", description = "It is sometimes necessary to specify a particular UUID to use for the Bluetooth connection.") @RpcDefault(DEFAULT_UUID) String uuid) {
    Intent deviceChooserIntent = new Intent(mService, BluetoothDeviceManager.class);
    Intent result = mAndroidFacade.startActivityForResult(deviceChooserIntent);
    if (result != null && result.hasExtra(BluetoothDeviceManager.EXTRA_DEVICE_ADDRESS)) {
      String address = result.getStringExtra(BluetoothDeviceManager.EXTRA_DEVICE_ADDRESS);
      BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
      mBluetoothService.connect(device, UUID.fromString(uuid));
      return true;
    }
    return false;
  }

  @Rpc(description = "Listens for and accepts a Bluetooth connection.")
  public void bluetoothAccept(
      @RpcParameter(name = "uuid", description = "It is sometimes necessary to specify a particular UUID to use for the Bluetooth connection.") @RpcDefault(DEFAULT_UUID) String uuid) {
    mBluetoothService.start(UUID.fromString(uuid));
  }

  @Rpc(description = "Requests that the device be discoverable for Bluetooth connections.")
  public void bluetoothMakeDiscoverable(
      @RpcParameter(name = "duration", description = "period of time, in seconds, during which the device should be discoverable") @RpcDefault("300") Integer duration) {
    if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
      // Use startActivityForResult to make this a synchronous call.
      mAndroidFacade.startActivityForResult(discoverableIntent);
    }
  }

  @Rpc(description = "Sends bytes over the currently open Bluetooth connection.")
  public void bluetoothWrite(String bytes) {
    mBluetoothService.write(bytes.getBytes());
  }

  // The following RPCs belong in the SettingsFacade namespace.

  @Rpc(description = "Checks Bluetooth state.", returns = "True if Bluetooth is enabled.")
  public Boolean checkBluetoothState() {
    return mBluetoothAdapter.isEnabled();
  }

  @Rpc(description = "Toggle Bluetooth on and off.", returns = "True if Bluetooth is enabled.")
  public Boolean toggleBluetoothState(
      @RpcParameter(name = "enabled") @RpcOptional Boolean enabled,
      @RpcParameter(name = "prompt", description = "Prompt the user to confirm changing the Bluetooth state.") @RpcDefault("true") Boolean prompt) {
    if (enabled == null) {
      enabled = !checkBluetoothState();
    }
    if (enabled) {
      if (prompt) {
        AseLog.v("Prompting");
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // TODO(damonkohler): Use the result to determine if this was
        // successful. At any rate, keep using startActivityForResult in order
        // to synchronize this call.
        mAndroidFacade.startActivityForResult(intent);
      } else {
        // TODO(damonkohler): Make this synchronous as well.
        mBluetoothAdapter.enable();
      }
    } else {
      // TODO(damonkohler): Add support for prompting on disable.
      // TODO(damonkohler): Make this synchronous as well.
      mBluetoothAdapter.disable();
    }
    return enabled;
  }

  @Override
  public void shutdown() {
    mBluetoothService.stop();
  }
}
