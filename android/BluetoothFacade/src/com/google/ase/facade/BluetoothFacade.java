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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
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
  private final PipedOutputStream mOutputStream;
  private final PipedInputStream mInputStream;
  private final BufferedReader mReader;

  private final Handler mHandler = new Handler() {

    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case BluetoothService.MESSAGE_STATE_CHANGE:
        switch (msg.arg1) {
        case BluetoothService.STATE_CONNECTED:
          AseLog.v("Bluetooth connected.");
          mEventFacade.postEvent("bluetooth", "connected");
          break;
        case BluetoothService.STATE_CONNECTING:
          AseLog.v("Bluetooth connecting.");
          mEventFacade.postEvent("bluetooth", "connecting");
          break;
        case BluetoothService.STATE_LISTEN:
          AseLog.v("Bluetooth listening.");
          mEventFacade.postEvent("bluetooth", "listening");
          break;
        case BluetoothService.STATE_IDLE:
          AseLog.v("Bluetooth in null state.");
          mEventFacade.postEvent("bluetooth", "idle");
          break;
        }
        break;
      case BluetoothService.MESSAGE_WRITE:
        // Wrote to Bluetooth channel.
        break;
      case BluetoothService.MESSAGE_READ:
        try {
          mOutputStream.write((byte[]) msg.obj);
        } catch (IOException e) {
          AseLog.e("Failed to read from Bluetooth.", e);
        }
        break;
      case BluetoothService.MESSAGE_DEVICE_NAME:
        mConnectedDeviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
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

  public BluetoothFacade(Service service, AndroidFacade androidFacade, EventFacade eventFacade)
      throws IOException {
    // Initialize these first in case an exception is thrown.
    mOutputStream = new PipedOutputStream();
    mInputStream = new PipedInputStream(mOutputStream);
    mReader = new BufferedReader(new InputStreamReader(mInputStream, "ASCII"));

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

  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean bluetoothReady() throws IOException {
    return mReader.ready();
  }

  @Rpc(description = "Read up to bufferSize bytes.")
  public String bluetoothRead(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize) throws IOException {
    char[] buffer = new char[bufferSize];
    int bytesRead = mReader.read(buffer);
    if (bytesRead == -1) {
      AseLog.e("Read failed.");
      throw new IOException("Read failed.");
    }
    return new String(buffer, 0, bytesRead);
  }

  @Rpc(description = "Read the next line.")
  public String bluetoothReadLine() throws IOException {
    return mReader.readLine();
  }

  @Rpc(description = "Returns the name of the connected device.")
  public String bluetoothGetConnectedDeviceName() {
    return mConnectedDeviceName;
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
