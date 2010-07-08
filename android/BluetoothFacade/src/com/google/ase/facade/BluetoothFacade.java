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

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Looper;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.MainThreadInitializationFactory;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Callable;

public class BluetoothFacade extends RpcReceiver {

  // UUID for ASE.
  private static final String DEFAULT_UUID = "457807c0-4897-11df-9879-0800200c9a66";

  AndroidFacade mAndroidFacade;
  BluetoothAdapter mBluetoothAdapter;
  BluetoothServer mBluetoothServer;

  public BluetoothFacade(FacadeManager manager) {
    super(manager);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    Looper.prepare();
    mBluetoothServer = new BluetoothServer(manager.getReceiver(EventFacade.class));
    mBluetoothAdapter =
        MainThreadInitializationFactory.init(manager.getService(), new Callable<BluetoothAdapter>() {
          @Override
          public BluetoothAdapter call() throws Exception {
            return BluetoothAdapter.getDefaultAdapter();
          }
        });
  }

  @Rpc(description = "Displays a dialog with discoverable devices and connects to one chosen by the user.", returns = "True if the connection was established successfully.")
  public boolean bluetoothConnect(
      @RpcParameter(name = "uuid", description = "It is sometimes necessary to specify a particular UUID to use for the Bluetooth connection.") @RpcDefault(DEFAULT_UUID) String uuid) {
    Intent deviceChooserIntent = new Intent();
    deviceChooserIntent.setComponent(Constants.BLUETOOTH_DEVICE_LIST_COMPONENT_NAME);
    Intent result = mAndroidFacade.startActivityForResult(deviceChooserIntent);
    if (result != null && result.hasExtra(Constants.EXTRA_DEVICE_ADDRESS)) {
      String address = result.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
      BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
      mBluetoothServer.connect(device, UUID.fromString(uuid));
      return true;
    }
    return false;
  }

  @Rpc(description = "Listens for and accepts a Bluetooth connection.")
  public void bluetoothAccept(
      @RpcParameter(name = "uuid", description = "It is sometimes necessary to specify a particular UUID to use for the Bluetooth connection.") @RpcDefault(DEFAULT_UUID) String uuid) {
    mBluetoothServer.start(UUID.fromString(uuid));
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
  public void bluetoothWrite(@RpcParameter(name = "bytes") String bytes) throws IOException {
    if (mBluetoothServer != null) {
      mBluetoothServer.getOutputStream().write(bytes.getBytes());
    } else {
      throw new IOException("Bluetooth not ready.");
    }
  }

  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean bluetoothReadReady() throws IOException {
    if (mBluetoothServer != null) {
      return mBluetoothServer.getReader().ready();
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Read up to bufferSize bytes.")
  public String bluetoothRead(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize) throws IOException {
    if (mBluetoothServer != null) {
      char[] buffer = new char[bufferSize];
      int bytesRead = mBluetoothServer.getReader().read(buffer);
      if (bytesRead == -1) {
        AseLog.e("Read failed.");
        throw new IOException("Read failed.");
      }
      return new String(buffer, 0, bytesRead);
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Read the next line.")
  public String bluetoothReadLine() throws IOException {
    if (mBluetoothServer != null) {
      return mBluetoothServer.getReader().readLine();
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Returns the name of the connected device.")
  public String bluetoothGetConnectedDeviceName() {
    if (mBluetoothServer != null) {
      return mBluetoothServer.getDeviceName();
    }
    return null;
  }

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
        // TODO(damonkohler): Use the result to determine if this was successful. At any rate, keep
        // using startActivityForResult in order to synchronize this call.
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
    mBluetoothServer.stop();
  }
}
