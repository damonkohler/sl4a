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

package com.googlecode.android_scripting.facade;

import android.app.Service;
// import android.bluetooth.BluetoothActivityEnergyInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.bluetooth.BluetoothNonpublicApi;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.Collection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.binary.Base64Codec;

/**
 * Basic Bluetooth functions.
 */
// Discovery functions added by Eden Sayag

@RpcMinSdk(11)
public class Bluetooth4Facade extends RpcReceiver {

  // UUID for SL4A.
  private static final String DEFAULT_UUID = "457807c0-4897-11df-9879-0800200c9a66";
  private static final String SDP_NAME = "SL4A";

    private final Service mService;
    private final BroadcastReceiver mDiscoveryReceiver;
    private final IntentFilter discoveryFilter;
    private final EventFacade mEventFacade;
    private final BluetoothStateReceiver mStateReceiver;
    private static final Object mReceiverLock = new Object();
    private BluetoothStateReceiver mMultiStateReceiver;
    private final BleStateReceiver mBleStateReceiver;
    private Map<String, Bluetooth4Connection> connections =
            new HashMap<String, Bluetooth4Connection>();
  private AndroidFacade mAndroidFacade;
  private BluetoothAdapter mBluetoothAdapter;

    public static ConcurrentHashMap<String, BluetoothDevice> DiscoveredDevices;

  public Bluetooth4Facade(FacadeManager manager) {
    super(manager);
    mAndroidFacade = manager.getReceiver(AndroidFacade.class);
    mBluetoothAdapter = MainThread.run(manager.getService(), new Callable<BluetoothAdapter>() {
      @Override
      public BluetoothAdapter call() throws Exception {
        return BluetoothAdapter.getDefaultAdapter();
      }
    });
        mEventFacade = manager.getReceiver(EventFacade.class);
        mService = manager.getService();

        DiscoveredDevices = new ConcurrentHashMap<String, BluetoothDevice>();
        discoveryFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        discoveryFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mDiscoveryReceiver = new DiscoveryCacheReceiver();
        mStateReceiver = new BluetoothStateReceiver();
        mMultiStateReceiver = null;
        mBleStateReceiver = new BleStateReceiver();
  }

