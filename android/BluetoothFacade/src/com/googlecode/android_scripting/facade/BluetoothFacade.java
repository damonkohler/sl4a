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

package com.googlecode.android_scripting.facade;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.apache.commons.codec.binary.Base64;

public class BluetoothFacade extends RpcReceiver {

  // UUID for SL4A.
  private static final String DEFAULT_UUID = "457807c0-4897-11df-9879-0800200c9a66";
  private static final String SDP_NAME = "SL4A";

  private AndroidFacade mAndroidFacade;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothSocket mSocket;
  private BluetoothServerSocket mServerSocket;
  private BluetoothDevice mDevice;
  private OutputStream mOutputStream;
  private InputStream mInputStream;
  private BufferedReader mReader;

  public BluetoothFacade(FacadeManager manager) {
    super(manager);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    mBluetoothAdapter = MainThread.run(manager.getService(), new Callable<BluetoothAdapter>() {
      @Override
      public BluetoothAdapter call() throws Exception {
        return BluetoothAdapter.getDefaultAdapter();
      }
    });
  }

  @Rpc(description = "Send bytes over the currently open Bluetooth connection.")
  public void bluetoothWriteBinary(
      @RpcParameter(name = "base64", description = "A base64 encoded String of the bytes to be sent.") String base64)
      throws IOException {
    if (mOutputStream != null) {
      mOutputStream.write(Base64.decodeBase64(base64.getBytes()));
    } else {
      throw new IOException("Bluetooth not ready.");
    }
  }

  @Rpc(description = "Read bufferSize bytes and return a chunked, base64 encoded string.")
  public String bluetoothReadBinary(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize) throws IOException {
    if (mReader != null) {
      byte[] buffer = new byte[bufferSize];
      int position = 0;
      do {
        int bytesRead = mInputStream.read(buffer, position, bufferSize - position);
        if (bytesRead == -1) {
          Log.e("Read failed.");
          throw new IOException("Read failed.");
        }
        position += bytesRead;
      } while (position < bufferSize);
      return new String(Base64.encodeBase64(buffer));
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Returns an estimate of the number of bytes available for reading without blocking.")
  public Integer bluetoothAvailable() throws IOException {
    if (mInputStream != null) {
      return mInputStream.available();
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Skips all pending input.")
  public void bluetoothSkipPendingInput() throws IOException {
    if (mInputStream != null) {
      long bytesSkipped;
      do {
        bytesSkipped = mInputStream.skip(mInputStream.available());
      } while (bytesSkipped > 0);
    } else {
      throw new IOException("Bluetooth not ready.");
    }
  }

  @Rpc(description = "Connect to a device over Bluetooth. Blocks until the connection is established or fails.", returns = "True if the connection was established successfully.")
  public boolean bluetoothConnect(
      @RpcParameter(name = "uuid", description = "The UUID passed here must match the UUID used by the server device.") @RpcDefault(DEFAULT_UUID) String uuid,
      @RpcParameter(name = "address", description = "The user will be presented with a list of discovered devices to choose from if an address is not provided.") @RpcOptional String address)
      throws IOException {
    if (address == null) {
      Intent deviceChooserIntent = new Intent();
      deviceChooserIntent.setComponent(Constants.BLUETOOTH_DEVICE_LIST_COMPONENT_NAME);
      Intent result = mAndroidFacade.startActivityForResult(deviceChooserIntent);
      if (result != null && result.hasExtra(Constants.EXTRA_DEVICE_ADDRESS)) {
        address = result.getStringExtra(Constants.EXTRA_DEVICE_ADDRESS);
      } else {
        return false;
      }
    } else {
      // Android only accepts all upper case addresses. This is only for convenience.
      address = address.toUpperCase();
    }
    mDevice = mBluetoothAdapter.getRemoteDevice(address);
    mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
    // Always cancel discovery because it will slow down a connection.
    mBluetoothAdapter.cancelDiscovery();
    mSocket.connect();
    connected();
    return true;
  }

  @Rpc(description = "Listens for and accepts a Bluetooth connection. Blocks until the connection is established or fails.")
  public void bluetoothAccept(@RpcParameter(name = "uuid") @RpcDefault(DEFAULT_UUID) String uuid)
      throws IOException {
    mServerSocket =
        mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, UUID.fromString(uuid));
    mSocket = mServerSocket.accept();
    mDevice = mSocket.getRemoteDevice();
    connected();
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

  @Rpc(description = "Sends ASCII characters over the currently open Bluetooth connection.")
  public void bluetoothWrite(@RpcParameter(name = "ascii") String ascii) throws IOException {
    if (mOutputStream != null) {
      mOutputStream.write(ascii.getBytes());
    } else {
      throw new IOException("Bluetooth not ready.");
    }
  }

  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean bluetoothReadReady() throws IOException {
    if (mReader != null) {
      return mReader.ready();
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Read up to bufferSize ASCII characters.")
  public String bluetoothRead(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize) throws IOException {
    if (mReader != null) {
      char[] buffer = new char[bufferSize];
      int bytesRead = mReader.read(buffer);
      if (bytesRead == -1) {
        Log.e("Read failed.");
        throw new IOException("Read failed.");
      }
      return new String(buffer, 0, bytesRead);
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Read the next line.")
  public String bluetoothReadLine() throws IOException {
    if (mReader != null) {
      return mReader.readLine();
    }
    throw new IOException("Bluetooth not ready.");
  }

  @Rpc(description = "Returns the name of the connected device.")
  public String bluetoothGetConnectedDeviceName() {
    return mDevice.getName();
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

  public void connected() throws IOException {
    mOutputStream = mSocket.getOutputStream();
    mInputStream = mSocket.getInputStream();
    mReader = new BufferedReader(new InputStreamReader(mInputStream, "ASCII"));
  }

  @Rpc(description = "Stops Bluetooth connection.")
  public void bluetoothStop() {
    if (mSocket != null) {
      try {
        mSocket.close();
      } catch (IOException e) {
        Log.e(e);
      }
    }
    mSocket = null;
    if (mServerSocket != null) {
      try {
        mServerSocket.close();
      } catch (IOException e) {
        Log.e(e);
      }
    }
    mServerSocket = null;
    if (mInputStream != null) {
      try {
        mInputStream.close();
      } catch (IOException e) {
        Log.e(e);
      }
    }
    mInputStream = null;
    if (mOutputStream != null) {
      try {
        mOutputStream.close();
      } catch (IOException e) {
        Log.e(e);
      }
    }
    mOutputStream = null;
    if (mReader != null) {
      try {
        mReader.close();
      } catch (IOException e) {
        Log.e(e);
      }
    }
    mReader = null;
  }

  @Override
  public void shutdown() {
    bluetoothStop();
  }
}
