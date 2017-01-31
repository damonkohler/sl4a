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

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.bluetooth.BluetoothPairingHelper;
import com.googlecode.android_scripting.facade.FacadeManager;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.lang.reflect.Field;
import java.lang.Thread;

import org.apache.commons.codec.binary.Base64Codec;

/**
 * Bluetooth functions.
 *
 */
// Discovery functions added by Eden Sayag

public class BluetoothRfcommFacade extends RpcReceiver {

  // UUID for SL4A.
  private static final String DEFAULT_UUID = "457807c0-4897-11df-9879-0800200c9a66";
  private static final String SDP_NAME = "SL4A";
  private final Service mService;
  private final BluetoothAdapter mBluetoothAdapter;
  private Map<String, BluetoothConnection>
          connections = new HashMap<String, BluetoothConnection>();
  private final EventFacade mEventFacade;
  private ConnectThread mConnectThread;
  private AcceptThread mAcceptThread;

  public BluetoothRfcommFacade(FacadeManager manager) {
    super(manager);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mService = manager.getService();
    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
  }

  private BluetoothConnection getConnection(String connID) throws IOException {
    BluetoothConnection conn = null;
    if (connID.trim().length() > 0) {
      conn = connections.get(connID);
    } else if (connections.size() == 1) {
      conn = (BluetoothConnection) connections.values().toArray()[0];
    }
    if (conn == null) {
      throw new IOException("Bluetooth connection not established.");
    }
    return conn;
  }

  private String addConnection(BluetoothConnection conn) {
    String uuid = UUID.randomUUID().toString();
    connections.put(uuid, conn);
    conn.setUUID(uuid);
    return uuid;
  }

  @Rpc(description = "Begins a thread initiate an Rfcomm connection over Bluetooth. ")
  public void bluetoothRfcommBeginConnectThread(
      @RpcParameter(name = "address", description = "The mac address of the device to connect to.")
      String address,
      @RpcParameter(name = "uuid",
      description = "The UUID passed here must match the UUID used by the server device.")
      @RpcDefault(DEFAULT_UUID)
      String uuid)
      throws IOException {
    BluetoothDevice mDevice;
    mDevice = mBluetoothAdapter.getRemoteDevice(address);
    ConnectThread connectThread = new ConnectThread(mDevice, uuid);
    connectThread.start();
    mConnectThread = connectThread;
  }

  @Rpc(description = "Kill thread")
  public void bluetoothRfcommKillConnThread() {
    try {
      mConnectThread.cancel();
      mConnectThread.join(5000);
    } catch (InterruptedException e) {
      Log.e("Interrupted Exception: " + e.toString());
    }
  }

  /**
   * Closes an active Rfcomm Client socket
   */
  @Rpc(description = "Close an active Rfcomm Client socket")
  public void bluetoothRfcommEndConnectThread()
    throws IOException {
    mConnectThread.cancel();
  }

  /**
   * Closes an active Rfcomm Server socket
   */
  @Rpc(description = "Close an active Rfcomm Server socket")
  public void bluetoothRfcommEndAcceptThread()
    throws IOException {
    mAcceptThread.cancel();
  }

  @Rpc(description = "Returns active Bluetooth connections.")
  public Map<String, String> bluetoothRfcommActiveConnections() {
    Map<String, String> out = new HashMap<String, String>();
    for (Map.Entry<String, BluetoothConnection> entry : connections.entrySet()) {
      if (entry.getValue().isConnected()) {
        out.put(entry.getKey(), entry.getValue().getRemoteBluetoothAddress());
      }
    }
    return out;
  }

  @Rpc(description = "Returns the name of the connected device.")
  public String bluetoothRfcommGetConnectedDeviceName(
      @RpcParameter(name = "connID", description = "Connection id")
      @RpcOptional @RpcDefault("")
      String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    return conn.getConnectedDeviceName();
  }

  @Rpc(description = "Begins a thread to accept an Rfcomm connection over Bluetooth. ")
  public void bluetoothRfcommBeginAcceptThread(
      @RpcParameter(name = "uuid") @RpcDefault(DEFAULT_UUID) String uuid,
      @RpcParameter(name = "timeout",
                    description = "How long to wait for a new connection, 0 is wait for ever")
      @RpcDefault("0") Integer timeout)
      throws IOException {
    Log.d("Accept bluetooth connection");
    BluetoothServerSocket mServerSocket;
    AcceptThread acceptThread = new AcceptThread(uuid, timeout.intValue());
    acceptThread.start();
    mAcceptThread = acceptThread;
  }

