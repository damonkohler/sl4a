/*
 * Copyright (C) 2012 Shimoda
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

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64Codec;

/**
 * USBHostSerialFacade functions. {{{1
 * 
 */

@RpcMinSdk(12)
public class USBHostSerialFacade extends RpcReceiver {

  // UUID for SL4A.
  private static final String DEFAULT_HASHCODE = "";

  private Map<String, UsbSerialConnection> connections = new HashMap<String, UsbSerialConnection>();
  private AndroidFacade mAndroidFacade;
  private Service mService;
  private UsbManager mUsbManager;
  private USBHostSerialReceiver mReceiver;

  // USB
  public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
  public String options = "";

  /**
   * constructor: {{{1
   */
  public USBHostSerialFacade(FacadeManager manager) {
    super(manager);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    mService = manager.getService();
    mUsbManager = (UsbManager) mService.getSystemService(Context.USB_SERVICE);
    mReceiver = null;
  }

  /**
   * registerIntent: from usbserialConnect {{{1
   */
  private void registerIntent() {
    if (mReceiver != null) {
      // this function was already called.
      return;
    }

    Log.d("Register USB Intents...");
    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
    filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
    mReceiver = new USBHostSerialReceiver();
    mService.registerReceiver(mReceiver, filter);
  }

  /**
   * usbserialGetDeviceList: {{{1
   */
  @Rpc(description = "Returns USB devices reported by USB Host API.",
       returns = "Map of id and string information ',' separated"
  )
  public Map<String, String> usbserialGetDeviceList() {
    Map<String, String> ret = new HashMap<String, String>();
    Map<String, UsbDevice> map = mUsbManager.getDeviceList();
    for (Map.Entry<String, UsbDevice> entry : map.entrySet()) {
      UsbDevice dev = entry.getValue();
      String v = "[\"";
      v += dev.getDeviceName();
      v += String.format("\",\"%04X", dev.getVendorId());
      v += String.format("\",\"%04X", dev.getProductId());
      v += "\",\"" + dev.hashCode();
      v += "\"]";
      ret.put(entry.getKey(), v);
    }
    return ret;
  }

  /**
   * collectUSBDevices: start USB connection, from usbserialConnect {{{1
   */
  private String connectUSBDevice(String hash) {
    Integer nHash;
    String ret = "";
    boolean deviceFound = false;

    PendingIntent mPermissionIntent =
        PendingIntent.getBroadcast(mService, 0, new Intent(ACTION_USB_PERMISSION), 0);
    Map<String, UsbDevice> map = mUsbManager.getDeviceList();

    if (hash.equals("")) {
      nHash = 0;
    } else {
      nHash = Integer.parseInt(hash);
    }

    Log.d("USBHostSerial: collectUSBDevices()");
    for (UsbDevice device : map.values()) {
      Log.d("USBHostSerial: try to check " + device.hashCode());
      if (!(nHash == 0) && (nHash != device.hashCode())) {
        continue;
      }
      Log.d("USBHostSerial: requestPermission to =>" + device.getDeviceName());

      UsbSerialConnection conn;
      try {
        conn = new UsbSerialConnection();
      } catch (IOException e) {
        Log.d("can't create UsbSerialConnection object");
        continue;
      }
      conn.mDevice = device;
      conn.options = new String(options);
      addConnection(conn);

      mUsbManager.requestPermission(device, mPermissionIntent);
      deviceFound = true;
      ret += ",\"" + conn.getUUID() + "\"";
    }
    if (!deviceFound) {
      connectionFailed();
      return "device not found";
    }
    return "[\"OK\"" + ret + "]";
  }

