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

package com.google.ase.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.ase.R;

public class BluetoothDeviceManager extends ListActivity {

  private BluetoothAdapter mBluetoothAdapter;
  private final DeviceListAdapter mDeviceListAdapter = new DeviceListAdapter();

  public static String EXTRA_DEVICE_ADDRESS = "device_address";

  private class DeviceListAdapter extends BaseAdapter {
    List<BluetoothDevice> mDeviceList;

    public DeviceListAdapter() {
      mDeviceList = new ArrayList<BluetoothDevice>();
    }

    public void addDevice(BluetoothDevice device) {
      mDeviceList.add(device);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
      return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
      final BluetoothDevice device = mDeviceList.get(position);

      final TextView textView = new TextView(BluetoothDeviceManager.this);
      textView.setText(device.getName() + " (" + device.getAddress() + ")");

      return textView;
    }

  }

  private final BroadcastReceiver mBluetoothReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();

      if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        // Get the BluetoothDevice object from the Intent
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // If it's already paired, skip it, because it's been listed already
        if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
          onDiscover(device);
        }
      } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
        onScanFinished();
      }
    }
  };

  private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      BluetoothDevice device = (BluetoothDevice) mDeviceListAdapter.getItem(position);
      final Intent result = new Intent();
      result.putExtra(EXTRA_DEVICE_ADDRESS, device.getAddress());
      setResult(RESULT_OK, result);
      finish();
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    CustomizeWindow
            .requestCustomTitle(this, "Bluetooth Devices", R.layout.bluetooth_device_manager);

    setResult(RESULT_CANCELED);

    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    if (mBluetoothAdapter.isDiscovering()) {
      mBluetoothAdapter.cancelDiscovery();
    }

    Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
    for (BluetoothDevice device : pairedDevices) {
      mDeviceListAdapter.addDevice(device);
    }

    final IntentFilter deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    registerReceiver(mBluetoothReceiver, deviceFoundFilter);

    final IntentFilter discoveryFinishedFilter =
            new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    this.registerReceiver(mBluetoothReceiver, discoveryFinishedFilter);

    if (!mBluetoothAdapter.isEnabled()) {
      mBluetoothAdapter.enable();
    }

    setListAdapter(mDeviceListAdapter);
    getListView().setOnItemClickListener(mOnItemClickListener);

    mBluetoothAdapter.startDiscovery();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mBluetoothAdapter != null) {
      mBluetoothAdapter.cancelDiscovery();
    }

    unregisterReceiver(mBluetoothReceiver);
  }

  private void onDiscover(BluetoothDevice device) {
    mDeviceListAdapter.addDevice(device);
  }

  private void onScanFinished() {

  }
}
