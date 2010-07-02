package com.google.ase.facade;

import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

import java.util.List;

public class WifiFacade extends RpcReceiver {

  private final Service mService;
  private final WifiManager mWifi;

  public WifiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mWifi = (WifiManager) mService.getSystemService(Context.WIFI_SERVICE);
  }

  @Rpc(description = "Returns the list of access points found during the most recent Wifi scan.")
  public List<ScanResult> wifiGetScanResults() {
    return mWifi.getScanResults();
  }

  @Rpc(description = "Starts a scan for Wifi access points.", returns = "True if the scan was initiated successfully.")
  public Boolean wifiStartScan() {
    return mWifi.startScan();
  }

  @Rpc(description = "Checks Wifi state.", returns = "True if Wifi is enabled.")
  public Boolean checkWifiState() {
    return mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED
        || mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
  }

  @Rpc(description = "Toggle Wifi on and off.", returns = "True if Wifi is enabled.")
  public Boolean toggleWifiState(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkWifiState();
    }
    mWifi.setWifiEnabled(enabled);
    return enabled;
  }

  @Override
  public void shutdown() {
  }
}