  /**
   * USBHostSerialReceiver class: for mUsbReceiver member, receive intent {{{1
   */
  private class USBHostSerialReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.d("USB Intent: onReceive with, " + action);
      if (ACTION_USB_PERMISSION.equals(action)) {
        synchronized (this) {
          UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            openDevice(device);
          } else {
            connectionFailed();
            Log.d("permission denied for device " + device);
          }
        }
      } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
        for (Map.Entry<String, UsbSerialConnection> conn : connections.entrySet()) {
          openDevice(conn.getValue().mDevice);
        }
      } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
        connectionLost();
        for (Map.Entry<String, UsbSerialConnection> conn : connections.entrySet()) {
          conn.getValue().mConnection.close();
        }
      }
    }
  }

  /**
   * openDevice: open the USB device {{{1
   */
  private void openDevice(UsbDevice device) {
    switch (device.getDeviceClass()) {
    case UsbConstants.USB_CLASS_PER_INTERFACE:
      // / 78K Trg-USB Serial
      Log.d("USB openDevice, device => USB_CLASS_PER_INTERFACE");
      break;
    case UsbConstants.USB_CLASS_COMM:
      // / PIC24 USB CDC
      Log.d("USB openDevice, device => USB_CLASS_COMM");
      break;
    default:
      Log.d("USB openDevice, device is not serial...");
      return;
    }

    UsbSerialConnection conn;
    try {
      conn = getConnection(device);
    } catch (IOException e) {
      Log.d("USB openDevice, can't get from connections 1");
      return;
    }
    if (conn == null) {
      Log.d("USB openDevice, can't get from connections 2");
      return;
    }

    Log.d("USB openDevice, try to open...");

    UsbDeviceConnection connection = mUsbManager.openDevice(device);
    if (connection == null) {
      Log.d("connection failed at openDevice...");
      // conn.close();
      return;
    }

    int i, j;

    Log.d("USB open SUCCESS");
    if (conn.mConnection != null) {
      Log.i("already connected? => stop thread");
      conn.stop();
    }
    conn.mConnection = connection;

    if (options.contains("trg78k")) {
      Log.d("USB Host Serial: reset the device (trg78k)");
      // conn.mConnection.controlTransfer(0x21, 0x22, 0x00, 0, null, 0, 0);
      byte[] data = { (byte) 0x03, (byte) 0x01 };
      conn.mConnection.controlTransfer(0x40, 0x00, 0x00, 0, null, 0, 0);
      conn.mConnection.controlTransfer(0x40, 0x0b, 0x00, 0, data, 2, 300);
    }

    for (i = 0; i < device.getInterfaceCount(); i++) {
      UsbInterface ui = device.getInterface(i);
      for (j = 0; j < ui.getEndpointCount(); j++) {
        Log.d(String.format("EndPoint loop...(%d, %d)", i, j));
        UsbEndpoint endPoint = ui.getEndpoint(j);
        switch (endPoint.getType()) {
        case UsbConstants.USB_ENDPOINT_XFER_BULK:
          if (endPoint.getDirection() == UsbConstants.USB_DIR_IN) {
            conn.mInterfaceIn = ui;
            conn.mEndpointIn = endPoint;
            Log.d("USB mEndpointIn initialized!");
          } else {
            conn.mInterfaceOut = ui;
            conn.mEndpointOut = endPoint;
            Log.d("USB mEndpointOut initialized!");
          }
          break;
        case UsbConstants.USB_ENDPOINT_XFER_CONTROL:
          break;
        case UsbConstants.USB_ENDPOINT_XFER_INT:
          conn.mEndpointIntr = endPoint;
          Log.d("USB mEndpointIntr initialized!");
          break;
        case UsbConstants.USB_ENDPOINT_XFER_ISOC:
          break;
        }
      }
    }

    if (options.contains("pl2303")) {
      Log.d("USB Host Serial: setup device (pl2303)");
      int ret;
      byte[] data = new byte[10];
      final byte rdReq = 0x01;
      final byte rdReqType = (byte) 0xC0;
      final byte wrReq = 0x01;
      final byte wrReqType = 0x40;
      final byte glReq = 0x21;
      final byte glReqType = (byte) 0xA1;
      final byte slReq = 0x20;
      final byte slReqType = (byte) 0x21;
      final byte bkReq = 0x23;
      final byte bkReqType = 0x21;
      int pl2303type = 1;

      UsbInterface ui = device.getInterface(0);
      connection.claimInterface(ui, true);
      /*
       * try { /// this cause device close by native_claiminterface. Thread.sleep(1000); } catch
       * (InterruptedException e) { Log.d("USB Host Serial: failed to sleep(1000)"); }
       */
      // / after API Level 13: if (connection.getRawDescriptors()[7] == 64) {
      // pl2303type = 1;
      // }

      connection.controlTransfer(rdReqType, rdReq, 0x8484, 0, data, 1, 100);
      connection.controlTransfer(wrReqType, wrReq, 0x0404, 0, null, 0, 100);
      connection.controlTransfer(rdReqType, rdReq, 0x8484, 0, data, 1, 100);
      connection.controlTransfer(rdReqType, rdReq, 0x8383, 0, data, 1, 100);
      connection.controlTransfer(rdReqType, rdReq, 0x8484, 0, data, 1, 100);
      connection.controlTransfer(wrReqType, wrReq, 0x0404, 1, null, 0, 100);
      connection.controlTransfer(rdReqType, rdReq, 0x8484, 0, data, 1, 100);
      connection.controlTransfer(rdReqType, rdReq, 0x8383, 0, data, 1, 100);
      connection.controlTransfer(wrReqType, wrReq, 0x0000, 1, null, 0, 100);
      connection.controlTransfer(wrReqType, wrReq, 0x0001, 0, null, 0, 100);

      // **type HX**
      // device class != 2
      // packet size == 64
      if (pl2303type == 1) {
        connection.controlTransfer(wrReqType, wrReq, 0x0002, 0x44, null, 0, 100);
      } else {
        connection.controlTransfer(wrReqType, wrReq, 0x0002, 0x24, null, 0, 100);
      }

      /*
       * // reset the device (HX) if (type != 2) { // TODO: halt } else {
       * connection.controlTransfer(wrReqType, wrReq, 0x0008, 0, null, 0, 100);
       * connection.controlTransfer(wrReqType, wrReq, 0x0009, 0, null, 0, 100); }
       */
      // initilize serial
      ret = connection.controlTransfer(glReqType, glReq, 0x0000, 0x00, data, 7, 100);
      Log.d(String.format("pl2303: GetLineRequest: %x => %x-%x-%x-%x-%x-%x-%x", ret, data[0],
          data[1], data[2], data[3], data[4], data[5], data[6]));

      // ret = connection.controlTransfer(wrReqType, wrReq, 0x0000, 0x01, null, 0, 100);
      // Log.d(String.format("pl2303: WriteRequest: %x", ret));

      int baud = 9600;
      data[0] = (byte) (baud & 0xFF);
      data[1] = (byte) ((baud >> 8) & 0xFF);
      data[2] = (byte) ((baud >> 16) & 0xFF);
      data[3] = (byte) ((baud >> 24) & 0xFF);
      data[4] = 0; // stopbit: 1bit, 1: 1.5bits, 2: 2bits
      data[5] = 0; // parity: None, 1:odd, 2:even, 3:mark, 4:space
      data[6] = 8; // data size: 8, 5, 6, 7
      data[7] = 0x00;
      ret = connection.controlTransfer(slReqType, slReq, 0x0000, 0x00, data, 7, 100);
      Log.d(String.format("pl2303: SetLineRequest: %x", ret));

      // set break off
      ret = connection.controlTransfer(bkReqType, bkReq, 0x0000, 0x00, null, 0, 100);

      if (pl2303type == 1) {
        // for RTSCTS and HX device
        ret = connection.controlTransfer(wrReqType, wrReq, 0x0000, 0x61, null, 0, 100);
      } else {
        // for not RTSCTS
        ret = connection.controlTransfer(wrReqType, wrReq, 0x0000, 0x00, null, 0, 100);
      }

      // connection.releaseInterface(ui);
    }

    if (!conn.isConnected()) {
      connectionFailed();
      return;
    }
    conn.start();
  }

  /**
   * usbserialDisconnect: {{{1
   */
  @Rpc(description = "Disconnect all USB-device.")
  public void usbserialDisconnect(
      @RpcParameter(name = "connID", description = "Connection id")
        @RpcOptional @RpcDefault("") String connID
  ) {
    for (Map.Entry<String, UsbSerialConnection> entry : connections.entrySet()) {
      UsbSerialConnection conn = entry.getValue();
      if (connID != "" && !conn.getUUID().equals(connID)) {
        continue;
      }
      removeConnection(conn);
    }

    if (mReceiver != null) {
      mService.unregisterReceiver(mReceiver);
      mReceiver = null;
    }
  }

  /**
   * usbserialActiveConnections: {{{1
   */
  @Rpc(description = "Returns active USB-device connections.",
       returns = "Active USB-device connections by Map UUID vs device-name."
  )
  public Map<String, String> usbserialActiveConnections() {
    Map<String, String> out = new HashMap<String, String>();
    for (Map.Entry<String, UsbSerialConnection> entry : connections.entrySet()) {
      if (entry.getValue().isConnected()) {
        out.put(entry.getKey(), entry.getValue().mDevice.getDeviceName());
      }
    }

    return out;
  }

  /**
   * getConnection (String): obtain connection object from the connection entries {{{1
   */
  private UsbSerialConnection getConnection(String connID) throws IOException {
    UsbSerialConnection conn = null;
    if (connID.trim().length() > 0) {
      conn = connections.get(connID);
    } else if (connections.size() == 1) {
      conn = (UsbSerialConnection) connections.values().toArray()[0];
    }
    if (conn == null) {
      throw new IOException("USB-Device not ready for this connID.");
    }
    return conn;
  }

  /**
   * getConnection (UsbDevice): obtain connection object from the connection entries {{{1
   */
  private UsbSerialConnection getConnection(UsbDevice device) throws IOException {
    for (Map.Entry<String, UsbSerialConnection> entry : connections.entrySet()) {
      UsbSerialConnection conn;
      conn = entry.getValue();
      if (conn.mDevice == device) {
        return conn;
      }
      if (conn.mDevice.hashCode() == device.hashCode()) {
        return conn;
      }
      Log.d(String.format("USB Host Serial: %s != %s", conn.mDevice.getDeviceName(),
          device.getDeviceName()));
    }
    return null;
  }

  /**
   * usbserialWriteBinary: {{{1
   */
  @Rpc(description = "Send bytes over the currently open USB Serial connection.")
  public void usbserialWriteBinary(
      @RpcParameter(name = "base64", description = "A base64 encoded String of the bytes to be sent.") String base64,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {
    UsbSerialConnection conn = getConnection(connID);
    try {
      conn.write(Base64Codec.decodeBase64(base64));
    } catch (IOException e) {
      removeConnection(conn);
      throw e;
    }
  }

  /**
   * usbserialReadBinary: {{{1
   */
  @Rpc(description = "Read up to bufferSize bytes and return a chunked, base64 encoded string.")
  public String usbserialReadBinary(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {

    UsbSerialConnection conn = getConnection(connID);
    try {
      return Base64Codec.encodeBase64String(conn.readBinary(bufferSize));
    } catch (IOException e) {
      removeConnection(conn);
      throw e;
    }
  }

  /**
   * addConnection: add the connectino object to Map and generate UUID {{{1
   */
  private String addConnection(UsbSerialConnection conn) {
    String uuid = UUID.randomUUID().toString();
    connections.put(uuid, conn);
    conn.setUUID(uuid);
    return uuid;
  }

  /**
   * removeConnection: close the connection and remove from connection entries {{{1
   */
  private void removeConnection(UsbSerialConnection conn) {
    if (conn.mConnection != null) {
      conn.mConnection.close();
    }
    conn.stop();
    connections.remove(conn.getUUID());
  }

  /**
   * connectionFailed: Indicate that the connection attempt failed and notify the UI Activity. {{{1
   */
  private void connectionFailed() {
    mAndroidFacade.makeToast("USBHostSerial: Unable to connect device");
  }

  /**
   * connectionLost: Indicate that the connection was lost and notify the UI Activity. {{{1
   */
  private void connectionLost() {
    mAndroidFacade.makeToast("USBHostSerial: connection lost");
  }

  /**
   * usbserialConnect: {{{1
   */
  @Rpc(description = "Connect to a device with USB-Host. request the connection and exit.",
       returns = "messages the request status.")
  public String usbserialConnect(
      @RpcParameter(name = "hashCode", description = "The hash-code passed here must match with USB Host API.") @RpcDefault(DEFAULT_HASHCODE) String hash,
      @RpcParameter(name = "options", description = "comma separated options, acceptable: trg78k, pl2303") @RpcDefault("") String options)
      throws IOException {

    if (!options.equals("")) {
      this.options = new String(options);
    }

    registerIntent();
    return connectUSBDevice(hash);
  }

  /**
   * usbserialHostEnable: {{{1
   */
  @Rpc(description = "Requests that the host be enable for USB Serial connections.",
       returns = "True if the USB Device is accesible, False if the USB Device not enumerated (some devices firmware is not build the USB Host API collectly."
  )
  public Boolean usbserialHostEnable() {
    Map<String, UsbDevice> map = mUsbManager.getDeviceList();
    if (map.isEmpty()) {
      return false;
    }
    return true;
  }

  /**
   * usbserialWrite: {{{1
   */
  @Rpc(description = "Sends ASCII characters over the currently open USB Serial connection.")
  public void usbserialWrite(@RpcParameter(name = "ascii") String ascii,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") String connID)
      throws IOException {
    UsbSerialConnection conn = getConnection(connID);
    try {
      conn.write(ascii);
    } catch (IOException e) {
      removeConnection(conn);
      throw e;
    }
  }

  /**
   * usbserialReadReady: {{{1
   */
  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean usbserialReadReady(
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {
    UsbSerialConnection conn = getConnection(connID);
    try {
      return conn.readReady();
    } catch (IOException e) {
      removeConnection(conn);
      throw e;
    }
  }

  /**
   * usbserialRead: {{{1
   */
  @Rpc(description = "Read up to bufferSize ASCII characters.")
  public String usbserialRead(
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") String connID,
      @RpcParameter(name = "bufferSize") @RpcOptional @RpcDefault("4096") Integer bufferSize)
      throws IOException {
    UsbSerialConnection conn = getConnection(connID);
    if (!conn.readReady()) {
      return "";
    }

    try {
      return conn.read(bufferSize);
    } catch (IOException e) {
      removeConnection(conn);
      throw e;
    }
  }

  /**
   * usbserialGetDeviceName: {{{1
   */
  @Rpc(description = "Queries a remote device for it's name or null if it can't be resolved")
  public String usbserialGetDeviceName(
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("") String connID) {
    UsbSerialConnection conn;
    try {
      conn = getConnection(connID);
    } catch (IOException e) {
      return null;
    }
    try {
      return conn.mDevice.getDeviceName();
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * shutdown: Facade shutdown code {{{1
   */
  @Override
  public void shutdown() {
    for (Map.Entry<String, UsbSerialConnection> entry : connections.entrySet()) {
      entry.getValue().stop();
    }
    connections.clear();
  }
}

/**
 * UsbSerialConnection class: connection management object {{{1
 */
class UsbSerialConnection {
  public UsbDevice mDevice;
  public UsbDeviceConnection mConnection;
  public UsbEndpoint mEndpointIntr;
  public UsbInterface mInterfaceIn;
  public UsbEndpoint mEndpointIn;
  public UsbInterface mInterfaceOut;
  public UsbEndpoint mEndpointOut;

  private SerialInputStream mOutputStream;
  private SerialInputStream mInputStream;
  private String UUID;
  public String options;

  private boolean fMock;
  private UsbcdcThread mThread;

  /**
   * constructor: {{{1
   */
  public UsbSerialConnection() throws IOException {
    byte[] bufin = new byte[4096];
    byte[] bufout = new byte[4096];
    mOutputStream = new SerialInputStream(bufin, 0, 0);
    mInputStream = new SerialInputStream(bufout, 0, 0);

    mDevice = null;
    fMock = false;
    mThread = null;

    mConnection = null;
    mEndpointIntr = null;
    mEndpointIn = null;
    mEndpointOut = null;
  }

  /**
   * start: start the main thread to receive/send serial data {{{1
   */
  public boolean start() {
    mThread = new UsbcdcThread();
    mThread.start();
    return true;
  }

  /**
   * setUUID: {{{1
   */
  public void setUUID(String UUID) {
    this.UUID = UUID;
  }

  /**
   * getUUID: {{{1
   */
  public String getUUID() {
    return UUID;
  }

  /**
   * isConnected: check the members to indicate the connections to USB device {{{1
   */
  public boolean isConnected() {
    if (mDevice == null
     || mConnection == null
     || mEndpointIn == null
     || mEndpointOut == null
    ) {
      return false;
    }
    return true;
  }

  /**
   * write (byte[]): write raw bytes {{{1
   */
  public void write(byte[] out) throws IOException {
    if (isConnected()) {
      mOutputStream.append(out);
    } else {
      throw new IOException("USB Host Serial not ready.");
    }
  }

  /**
   * write (String): write String. String is converted to bytes {{{1
   */
  public void write(String out) throws IOException {
    this.write(out.getBytes());
  }

  /**
   * readReady: check the connection and the remaings in buffer {{{1
   */
  public Boolean readReady() throws IOException {
    if (isConnected()) {
      int ret = mInputStream.available();
      Log.d("UHS: readReady check buffer..." + ret);
      return ret != 0;
    }
    // throw new IOException("USB Serial not ready.");
    return false;
  }

  /**
   * readBinary (): read raw bytes {{{1
   */
  public byte[] readBinary() throws IOException {
    return this.readBinary(4096);
  }

  /**
   * readBinary (int): read raw bytes {{{1
   */
  public byte[] readBinary(int bufferSize) throws IOException {
    if (isConnected()) {
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

    throw new IOException("USB Serial not ready.");
  }

  /**
   * read (), return String: {{{1
   */
  public String read() throws IOException {
    return this.read(4096);
  }

  /**
   * read (int), return String: {{{1
   */
  public String read(int bufferSize) throws IOException {
    if (isConnected()) {
      byte[] buffer = new byte[bufferSize];
      int bytesRead = mInputStream.read(buffer, 0, bufferSize);
      if (bytesRead < 0) {
        Log.e("Read failed.");
        throw new IOException("Read failed.");
      }
      return new String(buffer, 0, bytesRead);
    }
    throw new IOException("USB Serial not ready.");
  }

  /**
   * stop: send stop signal to the main thread {{{1
   */
  public void stop() {
    mThread.keepAlive = false;
    mThread = null;
  }

  /**
   * receiveData: receive data in USB-Loop {{{1
   */
  private byte[] receiveData() {
    if (!isConnected()) {
      return null;
    }

    // Log.d(TAG, "receiving data...");
    synchronized (this) {
      byte[] buffer = new byte[mEndpointIn.getMaxPacketSize()];
      if (mConnection.claimInterface(mInterfaceIn, true)) {
        int size =
            mConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, mEndpointIn.getInterval());
        Log.d("UHS: try to read..." + size);
        if (size > 0) {
          byte[] ret = new byte[size];
          System.arraycopy(buffer, 0, ret, 0, size);
          Log.d("UHS: receiveData!..." + new String(ret));
          return ret;
        }
      }
    }
    return null;
  }

  /**
   * receiveData2: receive data in USB-Loop {{{1
   */
  private byte[] receiveData2() {
    if (!isConnected()) {
      Log.d("UHS: receiveData2, ??? not connected?");
      return null;
    }

    // Log.d("UHS: receiveData2, reading...");
    synchronized (this) {
      byte[] buffer = new byte[mEndpointIn.getMaxPacketSize()];
      int size = mConnection.bulkTransfer(mEndpointIn, buffer, buffer.length, 100);
      // Log.d("UHS: receiveData2, try to read..." + size);
      if (size > 0) {
        byte[] ret = new byte[size];
        System.arraycopy(buffer, 0, ret, 0, size);
        Log.d("UHS: receiveData2, got! ..." + new String(ret));
        return ret;
      }
    }
    return null;
  }

  /**
   * sendData: send data in USB-Loop {{{1
   */
  private boolean sendData(SerialInputStream rd) {
    if (!isConnected()) {
      Log.d("can't sendData, USB not initialzed!");
      return true;
    }

    synchronized (this) {
      if (mConnection.claimInterface(mInterfaceOut, true)) {
        int size;
        int ifmax = mEndpointOut.getMaxPacketSize();
        int rdmax = rd.available();

        if (rdmax > ifmax) {
          rdmax = ifmax;
        }
        byte[] buf = new byte[rdmax];
        size = rd.read(buf, 0, rdmax);
        if (size < 0) {
          return true;
        }

        Log.d("Send byte: " + new String(buf));
        size = mConnection.bulkTransfer(mEndpointOut, buf, buf.length, mEndpointOut.getInterval());
        if (size == 0) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * sendData2: send data in USB-Loop {{{1
   */
  private boolean sendData2(SerialInputStream rd) {
    if (!isConnected()) {
      Log.d("can't sendData, USB not initialzed!");
      return true;
    }

    synchronized (this) {
      int size;
      int ifmax = mEndpointOut.getMaxPacketSize();
      int rdmax = rd.available();

      if (rdmax > ifmax) {
        rdmax = ifmax;
      }
      byte[] buf = new byte[rdmax];
      size = rd.read(buf, 0, rdmax);
      if (size < 0) {
        return true;
      }

      Log.d("UHS: sendData2, send bytes: " + new String(buf));
      size = mConnection.bulkTransfer(mEndpointOut, buf, buf.length, 100);
      if (size == 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * UsbcdcThread class: USB Serial Thread {{{1
   */
  private class UsbcdcThread extends Thread {
    public boolean keepAlive = false;

    /**
     * run: start point of the main thread {{{1
     */
    @Override
    public void run() {
      keepAlive = true;
      if (!fMock) {
        if (options.contains("pl2303")) {
          _runUSBCDC_pl2303();
        } else {
          _runUSBCDC();
        }
      } else {
        // _runMockDevice(); // for debug
      }
    }

    /**
     * _runUSBCDC: actual loop of the main thread {{{1
     */
    public void _runUSBCDC() {
      byte[] buf;

      Log.d("USB Host Serial: thread start.");
      while (keepAlive) {
        buf = receiveData();
        if (buf != null) {
          mInputStream.append(buf);
        }

        if (mOutputStream.available() != 0) {
          sendData(mOutputStream);
        }

        try {
          Thread.sleep(30);
        } catch (InterruptedException e) {
        }
      }
      Log.d("thread finished...");
    }

    public void _runUSBCDC_pl2303() {

      Log.d("USB Host Serial: pl2303 thread start.");

      int siz = mEndpointIntr.getMaxPacketSize();
      byte[] buf = new byte[siz];
      ByteBuffer bufstat = ByteBuffer.allocate(siz);
      UsbRequest req = new UsbRequest();
      req.initialize(mConnection, mEndpointIntr);

      Log.d("UHS: pl2303 thread: start loop.");
      while (keepAlive) {
        //req.queue(bufstat, siz);
        //UsbRequest ret = mConnection.requestWait();
        //if (ret.getEndpoint() == mEndpointIntr) {
        //  Log.d(String.format("chanage state to => %x", bufstat.get(8)));
        //} else {
          buf = receiveData2();
          if (buf != null) {
            mInputStream.append(buf);
          }
          if (mOutputStream.available() != 0) {
            sendData2(mOutputStream);
          }
          // }

          try {
            Thread.sleep(30);
          } catch (InterruptedException e) {
          }
        //}
      }
      Log.d("thread finished...");
    }
  }

  /**
   * SerialInputStream class: {{{1
   */
  public class SerialInputStream extends ByteArrayInputStream {
    public SerialInputStream(byte[] copybuf, int offset, int length) {
      super(copybuf, offset, length);
    }

    public int append(byte[] data) {
      Log.d("UHS, stream, append: data=>" + new String(data));
      int newl = count + data.length;
      if (buf.length < newl) {
        // high cost, please consider buffer size of stream.
        int oldl = count - pos;
        byte[] newb = new byte[oldl + data.length];
        System.arraycopy(buf, pos, newb, 0, oldl);
        System.arraycopy(data, 0, newb, oldl, data.length);
        buf = newb;
        pos = 0;
        count = oldl + data.length;
      } else {
        // copy data and update end.
        Log.d(String.format("UHS, append: count=%d, length=%d, pos=%d", count, data.length, pos));
        System.arraycopy(data, 0, buf, count, data.length);
        count = count + data.length;
      }
      return count - pos;
    }
  }
}
// // end of file {{{1
// vi: ft=java:et:ts=2:nowrap:fdm=marker
