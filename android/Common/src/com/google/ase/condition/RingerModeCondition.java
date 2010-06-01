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
package com.google.ase.condition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.trigger.ConditionListener;

/**
 * This condition invokes a trigger whenever the ringer mode changes. The "ringer_mode" element in
 * the "state" map of the extras passed to the trigger script contains the new ringer mode.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class RingerModeCondition implements Condition {
  private static final String RINGER_MODE_STATE_EXTRA = "ringer_mode";
  private ConditionListener mConditionListener;
  private final AudioManager mAudioManager;
  private Context mContext;
  private final Configuration mConfiguration;
  private int mPreviousRingerMode;
  
  /** Our broadcast receiver dealing with changes to the ringer mode. */
  private final BroadcastReceiver ringerModeBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);

      switch (ringerMode) {
      case AudioManager.RINGER_MODE_NORMAL:
      case AudioManager.RINGER_MODE_SILENT:
      case AudioManager.RINGER_MODE_VIBRATE:
        if (mPreviousRingerMode != ringerMode) {
          mPreviousRingerMode = ringerMode;
          invokeListener();
        } else {
          ringerMode = mConfiguration.getMode();
        }
      default:
        AseLog.e("Invalid ringer mode.");
      }
    }
  };

  /** Configuration of the RingerModeCondition.  Stores the mode on which to trigger. */
  public static class Configuration implements ConditionConfiguration {
    int mmMode;

    /**
     * @param mode
     *          the mode on which to trigger: see {@link AudioManager#RINGER_MODE_NORMAL} etc.
     */
    public Configuration(int mode) {
      mmMode = mode;
    }

    public int getMode() {
      return mmMode;
    }

    @Override
    public Condition getCondition(Context context) {
      return new RingerModeCondition(context, this);
    }
  }

  private RingerModeCondition(Context context, Configuration configuration) {
    mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    mContext = context;
    mConfiguration = configuration;
  }

  @Override
  public void addListener(ConditionListener listener) {
    mConditionListener = listener;
  }

  @Override
  public void start() {
    mPreviousRingerMode = mAudioManager.getRingerMode();
    invokeListener();
    mContext.registerReceiver(ringerModeBroadcastReceiver, new IntentFilter(
        AudioManager.RINGER_MODE_CHANGED_ACTION));
  }

  private void invokeListener() {
    if (mConditionListener != null) {
      Bundle state = new Bundle();
      state.putInt(RINGER_MODE_STATE_EXTRA, mPreviousRingerMode);
      mConditionListener.run(state);
    }
  }

  @Override
  public void stop() {
    mContext.unregisterReceiver(ringerModeBroadcastReceiver);
  }
}
