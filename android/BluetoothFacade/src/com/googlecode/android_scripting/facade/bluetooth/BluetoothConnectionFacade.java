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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothA2dpSink;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothHeadsetClient;
import android.bluetooth.BluetoothInputDevice;
import android.bluetooth.BluetoothMapClient;
import android.bluetooth.BluetoothPbapClient;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothPan;
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
import com.googlecode.android_scripting.facade.bluetooth.BluetoothPairingHelper;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

import org.json.JSONArray;
import org.json.JSONException;

public class BluetoothConnectionFacade extends RpcReceiver {

    private final Service mService;
    private final Context mContext;
    private final BluetoothAdapter mBluetoothAdapter;
    private final BluetoothManager mBluetoothManager;
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
    private final IntentFilter mPbapClientStateChangeFilter;
    private final IntentFilter mPanStateChangeFilter;
    private final IntentFilter mMapClientStateChangeFilter;

    private final Bundle mGoodNews;
    private final Bundle mBadNews;

    private BluetoothA2dpFacade mA2dpProfile;
    private BluetoothA2dpSinkFacade mA2dpSinkProfile;
    private BluetoothHidFacade mHidProfile;
    private BluetoothHspFacade mHspProfile;
    private BluetoothHfpClientFacade mHfpClientProfile;
    private BluetoothPbapClientFacade mPbapClientProfile;
    private BluetoothPanFacade mPanProfile;
    private BluetoothMapClientFacade mMapClientProfile;

    public BluetoothConnectionFacade(FacadeManager manager) {
        super(manager);
        mService = manager.getService();
        mContext = mService.getApplicationContext();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothManager = (BluetoothManager) mContext.getSystemService(
                Service.BLUETOOTH_SERVICE);
        // Use a synchronized map to avoid racing problems
        listeningDevices = Collections.synchronizedMap(new HashMap<String, BroadcastReceiver>());

        mEventFacade = manager.getReceiver(EventFacade.class);
        mPairingHelper = new BluetoothPairingHelper(mEventFacade);
        mA2dpProfile = manager.getReceiver(BluetoothA2dpFacade.class);
        mA2dpSinkProfile = manager.getReceiver(BluetoothA2dpSinkFacade.class);
        mHidProfile = manager.getReceiver(BluetoothHidFacade.class);
        mHspProfile = manager.getReceiver(BluetoothHspFacade.class);
        mHfpClientProfile = manager.getReceiver(BluetoothHfpClientFacade.class);
        mPbapClientProfile = manager.getReceiver(BluetoothPbapClientFacade.class);
        mPanProfile = manager.getReceiver(BluetoothPanFacade.class);
        mMapClientProfile = manager.getReceiver(BluetoothMapClientFacade.class);

        mDiscoverConnectFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mDiscoverConnectFilter.addAction(BluetoothDevice.ACTION_UUID);
        mDiscoverConnectFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        mPairingFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        mPairingFilter.addAction(BluetoothDevice.ACTION_CONNECTION_ACCESS_REQUEST);
        mPairingFilter.addAction(BluetoothDevice.ACTION_CONNECTION_ACCESS_REPLY);
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
        mPbapClientStateChangeFilter =
            new IntentFilter(BluetoothPbapClient.ACTION_CONNECTION_STATE_CHANGED);
        mPanStateChangeFilter =
            new IntentFilter(BluetoothPan.ACTION_CONNECTION_STATE_CHANGED);
        mMapClientStateChangeFilter =
            new IntentFilter(BluetoothMapClient.ACTION_CONNECTION_STATE_CHANGED);

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
         * @param bond     If true, bond the device only.
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
            Log.d("Action received: " + action);

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            // Check if received the specified device
            if (!BluetoothFacade.deviceMatch(device, mDeviceID)) {
                Log.e("Action devices does match act: " + device + " exp " + mDeviceID);
                return;
            }
            // Find the state.
            int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, -1);
            if (state == -1) {
                Log.e("Action does not have a state.");
                return;
            }

