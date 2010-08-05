package com.googlecode.android_scripting.facade;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

public class WifiFacade extends RpcReceiver {

  private final Service mService;
  private final WifiManager mWifi;
  private WifiLock mLock;

  public WifiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mWifi = (WifiManager) mService.getSystemService(Context.WIFI_SERVICE);
    mLock = null;
  }

  @Rpc(description = "Returns the list of access points found during the most recent Wifi scan.")
  public List<ScanResult> wifiGetScanResults() {
    return mWifi.getScanResults();
  }

  @Rpc(description = "Acquires a full Wifi lock.")
  public void wifiLockAcquireFull() {
    if (mLock == null) {
      mLock = mWifi.createWifiLock(WifiManager.WIFI_MODE_FULL, "sl4a");
    }
  }

  @Rpc(description = "Acquires a scan only Wifi lock.")
  public void wifiLockAcquireScanOnly() {
    if (mLock == null) {
      mLock = mWifi.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, "sl4a");
    }
  }

  @Rpc(description = "Releases a previously acquired Wifi lock.")
  public void wifiLockRelease() {
    if (mLock != null) {
      mLock.release();
    }
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
    wifiLockRelease();
  }
}
