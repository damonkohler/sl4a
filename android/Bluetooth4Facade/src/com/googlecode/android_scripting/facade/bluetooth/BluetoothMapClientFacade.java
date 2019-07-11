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

import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
// import android.bluetooth.BluetoothMapClient;
import android.bluetooth.BluetoothProfile;
// import android.bluetooth.BluetoothUuid;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.ParcelUuid;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.bluetooth.BluetoothNonpublicApi;
import com.googlecode.android_scripting.bluetooth.BluetoothUuid;
import com.googlecode.android_scripting.facade.Bluetooth4Facade;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.List;

public class BluetoothMapClientFacade extends RpcReceiver {
    static final ParcelUuid[] MAP_UUIDS = {
            BluetoothUuid.MAP,
            BluetoothUuid.MNS,
            BluetoothUuid.MAS,
    };
    public static final String MAP_EVENT = "MapMessageReceived";
    public static final String MAP_SMS_SENT_SUCCESS = "SmsSentSuccess";
    public static final String MAP_SMS_DELIVER_SUCCESS = "SmsDeliverSuccess";

    private final Service mService;
    private final BluetoothAdapter mBluetoothAdapter;
    private final EventFacade mEventFacade;
    private final NotificationReceiver mNotificationReceiver;

    private Intent mSendIntent;
    private Intent mDeliveryIntent;
    private PendingIntent mSentIntent;
    private PendingIntent mDeliveredIntent;

    private static boolean sIsMapReady = false;
    // private static BluetoothMapClient sMapProfile = null;
    private static BluetoothProfile sMapProfile = null;

    public class BluetoothMapClient implements BluetoothProfile {
        @Override
        public List<BluetoothDevice> getConnectedDevices() {
            return null;
        }

        @Override
        public int getConnectionState(BluetoothDevice bluetoothDevice) {
            return 0;
        }

        @Override
        public List<BluetoothDevice> getDevicesMatchingConnectionStates(int[] ints) {
            return null;
        }
    }

