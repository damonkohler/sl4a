/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.service;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.AssetFileDescriptor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.activity.ScriptingLayerService;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;

import org.connectbot.transport.ProcessTransport;
import org.connectbot.util.PreferenceConstants;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manager for SSH connections that runs as a background service. This service holds a list of
 * currently connected SSH bridges that are ready for connection up to a GUI if needed.
 * 
 * @author jsharkey
 * @author modified by raaar
 */
public class TerminalManager implements OnSharedPreferenceChangeListener {

  private static final long VIBRATE_DURATION = 30;

  private final List<TerminalBridge> bridges = new CopyOnWriteArrayList<TerminalBridge>();

  private final Map<Integer, WeakReference<TerminalBridge>> mHostBridgeMap =
      new ConcurrentHashMap<Integer, WeakReference<TerminalBridge>>();

  private Handler mDisconnectHandler = null;

  private final Resources mResources;

  private final SharedPreferences mPreferences;

  private boolean hardKeyboardHidden;

  private Vibrator vibrator;
  private boolean wantKeyVibration;
  private boolean wantBellVibration;
  private boolean wantAudible;
  private boolean resizeAllowed = false;
  private MediaPlayer mediaPlayer;

  private final ScriptingLayerService mService;

  public TerminalManager(ScriptingLayerService service) {
    mService = service;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(mService);
    registerOnSharedPreferenceChangeListener(this);
    mResources = mService.getResources();
    hardKeyboardHidden =
        (mResources.getConfiguration().hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES);
    vibrator = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
    wantKeyVibration = mPreferences.getBoolean(PreferenceConstants.BUMPY_ARROWS, true);
    wantBellVibration = mPreferences.getBoolean(PreferenceConstants.BELL_VIBRATE, true);
    wantAudible = mPreferences.getBoolean(PreferenceConstants.BELL, true);
    if (wantAudible) {
      enableMediaPlayer();
    }
  }

  /**
   * Disconnect all currently connected bridges.
   */
  private void disconnectAll() {
    TerminalBridge[] bridgesArray = null;
    if (bridges.size() > 0) {
      bridgesArray = bridges.toArray(new TerminalBridge[bridges.size()]);
    }
    if (bridgesArray != null) {
      // disconnect and dispose of any existing bridges
      for (TerminalBridge bridge : bridgesArray) {
        bridge.dispatchDisconnect(true);
      }
    }
  }

  /**
   * Open a new session using the given parameters.
   * 
   * @throws InterruptedException
   * @throws Sl4aException
   */
  public TerminalBridge openConnection(int id) throws IllegalArgumentException, IOException,
      InterruptedException, Sl4aException {
    // throw exception if terminal already open
    if (getConnectedBridge(id) != null) {
      throw new IllegalArgumentException("Connection already open");
    }

    InterpreterProcess process = mService.getProcess(id);

    TerminalBridge bridge = new TerminalBridge(this, process, new ProcessTransport(process));
    bridge.connect();

    WeakReference<TerminalBridge> wr = new WeakReference<TerminalBridge>(bridge);
    bridges.add(bridge);
    mHostBridgeMap.put(id, wr);

    return bridge;
  }

  /**
   * Find a connected {@link TerminalBridge} with the given HostBean.
   * 
   * @param id
   *          the HostBean to search for
   * @return TerminalBridge that uses the HostBean
   */
  public TerminalBridge getConnectedBridge(int id) {
    WeakReference<TerminalBridge> wr = mHostBridgeMap.get(id);
    if (wr != null) {
      return wr.get();
    } else {
      return null;
    }
  }

