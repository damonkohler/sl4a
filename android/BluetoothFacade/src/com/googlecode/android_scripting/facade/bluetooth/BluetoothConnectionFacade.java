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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class BluetoothConnectionFacade extends RpcReceiver {

    private final Service mService;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothPairingHelper mPairingHelper;
    private final Map<String, BroadcastReceiver> listeningDevices;
    private final EventFacade mEventFacade;

    private final IntentFilter mDiscoverConnectFilter;
    private final IntentFilter mPairingFilter;
    private final IntentFilter mBondFilter;
    private final IntentFilter mA2dpStateChangeFilter;
    private final IntentFilter mA2dpSinkStateChangeFilter;
    private final IntentFilter mHidStateChangeFilter;
    private final IntentFilter mHspStateChangeFilter;
    private final IntentFilter mHfpClientStateChangeFilter;

    private final Bundle mGoodNews;
    private final Bundle mBadNews;

    private BluetoothA2dpFacade mA2dpProfile;
    private BluetoothA2dpSinkFacade mA2dpSinkProfile;
    private BluetoothHidFacade mHidProfile;
    private BluetoothHspFacade mHspProfile;
    private BluetoothHfpClientFacade mHfpClientProfile;

    public BluetoothConnectionFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mPairingHelper = new BluetoothPairingHelper();
        // Use a synchronized map to avoid racing problems
        listeningDevices = Collections.synchronizedMap(new HashMap<String, BroadcastReceiver>());

        mEventFacade = manager.getReceiver(EventFacade.class);
        mA2dpProfile = manager.getReceiver(BluetoothA2dpFacade.class);
        mA2dpSinkProfile = manager.getReceiver(BluetoothA2dpSinkFacade.class);
        mHidProfile = manager.getReceiver(BluetoothHidFacade.class);
        mHspProfile = manager.getReceiver(BluetoothHspFacade.class);
        mHfpClientProfile = manager.getReceiver(BluetoothHfpClientFacade.class);

        mDiscoverConnectFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mDiscoverConnectFilter.addAction(BluetoothDevice.ACTION_UUID);
        mDiscoverConnectFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mPairingFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        mPairingFilter.addAction(BluetoothDevice.ACTION_CONNECTION_ACCESS_REQUEST);
        mPairingFilter.setPriority(999);

        mBondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mBondFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mBondFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mA2dpStateChangeFilter = new IntentFilter(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED);
        mA2dpSinkStateChangeFilter =
            new IntentFilter(BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED);
        mHidStateChangeFilter =
            new IntentFilter(BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED);
        mHspStateChangeFilter = new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED);
        mHfpClientStateChangeFilter =
            new IntentFilter(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED);

        mGoodNews = new Bundle();
        mGoodNews.putBoolean("Status", true);
        mBadNews = new Bundle();
        mBadNews.putBoolean("Status", false);
    }

    private void unregisterCachedListener(String listenerId) {
        BroadcastReceiver listener = listeningDevices.remove(listenerId);
        if (listener != null) {
            mService.unregisterReceiver(listener);
        }
    }

    /**
     * Connect to a specific device upon its discovery
     */
    public class DiscoverConnectReceiver extends BroadcastReceiver {
        private final String mDeviceID;
        private BluetoothDevice mDevice;

        /**
         * Constructor
         *
         * @param deviceID Either the device alias name or mac address.
         * @param bond If true, bond the device only.
         */
        public DiscoverConnectReceiver(String deviceID) {
            super();
            mDeviceID = deviceID;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // The specified device is found.
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothFacade.deviceMatch(device, mDeviceID)) {
                    Log.d("Found device " + device.getAliasName() + " for connection.");
                    mBluetoothAdapter.cancelDiscovery();
                    mDevice = device;
                }
            // After discovery stops.
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if (mDevice == null) {
                    Log.d("Device " + mDeviceID + " not discovered.");
                    mEventFacade.postEvent("Bond" + mDeviceID, mBadNews);
                    return;
                }
                boolean status = mDevice.fetchUuidsWithSdp();
                Log.d("Initiated ACL connection: " + status);
            } else if (action.equals(BluetoothDevice.ACTION_UUID)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothFacade.deviceMatch(device, mDeviceID)) {
                    Log.d("Initiating connections.");
                    connectProfile(device, mDeviceID);
                    mService.unregisterReceiver(listeningDevices.remove("Connect" + mDeviceID));
                }
            }
        }
    }

    /**
     * Connect to a specific device upon its discovery
     */
    public class DiscoverBondReceiver extends BroadcastReceiver {
        private final String mDeviceID;
        private BluetoothDevice mDevice = null;
        private boolean started = false;

        /**
         * Constructor
         *
         * @param deviceID Either the device alias name or Mac address.
         */
        public DiscoverBondReceiver(String deviceID) {
            super();
            mDeviceID = deviceID;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // The specified device is found.
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothFacade.deviceMatch(device, mDeviceID)) {
                    Log.d("Found device " + device.getAliasName() + " for connection.");
                    mBluetoothAdapter.cancelDiscovery();
                    mDevice = device;
                }
            // After discovery stops.
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if (mDevice == null) {
                    Log.d("Device " + mDeviceID + " was not discovered.");
                    mEventFacade.postEvent("Bond", mBadNews);
                    return;
                }
                // Attempt to initiate bonding.
                if (!started) {
                    Log.d("Bond with " + mDevice.getAliasName());
                    if (mDevice.createBond()) {
                        started = true;
                        Log.d("Bonding started.");
                    } else {
                        Log.e("Failed to bond with " + mDevice.getAliasName());
                        mEventFacade.postEvent("Bond", mBadNews);
                        mService.unregisterReceiver(listeningDevices.remove("Bond" + mDeviceID));
                    }
                }
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                Log.d("Bond state changing.");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (BluetoothFacade.deviceMatch(device, mDeviceID)) {
                    int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    Log.d("New state is " + state);
                    if (state == BluetoothDevice.BOND_BONDED) {
                        Log.d("Bonding with " + mDeviceID + " successful.");
                        mEventFacade.postEvent("Bond" + mDeviceID, mGoodNews);
                        mService.unregisterReceiver(listeningDevices.remove("Bond" + mDeviceID));
                    }
                }
            }
        }
    }

    public class ConnectStateChangeReceiver extends BroadcastReceiver {
        private final String mDeviceID;

        public ConnectStateChangeReceiver(String deviceID) {
            mDeviceID = deviceID;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Check if received the specified device
            if (!BluetoothFacade.deviceMatch(device, mDeviceID)) {
                return;
            }
            if (action.equals(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, -1);
                if (state == BluetoothA2dp.STATE_CONNECTED) {
                    Bundle a2dpGoodNews = (Bundle) mGoodNews.clone();
                    a2dpGoodNews.putString("Type", "a2dp");
                    mEventFacade.postEvent("A2dpConnect" + mDeviceID, a2dpGoodNews);
                    unregisterCachedListener("A2dpConnecting" + mDeviceID);
                }
            } else if (action.equals(BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothInputDevice.EXTRA_STATE, -1);
                if (state == BluetoothInputDevice.STATE_CONNECTED) {
                    mEventFacade.postEvent("HidConnect" + mDeviceID, mGoodNews);
                    unregisterCachedListener("HidConnecting" + mDeviceID);
                }
            } else if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
                if (state == BluetoothHeadset.STATE_CONNECTED) {
                    mEventFacade.postEvent("HspConnect" + mDeviceID, mGoodNews);
                    unregisterCachedListener("HspConnecting" + mDeviceID);
                }
            }
        }
    }

    private void connectProfile(BluetoothDevice device, String deviceID) {
        mService.registerReceiver(mPairingHelper, mPairingFilter);
        ParcelUuid[] deviceUuids = device.getUuids();
        Log.d("Device uuid is " + deviceUuids);
        if (deviceUuids == null) {
            mEventFacade.postEvent("BluetoothProfileConnectionEvent", mBadNews);
        }
        Log.d("Connecting to " + device.getAliasName());
        if (BluetoothUuid.containsAnyUuid(BluetoothA2dpFacade.SINK_UUIDS, deviceUuids)) {
            boolean status = mA2dpProfile.a2dpConnect(device);
            if (status) {
                Log.d("Connecting A2dp...");
                ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
                mService.registerReceiver(receiver, mA2dpStateChangeFilter);
                listeningDevices.put("A2dpConnecting" + deviceID, receiver);
            } else {
                Log.d("Failed starting A2dp connection.");
                Bundle a2dpBadNews = (Bundle) mBadNews.clone();
                a2dpBadNews.putString("Type", "a2dp");
                mEventFacade.postEvent("Connect", a2dpBadNews);
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothA2dpSinkFacade.SOURCE_UUIDS, deviceUuids)) {
            boolean status = mA2dpSinkProfile.a2dpSinkConnect(device);
            if (status) {
                Log.d("Connecting A2dp Sink...");
                ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
                mService.registerReceiver(receiver, mA2dpSinkStateChangeFilter);
                listeningDevices.put("A2dpSinkConnecting" + deviceID, receiver);
            } else {
                Log.d("Failed starting A2dp Sink connection.");
                Bundle a2dpSinkBadNews = (Bundle) mBadNews.clone();
                a2dpSinkBadNews.putString("Type", "a2dpsink");
                mEventFacade.postEvent("Connect", a2dpSinkBadNews);
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHidFacade.UUIDS, deviceUuids)) {
            boolean status = mHidProfile.hidConnect(device);
            if (status) {
                Log.d("Connecting Hid...");
                ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
                mService.registerReceiver(receiver, mHidStateChangeFilter);
                listeningDevices.put("HidConnecting" + deviceID, receiver);
            } else {
                Log.d("Failed starting Hid connection.");
                mEventFacade.postEvent("HidConnect" + deviceID, mBadNews);
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHspFacade.UUIDS, deviceUuids)) {
            boolean status = mHspProfile.hspConnect(device);
            if (status) {
                Log.d("Connecting Hsp...");
                ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
                mService.registerReceiver(receiver, mHspStateChangeFilter);
                listeningDevices.put("HspConnecting" + deviceID, receiver);
            } else {
                Log.d("Failed starting Hsp connection.");
                mEventFacade.postEvent("HspConnect" + deviceID, mBadNews);
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHfpClientFacade.UUIDS, deviceUuids)) {
            boolean status = mHfpClientProfile.hfpClientConnect(device);
            if (status) {
                Log.d("Connecting HFP Client ...");
                ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
                mService.registerReceiver(receiver, mHfpClientStateChangeFilter);
                listeningDevices.put("HfpClientConnecting" + deviceID, receiver);
            } else {
                Log.d("Failed starting Hfp Client connection.");
                mEventFacade.postEvent("HfpClientConnect" + deviceID, mBadNews);
            }
        }
        mService.unregisterReceiver(mPairingHelper);
    }

    private void disconnectProfiles(BluetoothDevice device, String deviceID) {
        Log.d("Disconnecting device " + device);
        // Blindly disconnect all profiles. We may not have some of them connected so that will be a
        // null op.
        mA2dpProfile.a2dpDisconnect(device);
        mA2dpSinkProfile.a2dpSinkDisconnect(device);
        mHidProfile.hidDisconnect(device);
        mHspProfile.hspDisconnect(device);
        mHfpClientProfile.hfpClientDisconnect(device);
    }

    @Rpc(description = "Start intercepting all bluetooth connection pop-ups.")
    public void bluetoothStartPairingHelper() {
        mService.registerReceiver(mPairingHelper, mPairingFilter);
    }

    @Rpc(description = "Return a list of devices connected through bluetooth")
    public List<BluetoothDevice> bluetoothGetConnectedDevices() {
        ArrayList<BluetoothDevice> results = new ArrayList<BluetoothDevice>();
        for (BluetoothDevice bd : mBluetoothAdapter.getBondedDevices()) {
            if (bd.isConnected()) {
                results.add(bd);
            }
        }
        return results;
    }

    @Rpc(description = "Return true if a bluetooth device is connected.")
    public Boolean bluetoothIsDeviceConnected(String deviceID) {
        for (BluetoothDevice bd : mBluetoothAdapter.getBondedDevices()) {
            if (BluetoothFacade.deviceMatch(bd, deviceID)) {
                return bd.isConnected();
            }
        }
        return false;
    }

    @Rpc(description = "Connect to a specified device once it's discovered.",
         returns = "Whether discovery started successfully.")
    public Boolean bluetoothDiscoverAndConnect(
            @RpcParameter(name = "deviceID",
                          description = "Name or MAC address of a bluetooth device.")
            String deviceID) {
        mBluetoothAdapter.cancelDiscovery();
        if (listeningDevices.containsKey(deviceID)) {
            Log.d("This device is already in the process of discovery and connecting.");
            return true;
        }
        DiscoverConnectReceiver receiver = new DiscoverConnectReceiver(deviceID);
        listeningDevices.put("Connect" + deviceID, receiver);
        mService.registerReceiver(receiver, mDiscoverConnectFilter);
        return mBluetoothAdapter.startDiscovery();
    }

    @Rpc(description = "Bond to a specified device once it's discovered.",
         returns = "Whether discovery started successfully. ")
    public Boolean bluetoothDiscoverAndBond(
            @RpcParameter(name = "deviceID",
                          description = "Name or MAC address of a bluetooth device.")
            String deviceID) {
        mBluetoothAdapter.cancelDiscovery();
        if (listeningDevices.containsKey(deviceID)) {
            Log.d("This device is already in the process of discovery and bonding.");
            return true;
        }
        if (BluetoothFacade.deviceExists(mBluetoothAdapter.getBondedDevices(), deviceID)) {
            Log.d("Device " + deviceID + " is already bonded.");
            mEventFacade.postEvent("Bond" + deviceID, mGoodNews);
            return true;
        }
        DiscoverBondReceiver receiver = new DiscoverBondReceiver(deviceID);
        if (listeningDevices.containsKey("Bond" + deviceID)) {
            mService.unregisterReceiver(listeningDevices.remove("Bond" + deviceID));
        }
        listeningDevices.put("Bond" + deviceID, receiver);
        mService.registerReceiver(receiver, mBondFilter);
        Log.d("Start discovery for bonding.");
        return mBluetoothAdapter.startDiscovery();
    }

    @Rpc(description = "Unbond a device.",
         returns = "Whether the device was successfully unbonded.")
    public Boolean bluetoothUnbond(
            @RpcParameter(name = "deviceID",
                          description = "Name or MAC address of a bluetooth device.")
            String deviceID) throws Exception {
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        return mDevice.removeBond();
    }

    @Rpc(description = "Connect to a device that is already bonded.")
    public void bluetoothConnectBonded(
            @RpcParameter(name = "deviceID",
                          description = "Name or MAC address of a bluetooth device.")
            String deviceID) throws Exception {
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        connectProfile(mDevice, deviceID);
    }

    // TODO: Split the disconnect RPC by profiles as well for granular control over the ACL
    @Rpc(description = "Disconnect from a device that is already connected.")
    public void bluetoothDisconnectConnected(
            @RpcParameter(name = "deviceID",
                          description = "Name or MAC address of a bluetooth device.")
            String deviceID) throws Exception {
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        disconnectProfiles(mDevice, deviceID);
    }

    @Override
    public void shutdown() {
        for(BroadcastReceiver receiver : listeningDevices.values()) {
            mService.unregisterReceiver(receiver);
        }
        listeningDevices.clear();
        mService.unregisterReceiver(mPairingHelper);
    }
}