    public BluetoothMapClientFacade(FacadeManager manager) {
        super(manager);
        Log.d("Creating BluetoothMapClientFacade");
        mService = manager.getService();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.getProfileProxy(mService, new MapServiceListener(),
                BluetoothNonpublicApi.MAP_CLIENT);
        mEventFacade = manager.getReceiver(EventFacade.class);

        mNotificationReceiver = new NotificationReceiver();
        mSendIntent = new Intent(BluetoothNonpublicApi.ACTION_MESSAGE_SENT_SUCCESSFULLY);
        mDeliveryIntent = new Intent(BluetoothNonpublicApi.ACTION_MESSAGE_DELIVERED_SUCCESSFULLY);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothNonpublicApi.ACTION_MESSAGE_RECEIVED);
        intentFilter.addAction(BluetoothNonpublicApi.ACTION_MESSAGE_SENT_SUCCESSFULLY);
        intentFilter.addAction(BluetoothNonpublicApi.ACTION_MESSAGE_DELIVERED_SUCCESSFULLY);
        mService.registerReceiver(mNotificationReceiver, intentFilter);
        Log.d("notification receiver registered");
    }

    class MapServiceListener implements BluetoothProfile.ServiceListener {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            sMapProfile = (BluetoothMapClient) proxy;
            sIsMapReady = true;
        }

        @Override
        public void onServiceDisconnected(int profile) {
            sIsMapReady = false;
        }
    }

    public Boolean mapClientConnect(BluetoothDevice device) {
        return BluetoothNonpublicApi.connectProfile(sMapProfile, device);
    }

    public Boolean mapClientDisconnect(BluetoothDevice device) {
        return BluetoothNonpublicApi.disconnectProfile(sMapProfile, device);
    }

    @Rpc(description = "Connect to an MAP MSE device.")
    public Boolean bluetoothMapClientConnect(
            @RpcParameter(name = "device", description = "Name or MAC address of a bluetooth "
                    + "device.")
                    String device)
            throws Exception {
        if (sMapProfile == null) return false;
        BluetoothDevice mDevice = Bluetooth4Facade.getDevice(mBluetoothAdapter.getBondedDevices(),
                device);
        return BluetoothNonpublicApi.connectProfile(sMapProfile, mDevice);
    }

    @Rpc(description = "Send a (text) message via bluetooth.")
    public Boolean mapSendMessage(
            @RpcParameter(name = "deviceID", description = "Name or MAC address of a device.")
                    String deviceID,
            @RpcParameter(name = "phoneNumbers", description = "Phone number of contact.")
                    String[] phoneNumbers,
            @RpcParameter(name = "message", description = "Message to send.") String message) {
        try {
            BluetoothDevice device =
                    Bluetooth4Facade.getDevice(sMapProfile.getConnectedDevices(), deviceID);
            mSentIntent = PendingIntent.getBroadcast(mService, 0, mSendIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            mDeliveredIntent = PendingIntent.getBroadcast(mService, 0, mDeliveryIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            Uri[] contacts = new Uri[phoneNumbers.length];
            for (int i = 0; i < phoneNumbers.length; i++) {
                Log.d("PhoneNumber count: " + phoneNumbers.length + " = " + phoneNumbers[i]);
                contacts[i] = Uri.parse(phoneNumbers[i]);
            }
            Log.e("sendMessage won't work in no-system app.");
            return false;
            // return sMapProfile.sendMessage(device, contacts, message, mSentIntent,
            //        mDeliveredIntent);
        } catch (Exception e) {
            Log.d("Error sending message, no such device " + e.toString());
        }
        return false;
    }

    public Boolean mapDisconnect(BluetoothDevice device) {
        return false;
        /*
        if (sMapProfile.getPriority(device) > BluetoothProfile.PRIORITY_ON) {
            sMapProfile.setPriority(device, BluetoothProfile.PRIORITY_ON);
        }
        return sMapProfile.disconnect(device);
        */
    }

    @Rpc(description = "Is Map profile ready.")
    public Boolean bluetoothMapClientIsReady() {
        return sIsMapReady;
    }

    @Rpc(description = "Disconnect an MAP device.")
    public Boolean bluetoothMapClientDisconnect(
            @RpcParameter(name = "deviceID", description = "Name or MAC address of a device.")
                    String deviceID)
            throws Exception {
        if (sMapProfile == null) return false;
        List<BluetoothDevice> connectedMapDevices = sMapProfile.getConnectedDevices();
        Log.d("Connected map devices: " + connectedMapDevices);
        BluetoothDevice mDevice = Bluetooth4Facade.getDevice(connectedMapDevices, deviceID);
        if (!connectedMapDevices.isEmpty() && connectedMapDevices.get(0).equals(mDevice)) {
            return mapDisconnect(mDevice);
        } else {
            return false;
        }
    }

    @Rpc(description = "Get all the devices connected through MAP.")
    public List<BluetoothDevice> bluetoothMapClientGetConnectedDevices() {
        while (!sIsMapReady) ;
        return sMapProfile.getDevicesMatchingConnectionStates(
                new int[]{BluetoothProfile.STATE_CONNECTED,
                        BluetoothProfile.STATE_CONNECTING,
                        BluetoothProfile.STATE_DISCONNECTING});
    }

    @Override
    public void shutdown() {
        mService.unregisterReceiver(mNotificationReceiver);
    }

    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("OnReceive" + intent);
            String action = intent.getAction();
            if (action.equals(BluetoothNonpublicApi.ACTION_MESSAGE_RECEIVED)) {
                mEventFacade.postEvent(MAP_EVENT,
                        intent.getStringExtra(android.content.Intent.EXTRA_TEXT));
            } else if (action.equals(BluetoothNonpublicApi.ACTION_MESSAGE_SENT_SUCCESSFULLY)) {
                mEventFacade.postEvent(MAP_SMS_SENT_SUCCESS,
                        intent.getStringExtra(android.content.Intent.EXTRA_TEXT));
            } else if (action.equals(BluetoothNonpublicApi.ACTION_MESSAGE_DELIVERED_SUCCESSFULLY)) {
                mEventFacade.postEvent(MAP_SMS_DELIVER_SUCCESS,
                        intent.getStringExtra(android.content.Intent.EXTRA_TEXT));
            }
        }
    }
}