            String connState = "";
            if (state == BluetoothProfile.STATE_CONNECTED) {
                connState = "Connecting";
            } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                connState = "Disconnecting";
            }

            int profile = -1;
            String listener = "";
            switch (action) {
                case BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.A2DP;
                    listener = "A2dp" + connState + mDeviceID;
                    break;
                case BluetoothInputDevice.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.INPUT_DEVICE;
                    listener = "Hid" + connState + mDeviceID;
                    break;
                case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.HEADSET;
                    listener = "Hsp" + connState + mDeviceID;
                    break;
                case BluetoothPan.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.PAN;
                    listener = "Pan" + connState + mDeviceID;
                    break;
                case BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.HEADSET_CLIENT;
                    listener = "HfpClient" + connState + mDeviceID;
                    break;
                case BluetoothA2dpSink.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.A2DP_SINK;
                    listener = "A2dpSink" + connState + mDeviceID;
                    break;
                case BluetoothPbapClient.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.PBAP_CLIENT;
                    listener = "PbapClient" + connState + mDeviceID;
                    break;
                case BluetoothMapClient.ACTION_CONNECTION_STATE_CHANGED:
                    profile = BluetoothProfile.MAP_CLIENT;
                    listener = "MapClient" + connState + mDeviceID;
                    break;
            }

            if (profile == -1) {
                Log.e("Action does not match any given profiles " + action);
                return;
            }


            // Post an event to Facade.
            if ((state == BluetoothProfile.STATE_CONNECTED) || (state
                    == BluetoothProfile.STATE_DISCONNECTED)) {
                Bundle news = new Bundle();
                news.putInt("profile", profile);
                news.putInt("state", state);
                news.putString("addr", device.getAddress());
                mEventFacade.postEvent("BluetoothProfileConnectionStateChanged", news);
                unregisterCachedListener(listener);
            }
        }
    }

    /**
     * Converts a given JSONArray to an ArrayList of Integers
     *
     * @param jsonArray the JSONArray to be converted
     * @return <code>List<Integer></></code> the converted list of Integers
     */
    private List<Integer> jsonArrayToIntegerList(JSONArray jsonArray) throws JSONException {
        if (jsonArray == null) {
            return null;
        }
        List<Integer> intArray = new ArrayList<Integer>();
        for (int i = 0; i < jsonArray.length(); i++) {
            intArray.add(jsonArray.getInt(i));
        }
        return intArray;

    }

    /**
     * Helper function used by{@link connectProfile} & {@link disconnectProfiles} to handle a
     * successful return from a profile connect/disconnect call
     *
     * @param deviceID      The device id string
     * @param profile       String representation of the profile - used to create keys in the
     *                      <code>listeningDevices</code> hashMap.
     * @param connState     String that denotes if this is called after a connect or disconnect
     *                      call.  Helps to share code between connect & disconnect
     * @param profileFilter The <code>IntentFilter</code> for this profile that we want to listen
     *                      on.
     */
    private void handleConnectionStateChanged(String deviceID, String profile, String connState,
            IntentFilter profileFilter) {
        Log.d(connState + " " + profile);
        ConnectStateChangeReceiver receiver = new ConnectStateChangeReceiver(deviceID);
        mService.registerReceiver(receiver, profileFilter);
        listeningDevices.put(profile + connState + deviceID, receiver);
    }

    /**
     * Helper function used by{@link connectProfile} & {@link disconnectProfiles} to handle a
     * unsuccessful return from a profile connect/disconnect call
     *
     * @param profile   String representation of the profile - used to post an Event on the
     *                  <code>mEventFacade</code>
     * @param connState String that denotes if this is called after a connect or disconnect call.
     *                  Helps to share code between connect & disconnect
     */
    private void handleConnectionStateChangeFailed(String profile, String connState) {
        Log.d("Failed Starting " + profile + " " + connState);
        Bundle badNews = (Bundle) mBadNews.clone();
        badNews.putString("Type", profile);
        mEventFacade.postEvent(connState, badNews);
    }

    /**
     * Connect on all the profiles to the given Bluetooth device
     *
     * @param device   The <code>BluetoothDevice</code> to connect to
     * @param deviceID Name (String) of the device to connect to
     */
    private void connectProfile(BluetoothDevice device, String deviceID) {
        mService.registerReceiver(mPairingHelper, mPairingFilter);
        ParcelUuid[] deviceUuids = device.getUuids();
        Log.d("Device uuid is " + Arrays.toString(deviceUuids));
        if (deviceUuids == null) {
            mEventFacade.postEvent("BluetoothProfileConnectionEvent", mBadNews);
        }
        Log.d("Connecting to " + device.getAliasName());
        if (BluetoothUuid.containsAnyUuid(BluetoothA2dpFacade.SINK_UUIDS, deviceUuids)) {
            boolean status = mA2dpProfile.a2dpConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "A2dp", "Connecting", mA2dpStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("a2dp", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothA2dpSinkFacade.SOURCE_UUIDS, deviceUuids)) {
            boolean status = mA2dpSinkProfile.a2dpSinkConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "A2dpSink", "Connecting",
                        mA2dpSinkStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("a2dpsink", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHidFacade.UUIDS, deviceUuids)) {
            boolean status = mHidProfile.hidConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "Hid", "Connecting", mHidStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("Hid", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHspFacade.UUIDS, deviceUuids)) {
            boolean status = mHspProfile.hspConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "Hsp", "Connecting", mHspStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("Hsp", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothHfpClientFacade.UUIDS, deviceUuids)) {
            boolean status = mHfpClientProfile.hfpClientConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "HfpClient", "Connecting",
                        mHfpClientStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("HfpClient", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothPanFacade.UUIDS, deviceUuids)) {
            boolean status = mPanProfile.panConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "Pan", "Connecting",
                        mPanStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("Pan", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothPbapClientFacade.UUIDS, deviceUuids)) {
            boolean status = mPbapClientProfile.pbapClientConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "PbapClient", "Connecting",
                        mPbapClientStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("PbapClient", "Connect");
            }
        }
        if (BluetoothUuid.containsAnyUuid(BluetoothMapClientFacade.MAP_UUIDS, deviceUuids)) {
            boolean status = mMapClientProfile.mapClientConnect(device);
            if (status) {
                handleConnectionStateChanged(deviceID, "MapClient", "Connecting",
                        mMapClientStateChangeFilter);
            } else {
                handleConnectionStateChangeFailed("MapClient", "Connect");
            }
        }
        mService.unregisterReceiver(mPairingHelper);
    }

    /**
     * Disconnect on all available profiles from the given device
     *
     * @param device   The <code>BluetoothDevice</code> to disconnect from
     * @param deviceID Name (String) of the device to disconnect from
     */
    private void disconnectProfiles(BluetoothDevice device, String deviceID) {
        Log.d("Disconnecting device " + device);
        // Blindly disconnect all profiles. We may not have some of them connected so that will be a
        // null op.
        mA2dpProfile.a2dpDisconnect(device);
        mA2dpSinkProfile.a2dpSinkDisconnect(device);
        mHidProfile.hidDisconnect(device);
        mHspProfile.hspDisconnect(device);
        mHfpClientProfile.hfpClientDisconnect(device);
        mPbapClientProfile.pbapClientDisconnect(device);
        mPanProfile.panDisconnect(device);
        mMapClientProfile.mapClientDisconnect(device);
    }

    /**
     * Disconnect from specific profiles provided in the given List of profiles.
     *
     * @param device     The {@link BluetoothDevice} to disconnect from
     * @param deviceID   Name/BDADDR (String) of the device to disconnect from
     * @param profileIds The list of profiles we want to disconnect on.
     */
    private void disconnectProfiles(BluetoothDevice device, String deviceID,
            List<Integer> profileIds) {
        boolean result;
        for (int profileId : profileIds) {
            switch (profileId) {
                case BluetoothProfile.A2DP_SINK:
                    result = mA2dpSinkProfile.a2dpSinkDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "A2dpSink", "Disconnecting",
                                mA2dpSinkStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("a2dpsink", "Disconnect");
                    }
                    break;
                case BluetoothProfile.A2DP:
                    result = mA2dpProfile.a2dpDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "A2dp", "Disconnecting",
                                mA2dpStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("a2dp", "Disconnect");
                    }
                    break;
                case BluetoothProfile.INPUT_DEVICE:
                    result = mHidProfile.hidDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "Hid", "Disconnecting",
                                mHidStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("Hid", "Disconnect");
                    }
                    break;
                case BluetoothProfile.HEADSET:
                    result = mHspProfile.hspDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "Hsp", "Disconnecting",
                                mHspStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("Hsp", "Disconnect");
                    }
                    break;
                case BluetoothProfile.HEADSET_CLIENT:
                    result = mHfpClientProfile.hfpClientDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "HfpClient", "Disconnecting",
                                mHfpClientStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("HfpClient", "Disconnect");
                    }
                    break;
                case BluetoothProfile.PBAP_CLIENT:
                    result = mPbapClientProfile.pbapClientDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "PbapClient", "Disconnecting",
                                mPbapClientStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("PbapClient", "Disconnect");
                    }
                    break;
                case BluetoothProfile.MAP_CLIENT:
                    result = mMapClientProfile.mapDisconnect(device);
                    if (result) {
                        handleConnectionStateChanged(deviceID, "MapClient", "Disconnecting",
                                mMapClientStateChangeFilter);
                    } else {
                        handleConnectionStateChangeFailed("MapClient", "Disconnect");
                    }
                    break;
                default:
                    Log.d("Unknown Profile Id to disconnect from. Quitting");
                    return; // returns on the first unknown profile  it encounters.
            }
        }
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

    @Rpc(description = "Return a list of devices connected through bluetooth LE")
    public List<BluetoothDevice> bluetoothGetConnectedLeDevices(Integer profile) {
        return mBluetoothManager.getConnectedDevices(profile);
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

    @Rpc(description = "Return list of connected bluetooth devices over a profile",
            returns = "List of devices connected over the profile")
    public List<BluetoothDevice> bluetoothGetConnectedDevicesOnProfile(
            @RpcParameter(name = "profileId",
                    description = "profileId same as BluetoothProfile")
                    Integer profileId) {
        BluetoothProfile profile = null;
        switch (profileId) {
            case BluetoothProfile.A2DP_SINK:
                return mA2dpSinkProfile.bluetoothA2dpSinkGetConnectedDevices();
            case BluetoothProfile.HEADSET_CLIENT:
                return mHfpClientProfile.bluetoothHfpClientGetConnectedDevices();
            case BluetoothProfile.PBAP_CLIENT:
                return mPbapClientProfile.bluetoothPbapClientGetConnectedDevices();
            case BluetoothProfile.MAP_CLIENT:
                return mMapClientProfile.bluetoothMapClientGetConnectedDevices();
            default:
                Log.w("Profile id " + profileId + " is not yet supported.");
                return new ArrayList<BluetoothDevice>();
        }
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

    @Rpc(description = "Disconnect from a device that is already connected.")
    public void bluetoothDisconnectConnected(
            @RpcParameter(name = "deviceID",
                    description = "Name or MAC address of a bluetooth device.")
                    String deviceID) throws Exception {
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        disconnectProfiles(mDevice, deviceID);
    }

    @Rpc(description = "Disconnect on a profile from a device that is already connected.")
    public void bluetoothDisconnectConnectedProfile(
            @RpcParameter(name = "deviceID",
                    description = "Name or MAC address of a bluetooth device.")
                    String deviceID,
            @RpcParameter(name = "profileSet",
                    description = "List of profiles to disconnect from.")
                    JSONArray profileSet
    ) throws Exception {
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        disconnectProfiles(mDevice, deviceID, jsonArrayToIntegerList(profileSet));
    }

    @Rpc(description = "Change permissions for a profile.")
    public void bluetoothChangeProfileAccessPermission(
            @RpcParameter(name = "deviceID",
                    description = "Name or MAC address of a bluetooth device.")
                    String deviceID,
            @RpcParameter(name = "profileID",
                    description = "Number of Profile to change access permission")
                    Integer profileID,
            @RpcParameter(name = "access",
                    description = "Access level 0 = Unknown, 1 = Allowed, 2 = Rejected")
                    Integer access
    ) throws Exception {
        if (access < 0 || access > 2) {
            Log.w("Unsupported access level.");
            return;
        }
        BluetoothDevice mDevice = BluetoothFacade.getDevice(mBluetoothAdapter.getBondedDevices(),
                deviceID);
        switch (profileID) {
            case BluetoothProfile.PBAP:
                mDevice.setPhonebookAccessPermission(access);
                break;
            default:
                Log.w("Unsupported profile access change.");
        }
    }


    @Override
    public void shutdown() {
        for (BroadcastReceiver receiver : listeningDevices.values()) {
            try {
                mService.unregisterReceiver(receiver);
            } catch (IllegalArgumentException ex) {
                Log.e("Failed to unregister " + ex);
            }
        }
        listeningDevices.clear();
        mService.unregisterReceiver(mPairingHelper);
    }
}
