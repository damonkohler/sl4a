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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.PowerManager;
import android.provider.Settings.SettingNotFoundException;
import android.view.WindowManager;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.lang.reflect.Method;

/**
 * Exposes phone settings functionality.
 * 
 * @author Frank Spychalski (frank.spychalski@gmail.com)
 */
public class SettingsFacade extends RpcReceiver {

  public static int AIRPLANE_MODE_OFF = 0;
  public static int AIRPLANE_MODE_ON = 1;

  private final Service mService;
  private final AudioManager mAudio;
  private final PowerManager mPower;

  /**
   * Creates a new SettingsFacade.
   * 
   * @param service
   *          is the {@link Context} the APIs will run under
   */
  public SettingsFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mAudio = (AudioManager) mService.getSystemService(Context.AUDIO_SERVICE);
    mPower = (PowerManager) mService.getSystemService(Context.POWER_SERVICE);
  }

  @Rpc(description = "Sets the screen timeout to this number of seconds.", returns = "The original screen timeout.")
  public Integer setScreenTimeout(@RpcParameter(name = "value") Integer value) {
    Integer oldValue = getScreenTimeout();
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.SCREEN_OFF_TIMEOUT, value * 1000);
    return oldValue;
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

  @Rpc(description = "Toggles airplane mode on and off.", returns = "True if airplane mode is enabled.")
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

  @Rpc(description = "Toggles ringer silent mode on and off.", returns = "True if ringer silent mode is enabled.")
  public Boolean toggleRingerSilentMode(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled) {
    if (enabled == null) {
      enabled = !checkRingerSilentMode();
    }
    mAudio.setRingerMode(enabled ? AudioManager.RINGER_MODE_SILENT
        : AudioManager.RINGER_MODE_NORMAL);
    return enabled;
  }

  @Rpc(description = "Toggles vibrate mode on and off. If ringer=true then set Ringer setting, else set Notification setting", returns = "True if vibrate mode is enabled.")
  public Boolean toggleVibrateMode(@RpcParameter(name = "enabled") @RpcOptional Boolean enabled,
      @RpcParameter(name = "ringer") @RpcOptional Boolean ringer) {
    int atype = ringer ? AudioManager.VIBRATE_TYPE_RINGER : AudioManager.VIBRATE_TYPE_NOTIFICATION;
    int asetting = enabled ? AudioManager.VIBRATE_SETTING_ON : AudioManager.VIBRATE_SETTING_OFF;
    mAudio.setVibrateSetting(atype, asetting);
    return enabled;
  }

  @Rpc(description = "Checks Vibration setting. If ringer=true then query Ringer setting, else query Notification setting", returns = "True if vibrate mode is enabled.")
  public Boolean getVibrateMode(@RpcParameter(name = "ringer") @RpcOptional Boolean ringer) {
    int atype = ringer ? AudioManager.VIBRATE_TYPE_RINGER : AudioManager.VIBRATE_TYPE_NOTIFICATION;
    return mAudio.shouldVibrate(atype);
  }

  @Rpc(description = "Returns the maximum ringer volume.")
  public int getMaxRingerVolume() {
    return mAudio.getStreamMaxVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Returns the current ringer volume.")
  public int getRingerVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Sets the ringer volume.")
  public void setRingerVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  @Rpc(description = "Returns the maximum media volume.")
  public int getMaxMediaVolume() {
    return mAudio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
  }

  @Rpc(description = "Returns the current media volume.")
  public int getMediaVolume() {
    return mAudio.getStreamVolume(AudioManager.STREAM_MUSIC);
  }

  @Rpc(description = "Sets the media volume.")
  public void setMediaVolume(@RpcParameter(name = "volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
  }

  @Rpc(description = "Returns the screen backlight brightness.", returns = "the current screen brightness between 0 and 255")
  public Integer getScreenBrightness() {
    try {
      return android.provider.Settings.System.getInt(mService.getContentResolver(),
          android.provider.Settings.System.SCREEN_BRIGHTNESS);
    } catch (SettingNotFoundException e) {
      return 0;
    }
  }

  @Rpc(description = "Sets the the screen backlight brightness.", returns = "the original screen brightness.")
  public Integer setScreenBrightness(
      @RpcParameter(name = "value", description = "brightness value between 0 and 255") Integer value) {
    if (value < 0) {
      value = 0;
    } else if (value > 255) {
      value = 255;
    }
    final int brightness = value;
    Integer oldValue = getScreenBrightness();
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.SCREEN_BRIGHTNESS, brightness);

    FutureActivityTask<Object> task = new FutureActivityTask<Object>() {
      @Override
      public void onCreate() {
        super.onCreate();
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.screenBrightness = brightness * 1.0f / 255;
        getActivity().getWindow().setAttributes(lp);
        setResult(null);
        finish();
      }
    };

    FutureActivityTaskExecutor taskExecutor =
        ((BaseApplication) mService.getApplication()).getTaskExecutor();
    taskExecutor.execute(task);

    return oldValue;
  }

  @Rpc(description = "Checks if the screen is on or off (requires API level 7).", returns = "True if the screen is currently on.")
  public Boolean checkScreenOn() throws Exception {
    Class<?> powerManagerClass = mPower.getClass();
    Boolean result = null;
    try {
      Method isScreenOn = powerManagerClass.getMethod("isScreenOn");
      result = (Boolean) isScreenOn.invoke(mPower);
    } catch (Exception e) {
      Log.e(e);
      throw new UnsupportedOperationException("This feature is only available after Eclair.");
    }
    return result;
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }
}
