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
import android.provider.Settings.SettingNotFoundException;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

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

  /**
   * Creates a new SettingsFacade.
   * 
   * @param service
   *          is the {@link Context} the APIs will run under
   */
  public SettingsFacade(Service service) {
    mService = service;
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
  public Boolean toggleAirplaneMode(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkAirplaneMode();
    }
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.AIRPLANE_MODE_ON, enabled ? AIRPLANE_MODE_ON
            : AIRPLANE_MODE_OFF);
    Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    intent.putExtra("state", enabled);
    mService.sendBroadcast(intent);
    return enabled;
  }

  @Rpc(description = "Checks the ringer silent mode setting.", returns = "True if ringer silent mode is enabled.")
  public Boolean checkRingerSilentMode() {
    return mAudio.getRingerMode() == AudioManager.RINGER_MODE_SILENT;
  }

  @Rpc(description = "Toggle ringer silent mode on and off.", returns = "True if ringer silent mode is enabled.")
  public Boolean toggleRingerSilentMode(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkRingerSilentMode();
    }
    mAudio.setRingerMode(enabled ? AudioManager.RINGER_MODE_SILENT
        : AudioManager.RINGER_MODE_NORMAL);
    return enabled;
  }

  @Rpc(description = "Returns the current ringer volume.")
  public int getRingerVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Sets the ringer volume.")
  public void setRingerVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  @Rpc(description = "Returns the current media volume.")
  public int getMediaVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
  }

  @Rpc(description = "Sets the media volume.")
  public void setMediaVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }
}
