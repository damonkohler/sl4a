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

package com.googlecode.android_scripting.event;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.googlecode.android_scripting.Log;

/**
 * This {@link EventListener} tracks ringer mode changes.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class RingerModeEventListener implements EventListener {

  private final AudioManager mAudioManager;
  private final Context mContext;

  private EventObserver mEventObserver;
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
        break;
      default:
        Log.e("Invalid ringer mode: " + newRingerMode);
      }
    }
  };

  private RingerModeEventListener(Context context) {
    mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    mContext = context;
  }

  @Override
  public void registerObserver(EventObserver listener) {
    mEventObserver = listener;
  }

  @Override
  public void start() {
    mRingerMode = mAudioManager.getRingerMode();
    invokeListener();
    mContext.registerReceiver(mRingerModeBroadcastReceiver, new IntentFilter(
        AudioManager.RINGER_MODE_CHANGED_ACTION));
  }

  private void invokeListener() {
    if (mEventObserver != null) {
      mEventObserver.run(new Event("ringer_mode", mRingerMode));
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
    public EventListener create(Context context) {
      return new RingerModeEventListener(context);
    }
  }
}