    class DiscoveryCacheReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Log.d("Found device " + device.getAliasName());
                if (!DiscoveredDevices.containsKey(device.getAddress())) {
                    String name = null;  // device.getAliasName();
                    if (name != null) {
                        DiscoveredDevices.put(name, device);
                    }
                    DiscoveredDevices.put(device.getAddress(), device);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                mEventFacade.postEvent("BluetoothDiscoveryFinished", new Bundle());
                mService.unregisterReceiver(mDiscoveryReceiver);
            }
        }
    }

    class BluetoothStateReceiver extends BroadcastReceiver {

        private final boolean mIsMultiBroadcast;

        public BluetoothStateReceiver() {
            mIsMultiBroadcast = false;
        }

        public BluetoothStateReceiver(boolean isMultiBroadcast) {
            mIsMultiBroadcast = isMultiBroadcast;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = mBluetoothAdapter.getState();
                Bundle msg = new Bundle();
                if (state == BluetoothAdapter.STATE_ON) {
                    msg.putString("State", "ON");
                    mEventFacade.postEvent("BluetoothStateChangedOn", msg);
                    if (!mIsMultiBroadcast) mService.unregisterReceiver(mStateReceiver);
                } else if(state == BluetoothAdapter.STATE_OFF) {
                    msg.putString("State", "OFF");
                    mEventFacade.postEvent("BluetoothStateChangedOff", msg);
                    if (!mIsMultiBroadcast) mService.unregisterReceiver(mStateReceiver);
                }
                msg.clear();
            }
        }
    }

    class BleStateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(BluetoothNonpublicApi.ACTION_BLE_STATE_CHANGED)) {
                int state = BluetoothNonpublicApi.getLeState(mBluetoothAdapter);
                if (state == BluetoothNonpublicApi.STATE_BLE_ON) {
                    mEventFacade.postEvent("BleStateChangedOn", new Bundle());
                    mService.unregisterReceiver(mBleStateReceiver);
                } else if (state == BluetoothAdapter.STATE_OFF) {
                    mEventFacade.postEvent("BleStateChangedOff", new Bundle());
                    mService.unregisterReceiver(mBleStateReceiver);
                }
            }
        }
    }


    public static boolean deviceMatch(BluetoothDevice device, String deviceID) {
        // if (deviceID.equals(device.getAliasName())) {return true;}
        return deviceID.equals(device.getAddress());
    }

    public static <T> BluetoothDevice getDevice(ConcurrentHashMap<String, T> devices, String device)
            throws Exception {
        if (devices.containsKey(device)) {
            return (BluetoothDevice) devices.get(device);
        } else {
            throw new Exception("Can't find device " + device);
        }
    }

    public static BluetoothDevice getDevice(Collection<BluetoothDevice> devices, String deviceID)
            throws Exception {
        Log.d("Looking for " + deviceID);
        for (BluetoothDevice bd : devices) {
            // Log.d(bd.getAliasName() + " " + bd.getAddress());
            if (deviceMatch(bd, deviceID)) {
                // Log.d("Found match " + bd.getAliasName() + " " + bd.getAddress());
                return bd;
            }
        }
        throw new Exception("Can't find device " + deviceID);
    }

    public static boolean deviceExists(Collection<BluetoothDevice> devices, String deviceID) {
        for (BluetoothDevice bd : devices) {
            if (deviceMatch(bd, deviceID)) {
                // Log.d("Found match " + bd.getAliasName() + " " + bd.getAddress());
                return true;
            }
        }
        return false;
    }

    @Rpc(description = "Requests that the device be made connectable.")
    public void bluetoothMakeConnectable() {
        BluetoothNonpublicApi.setScanMode(mBluetoothAdapter,
                                          BluetoothAdapter.SCAN_MODE_CONNECTABLE);
    }

  @Rpc(description = "Returns active Bluetooth connections.")
  public Map<String, String> bluetoothActiveConnections() {
    Map<String, String> out = new HashMap<String, String>();
    for (Map.Entry<String, Bluetooth4Connection> entry : connections.entrySet()) {
      if (entry.getValue().isConnected()) {
        out.put(entry.getKey(), entry.getValue().getRemoteBluetoothAddress());
      }
    }

    return out;
  }

    private Bluetooth4Connection getConnection(String connID) throws IOException {
        Bluetooth4Connection conn = null;
    if (connID.trim().length() > 0) {
      conn = connections.get(connID);
    } else if (connections.size() == 1) {
            conn = (Bluetooth4Connection) connections.values().toArray()[0];
    }
    if (conn == null) {
      throw new IOException("Bluetooth not ready for this connID.");
    }
    return conn;
  }

  @Rpc(description = "Send bytes over the currently open Bluetooth connection.")
  public void bluetoothWriteBinary(
      @RpcParameter(name = "base64", description = "A base64 encoded String of the bytes to be sent.") String base64,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      conn.write(Base64Codec.decodeBase64(base64));
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read up to bufferSize bytes and return a chunked, base64 encoded string.")
  public String bluetoothReadBinary(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      return Base64Codec.encodeBase64String(conn.readBinary(bufferSize));
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

    private String addConnection(Bluetooth4Connection conn) {
    String uuid = UUID.randomUUID().toString();
    connections.put(uuid, conn);
    conn.setUUID(uuid);
    return uuid;
  }

  @Rpc(description = "Connect to a device over Bluetooth. Blocks until the connection is established or fails.", returns = "True if the connection was established successfully.")
  public String bluetoothConnect(
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
        return null;
      }
    }
    BluetoothDevice mDevice;
    BluetoothSocket mSocket;
        Bluetooth4Connection conn;
    mDevice = mBluetoothAdapter.getRemoteDevice(address);
    mSocket = mDevice.createRfcommSocketToServiceRecord(UUID.fromString(uuid));
    // Always cancel discovery because it will slow down a connection.
    mBluetoothAdapter.cancelDiscovery();
    mSocket.connect();
        conn = new Bluetooth4Connection(mSocket);
    return addConnection(conn);
  }

  @Rpc(description = "Listens for and accepts a Bluetooth connection. Blocks until the connection is established or fails.")
  public String bluetoothAccept(
      @RpcParameter(name = "uuid") @RpcDefault(DEFAULT_UUID) String uuid,
      @RpcParameter(name = "timeout", description = "How long to wait for a new connection, 0 is wait for ever") @RpcDefault("0") Integer timeout)
      throws IOException {
    BluetoothServerSocket mServerSocket;
    mServerSocket =
        mBluetoothAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, UUID.fromString(uuid));
    BluetoothSocket mSocket = mServerSocket.accept(timeout.intValue());
        Bluetooth4Connection conn = new Bluetooth4Connection(mSocket, mServerSocket);
    return addConnection(conn);
  }

  @Rpc(description = "Requests that the device be discoverable for Bluetooth connections.")
  public void bluetoothMakeDiscoverable(
            @RpcParameter(name = "duration",
                          description = "period of time, in seconds,"
                                      + "during which the device should be discoverable")
            @RpcDefault("300")
            Integer duration) {
        Log.d("Making discoverable for " + duration + " seconds.\n");
        BluetoothNonpublicApi.setScanMode(mBluetoothAdapter,
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE, duration);
    if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
      Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
      // Use startActivityForResult to make this a synchronous call.
      mAndroidFacade.startActivityForResult(discoverableIntent);
    }
  }

  @Rpc(description = "Sends ASCII characters over the currently open Bluetooth connection.")
  public void bluetoothWrite(@RpcParameter(name = "ascii") String ascii,
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      conn.write(ascii);
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Returns True if the next read is guaranteed not to block.")
  public Boolean bluetoothReadReady(
      @RpcParameter(name = "connID", description = "Connection id") @RpcDefault("") @RpcOptional String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      return conn.readReady();
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read up to bufferSize ASCII characters.")
  public String bluetoothRead(
      @RpcParameter(name = "bufferSize") @RpcDefault("4096") Integer bufferSize,
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("") String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      return conn.read(bufferSize);
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

  @Rpc(description = "Read the next line.")
  public String bluetoothReadLine(
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("") String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    try {
      return conn.readLine();
    } catch (IOException e) {
      connections.remove(conn.getUUID());
      throw e;
    }
  }

    @Rpc(description = "Requests that the device be not discoverable.")
    public void bluetoothMakeUndiscoverable() {
        Log.d("Making undiscoverable\n");
        BluetoothNonpublicApi.setScanMode(mBluetoothAdapter, BluetoothAdapter.SCAN_MODE_NONE);
    }

  @Rpc(description = "Queries a remote device for it's name or null if it can't be resolved")
  public String bluetoothGetRemoteDeviceName(
            @RpcParameter(name = "address", description = "Bluetooth Address For Target Device")
            String address) {
    try {
      BluetoothDevice mDevice;
      mDevice = mBluetoothAdapter.getRemoteDevice(address);
      return mDevice.getName();
    } catch (Exception e) {
      return null;
    }
  }

    @Rpc(description = "Get local Bluetooth device name")
  public String bluetoothGetLocalName() {
    return mBluetoothAdapter.getName();
  }

    @Rpc(description = "Sets the Bluetooth visible device name", returns = "true on success")
  public boolean bluetoothSetLocalName(
        @RpcParameter(name = "name", description = "New local name")
        String name) {
    return mBluetoothAdapter.setName(name);
  }

  @Rpc(description = "Gets the scan mode for the local dongle.\r\n" + "Return values:\r\n"
            + "\t-1 when Bluetooth is disabled.\r\n"
            + "\t0 if non discoverable and non connectable.\r\n"
      + "\r1 connectable non discoverable." + "\r3 connectable and discoverable.")
  public int bluetoothGetScanMode() {
    if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_OFF
        || mBluetoothAdapter.getState() == BluetoothAdapter.STATE_TURNING_OFF) {
      return -1;
    }
    switch (mBluetoothAdapter.getScanMode()) {
    case BluetoothAdapter.SCAN_MODE_NONE:
      return 0;
    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
      return 1;
    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
      return 3;
    default:
      return mBluetoothAdapter.getScanMode() - 20;
    }
  }

  @Rpc(description = "Returns the name of the connected device.")
  public String bluetoothGetConnectedDeviceName(
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("") String connID)
      throws IOException {
        Bluetooth4Connection conn = getConnection(connID);
    return conn.getConnectedDeviceName();
  }

  @Rpc(description = "Checks Bluetooth state.", returns = "True if Bluetooth is enabled.")
  public Boolean checkBluetoothState() {
    return mBluetoothAdapter.isEnabled();
  }

    @Rpc(description = "Factory reset bluetooth settings.", returns = "True if successful.")
    public boolean bluetoothFactoryReset() {
        Log.e("factoryReset won't work in no-system app.");
        return false;
        // return mBluetoothAdapter.factoryReset();
    }

  @Rpc(description = "Toggle Bluetooth on and off.", returns = "True if Bluetooth is enabled.")
  public Boolean toggleBluetoothState(
      @RpcParameter(name = "enabled") @RpcOptional Boolean enabled,
      @RpcParameter(name = "prompt",
                          description = "Prompt the user to confirm changing the Bluetooth state.")
            @RpcDefault("true")
            Boolean prompt) {
        mService.registerReceiver(mStateReceiver,
                                  new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
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
      shutdown();
      mBluetoothAdapter.disable();
    }
    return enabled;
  }

  @Rpc(description = "Stops Bluetooth connection.")
  public void bluetoothStop(
      @RpcParameter(name = "connID", description = "Connection id") @RpcOptional @RpcDefault("") String connID) {
        Bluetooth4Connection conn;
    try {
      conn = getConnection(connID);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }
    if (conn == null) {
      return;
    }

    conn.stop();
    connections.remove(conn.getUUID());
  }

  @Rpc(description = "Returns the hardware address of the local Bluetooth adapter. ")
  public String bluetoothGetLocalAddress() {
    return mBluetoothAdapter.getAddress();
  }


    @Rpc(description = "Start the remote device discovery process. ",
         returns = "true on success, false on error")
  public Boolean bluetoothDiscoveryStart() {
        DiscoveredDevices.clear();
        mService.registerReceiver(mDiscoveryReceiver, discoveryFilter);
    return mBluetoothAdapter.startDiscovery();
  }

    @Rpc(description = "Cancel the current device discovery process.",
         returns = "true on success, false on error")
  public Boolean bluetoothDiscoveryCancel() {
        try {
            mService.unregisterReceiver(mDiscoveryReceiver);
        } catch (IllegalArgumentException e) {
            Log.d("IllegalArgumentExeption found when trying to unregister reciever");
        }
    return mBluetoothAdapter.cancelDiscovery();
  }

    @Rpc(description = "If the local Bluetooth adapter is currently"
                     + "in the device discovery process.")
  public Boolean bluetoothIsDiscovering() {
    return mBluetoothAdapter.isDiscovering();
  }

    @Rpc(description = "Get all the discovered bluetooth devices.")
    public Collection<BluetoothDevice> bluetoothGetDiscoveredDevices() {
        while (bluetoothIsDiscovering())
            ;
        return DiscoveredDevices.values();
    }

    @Rpc(description = "Enable or disable the Bluetooth HCI snoop log")
    public boolean bluetoothConfigHciSnoopLog(
            @RpcParameter(name = "value", description = "enable or disable log")
            Boolean value
            ) {
        Log.e("configHciSnoopLog won't work in no-system app.");
        return false;
        // TODO: try to implement.
        // return mBluetoothAdapter.configHciSnoopLog(value);
    }

    @Rpc(description = "Get Bluetooth controller activity energy info.")
    public String bluetoothGetControllerActivityEnergyInfo(
        @RpcParameter(name = "value")
        Integer value
            ) {
        return "Disabled in user app SL4A";
        /* TODO: try to implement.
        BluetoothActivityEnergyInfo energyInfo = mBluetoothAdapter
            .getControllerActivityEnergyInfo(value);
        while (energyInfo == null) {
          energyInfo = mBluetoothAdapter.getControllerActivityEnergyInfo(value);
        }
        return energyInfo.toString();
         */
    }

    @Rpc(description = "Return true if hardware has entries" +
            "available for matching beacons.")
    public boolean bluetoothIsHardwareTrackingFiltersAvailable() {
        Log.e("isHardwareTrackingFiltersAvailable won't in no-system app.");
        return false;
        // TODO: try to implement.
        // return mBluetoothAdapter.isHardwareTrackingFiltersAvailable();
    }

    @Rpc(description = "Gets the current state of LE.")
    public int bluetoothGetLeState() {
        return BluetoothNonpublicApi.getLeState(mBluetoothAdapter);
    }

    @Rpc(description = "Enables BLE functionalities.")
    public boolean bluetoothEnableBLE() {
        mService.registerReceiver(mBleStateReceiver,
            new IntentFilter(BluetoothNonpublicApi.ACTION_BLE_STATE_CHANGED));
        Log.e("enableBLE won't work in no-system app.");
        return false;
        // return mBluetoothAdapter.enableBLE();
    }

    @Rpc(description = "Disables BLE functionalities.")
    public boolean bluetoothDisableBLE() {
        mService.registerReceiver(mBleStateReceiver,
            new IntentFilter(BluetoothNonpublicApi.ACTION_BLE_STATE_CHANGED));
        Log.e("disableBLE won't work in no-system app.");
        return false;
        // return mBluetoothAdapter.disableBLE();
    }

    @Rpc(description = "Listen for a Bluetooth LE State Change.")
    public boolean bluetoothListenForBleStateChange() {
        mService.registerReceiver(mBleStateReceiver,
            new IntentFilter(BluetoothNonpublicApi.ACTION_BLE_STATE_CHANGED));
        return true;
    }

    @Rpc(description = "Stop Listening for a Bluetooth LE State Change.")
    public boolean bluetoothStopListeningForBleStateChange() {
        mService.unregisterReceiver(mBleStateReceiver);
        return true;
    }

    @Rpc(description = "Listen for Bluetooth State Changes.")
    public boolean bluetoothStartListeningForAdapterStateChange() {
        synchronized (mReceiverLock) {
            if (mMultiStateReceiver != null) {
                Log.e("Persistent Bluetooth Receiver State Change Listener Already Active");
                return false;
            }
            mMultiStateReceiver = new BluetoothStateReceiver(true);
            mService.registerReceiver(mMultiStateReceiver,
                    new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        }
        return true;
    }

    @Rpc(description = "Stop Listening for Bluetooth State Changes.")
    public boolean bluetoothStopListeningForAdapterStateChange() {
        synchronized (mReceiverLock) {
            if (mMultiStateReceiver == null) {
                Log.d("No Persistent Bluetooth Receiever State Change Listener Found to Stop");
                return false;
            }
            mService.unregisterReceiver(mMultiStateReceiver);
            mMultiStateReceiver = null;
        }
        return true;
    }

  @Override
  public void shutdown() {
    for (Map.Entry<String, Bluetooth4Connection> entry : connections.entrySet()) {
      entry.getValue().stop();
    }
        if (mMultiStateReceiver != null ) bluetoothStopListeningForAdapterStateChange();
    connections.clear();
  }
}

class Bluetooth4Connection {
  private BluetoothSocket mSocket;
  private BluetoothDevice mDevice;
  private OutputStream mOutputStream;
  private InputStream mInputStream;
  private BufferedReader mReader;
  private BluetoothServerSocket mServerSocket;
  private String UUID;

    public Bluetooth4Connection(BluetoothSocket mSocket) throws IOException {
    this(mSocket, null);
  }

    public Bluetooth4Connection(BluetoothSocket mSocket, BluetoothServerSocket mServerSocket)
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

  public void stop() {
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
}
