package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.List;

/**
 * Wifi functions.
 * 
 */
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

  private void makeLock(int wifiMode) {
    if (mLock == null) {
      mLock = mWifi.createWifiLock(wifiMode, "sl4a");
      mLock.acquire();
    }
  }

  @Rpc(description = "Returns the list of access points found during the most recent Wifi scan.")
  public List<ScanResult> wifiGetScanResults() {
    return mWifi.getScanResults();
  }

  @Rpc(description = "Acquires a full Wifi lock.")
  public void wifiLockAcquireFull() {
    makeLock(WifiManager.WIFI_MODE_FULL);
  }

  @Rpc(description = "Acquires a scan only Wifi lock.")
  public void wifiLockAcquireScanOnly() {
    makeLock(WifiManager.WIFI_MODE_SCAN_ONLY);
  }

  @Rpc(description = "Releases a previously acquired Wifi lock.")
  public void wifiLockRelease() {
    if (mLock != null) {
      mLock.release();
      mLock = null;
    }
  }

  @Rpc(description = "Starts a scan for Wifi access points.", returns = "True if the scan was initiated successfully.")
  public Boolean wifiStartScan() {
    return mWifi.startScan();
  }

  @Rpc(description = "Checks Wifi state.", returns = "True if Wifi is enabled.")
  public Boolean checkWifiState() {
    return mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
  }

  @Rpc(description = "Toggle Wifi on and off.", returns = "True if Wifi is enabled.")
  public Boolean toggleWifiState(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkWifiState();
    }
    mWifi.setWifiEnabled(enabled);
    return enabled;
  }

  @Rpc(description = "Disconnects from the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiDisconnect() {
    return mWifi.disconnect();
  }

  @Rpc(description = "Returns information about the currently active access point.")
  public WifiInfo wifiGetConnectionInfo() {
    return mWifi.getConnectionInfo();
  }

  @Rpc(description = "Reassociates with the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiReassociate() {
    return mWifi.reassociate();
  }

  @Rpc(description = "Reconnects to the currently active access point.", returns = "True if the operation succeeded.")
  public Boolean wifiReconnect() {
    return mWifi.reconnect();
  }

  @Override
  public void shutdown() {
    wifiLockRelease();
  }
}
