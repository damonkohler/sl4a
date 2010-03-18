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
import android.media.AudioManager;
import android.net.wifi.WifiManager;
import android.provider.Settings.SettingNotFoundException;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultBoolean;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

/**
 * Exposes device settings related functionality.
 */
public class SettingsFacade implements RpcReceiver {

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

  @Rpc(description = "Returns the current screen timeout in seconds.")
  public Integer getScreenTimeout() {
    try {
      return android.provider.Settings.System.getInt(mService.getContentResolver(),
          android.provider.Settings.System.SCREEN_OFF_TIMEOUT) / 1000;
    } catch (SettingNotFoundException e) {
      return 0;
    }
  }

  @Rpc(description = "Returns the current ringer volume.", returns = "The current volume as an integer.")
  public int getRingerVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Sets whether or not the ringer should be silent.")
  public void setRingerSilent(
      @RpcDefaultBoolean(name = "silent", defaultValue = true) Boolean silent) {
    if (silent) {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    } else {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
  }

  @Rpc(description = "Sets the ringer volume.")
  public void setRingerVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  @Rpc(description = "Enables or disables Wifi according to the supplied boolean.")
  public void setWifiEnabled(
      @RpcDefaultBoolean(name = "enabled", defaultValue = true) Boolean enabled) {
    mWifi.setWifiEnabled(enabled);
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }
}
