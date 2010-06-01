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

public class RingerModeCondition implements Condition {
  private static final String RINGER_MODE_STATE_EXTRA = "ringer_mode";
  private ConditionListener mConditionListener;
  private final AudioManager mAudioManager;
  private final Context mContext;
  private int mRingerMode;

  /** Our broadcast receiver dealing with changes to the ringer mode. */
  private final BroadcastReceiver mRingerModeBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int newRingerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);
      switch (newRingerMode) {
      case AudioManager.RINGER_MODE_NORMAL:
      case AudioManager.RINGER_MODE_SILENT:
      case AudioManager.RINGER_MODE_VIBRATE:
        if (mRingerMode != newRingerMode) {
          mRingerMode = newRingerMode;
          invokeListener();
        }
      default:
        AseLog.e("Invalid ringer mode.");
      }
    }
  };

  private RingerModeCondition(Context context) {
    mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    mContext = context;
  }

  @Override
  public void addListener(ConditionListener listener) {
    mConditionListener = listener;
  }

  @Override
  public void start() {
    mRingerMode = mAudioManager.getRingerMode();
    invokeListener();
    mContext.registerReceiver(mRingerModeBroadcastReceiver, new IntentFilter(
        AudioManager.RINGER_MODE_CHANGED_ACTION));
  }

  private void invokeListener() {
    if (mConditionListener != null) {
      Bundle state = new Bundle();
      state.putInt(RINGER_MODE_STATE_EXTRA, mRingerMode);
      mConditionListener.run(state);
    }
  }

  @Override
  public void stop() {
    mContext.unregisterReceiver(mRingerModeBroadcastReceiver);
  }
}
