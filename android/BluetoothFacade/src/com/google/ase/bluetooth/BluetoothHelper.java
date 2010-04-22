package com.google.ase.bluetooth;

import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothHelper {
  private BluetoothHelper() {
  }

  public static interface DeviceListener {
    public void addBondedDevice(String name, String address);

    public void addDevice(String name, String address);

    public void scanDone();
  }

  public static void findDevices(final Context context, final DeviceListener listener) {
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();

        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
          // Get the BluetoothDevice object from the Intent
          BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

          // If it's already paired, skip it, because it's been listed already
          if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
            listener.addDevice(device.getName(), device.getAddress());
          }
        } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
          listener.scanDone();
          context.unregisterReceiver(this);
        }
      }
    };

    if (bluetoothAdapter.isDiscovering()) {
      bluetoothAdapter.cancelDiscovery();
    }

    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    for (BluetoothDevice device : pairedDevices) {
      listener.addBondedDevice(device.getName(), device.getAddress());
    }

    final IntentFilter deviceFoundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    context.registerReceiver(bluetoothReceiver, deviceFoundFilter);

    final IntentFilter discoveryFinishedFilter =
        new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    context.registerReceiver(bluetoothReceiver, discoveryFinishedFilter);

    if (!bluetoothAdapter.isEnabled()) {
      bluetoothAdapter.enable();
    }

    bluetoothAdapter.startDiscovery();
  }
}
