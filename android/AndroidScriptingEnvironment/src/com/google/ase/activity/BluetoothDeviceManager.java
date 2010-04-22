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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.bluetooth.BluetoothHelper.DeviceListener;

public class BluetoothDeviceManager extends ListActivity {
  private static class DeviceInfo {
    public final String mName;
    public final String mAddress;
    public DeviceInfo(String name, String address) {
      mName = name;
      mAddress = address;
    }
  }

  private final DeviceListAdapter mDeviceListAdapter = new DeviceListAdapter();

  private class DeviceListAdapter extends BaseAdapter implements DeviceListener {
    List<DeviceInfo> mDeviceList;

    public DeviceListAdapter() {
      mDeviceList = new ArrayList<DeviceInfo>();
    }

    public void addDevice(String name, String address) {
      mDeviceList.add(new DeviceInfo(name, address));
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
      final DeviceInfo device = mDeviceList.get(position);

      final TextView textView = new TextView(BluetoothDeviceManager.this);
      textView.setText(device.mName + " (" + device.mAddress + ")");

      return textView;
    }

    @Override
    public void addBondedDevice(String name, String address) {
      addDevice(name, address);
    }

    @Override
    public void scanDone() {
    }
  }

  private final OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
      DeviceInfo device = (DeviceInfo) mDeviceListAdapter.getItem(position);
      final Intent result = new Intent();
      result.putExtra(Constants.EXTRA_DEVICE_ADDRESS, device.mAddress);
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

    setListAdapter(mDeviceListAdapter);
    getListView().setOnItemClickListener(mOnItemClickListener);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }
}
