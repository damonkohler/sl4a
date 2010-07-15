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
package com.googlecode.android_scripting.condition;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;


import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.trigger.EventListener;

/**
 * This condition invokes a trigger whenever the ringer mode changes. The "ringer_mode" element in
 * the "state" map of the extras passed to the trigger script contains the new ringer mode.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class RingerModeEvent implements Event {
  private static final String RINGER_MODE_STATE_EXTRA = "ringer_mode";

  private final AudioManager mAudioManager;
  private final Context mContext;

  private EventListener mConditionListener;
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
        Log.e("Invalid ringer mode: " + newRingerMode);
      }
    }
  };

  private RingerModeEvent(Context context) {
    mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    mContext = context;
  }

  @Override
  public void addListener(EventListener listener) {
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
    try {
      mContext.unregisterReceiver(mRingerModeBroadcastReceiver);
    } catch (IllegalArgumentException e) {
      // This occurs when the receiver wasn't even registered.
      // We just ignore this. This occurs, for example, when
      // the TriggerService is not running while stop() is being
      // called.
    }
  }

  public static class Factory implements EventFactory {
    private static final long serialVersionUID = 7593570695879937214L;

    @Override
    public Event create(Context context) {
      return new RingerModeEvent(context);
    }
  }
}
