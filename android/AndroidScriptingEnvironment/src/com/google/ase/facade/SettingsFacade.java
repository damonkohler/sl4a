/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.facade;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcOptionalBoolean;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * Exposes phone settings functionality.
 * 
 * @author Frank Spychalski (frank.spychalski@gmail.com)
 */
public class SettingsFacade implements RpcReceiver {

  public static int AIRPLANE_MODE_OFF = 0;
  public static int AIRPLANE_MODE_ON = 1;

  private final Service mService;
  private final AudioManager mAudio;
  private final WifiManager mWifi;

  /**
   * Creates a new SettingsFacade.
   * 
   * @param service
   *          is the {@link Context} the APIs will run under
   */
  public SettingsFacade(Service service) {
    mService = service;
    mWifi = (WifiManager) mService.getSystemService(Context.WIFI_SERVICE);
    mAudio = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
  }

  @Rpc(description = "Set the screen timeout to this number of seconds.", returns = "The original screen timeout.")
  public Integer setScreenTimeout(@RpcParameter(name = "value") Integer value) {
    Integer old_value = getScreenTimeout();
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.SCREEN_OFF_TIMEOUT, value * 1000);
    return old_value;
  }

  @Rpc(description = "Returns the current screen timeout in seconds.", returns = "the current screen timeout in seconds.")
  public Integer getScreenTimeout() {
    try {
      return android.provider.Settings.System.getInt(mService.getContentResolver(),
          android.provider.Settings.System.SCREEN_OFF_TIMEOUT) / 1000;
    } catch (SettingNotFoundException e) {
      return 0;
    }
  }

  @Rpc(description = "Checks the airplane mode setting.", returns = "True if airplane mode is enabled.")
  public Boolean checkAirplaneMode() {
    try {
      return android.provider.Settings.System.getInt(mService.getContentResolver(),
          android.provider.Settings.System.AIRPLANE_MODE_ON) == AIRPLANE_MODE_ON;
    } catch (SettingNotFoundException e) {
      return false;
    }
  }

  @Rpc(description = "Toggle airplane mode on and off.", returns = "True if airplane mode is enabled.")
  public Boolean toggleAirplaneMode(@RpcOptionalBoolean(name = "enabled") Boolean enabled) {
    boolean set_airplane_mode = enabled == null ? !checkAirplaneMode() : enabled;
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.AIRPLANE_MODE_ON, set_airplane_mode ? AIRPLANE_MODE_ON
            : AIRPLANE_MODE_OFF);
    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    intent.putExtra("state", set_airplane_mode);
    mService.sendBroadcast(intent);
    return set_airplane_mode;
  }

  @Rpc(description = "Checks the ringer silent mode setting.", returns = "True if ringer silent mode is enabled.")
  public Boolean checkRingerSilentMode() {
    return mAudio.getRingerMode() == AudioManager.RINGER_MODE_SILENT;
  }

  @Rpc(description = "Toggle ringer silent mode on and off.", returns = "True if ringer silent mode is enabled.")
  public Boolean toggleRingerSilentMode(@RpcOptionalBoolean(name = "enabled") Boolean enabled) {
    if (enabled == null) {
      enabled = checkRingerSilentMode();
    }
    mAudio.setRingerMode(enabled ? AudioManager.RINGER_MODE_SILENT
        : AudioManager.RINGER_MODE_NORMAL);
    return enabled;
  }

  @Rpc(description = "Returns the current ringer volume.", returns = "The current volume as an integer.")
  public int getRingerVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Sets the ringer volume.")
  public void setRingerVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  @Rpc(description = "Checks Wifi state.", returns = "True if Wifi is enabled.")
  public Boolean checkWifiState() {
    return mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLED
        || mWifi.getWifiState() == WifiManager.WIFI_STATE_ENABLING;
  }

  @Rpc(description = "Toggle Wifi on and off.", returns = "True if Wifi is enabled.")
  public Boolean toggleWifiState(@RpcOptionalBoolean(name = "enabled") Boolean enabled) {
    if (enabled == null) {
      enabled = checkWifiState();
    }
    mWifi.setWifiEnabled(enabled);
    return enabled;
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }
}
