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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Bundle;

import com.googlecode.android_scripting.ConvertUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class GattServerFacade extends RpcReceiver {
  private final EventFacade mEventFacade;
  private BluetoothAdapter mBluetoothAdapter;
  private BluetoothManager mBluetoothManager;
  private final Service mService;
  private final Context mContext;
  private final HashMap<Integer, BluetoothGattCharacteristic> mCharacteristicList;
  private final HashMap<Integer, BluetoothGattDescriptor> mDescriptorList;
  private final HashMap<Integer, BluetoothGattServer> mBluetoothGattServerList;
  private final HashMap<Integer, myBluetoothGattServerCallback> mBluetoothGattServerCallbackList;
  private final HashMap<Integer, BluetoothGattService> mGattServiceList;
  private final HashMap<Integer, List<BluetoothGattService>> mBluetoothGattDiscoveredServicesList;
  private final HashMap<Integer, List<BluetoothDevice>> mGattServerDiscoveredDevicesList;
  private static int CharacteristicCount;
  private static int DescriptorCount;
  private static int GattServerCallbackCount;
  private static int GattServerCount;
  private static int GattServiceCount;

  public GattServerFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mContext = mService.getApplicationContext();
    mBluetoothAdapter = MainThread.run(mService, new Callable<BluetoothAdapter>() {
      @Override
      public BluetoothAdapter call() throws Exception {
        return BluetoothAdapter.getDefaultAdapter();
      }
    });
    mBluetoothManager = (BluetoothManager) mContext.getSystemService(Service.BLUETOOTH_SERVICE);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mCharacteristicList = new HashMap<Integer, BluetoothGattCharacteristic>();
    mDescriptorList = new HashMap<Integer, BluetoothGattDescriptor>();
    mBluetoothGattServerList = new HashMap<Integer, BluetoothGattServer>();
    mBluetoothGattServerCallbackList = new HashMap<Integer, myBluetoothGattServerCallback>();
    mGattServiceList = new HashMap<Integer, BluetoothGattService>();
    mBluetoothGattDiscoveredServicesList = new HashMap<Integer, List<BluetoothGattService>>();
    mGattServerDiscoveredDevicesList = new HashMap<Integer, List<BluetoothDevice>>();
  }

  /**
   * Open a new Gatt server.
   *
   * @param index the bluetooth gatt server callback to open on
   * @return the index of the newly opened gatt server
   * @throws Exception
   */
  @Rpc(description = "Open new gatt server")
  public int gattServerOpenGattServer(@RpcParameter(name = "index") Integer index)
      throws Exception {
    if (mBluetoothGattServerCallbackList.get(index) != null) {
      BluetoothGattServer mGattServer =
          mBluetoothManager.openGattServer(mContext, mBluetoothGattServerCallbackList.get(index));
      GattServerCount += 1;
      int in = GattServerCount;
      mBluetoothGattServerList.put(in, mGattServer);
      return in;
    } else {
      throw new Exception("Invalid index input:" + Integer.toString(index));
    }
  }

  /**
   * Add a service to a bluetooth gatt server
   *
   * @param index the bluetooth gatt server to add a service to
   * @param serviceIndex the service to add to the bluetooth gatt server
   * @throws Exception
   */
  @Rpc(description = "Add service to bluetooth gatt server")
  public void gattServerAddService(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "serviceIndex") Integer serviceIndex) throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
      if (mGattServiceList.get(serviceIndex) != null) {
        mBluetoothGattServerList.get(index).addService(mGattServiceList.get(serviceIndex));
      } else {
        throw new Exception("Invalid serviceIndex input:" + Integer.toString(serviceIndex));
      }
    } else {
      throw new Exception("Invalid index input:" + Integer.toString(index));
    }
  }

  /**
   * Add a service to a bluetooth gatt server
   *
   * @param index the bluetooth gatt server to add a service to
   * @param serviceIndex the service to add to the bluetooth gatt server
   * @throws Exception
   */
  @Rpc(description = "Clear services from bluetooth gatt server")
  public void gattServerClearServices(@RpcParameter(name = "index") Integer index) throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
        mBluetoothGattServerList.get(index).clearServices();
    } else {
      throw new Exception("Invalid index input:" + Integer.toString(index));
    }
  }

  /**
   * Get connected devices of the gatt server
   *
   * @param gattServerIndex the gatt server index
   * @throws Exception
   */
  @Rpc(description = "Return a list of connected gatt devices.")
  public List<BluetoothDevice> gattServerGetConnectedDevices(
      @RpcParameter(name = "gattServerIndex") Integer gattServerIndex) throws Exception {
    if (mBluetoothGattServerList.get(gattServerIndex) == null) {
      throw new Exception("Invalid gattServerIndex: " + Integer.toString(gattServerIndex));
    }
    List<BluetoothDevice> connectedDevices =
        mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
    mGattServerDiscoveredDevicesList.put(gattServerIndex, connectedDevices);
    return connectedDevices;
  }

  /**
   * Get connected devices of the gatt server
   *
   * @param gattServerIndex the gatt server index
   * @param bluetoothDeviceIndex the remotely connected bluetooth device
   * @param requestId the ID of the request that was received with the callback
   * @param status the status of the request to be sent to the remote devices
   * @param offset value offset for partial read/write response
   * @param value the value of the attribute that was read/written
   * @throws Exception
   */
  @Rpc(description = "Send a response after a write.")
  public void gattServerSendResponse(
      @RpcParameter(name = "gattServerIndex") Integer gattServerIndex,
      @RpcParameter(name = "bluetoothDeviceIndex") Integer bluetoothDeviceIndex,
      @RpcParameter(name = "requestId") Integer requestId,
      @RpcParameter(name = "status") Integer status, @RpcParameter(name = "offset") Integer offset,
      @RpcParameter(name = "value") byte[] value) throws Exception {

    BluetoothGattServer gattServer = mBluetoothGattServerList.get(gattServerIndex);
    if (gattServer == null)
      throw new Exception("Invalid gattServerIndex: " + Integer.toString(gattServerIndex));
    List<BluetoothDevice> connectedDevices = mGattServerDiscoveredDevicesList.get(gattServerIndex);
    if (connectedDevices == null)
      throw new Exception(
          "Connected device list empty for gattServerIndex:" + Integer.toString(gattServerIndex));
    BluetoothDevice bluetoothDevice = connectedDevices.get(bluetoothDeviceIndex);
    if (bluetoothDevice == null)
      throw new Exception(
          "Invalid bluetoothDeviceIndex: " + Integer.toString(bluetoothDeviceIndex));
    gattServer.sendResponse(bluetoothDevice, requestId, status, offset, value);
  }

  /**
   * Notify that characteristic was changed
   *
   * @param gattServerIndex the gatt server index
   * @param bluetoothDeviceIndex the remotely connected bluetooth device
   * @param characteristicIndex characteristic index
   * @param confirm shall we expect confirmation
   * @throws Exception
   */
  @Rpc(description = "Notify that characteristic was changed.")
  public void gattServerNotifyCharacteristicChanged(
      @RpcParameter(name = "gattServerIndex") Integer gattServerIndex,
      @RpcParameter(name = "bluetoothDeviceIndex") Integer bluetoothDeviceIndex,
      @RpcParameter(name = "characteristicIndex") Integer characteristicIndex,
      @RpcParameter(name = "confirm") Boolean confirm) throws Exception {

    BluetoothGattServer gattServer = mBluetoothGattServerList.get(gattServerIndex);
    if (gattServer == null)
      throw new Exception("Invalid gattServerIndex: " + Integer.toString(gattServerIndex));
    List<BluetoothDevice> connectedDevices = mGattServerDiscoveredDevicesList.get(gattServerIndex);
    if (connectedDevices == null)
      throw new Exception(
          "Connected device list empty for gattServerIndex:" + Integer.toString(gattServerIndex));
    BluetoothDevice bluetoothDevice = connectedDevices.get(bluetoothDeviceIndex);
    if (bluetoothDevice == null)
      throw new Exception(
          "Invalid bluetoothDeviceIndex: " + Integer.toString(bluetoothDeviceIndex));

    BluetoothGattCharacteristic bluetoothCharacteristic = mCharacteristicList.get(characteristicIndex);
    if (bluetoothCharacteristic == null)
      throw new Exception(
          "Invalid characteristicIndex: " + Integer.toString(characteristicIndex));

    gattServer.notifyCharacteristicChanged(bluetoothDevice, bluetoothCharacteristic, confirm);
  }

  /**
   * Create a new bluetooth gatt service
   *
   * @param uuid the UUID that characterises the service
   * @param serviceType the service type
   * @return The index of the new bluetooth gatt service
   */
  @Rpc(description = "Create new bluetooth gatt service")
  public int gattServerCreateService(@RpcParameter(name = "uuid") String uuid,
      @RpcParameter(name = "serviceType") Integer serviceType) {
    GattServiceCount += 1;
    int index = GattServiceCount;
    mGattServiceList.put(index, new BluetoothGattService(UUID.fromString(uuid), serviceType));
    return index;
  }

  /**
   * Add a characteristic to a bluetooth gatt service
   *
   * @param index the bluetooth gatt service index
   * @param serviceUuid the service Uuid to get
   * @param characteristicIndex the character index to use
   * @throws Exception
   */
  @Rpc(description = "Add a characteristic to a bluetooth gatt service")
  public void gattServiceAddCharacteristic(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "serviceUuid") String serviceUuid,
      @RpcParameter(name = "characteristicIndex") Integer characteristicIndex) throws Exception {
    if (mBluetoothGattServerList.get(index) != null
        && mBluetoothGattServerList.get(index).getService(UUID.fromString(serviceUuid)) != null
        && mCharacteristicList.get(characteristicIndex) != null) {
      mBluetoothGattServerList.get(index).getService(UUID.fromString(serviceUuid))
          .addCharacteristic(mCharacteristicList.get(characteristicIndex));
    } else {
      if (mBluetoothGattServerList.get(index) == null) {
        throw new Exception("Invalid index input:" + index);
      } else if (mCharacteristicList.get(characteristicIndex) == null) {
        throw new Exception("Invalid characteristicIndex input:" + characteristicIndex);
      } else {
        throw new Exception("Invalid serviceUuid input:" + serviceUuid);
      }
    }
  }

  /**
   * Add a characteristic to a bluetooth gatt service
   *
   * @param index the bluetooth gatt service to add a characteristic to
   * @param characteristicIndex the characteristic to add
   * @throws Exception
   */
  @Rpc(description = "Add a characteristic to a bluetooth gatt service")
  public void gattServerAddCharacteristicToService(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "characteristicIndex") Integer characteristicIndex

  ) throws Exception {
    if (mGattServiceList.get(index) != null) {
      if (mCharacteristicList.get(characteristicIndex) != null) {
        mGattServiceList.get(index).addCharacteristic(mCharacteristicList.get(characteristicIndex));
      } else {
        throw new Exception("Invalid index input:" + index);
      }
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Close a bluetooth gatt
   *
   * @param index the bluetooth gatt index to close
   * @throws Exception
   */
  @Rpc(description = "Close a bluetooth gatt")
  public void gattServerClose(@RpcParameter(name = "index") Integer index) throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
      mBluetoothGattServerList.get(index).close();
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Get a list of Bluetooth Devices connnected to the bluetooth gatt
   *
   * @param index the bluetooth gatt index
   * @return List of BluetoothDevice Objects
   * @throws Exception
   */
  @Rpc(description = "Get a list of Bluetooth Devices connnected to the bluetooth gatt")
  public List<BluetoothDevice> gattGetConnectedDevices(@RpcParameter(name = "index") Integer index)
      throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
      return mBluetoothGattServerList.get(index).getConnectedDevices();
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Get the service from an input UUID
   *
   * @param index the bluetooth gatt index
   * @return BluetoothGattService related to the bluetooth gatt
   * @throws Exception
   */
  @Rpc(description = "Get the service from an input UUID")
  public ArrayList<String> gattGetServiceUuidList(@RpcParameter(name = "index") Integer index)
      throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
      ArrayList<String> serviceUuidList = new ArrayList<String>();
      for (BluetoothGattService service : mBluetoothGattServerList.get(index).getServices()) {
        serviceUuidList.add(service.getUuid().toString());
      }
      return serviceUuidList;
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Get the service from an input UUID
   *
   * @param index the bluetooth gatt index
   * @param uuid the String uuid that matches the service
   * @return BluetoothGattService related to the bluetooth gatt
   * @throws Exception
   */
  @Rpc(description = "Get the service from an input UUID")
  public BluetoothGattService gattGetService(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "uuid") String uuid) throws Exception {
    if (mBluetoothGattServerList.get(index) != null) {
      return mBluetoothGattServerList.get(index).getService(UUID.fromString(uuid));
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Add a descriptor to a bluetooth gatt characteristic
   *
   * @param index the bluetooth gatt characteristic to add a descriptor to
   * @param descriptorIndex the descritor index to add to the characteristic
   * @throws Exception
   */
  @Rpc(description = "add descriptor to blutooth gatt characteristic")
  public void gattServerCharacteristicAddDescriptor(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "descriptorIndex") Integer descriptorIndex) throws Exception {
    if (mCharacteristicList.get(index) != null) {
      if (mDescriptorList.get(descriptorIndex) != null) {
        mCharacteristicList.get(index).addDescriptor(mDescriptorList.get(descriptorIndex));
      } else {
        throw new Exception("Invalid descriptorIndex input:" + descriptorIndex);
      }
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Create a new Characteristic object
   *
   * @param characteristicUuid uuid The UUID for this characteristic
   * @param property Properties of this characteristic
   * @param permission permissions Permissions for this characteristic
   * @return
   */
  @Rpc(description = "Create a new Characteristic object")
  public int gattServerCreateBluetoothGattCharacteristic(
      @RpcParameter(name = "characteristicUuid") String characteristicUuid,
      @RpcParameter(name = "property") Integer property,
      @RpcParameter(name = "permission") Integer permission) {
    CharacteristicCount += 1;
    int index = CharacteristicCount;
    BluetoothGattCharacteristic characteristic =
        new BluetoothGattCharacteristic(UUID.fromString(characteristicUuid), property, permission);
    mCharacteristicList.put(index, characteristic);
    return index;
  }

  /**
   * Set value to a bluetooth gatt characteristic
   *
   * @param index the bluetooth gatt characteristic
   * @param value value
   * @throws Exception
   */
  @Rpc(description = "add descriptor to blutooth gatt characteristic")
  public void gattServerCharacteristicSetValue(@RpcParameter(name = "index") Integer index,
      @RpcParameter(name = "value") byte[] value) throws Exception {
    if (mCharacteristicList.get(index) != null) {
      mCharacteristicList.get(index).setValue(value);
    } else {
      throw new Exception("Invalid index input:" + index);
    }
  }

  /**
   * Create a new GattCallback object
   *
   * @return the index of the callback object
   */
  @Rpc(description = "Create a new GattCallback object")
  public Integer gattServerCreateGattServerCallback() {
    GattServerCallbackCount += 1;
    int index = GattServerCallbackCount;
    mBluetoothGattServerCallbackList.put(index, new myBluetoothGattServerCallback(index));
    return index;
  }

  /**
   * Create a new Descriptor object
   *
   * @param descriptorUuid the UUID for this descriptor
   * @param permissions Permissions for this descriptor
   * @return the index of the Descriptor object
   */
  @Rpc(description = "Create a new Descriptor object")
  public int gattServerCreateBluetoothGattDescriptor(
      @RpcParameter(name = "descriptorUuid") String descriptorUuid,
      @RpcParameter(name = "permissions") Integer permissions) {
    DescriptorCount += 1;
    int index = DescriptorCount;
    BluetoothGattDescriptor descriptor =
        new BluetoothGattDescriptor(UUID.fromString(descriptorUuid), permissions);
    mDescriptorList.put(index, descriptor);
    return index;
  }

  private class myBluetoothGattServerCallback extends BluetoothGattServerCallback {
    private final Bundle mResults;
    private final int index;
    private final String mEventType;

    public myBluetoothGattServerCallback(int idx) {
      mResults = new Bundle();
      mEventType = "GattServer";
      index = idx;
    }

    @Override
    public void onServiceAdded(int status, BluetoothGattService service) {
      Log.d("gatt_server change onServiceAdded " + mEventType + " " + index);
      mResults.putString("serviceUuid", service.getUuid().toString());
      mResults.putInt("instanceId", service.getInstanceId());
      mEventFacade.postEvent(mEventType + index + "onServiceAdded", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
        BluetoothGattCharacteristic characteristic) {
      Log.d("gatt_server change onCharacteristicReadRequest " + mEventType + " " + index);
      mResults.putInt("requestId", requestId);
      mResults.putInt("offset", offset);
      mResults.putInt("instanceId", characteristic.getInstanceId());
      mResults.putInt("properties", characteristic.getProperties());
      mResults.putString("uuid", characteristic.getUuid().toString());
      mResults.putInt("permissions", characteristic.getPermissions());
      mEventFacade.postEvent(mEventType + index + "onCharacteristicReadRequest", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
        BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded,
        int offset, byte[] value) {
      Log.d("gatt_server change onCharacteristicWriteRequest " + mEventType + " " + index);
      mResults.putInt("requestId", requestId);
      mResults.putInt("offset", offset);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putBoolean("preparedWrite", preparedWrite);
      mResults.putBoolean("responseNeeded", responseNeeded);
      mResults.putByteArray("value", value);
      mResults.putInt("instanceId", characteristic.getInstanceId());
      mResults.putInt("properties", characteristic.getProperties());
      mResults.putString("uuid", characteristic.getUuid().toString());
      mResults.putInt("permissions", characteristic.getPermissions());
      mEventFacade.postEvent(mEventType + index + "onCharacteristicWriteRequest", mResults.clone());
      mResults.clear();

    }

    @Override
    public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
        BluetoothGattDescriptor descriptor) {
      Log.d("gatt_server change onDescriptorReadRequest " + mEventType + " " + index);
      mResults.putInt("requestId", requestId);
      mResults.putInt("offset", offset);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putInt("instanceId", descriptor.getInstanceId());
      mResults.putInt("permissions", descriptor.getPermissions());
      mResults.putString("uuid", descriptor.getUuid().toString());
      mEventFacade.postEvent(mEventType + index + "onDescriptorReadRequest", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
        BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded,
        int offset, byte[] value) {
      Log.d("gatt_server change onDescriptorWriteRequest " + mEventType + " " + index);
      mResults.putInt("requestId", requestId);
      mResults.putInt("offset", offset);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putBoolean("preparedWrite", preparedWrite);
      mResults.putBoolean("responseNeeded", responseNeeded);
      mResults.putByteArray("value", value);
      mResults.putInt("instanceId", descriptor.getInstanceId());
      mResults.putInt("permissions", descriptor.getPermissions());
      mResults.putString("uuid", descriptor.getUuid().toString());
      mEventFacade.postEvent(mEventType + index + "onDescriptorWriteRequest", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute) {
      Log.d("gatt_server change onExecuteWrite " + mEventType + " " + index);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putInt("requestId", requestId);
      mResults.putBoolean("execute", execute);
      mEventFacade.postEvent(mEventType + index + "onExecuteWrite", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onNotificationSent(BluetoothDevice device, int status) {
      Log.d("gatt_server change onNotificationSent " + mEventType + " " + index);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putInt("status", status);
      mEventFacade.postEvent(mEventType + index + "onNotificationSent", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
      Log.d("gatt_server change onConnectionStateChange " + mEventType + " " + index);
      if (newState == BluetoothProfile.STATE_CONNECTED) {
        Log.d("State Connected to mac address " + device.getAddress() + " status " + status);
      } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        Log.d("State Disconnected from mac address " + device.getAddress() + " status " + status);
      }
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putInt("status", status);
      mResults.putInt("newState", newState);
      mEventFacade.postEvent(mEventType + index + "onConnectionStateChange", mResults.clone());
      mResults.clear();
    }

    @Override
    public void onMtuChanged(BluetoothDevice device, int mtu) {
      Log.d("gatt_server change onMtuChanged " + mEventType + " " + index);
      mResults.putParcelable("BluetoothDevice", device);
      mResults.putInt("mtu", mtu);
      mEventFacade.postEvent(mEventType + index + "onMtuChanged", mResults.clone());
      mResults.clear();
    }
  }

  @Override
  public void shutdown() {
    if (!mBluetoothGattServerList.isEmpty()) {
      if (mBluetoothGattServerList.values() != null) {
        for (BluetoothGattServer mBluetoothGattServer : mBluetoothGattServerList.values()) {
          mBluetoothGattServer.close();
        }
      }
    }
  }
}