  @Rpc(description = "Sends ASCII characters over the currently open Bluetooth connection.")
  public void bluetoothRfcommWrite(@RpcParameter(name = "ascii") String ascii,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    try {
      conn.write(ascii);
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read up to bufferSize ASCII characters.")
  public String bluetoothRfcommRead(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize,
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("")
      String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    try {
      return conn.read(bufferSize);
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Send bytes over the currently open Bluetooth connection.")
  public void bluetoothRfcommWriteBinary(
      @RpcParameter(name = "base64",
                    description = "A base64 encoded String of the bytes to be sent.")
      String base64,
      @RpcParameter(name = "connID", description = "Connection id")
      @RpcDefault("") @RpcOptional
      String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    try {
      conn.write(Base64Codec.decodeBase64(base64));
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read up to bufferSize bytes and return a chunked, base64 encoded string.")
  public String bluetoothRfcommReadBinary(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize,
      @RpcParameter(name = "connID", description = "Connection id")
      @RpcDefault("") @RpcOptional
      String connID)
      throws IOException {

    BluetoothConnection conn = getConnection(connID);
    try {
      return Base64Codec.encodeBase64String(conn.readBinary(bufferSize));
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean bluetoothRfcommReadReady(
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional
      String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    try {
      return conn.readReady();
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read the next line.")
  public String bluetoothRfcommReadLine(
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("")
      String connID)
      throws IOException {
    BluetoothConnection conn = getConnection(connID);
    try {
      return conn.readLine();
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Stops Bluetooth connection.")
  public void bluetoothRfcommStop(
      @RpcParameter
      (name = "connID", description = "Connection id") @RpcOptional @RpcDefault("")
      String connID) {
    BluetoothConnection conn;
    try {
      conn = getConnection(connID);
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }
    if (conn == null) {
      return;
    }

    conn.stop();
    connections.remove(conn.getUUID());

    if (mAcceptThread != null) {
        mAcceptThread.cancel();
    }
    if (mConnectThread != null) {
        mConnectThread.cancel();
    }
  }

  @Override
  public void shutdown() {
    for (Map.Entry<String, BluetoothConnection> entry : connections.entrySet()) {
      entry.getValue().stop();
    }
    connections.clear();
    if (mAcceptThread != null) {
        mAcceptThread.cancel();
    }
    if (mConnectThread != null) {
        mConnectThread.cancel();
    }
  }

  private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;

    public ConnectThread(BluetoothDevice device, String uuid) {
      BluetoothSocket tmp = null;
      try {
        tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
      } catch (IOException createSocketException) {
        Log.e("Failed to create socket: " + createSocketException.toString());
      }
      mmSocket = tmp;
    }

    public void run() {
      mBluetoothAdapter.cancelDiscovery();
      try {
        BluetoothConnection conn;
        mmSocket.connect();
        conn = new BluetoothConnection(mmSocket);
        Log.d("Connection Successful");
        addConnection(conn);
      } catch(IOException connectException) {
        cancel();
        return;
      }
    }

    public void cancel() {
      if (mmSocket != null) {
        try {
          mmSocket.close();
        } catch (IOException closeException){
          Log.e("Failed to close socket: " + closeException.toString());
        }
      }
    }

    public BluetoothSocket getSocket() {
      return mmSocket;
    }
  }


  private class AcceptThread extends Thread {
    private final BluetoothServerSocket mmServerSocket;
    private final int mTimeout;
    private BluetoothSocket mmSocket;

    public AcceptThread(String uuid, int timeout) {
      BluetoothServerSocket tmp = null;
      mTimeout = timeout;
      try {
        tmp =
            mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, UUID.fromString(uuid));
      } catch (IOException createSocketException) {
        Log.e("Failed to create socket: " + createSocketException.toString());
      }
      mmServerSocket = tmp;
    }

    public void run() {
      try {
        mmSocket = mmServerSocket.accept(mTimeout);
        BluetoothConnection conn = new BluetoothConnection(mmSocket, mmServerSocket);
        addConnection(conn);
      } catch(IOException connectException) {
        Log.e("Failed to connect socket: " + connectException.toString());
        if (mmSocket != null) {
          cancel();
        }
        return;
      }
    }

    public void cancel() {
      if (mmSocket != null) {
        try {
          mmSocket.close();
        } catch (IOException closeException){
          Log.e("Failed to close socket: " + closeException.toString());
        }
      }
      if (mmServerSocket != null) {
        try{
          mmServerSocket.close();
        } catch (IOException closeException) {
          Log.e("Failed to close socket: " + closeException.toString());
        }
      }
    }

    public BluetoothSocket getSocket() {
      return mmSocket;
    }
  }

}


class BluetoothConnection {
  private BluetoothSocket mSocket;
  private BluetoothDevice mDevice;
  private OutputStream mOutputStream;
  private InputStream mInputStream;
  private BufferedReader mReader;
  private BluetoothServerSocket mServerSocket;
  private String UUID;

  public BluetoothConnection(BluetoothSocket mSocket) throws IOException {
    this(mSocket, null);
  }

  public BluetoothConnection(BluetoothSocket mSocket, BluetoothServerSocket mServerSocket)
      throws IOException {
    this.mSocket = mSocket;
    mOutputStream = mSocket.getOutputStream();
    mInputStream = mSocket.getInputStream();
    mDevice = mSocket.getRemoteDevice();
    mReader = new BufferedReader(new InputStreamReader(mInputStream, "ASCII"));
    this.mServerSocket = mServerSocket;
  }

  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  public String getUUID() {
    return UUID;
  }

  public String getRemoteBluetoothAddress() {
    return mDevice.getAddress();
  }

  public boolean isConnected() {
    if (mSocket == null) {
      return false;
    }
    try {
      mSocket.getRemoteDevice();
      mInputStream.available();
      mReader.ready();
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public void write(byte[] out) throws IOException {
    if (mOutputStream != null) {
      mOutputStream.write(out);
    } else {
      throw new IOException("Bluetooth not ready.");
    }
  }

  public void write(String out) throws IOException {
    this.write(out.getBytes());
  }

  public Boolean readReady() throws IOException {
    if (mReader != null) {
      return mReader.ready();
    }
    throw new IOException("Bluetooth not ready.");
  }

  public byte[] readBinary() throws IOException {
    return this.readBinary(4096);
  }

  public byte[] readBinary(int bufferSize) throws IOException {
    if (mReader != null) {
      byte[] buffer = new byte[bufferSize];
      int bytesRead = mInputStream.read(buffer);
      if (bytesRead == -1) {
        Log.e("Read failed.");
        throw new IOException("Read failed.");
      }
      byte[] truncatedBuffer = new byte[bytesRead];
      System.arraycopy(buffer, 0, truncatedBuffer, 0, bytesRead);
      return truncatedBuffer;
    }

    throw new IOException("Bluetooth not ready.");

  }

  public String read() throws IOException {
    return this.read(4096);
  }

  public String read(int bufferSize) throws IOException {
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

  public String readLine() throws IOException {
    if (mReader != null) {
      return mReader.readLine();
    }
    throw new IOException("Bluetooth not ready.");
  }

  public String getConnectedDeviceName() {
    return mDevice.getName();
  }

  private synchronized void clearFileDescriptor() {
    try {
      Field field = BluetoothSocket.class.getDeclaredField("mPfd");
      field.setAccessible(true);
      ParcelFileDescriptor mPfd = (ParcelFileDescriptor) field.get(mSocket);
      Log.d("Closing mPfd: " + mPfd);
      if (mPfd == null)
        return;
      mPfd.close();
      mPfd = null;
      try { field.set(mSocket, mPfd); }
      catch(Exception e) {
          Log.d("Exception setting mPfd = null in cleanCloseFix(): " + e.toString());
      }
    } catch (Exception e) {
        Log.w("ParcelFileDescriptor could not be cleanly closed.", e);
    }
  }

  public void stop() {
    if (mSocket != null) {
      try {
        mSocket.close();
        clearFileDescriptor();
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
}
