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
  private ConditionListener mBeginListener;
  private ConditionListener mEndListener;
  private final AudioManager mAudioManager;
  private Context mContext;
  private final Configuration mConfiguration;
  private boolean mInCondition;
  
  /** Our broadcast receiver dealing with changes to the ringer mode. */
  private final BroadcastReceiver ringerModeBroadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      int ringerMode = intent.getIntExtra(AudioManager.EXTRA_RINGER_MODE, -1);

      switch (ringerMode) {
      case AudioManager.RINGER_MODE_NORMAL:
      case AudioManager.RINGER_MODE_SILENT:
      case AudioManager.RINGER_MODE_VIBRATE:
        if (mConfiguration.getMode() == ringerMode && !mInCondition) {
          invokeBegin(null);
          mInCondition = true;
        } else if (mInCondition) {
          mInCondition = false;
          invokeEnd(null);
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
  public void addBeginListener(ConditionListener listener) {
    mBeginListener = listener;
  }

  @Override
  public void addEndListener(ConditionListener listener) {
    mEndListener = listener;
  }

  @Override
  public void start() {
    if (mAudioManager.getRingerMode() == mConfiguration.getMode()) {
      mInCondition = true;
      invokeBegin(null);
    } else {
      mInCondition = false;
      invokeEnd(null);
    }

    mContext.registerReceiver(ringerModeBroadcastReceiver, new IntentFilter(
        AudioManager.RINGER_MODE_CHANGED_ACTION));
  }

  private void invokeBegin(Bundle state) {
    if (mBeginListener != null) {
      mBeginListener.run(state);
    }
  }

  private void invokeEnd(Bundle state) {
    if (mEndListener != null) {
      mEndListener.run(state);
    }
  }

  @Override
  public void stop() {
    mContext.unregisterReceiver(ringerModeBroadcastReceiver);
  }
}