  /**
   * Called by child bridge when somehow it's been disconnected.
   */
  public void closeConnection(TerminalBridge bridge, boolean killProcess) {
    if (killProcess) {
      bridges.remove(bridge);
      mHostBridgeMap.remove(bridge.getId());
      if (mService.getProcess(bridge.getId()).isAlive()) {
        Intent intent = new Intent(mService, mService.getClass());
        intent.setAction(Constants.ACTION_KILL_PROCESS);
        intent.putExtra(Constants.EXTRA_PROXY_PORT, bridge.getId());
        mService.startService(intent);
      }
    }
    if (mDisconnectHandler != null) {
      Message.obtain(mDisconnectHandler, -1, bridge).sendToTarget();
    }
  }

  /**
   * Allow {@link TerminalBridge} to resize when the parent has changed.
   * 
   * @param resizeAllowed
   */
  public void setResizeAllowed(boolean resizeAllowed) {
    this.resizeAllowed = resizeAllowed;
  }

  public boolean isResizeAllowed() {
    return resizeAllowed;
  }

  public void stop() {
    resizeAllowed = false;
    disconnectAll();
    disableMediaPlayer();
  }

  public int getIntParameter(String key, int defValue) {
    return mPreferences.getInt(key, defValue);
  }

  public String getStringParameter(String key, String defValue) {
    return mPreferences.getString(key, defValue);
  }

  public void tryKeyVibrate() {
    if (wantKeyVibration) {
      vibrate();
    }
  }

  private void vibrate() {
    if (vibrator != null) {
      vibrator.vibrate(VIBRATE_DURATION);
    }
  }

  private void enableMediaPlayer() {
    mediaPlayer = new MediaPlayer();

    float volume =
        mPreferences.getFloat(PreferenceConstants.BELL_VOLUME,
            PreferenceConstants.DEFAULT_BELL_VOLUME);

    mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
    mediaPlayer.setOnCompletionListener(new BeepListener());

    AssetFileDescriptor file = mResources.openRawResourceFd(R.raw.bell);
    try {
      mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
      file.close();
      mediaPlayer.setVolume(volume, volume);
      mediaPlayer.prepare();
    } catch (IOException e) {
      Log.e("Error setting up bell media player", e);
    }
  }

  private void disableMediaPlayer() {
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

  public void playBeep() {
    if (mediaPlayer != null) {
      mediaPlayer.start();
    }
    if (wantBellVibration) {
      vibrate();
    }
  }

  private static class BeepListener implements OnCompletionListener {
    public void onCompletion(MediaPlayer mp) {
      mp.seekTo(0);
    }
  }

  public boolean isHardKeyboardHidden() {
    return hardKeyboardHidden;
  }

  public void setHardKeyboardHidden(boolean b) {
    hardKeyboardHidden = b;
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    if (PreferenceConstants.BELL.equals(key)) {
      wantAudible = sharedPreferences.getBoolean(PreferenceConstants.BELL, true);
      if (wantAudible && mediaPlayer == null) {
        enableMediaPlayer();
      } else if (!wantAudible && mediaPlayer != null) {
        disableMediaPlayer();
      }
    } else if (PreferenceConstants.BELL_VOLUME.equals(key)) {
      if (mediaPlayer != null) {
        float volume =
            sharedPreferences.getFloat(PreferenceConstants.BELL_VOLUME,
                PreferenceConstants.DEFAULT_BELL_VOLUME);
        mediaPlayer.setVolume(volume, volume);
      }
    } else if (PreferenceConstants.BELL_VIBRATE.equals(key)) {
      wantBellVibration = sharedPreferences.getBoolean(PreferenceConstants.BELL_VIBRATE, true);
    } else if (PreferenceConstants.BUMPY_ARROWS.equals(key)) {
      wantKeyVibration = sharedPreferences.getBoolean(PreferenceConstants.BUMPY_ARROWS, true);
    }
  }

  public void setDisconnectHandler(Handler disconnectHandler) {
    mDisconnectHandler = disconnectHandler;
  }

  public List<TerminalBridge> getBridgeList() {
    return bridges;
  }

  public Resources getResources() {
    return mResources;
  }

  public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    mPreferences.registerOnSharedPreferenceChangeListener(listener);
  }

}
