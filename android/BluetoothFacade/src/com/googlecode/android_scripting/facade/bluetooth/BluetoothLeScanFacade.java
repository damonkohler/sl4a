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
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.le.ScanFilter.Builder;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.ConvertUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.MainThread;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * BluetoothLe Scan functions.
 */

public class BluetoothLeScanFacade extends RpcReceiver {

    private final EventFacade mEventFacade;

    private BluetoothAdapter mBluetoothAdapter;
    private static int ScanCallbackCount;
    private static int FilterListCount;
    private static int LeScanCallbackCount;
    private static int ScanSettingsCount;
    private final Service mService;
    private final BluetoothLeScanner mScanner;
    private android.bluetooth.le.ScanSettings.Builder mScanSettingsBuilder;
    private Builder mScanFilterBuilder;
    private final HashMap<Integer, myScanCallback> mScanCallbackList;
    private final HashMap<Integer, myLeScanCallback> mLeScanCallbackList;
    private final HashMap<Integer, ArrayList<ScanFilter>> mScanFilterList;
    private final HashMap<Integer, ScanSettings> mScanSettingsList;

    public BluetoothLeScanFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mBluetoothAdapter = MainThread.run(mService,
                new Callable<BluetoothAdapter>() {
                    @Override
                    public BluetoothAdapter call() throws Exception {
                        return BluetoothAdapter.getDefaultAdapter();
                    }
                });
        mScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mEventFacade = manager.getReceiver(EventFacade.class);
        mScanFilterList = new HashMap<Integer, ArrayList<ScanFilter>>();
        mLeScanCallbackList = new HashMap<Integer, myLeScanCallback>();
        mScanSettingsList = new HashMap<Integer, ScanSettings>();
        mScanCallbackList = new HashMap<Integer, myScanCallback>();
        mScanFilterBuilder = new Builder();
        mScanSettingsBuilder = new android.bluetooth.le.ScanSettings.Builder();
    }

    /**
     * Constructs a myScanCallback obj and returns its index
     *
     * @return Integer myScanCallback.index
     */
    @Rpc(description = "Generate a new myScanCallback Object")
    public Integer bleGenScanCallback() {
        ScanCallbackCount += 1;
        int index = ScanCallbackCount;
        myScanCallback mScan = new myScanCallback(index);
        mScanCallbackList.put(mScan.index, mScan);
        return mScan.index;
    }

    /**
     * Constructs a myLeScanCallback obj and returns its index
     *
     * @return Integer myScanCallback.index
     */
    @Rpc(description = "Generate a new myScanCallback Object")
    public Integer bleGenLeScanCallback() {
        LeScanCallbackCount += 1;
        int index = LeScanCallbackCount;
        myLeScanCallback mScan = new myLeScanCallback(index);
        mLeScanCallbackList.put(mScan.index, mScan);
        return mScan.index;
    }

    /**
     * Constructs a new filter list array and returns its index
     *
     * @return Integer index
     */
    @Rpc(description = "Generate a new Filter list")
    public Integer bleGenFilterList() {
        FilterListCount += 1;
        int index = FilterListCount;
        mScanFilterList.put(index, new ArrayList<ScanFilter>());
        return index;
    }

    /**
     * Constructs a new filter list array and returns its index
     *
     * @return Integer index
     */
    @Rpc(description = "Generate a new Filter list")
    public Integer bleBuildScanFilter(
            @RpcParameter(name = "filterIndex")
            Integer filterIndex
            ) {
        mScanFilterList.get(filterIndex).add(mScanFilterBuilder.build());
        mScanFilterBuilder = new Builder();
        return mScanFilterList.get(filterIndex).size()-1;
    }

    /**
     * Constructs a new scan setting and returns its index
     *
     * @return Integer index
     */
    @Rpc(description = "Generate a new scan settings Object")
    public Integer bleBuildScanSetting() {
        ScanSettingsCount += 1;
        int index = ScanSettingsCount;
        mScanSettingsList.put(index, mScanSettingsBuilder.build());
        mScanSettingsBuilder = new android.bluetooth.le.ScanSettings.Builder();
        return index;
    }

    /**
     * Stops a ble scan
     *
     * @param index the id of the myScan whose ScanCallback to stop
     * @throws Exception
     */
    @Rpc(description = "Stops an ongoing ble advertisement scan")
    public void bleStopBleScan(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        Log.d("bluetooth_le_scan mScanCallback " + index);
        if (mScanCallbackList.get(index) != null) {
            myScanCallback mScanCallback = mScanCallbackList.get(index);
            mScanner.stopScan(mScanCallback);
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Stops a classic ble scan
     *
     * @param index the id of the myScan whose LeScanCallback to stop
     * @throws Exception
     */
    @Rpc(description = "Stops an ongoing classic ble scan")
    public void bleStopClassicBleScan(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        Log.d("bluetooth_le_scan mLeScanCallback " + index);
        if (mLeScanCallbackList.get(index) != null) {
            myLeScanCallback mLeScanCallback = mLeScanCallbackList.get(index);
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Starts a ble scan
     *
     * @param index the id of the myScan whose ScanCallback to start
     * @throws Exception
     */
    @Rpc(description = "Starts a ble advertisement scan")
    public void bleStartBleScan(
            @RpcParameter(name = "filterListIndex")
            Integer filterListIndex,
            @RpcParameter(name = "scanSettingsIndex")
            Integer scanSettingsIndex,
            @RpcParameter(name = "callbackIndex")
            Integer callbackIndex
            ) throws Exception {
        Log.d("bluetooth_le_scan starting a background scan");
        ArrayList<ScanFilter> mScanFilters = new ArrayList<ScanFilter>();
        mScanFilters.add(new ScanFilter.Builder().build());
        ScanSettings mScanSettings = new ScanSettings.Builder().build();
        if (mScanFilterList.get(filterListIndex) != null) {
            mScanFilters = mScanFilterList.get(filterListIndex);
        } else {
            throw new Exception("Invalid filterListIndex input:"
                    + Integer.toString(filterListIndex));
        }
        if (mScanSettingsList.get(scanSettingsIndex) != null) {
            mScanSettings = mScanSettingsList.get(scanSettingsIndex);
        } else if (!mScanSettingsList.isEmpty()) {
            throw new Exception("Invalid scanSettingsIndex input:"
                    + Integer.toString(scanSettingsIndex));
        }
        if (mScanCallbackList.get(callbackIndex) != null) {
            mScanner.startScan(mScanFilters, mScanSettings, mScanCallbackList.get(callbackIndex));
        } else {
            throw new Exception("Invalid filterListIndex input:"
                    + Integer.toString(filterListIndex));
        }
    }

    /**
     * Starts a classic ble scan
     *
     * @param index the id of the myScan whose ScanCallback to start
     * @throws Exception
     */
    @Rpc(description = "Starts a classic ble advertisement scan")
    public boolean bleStartClassicBleScan(
            @RpcParameter(name = "leCallbackIndex")
            Integer leCallbackIndex
            ) throws Exception {
        Log.d("bluetooth_le_scan starting a background scan");
        boolean result = false;
        if (mLeScanCallbackList.get(leCallbackIndex) != null) {
            result = mBluetoothAdapter.startLeScan(mLeScanCallbackList.get(leCallbackIndex));
        } else {
            throw new Exception("Invalid leCallbackIndex input:"
                    + Integer.toString(leCallbackIndex));
        }
        return result;
    }

    /**
     * Starts a classic ble scan with service Uuids
     *
     * @param index the id of the myScan whose ScanCallback to start
     * @throws Exception
     */
    @Rpc(description = "Starts a classic ble advertisement scan with service Uuids")
    public boolean bleStartClassicBleScanWithServiceUuids(
            @RpcParameter(name = "leCallbackIndex")
            Integer leCallbackIndex,
            @RpcParameter(name = "serviceUuids")
            String[] serviceUuidList
            ) throws Exception {
        Log.d("bluetooth_le_scan starting a background scan");
        UUID[] serviceUuids = new UUID[serviceUuidList.length];
        for (int i = 0; i < serviceUuidList.length; i++) {
            serviceUuids[i] = UUID.fromString(serviceUuidList[i]);
        }
        boolean result = false;
        if (mLeScanCallbackList.get(leCallbackIndex) != null) {
            result = mBluetoothAdapter.startLeScan(serviceUuids,
                    mLeScanCallbackList.get(leCallbackIndex));
            System.out.println(result);
        } else {
            throw new Exception("Invalid leCallbackIndex input:"
                    + Integer.toString(leCallbackIndex));
        }
        System.out.println(result);
        return result;
    }

    /**
     * Trigger onBatchScanResults
     *
     * @throws Exception
     */
    @Rpc(description = "Gets the results of the ble ScanCallback")
    public void bleFlushPendingScanResults(
            @RpcParameter(name = "callbackIndex")
            Integer callbackIndex
            ) throws Exception {
        if (mScanCallbackList.get(callbackIndex) != null) {
            mBluetoothAdapter
                    .getBluetoothLeScanner().flushPendingScanResults(
                            mScanCallbackList.get(callbackIndex));
        } else {
            throw new Exception("Invalid callbackIndex input:"
                    + Integer.toString(callbackIndex));
        }
    }

    /**
     * Set scanSettings for ble scan. Note: You have to set all variables at once.
     *
     * @param callbackType Bluetooth LE scan callback type
     * @param reportDelaySeconds Time of delay for reporting the scan result
     * @param scanMode Bluetooth LE scan mode.
     * @param scanResultType Bluetooth LE scan result type
     * @throws Exception
     */

    /**
     * Set the scan setting's callback type
     * @param callbackType Bluetooth LE scan callback type
     */
    @Rpc(description = "Set the scan setting's callback type")
    public void bleSetScanSettingsCallbackType(
            @RpcParameter(name = "callbackType")
            Integer callbackType) {
        mScanSettingsBuilder.setCallbackType(callbackType);
    }

    /**
     * Set the scan setting's report delay millis
     * @param reportDelayMillis Time of delay for reporting the scan result
     */
    @Rpc(description = "Set the scan setting's report delay millis")
    public void bleSetScanSettingsReportDelayMillis(
            @RpcParameter(name = "reportDelayMillis")
            Long reportDelayMillis) {
        mScanSettingsBuilder.setReportDelay(reportDelayMillis);
    }

    /**
     * Set the scan setting's scan mode
     * @param scanMode Bluetooth LE scan mode.
     */
    @Rpc(description = "Set the scan setting's scan mode")
    public void bleSetScanSettingsScanMode(
            @RpcParameter(name = "scanMode")
            Integer scanMode) {
        mScanSettingsBuilder.setScanMode(scanMode);
    }

    /**
     * Set the scan setting's scan result type
     * @param scanResultType Bluetooth LE scan result type
     */
    @Rpc(description = "Set the scan setting's scan result type")
    public void bleSetScanSettingsResultType(
            @RpcParameter(name = "scanResultType")
            Integer scanResultType) {
        mScanSettingsBuilder.setScanResultType(scanResultType);
    }
    /**
     * Get ScanSetting's callback type
     *
     * @param index the ScanSetting object to use
     * @return the ScanSetting's callback type
     * @throws Exception
     */
    @Rpc(description = "Get ScanSetting's callback type")
    public Integer bleGetScanSettingsCallbackType(
            @RpcParameter(name = "index")
            Integer index
            ) throws Exception {
        if (mScanSettingsList.get(index) != null) {
            ScanSettings mScanSettings = mScanSettingsList.get(index);
            return mScanSettings.getCallbackType();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanSetting's report delay in milli seconds
     *
     * @param index the ScanSetting object to useSystemClock
     * @return the ScanSetting's report delay in milliseconds
     * @throws Exception
     */
    @Rpc(description = "Get ScanSetting's report delay milliseconds")
    public Long bleGetScanSettingsReportDelayMillis(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mScanSettingsList.get(index) != null) {
            ScanSettings mScanSettings = mScanSettingsList.get(index);
            return mScanSettings.getReportDelayMillis();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanSetting's scan mode
     *
     * @param index the ScanSetting object to use
     * @return the ScanSetting's scan mode
     * @throws Exception
     */
    @Rpc(description = "Get ScanSetting's scan mode")
    public Integer bleGetScanSettingsScanMode(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mScanSettingsList.get(index) != null) {
            ScanSettings mScanSettings = mScanSettingsList.get(index);
            return mScanSettings.getScanMode();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanSetting's scan result type
     *
     * @param index the ScanSetting object to use
     * @return the ScanSetting's scan result type
     * @throws Exception
     */
    @Rpc(description = "Get ScanSetting's scan result type")
    public Integer bleGetScanSettingsScanResultType(
            @RpcParameter(name = "index")
            Integer index) throws Exception {
        if (mScanSettingsList.get(index) != null) {
            ScanSettings mScanSettings = mScanSettingsList.get(index);
            return mScanSettings.getScanResultType();
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's Manufacturer Id
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's manufacturer id
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's Manufacturer Id")
    public Integer bleGetScanFilterManufacturerId(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return mScanFilterList.get(index)
                        .get(filterIndex).getManufacturerId();
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's device address
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's device address
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's device address")
    public String bleGetScanFilterDeviceAddress(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return mScanFilterList.get(index).get(filterIndex).getDeviceAddress();
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's device name
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's device name
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's device name")
    public String bleGetScanFilterDeviceName(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return mScanFilterList.get(index).get(filterIndex).getDeviceName();
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's manufacturer data
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's manufacturer data
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's manufacturer data")
    public String bleGetScanFilterManufacturerData(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return ConvertUtils.convertByteArrayToString(mScanFilterList.get(index)
                        .get(filterIndex).getManufacturerData());
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's manufacturer data mask
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's manufacturer data mask
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's manufacturer data mask")
    public String bleGetScanFilterManufacturerDataMask(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return ConvertUtils.convertByteArrayToString(mScanFilterList.get(index)
                        .get(filterIndex).getManufacturerDataMask());
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's service data
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's service data
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's service data")
    public String bleGetScanFilterServiceData(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return ConvertUtils.convertByteArrayToString(mScanFilterList
                        .get(index).get(filterIndex).getServiceData());
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's service data mask
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's service data mask
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's service data mask")
    public String bleGetScanFilterServiceDataMask(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                return ConvertUtils.convertByteArrayToString(mScanFilterList.get(index)
                        .get(filterIndex).getServiceDataMask());
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's service uuid
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's service uuid
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's service uuid")
    public String bleGetScanFilterServiceUuid(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                if (mScanFilterList.get(index).get(filterIndex).getServiceUuid() != null) {
                    return mScanFilterList.get(index).get(filterIndex).getServiceUuid().toString();
                } else {
                    throw new Exception("No Service Uuid set for filter:"
                            + Integer.toString(filterIndex));
                }
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Get ScanFilter's service uuid mask
     *
     * @param index the ScanFilter object to use
     * @return the ScanFilter's service uuid mask
     * @throws Exception
     */
    @Rpc(description = "Get ScanFilter's service uuid mask")
    public String bleGetScanFilterServiceUuidMask(
            @RpcParameter(name = "index")
            Integer index,
            @RpcParameter(name = "filterIndex")
            Integer filterIndex)
            throws Exception {
        if (mScanFilterList.get(index) != null) {
            if (mScanFilterList.get(index).get(filterIndex) != null) {
                if (mScanFilterList.get(index).get(filterIndex).getServiceUuidMask() != null) {
                    return mScanFilterList.get(index).get(filterIndex).getServiceUuidMask()
                            .toString();
                } else {
                    throw new Exception("No Service Uuid Mask set for filter:"
                            + Integer.toString(filterIndex));
                }
            } else {
                throw new Exception("Invalid filterIndex input:" + Integer.toString(filterIndex));
            }
        } else {
            throw new Exception("Invalid index input:" + Integer.toString(index));
        }
    }

    /**
     * Add filter "macAddress" to existing ScanFilter
     *
     * @param macAddress the macAddress to filter against
     * @throws Exception
     */
    @Rpc(description = "Add filter \"macAddress\" to existing ScanFilter")
    public void bleSetScanFilterDeviceAddress(
            @RpcParameter(name = "macAddress")
            String macAddress
            ) {
            mScanFilterBuilder.setDeviceAddress(macAddress);
    }

    /**
     * Add filter "manufacturereDataId and/or manufacturerData" to existing ScanFilter
     *
     * @param manufacturerDataId the manufacturer data id to filter against
     * @param manufacturerDataMask the manufacturere data mask to filter against
     * @throws Exception
     */
    @Rpc(description = "Add filter \"manufacturereDataId and/or manufacturerData\" to existing ScanFilter")
    public void bleSetScanFilterManufacturerData(
            @RpcParameter(name = "manufacturerDataId")
            Integer manufacturerDataId,
            @RpcParameter(name = "manufacturerData")
            String manufacturerData,
            @RpcParameter(name = "manufacturerDataMask")
            @RpcOptional
            String manufacturerDataMask
            ){
        if (manufacturerDataMask != null) {
            mScanFilterBuilder.setManufacturerData(manufacturerDataId,
                    ConvertUtils.convertStringToByteArray(manufacturerData),
                    ConvertUtils.convertStringToByteArray(manufacturerDataMask));
        } else {
            mScanFilterBuilder.setManufacturerData(manufacturerDataId,
                    ConvertUtils.convertStringToByteArray(manufacturerData));
        }
    }

    /**
     * Add filter "serviceData and serviceDataMask" to existing ScanFilter
     *
     * @param serviceData the service data to filter against
     * @param serviceDataMask the servie data mask to filter against
     * @throws Exception
     */
    @Rpc(description = "Add filter \"serviceData and serviceDataMask\" to existing ScanFilter ")
    public void bleSetScanFilterServiceData(
            @RpcParameter(name = "serviceUuid")
            String serviceUuid,
            @RpcParameter(name = "serviceData")
            String serviceData,
            @RpcParameter(name = "serviceDataMask")
            @RpcOptional
            String serviceDataMask
            ) {
        if (serviceDataMask != null) {
            mScanFilterBuilder
                    .setServiceData(
                            ParcelUuid.fromString(serviceUuid),
                            ConvertUtils.convertStringToByteArray(serviceData),
                            ConvertUtils.convertStringToByteArray(
                                serviceDataMask));
        } else {
            mScanFilterBuilder.setServiceData(ParcelUuid.fromString(serviceUuid),
                    ConvertUtils.convertStringToByteArray(serviceData));
        }
    }

    /**
     * Add filter "serviceUuid and/or serviceMask" to existing ScanFilter
     *
     * @param serviceUuid the service uuid to filter against
     * @param serviceMask the service mask to filter against
     * @throws Exception
     */
    @Rpc(description = "Add filter \"serviceUuid and/or serviceMask\" to existing ScanFilter")
    public void bleSetScanFilterServiceUuid(
            @RpcParameter(name = "serviceUuid")
            String serviceUuid,
            @RpcParameter(name = "serviceMask")
            @RpcOptional
            String serviceMask
            ) {
        if (serviceMask != null) {
            mScanFilterBuilder
                    .setServiceUuid(ParcelUuid.fromString(serviceUuid),
                            ParcelUuid.fromString(serviceMask));
        } else {
            mScanFilterBuilder.setServiceUuid(ParcelUuid.fromString(serviceUuid));
        }
    }

    /**
     * Add filter "device name" to existing ScanFilter
     *
     * @param name the device name to filter against
     * @throws Exception
     */
    @Rpc(description = "Sets the scan filter's device name")
    public void bleSetScanFilterDeviceName(
            @RpcParameter(name = "name")
            String name
            ) {
            mScanFilterBuilder.setDeviceName(name);
    }

    @Rpc(description = "Set the scan setting's match mode")
    public void bleSetScanSettingsMatchMode(
            @RpcParameter(name = "mode") Integer mode) {
        mScanSettingsBuilder.setMatchMode(mode);
    }

    @Rpc(description = "Get the scan setting's match mode")
    public int bleGetScanSettingsMatchMode(
            @RpcParameter(name = "scanSettingsIndex") Integer scanSettingsIndex
            ) {
        return mScanSettingsList.get(scanSettingsIndex).getMatchMode();
    }

    @Rpc(description = "Set the scan setting's number of matches")
    public void bleSetScanSettingsNumOfMatches(
            @RpcParameter(name = "matches") Integer matches) {
        mScanSettingsBuilder.setNumOfMatches(matches);
    }

    @Rpc(description = "Get the scan setting's number of matches")
    public int bleGetScanSettingsNumberOfMatches(
            @RpcParameter(name = "scanSettingsIndex")
            Integer scanSettingsIndex) {
        return  mScanSettingsList.get(scanSettingsIndex).getNumOfMatches();
    }

    private class myScanCallback extends ScanCallback {
        public Integer index;
        String mEventType;
        private final Bundle mResults;

        public myScanCallback(Integer idx) {
            index = idx;
            mEventType = "BleScan";
            mResults = new Bundle();
        }

        @Override
        public void onScanFailed(int errorCode) {
            String errorString = "UNKNOWN_ERROR_CODE";
            if (errorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
                errorString = "SCAN_FAILED_ALREADY_STARTED";
            } else if (errorCode == ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                errorString = "SCAN_FAILED_APPLICATION_REGISTRATION_FAILED";
            } else if (errorCode == ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED) {
                errorString = "SCAN_FAILED_FEATURE_UNSUPPORTED";
            } else if (errorCode == ScanCallback.SCAN_FAILED_INTERNAL_ERROR) {
                errorString = "SCAN_FAILED_INTERNAL_ERROR";
            }
            Log.d("bluetooth_le_scan change onScanFailed " + mEventType + " " + index + " error "
                    + errorString);
            mResults.putInt("ID", index);
            mResults.putString("Type", "onScanFailed");
            mResults.putInt("ErrorCode", errorCode);
            mResults.putString("Error", errorString);
            mEventFacade.postEvent(mEventType + index + "onScanFailed",
                    mResults.clone());
            mResults.clear();
        }

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d("bluetooth_le_scan change onUpdate " + mEventType + " " + index);
            mResults.putInt("ID", index);
            mResults.putInt("CallbackType", callbackType);
            mResults.putString("Type", "onScanResult");
            mResults.putParcelable("Result", result);
            mEventFacade.postEvent(mEventType + index + "onScanResults", mResults.clone());
            mResults.clear();
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Log.d("reportResult " + mEventType + " " + index);
            mResults.putLong("Timestamp", System.currentTimeMillis() / 1000);
            mResults.putInt("ID", index);
            mResults.putString("Type", "onBatchScanResults");
            mResults.putParcelableList("Results", results);
            mEventFacade.postEvent(mEventType + index + "onBatchScanResult", mResults.clone());
            mResults.clear();
        }
    }

    private class myLeScanCallback implements LeScanCallback {
        public Integer index;
        String mEventType;
        private final Bundle mResults;

        public myLeScanCallback(Integer idx) {
            index = idx;
            mEventType = "ClassicBleScan";
            mResults = new Bundle();
        }

        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("bluetooth_classic_le_scan " + mEventType + " " + index);
            mResults.putParcelable("Device", device);
            mResults.putInt("Rssi", rssi);
            mResults.putString("ScanRecord", ConvertUtils.convertByteArrayToString(scanRecord));
            mResults.putString("Type", "onLeScan");
            mEventFacade.postEvent(mEventType + index + "onLeScan", mResults.clone());
            mResults.clear();
        }
    }

    @Override
    public void shutdown() {
      if (mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
          for (myScanCallback mScanCallback : mScanCallbackList.values()) {
              if (mScanCallback != null) {
                  try {
                    mBluetoothAdapter.getBluetoothLeScanner()
                      .stopScan(mScanCallback);
                  } catch (NullPointerException e) {
                    Log.e("Failed to stop ble scan callback.", e);
                  }
              }
          }
          for (myLeScanCallback mLeScanCallback : mLeScanCallbackList.values()) {
              if (mLeScanCallback != null) {
                  try {
                      mBluetoothAdapter.stopLeScan(mLeScanCallback);
                  } catch (NullPointerException e) {
                    Log.e("Failed to stop classic ble scan callback.", e);
                  }
              }
          }
      }
      mScanCallbackList.clear();
      mScanFilterList.clear();
      mScanSettingsList.clear();
      mLeScanCallbackList.clear();
    }
}
