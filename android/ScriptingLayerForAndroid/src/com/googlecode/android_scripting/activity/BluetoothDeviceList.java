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

package com.googlecode.android_scripting.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.bluetooth.BluetoothDiscoveryHelper;
import com.googlecode.android_scripting.bluetooth.BluetoothDiscoveryHelper.BluetoothDiscoveryListener;

import java.util.ArrayList;
import java.util.List;

public class BluetoothDeviceList extends ListActivity {

  private static class DeviceInfo {
    public final String mmName;
    public final String mmAddress;

    public DeviceInfo(String name, String address) {
      mmName = name;
      mmAddress = address;
    }
  }

  private final DeviceListAdapter mAdapter = new DeviceListAdapter();
  private final BluetoothDiscoveryHelper mBluetoothHelper =
      new BluetoothDiscoveryHelper(this, mAdapter);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Bluetooth Devices", R.layout.bluetooth_device_list);
    setListAdapter(mAdapter);
    Analytics.trackActivity(this);
  }

  @Override
  protected void onStart() {
    super.onStart();
    CustomizeWindow.toggleProgressBarVisibility(this, true);
    mBluetoothHelper.startDiscovery();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mBluetoothHelper.cancel();
  }

  @Override
  protected void onListItemClick(android.widget.ListView l, View v, int position, long id) {
    DeviceInfo device = (DeviceInfo) mAdapter.getItem(position);
    final Intent result = new Intent();
    result.putExtra(Constants.EXTRA_DEVICE_ADDRESS, device.mmAddress);
    setResult(RESULT_OK, result);
    finish();
  };

  private class DeviceListAdapter extends BaseAdapter implements BluetoothDiscoveryListener {
    List<DeviceInfo> mmDeviceList;

    public DeviceListAdapter() {
      mmDeviceList = new ArrayList<DeviceInfo>();
    }

    public void addDevice(String name, String address) {
      mmDeviceList.add(new DeviceInfo(name, address));
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mmDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
      return mmDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
      final DeviceInfo device = mmDeviceList.get(position);
      final TextView view = new TextView(BluetoothDeviceList.this);
      view.setPadding(2, 2, 2, 2);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
      view.setText(device.mmName + " (" + device.mmAddress + ")");
      return view;
    }

    @Override
    public void addBondedDevice(String name, String address) {
      addDevice(name, address);
    }

    @Override
    public void scanDone() {
      CustomizeWindow.toggleProgressBarVisibility(BluetoothDeviceList.this, false);
    }
  }
}
