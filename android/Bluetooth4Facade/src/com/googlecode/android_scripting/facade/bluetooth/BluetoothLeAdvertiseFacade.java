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

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseData.Builder;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * BluetoothLe Advertise functions.
 */

public class BluetoothLeAdvertiseFacade extends RpcReceiver {

    private final EventFacade mEventFacade;
    private BluetoothAdapter mBluetoothAdapter;
    private static int BleAdvertiseCallbackCount;
    private static int BleAdvertiseSettingsCount;
    private static int BleAdvertiseDataCount;
    private final HashMap<Integer, myAdvertiseCallback> mAdvertiseCallbackList;
    private final BluetoothLeAdvertiser mAdvertise;
    private final Service mService;
    private Builder mAdvertiseDataBuilder;
    private android.bluetooth.le.AdvertiseSettings.Builder mAdvertiseSettingsBuilder;
    private final HashMap<Integer, AdvertiseData> mAdvertiseDataList;
    private final HashMap<Integer, AdvertiseSettings> mAdvertiseSettingsList;

    public BluetoothLeAdvertiseFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mBluetoothAdapter = MainThread.run(mService,
                new Callable<BluetoothAdapter>() {
                    @Override
                    public BluetoothAdapter call() throws Exception {
                        return BluetoothAdapter.getDefaultAdapter();
                    }
                });
        mEventFacade = manager.getReceiver(EventFacade.class);
        mAdvertiseCallbackList = new HashMap<Integer, myAdvertiseCallback>();
        mAdvertise = mBluetoothAdapter.getBluetoothLeAdvertiser();
        mAdvertiseDataList = new HashMap<Integer, AdvertiseData>();
        mAdvertiseSettingsList = new HashMap<Integer, AdvertiseSettings>();
        mAdvertiseDataBuilder = new Builder();
        mAdvertiseSettingsBuilder = new android.bluetooth.le.AdvertiseSettings.Builder();
    }

    /**
     * Constructs a myAdvertiseCallback obj and returns its index
     *
     * @return myAdvertiseCallback.index
     */
    @Rpc(description = "Generate a new myAdvertisement Object")
    public Integer bleGenBleAdvertiseCallback() {
        BleAdvertiseCallbackCount += 1;
        int index = BleAdvertiseCallbackCount;
        myAdvertiseCallback mCallback = new myAdvertiseCallback(index);
        mAdvertiseCallbackList.put(mCallback.index,
                mCallback);
        return mCallback.index;
    }

    /**
     * Constructs a AdvertiseData obj and returns its index
     *
     * @return index
     */
    @Rpc(description = "Constructs a new Builder obj for AdvertiseData and returns its index")
    public Integer bleBuildAdvertiseData() {
        BleAdvertiseDataCount += 1;
        int index = BleAdvertiseDataCount;
        mAdvertiseDataList.put(index,
                mAdvertiseDataBuilder.build());
        mAdvertiseDataBuilder = new Builder();
        return index;
    }

    /**
     * Constructs a Advertise Settings obj and returns its index
     *
     * @return index
     */
    @Rpc(description = "Constructs a new Builder obj for AdvertiseData and returns its index")
    public Integer bleBuildAdvertiseSettings() {
        BleAdvertiseSettingsCount += 1;
        int index = BleAdvertiseSettingsCount;
        mAdvertiseSettingsList.put(index,
                mAdvertiseSettingsBuilder.build());
        mAdvertiseSettingsBuilder = new android.bluetooth.le.AdvertiseSettings.Builder();
        return index;
    }

    /**
     * Stops a ble advertisement
     *
     * @param index the id of the advertisement to stop advertising on
     * @throws Exception
     */
    @Rpc(description = "Stops an ongoing ble advertisement")
    public void bleStopBleAdvertising(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseCallbackList.get(index) != null) {
            Log.d("bluetooth_le mAdvertise " + index);
            mAdvertise.stopAdvertising(mAdvertiseCallbackList
                    .get(index));
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Starts ble advertising
     *
     * @param callbackIndex The advertisementCallback index
     * @param dataIndex the AdvertiseData index
     * @param settingsIndex the advertisementsettings index
     * @throws Exception
     */
    @Rpc(description = "Starts ble advertisement")
    public void bleStartBleAdvertising(
            @RpcParameter(name = "callbackIndex")
            Integer callbackIndex,
            @RpcParameter(name = "dataIndex")
            Integer dataIndex,
            @RpcParameter(name = "settingsIndex")
            Integer settingsIndex
            ) throws Exception {
        AdvertiseData mData = new AdvertiseData.Builder().build();
        AdvertiseSettings mSettings = new AdvertiseSettings.Builder().build();
        if (mAdvertiseDataList.get(dataIndex) != null) {
            mData = mAdvertiseDataList.get(dataIndex);
        } else {
            throw new Exception("Invalid dataIndex input:" + Integer.toString(dataIndex));
        }
        if (mAdvertiseSettingsList.get(settingsIndex) != null) {
            mSettings = mAdvertiseSettingsList.get(settingsIndex);
        } else {
            throw new Exception("Invalid settingsIndex input:" + Integer.toString(settingsIndex));
        }
        if (mAdvertiseCallbackList.get(callbackIndex) != null) {
            Log.d("bluetooth_le starting a background advertisement on callback index: "
                    + Integer.toString(callbackIndex));
            mAdvertise
                    .startAdvertising(mSettings, mData, mAdvertiseCallbackList.get(callbackIndex));
        } else {
            throw new Exception("Invalid callbackIndex input" + Integer.toString(callbackIndex));
        }
    }

    /**
     * Starts ble advertising with a scanResponse. ScanResponses are created in the same way
     * AdvertiseData is created since they share the same object type.
     *
     * @param callbackIndex The advertisementCallback index
     * @param dataIndex the AdvertiseData index
     * @param settingsIndex the advertisementsettings index
     * @param scanResponseIndex the scanResponse index
     * @throws Exception
     */
    @Rpc(description = "Starts ble advertisement")
    public void bleStartBleAdvertisingWithScanResponse(
            @RpcParameter(name = "callbackIndex")
            Integer callbackIndex,
            @RpcParameter(name = "dataIndex")
            Integer dataIndex,
            @RpcParameter(name = "settingsIndex")
            Integer settingsIndex,
            @RpcParameter(name = "scanResponseIndex")
            Integer scanResponseIndex
            ) throws Exception {
        AdvertiseData mData = new AdvertiseData.Builder().build();
        AdvertiseSettings mSettings = new AdvertiseSettings.Builder().build();
        AdvertiseData mScanResponse = new AdvertiseData.Builder().build();

        if (mAdvertiseDataList.get(dataIndex) != null) {
            mData = mAdvertiseDataList.get(dataIndex);
        } else {
            throw new Exception("Invalid dataIndex input:" + Integer.toString(dataIndex));
        }
        if (mAdvertiseSettingsList.get(settingsIndex) != null) {
            mSettings = mAdvertiseSettingsList.get(settingsIndex);
        } else {
            throw new Exception("Invalid settingsIndex input:" + Integer.toString(settingsIndex));
        }
        if (mAdvertiseDataList.get(scanResponseIndex) != null) {
            mScanResponse = mAdvertiseDataList.get(scanResponseIndex);
        } else {
            throw new Exception("Invalid scanResponseIndex input:"
                    + Integer.toString(settingsIndex));
        }
        if (mAdvertiseCallbackList.get(callbackIndex) != null) {
            Log.d("bluetooth_le starting a background advertise on callback index: "
                    + Integer.toString(callbackIndex));
            mAdvertise
                    .startAdvertising(mSettings, mData, mScanResponse,
                            mAdvertiseCallbackList.get(callbackIndex));
        } else {
            throw new Exception("Invalid callbackIndex input" + Integer.toString(callbackIndex));
        }
    }

    /**
     * Get ble advertisement settings mode
     *
     * @param index the advertise settings object to use
     * @return the mode of the advertise settings object
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement settings mode")
    public int bleGetAdvertiseSettingsMode(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseSettingsList.get(index) != null) {
            AdvertiseSettings mSettings = mAdvertiseSettingsList.get(index);
            return mSettings.getMode();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement settings tx power level
     *
     * @param index the advertise settings object to use
     * @return the tx power level of the advertise settings object
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement settings tx power level")
    public int bleGetAdvertiseSettingsTxPowerLevel(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseSettingsList.get(index) != null) {
            AdvertiseSettings mSettings = mAdvertiseSettingsList.get(index);
            return mSettings.getTxPowerLevel();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement settings isConnectable value
     *
     * @param index the advertise settings object to use
     * @return the boolean value whether the advertisement will indicate
     * connectable.
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement settings isConnectable value")
    public boolean bleGetAdvertiseSettingsIsConnectable(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseSettingsList.get(index) != null) {
            AdvertiseSettings mSettings = mAdvertiseSettingsList.get(index);
            return mSettings.isConnectable();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement data include tx power level
     *
     * @param index the advertise data object to use
     * @return True if include tx power level, false otherwise
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement data include tx power level")
    public Boolean bleGetAdvertiseDataIncludeTxPowerLevel(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseDataList.get(index) != null) {
            AdvertiseData mData = mAdvertiseDataList.get(index);
            return mData.getIncludeTxPowerLevel();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement data manufacturer specific data
     *
     * @param index the advertise data object to use
     * @param manufacturerId the id that corresponds to the manufacturer specific data.
     * @return the corresponding manufacturer specific data to the manufacturer id.
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement data manufacturer specific data")
    public byte[] bleGetAdvertiseDataManufacturerSpecificData(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "manufacturerId")
            Integer manufacturerId) throws Exception {
        if (mAdvertiseDataList.get(index) != null) {
            AdvertiseData mData = mAdvertiseDataList.get(index);
            if (mData.getManufacturerSpecificData() != null) {
                return mData.getManufacturerSpecificData().get(manufacturerId);
            } else {
                throw new Exception("Invalid manufacturerId input:" + Integer.toString(manufacturerId));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));

        }
    }

    /**
     * Get ble advertisement data include device name
     *
     * @param index the advertise data object to use
     * @return the advertisement data's include device name
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement include device name")
    public Boolean bleGetAdvertiseDataIncludeDeviceName(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseDataList.get(index) != null) {
            AdvertiseData mData = mAdvertiseDataList.get(index);
            return mData.getIncludeDeviceName();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement Service Data
     *
     * @param index the advertise data object to use
     * @param serviceUuid the uuid corresponding to the service data.
     * @return the advertisement data's service data
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement Service Data")
    public byte[] bleGetAdvertiseDataServiceData(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "serviceUuid")
            String serviceUuid) throws Exception {
        ParcelUuid uuidKey = ParcelUuid.fromString(serviceUuid);
        if (mAdvertiseDataList.get(index) != null) {
            AdvertiseData mData = mAdvertiseDataList.get(index);
            if (mData.getServiceData().containsKey(uuidKey)) {
                return mData.getServiceData().get(uuidKey);
            } else {
                throw new Exception("Invalid serviceUuid input:" + serviceUuid);
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ble advertisement Service Uuids
     *
     * @param index the advertise data object to use
     * @return the advertisement data's Service Uuids
     * @throws Exception
     */
    @Rpc(description = "Get ble advertisement Service Uuids")
    public List<ParcelUuid> bleGetAdvertiseDataServiceUuids(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mAdvertiseDataList.get(index) != null) {
            AdvertiseData mData = mAdvertiseDataList.get(index);
            return mData.getServiceUuids();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Set ble advertisement data service uuids
     *
     * @param uuidList
     * @throws Exception
     */
    @Rpc(description = "Set ble advertisement data service uuids")
    public void bleSetAdvertiseDataSetServiceUuids(
            @RpcParameter(name = "uuidList")
            String[] uuidList
            ) {
        for (String uuid : uuidList) {
            mAdvertiseDataBuilder.addServiceUuid(ParcelUuid.fromString(uuid));
        }
    }

    /**
     * Set ble advertise data service uuids
     *
     * @param serviceDataUuid
     * @param serviceData
     * @throws Exception
     */
    @Rpc(description = "Set ble advertise data service uuids")
    public void bleAddAdvertiseDataServiceData(
            @RpcParameter(name = "serviceDataUuid")
            String serviceDataUuid,
            @RpcParameter(name = "serviceData")
            byte[] serviceData
            ) {
        mAdvertiseDataBuilder.addServiceData(
                ParcelUuid.fromString(serviceDataUuid),
                serviceData);
    }

    /**
     * Set ble advertise data manufacturer id
     *
     * @param manufacturerId the manufacturer id to set
     * @param manufacturerSpecificData the manufacturer specific data to set
     * @throws Exception
     */
    @Rpc(description = "Set ble advertise data manufacturerId")
    public void bleAddAdvertiseDataManufacturerId(
            @RpcParameter(name = "manufacturerId")
            Integer manufacturerId,
            @RpcParameter(name = "manufacturerSpecificData")
            byte[] manufacturerSpecificData
            ) {
        mAdvertiseDataBuilder.addManufacturerData(manufacturerId,
                manufacturerSpecificData);
    }

    /**
     * Set ble advertise settings advertise mode
     *
     * @param advertiseMode
     * @throws Exception
     */
    @Rpc(description = "Set ble advertise settings advertise mode")
    public void bleSetAdvertiseSettingsAdvertiseMode(
            @RpcParameter(name = "advertiseMode")
            Integer advertiseMode
            ) {
        mAdvertiseSettingsBuilder.setAdvertiseMode(advertiseMode);
    }

    /**
     * Set ble advertise settings tx power level
     *
     * @param txPowerLevel the tx power level to set
     * @throws Exception
     */
    @Rpc(description = "Set ble advertise settings tx power level")
    public void bleSetAdvertiseSettingsTxPowerLevel(
            @RpcParameter(name = "txPowerLevel")
            Integer txPowerLevel
            ) {
        mAdvertiseSettingsBuilder.setTxPowerLevel(txPowerLevel);
    }

    /**
     * Set ble advertise settings the isConnectable value
     *
     * @param type the isConnectable value
     * @throws Exception
     */
    @Rpc(description = "Set ble advertise settings isConnectable value")
    public void bleSetAdvertiseSettingsIsConnectable(
            @RpcParameter(name = "value")
            Boolean value
            ) {
        mAdvertiseSettingsBuilder.setConnectable(value);
    }

    /**
     * Set ble advertisement data include tx power level
     *
     * @param includeTxPowerLevel boolean whether to include the tx power level or not in the
     *            advertisement
     */
    @Rpc(description = "Set ble advertisement data include tx power level")
    public void bleSetAdvertiseDataIncludeTxPowerLevel(
            @RpcParameter(name = "includeTxPowerLevel")
            Boolean includeTxPowerLevel
            ) {
        mAdvertiseDataBuilder.setIncludeTxPowerLevel(includeTxPowerLevel);
    }

    /**
     * Set ble advertisement settings set timeout
     *
     * @param timeoutSeconds Limit advertising to a given amount of time.
     */
    @Rpc(description = "Set ble advertisement data include tx power level")
    public void bleSetAdvertiseSettingsTimeout(
            @RpcParameter(name = "timeoutSeconds")
            Integer timeoutSeconds
            ) {
        mAdvertiseSettingsBuilder.setTimeout(timeoutSeconds);
    }

    /**
     * Set ble advertisement data include device name
     *
     * @param includeDeviceName boolean whether to include device name or not in the
     *            advertisement
     */
    @Rpc(description = "Set ble advertisement data include device name")
    public void bleSetAdvertiseDataIncludeDeviceName(
            @RpcParameter(name = "includeDeviceName")
            Boolean includeDeviceName
            ) {
        mAdvertiseDataBuilder.setIncludeDeviceName(includeDeviceName);
    }

    private class myAdvertiseCallback extends AdvertiseCallback {
        public Integer index;
        private final Bundle mResults;
        String mEventType;

        public myAdvertiseCallback(int idx) {
            index = idx;
            mEventType = "BleAdvertise";
            mResults = new Bundle();
        }

        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Log.d("bluetooth_le_advertisement onSuccess " + mEventType + " "
                    + index);
            mResults.putString("Type", "onSuccess");
            mResults.putParcelable("SettingsInEffect", settingsInEffect);
            mEventFacade.postEvent(mEventType + index + "onSuccess", mResults.clone());
            mResults.clear();
        }

        @Override
        public void onStartFailure(int errorCode) {
            String errorString = "UNKNOWN_ERROR_CODE";
            if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED) {
                errorString = "ADVERTISE_FAILED_ALREADY_STARTED";
            } else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE) {
                errorString = "ADVERTISE_FAILED_DATA_TOO_LARGE";
            } else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
                errorString = "ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
            } else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR) {
                errorString = "ADVERTISE_FAILED_INTERNAL_ERROR";
            } else if (errorCode == AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
                errorString = "ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
            }
            Log.d("bluetooth_le_advertisement onFailure " + mEventType + " "
                    + index + " error " + errorString);
            mResults.putString("Type", "onFailure");
            mResults.putInt("ErrorCode", errorCode);
            mResults.putString("Error", errorString);
            mEventFacade.postEvent(mEventType + index + "onFailure",
                    mResults.clone());
            mResults.clear();
        }
    }

    @Override
    public void shutdown() {
        if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
            for (myAdvertiseCallback mAdvertise : mAdvertiseCallbackList
                .values()) {
                if (mAdvertise != null) {
                    try{
                        mBluetoothAdapter.getBluetoothLeAdvertiser()
                            .stopAdvertising(mAdvertise);
                    } catch (NullPointerException e) {
                        Log.e("Failed to stop ble advertising.", e);
                    }
                }
            }
        }
        mAdvertiseCallbackList.clear();
        mAdvertiseSettingsList.clear();
        mAdvertiseDataList.clear();
    }
}
