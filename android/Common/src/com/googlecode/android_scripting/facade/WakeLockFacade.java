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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A facade exposing some of the functionality of the PowerManager, in particular wake locks.
 * 
 * @author Felix Arends (felixarends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class WakeLockFacade extends RpcReceiver {

  private final static String WAKE_LOCK_TAG =
      "com.googlecode.android_scripting.facade.PowerManagerFacade";

  private enum WakeLockType {
    FULL, PARTIAL, BRIGHT, DIM
  }

  private class WakeLockManager {
    private final PowerManager mmPowerManager;
    private final Map<WakeLockType, WakeLock> mmLocks = new HashMap<WakeLockType, WakeLock>();

    public WakeLockManager(Service service) {
      mmPowerManager = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
      addWakeLock(WakeLockType.PARTIAL, PowerManager.PARTIAL_WAKE_LOCK);
      addWakeLock(WakeLockType.FULL, PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE);
      addWakeLock(WakeLockType.BRIGHT, PowerManager.SCREEN_BRIGHT_WAKE_LOCK
          | PowerManager.ON_AFTER_RELEASE);
      addWakeLock(WakeLockType.DIM, PowerManager.SCREEN_DIM_WAKE_LOCK
          | PowerManager.ON_AFTER_RELEASE);
    }

    private void addWakeLock(WakeLockType type, int flags) {
      WakeLock full = mmPowerManager.newWakeLock(flags, WAKE_LOCK_TAG);
      full.setReferenceCounted(false);
      mmLocks.put(type, full);
    }

    public void acquire(WakeLockType type) {
      mmLocks.get(type).acquire();
      for (Entry<WakeLockType, WakeLock> entry : mmLocks.entrySet()) {
        if (entry.getKey() != type) {
          entry.getValue().release();
        }
      }
    }

    public void release() {
      for (Entry<WakeLockType, WakeLock> entry : mmLocks.entrySet()) {
        entry.getValue().release();
      }
    }
  }

  private final WakeLockManager mManager;

  public WakeLockFacade(FacadeManager manager) {
    super(manager);
    mManager = new WakeLockManager(manager.getService());
  }

  @Rpc(description = "Acquires a full wake lock (CPU on, screen bright, keyboard bright).")
  public void wakeLockAcquireFull() {
    mManager.acquire(WakeLockType.FULL);
  }

  @Rpc(description = "Acquires a partial wake lock (CPU on).")
  public void wakeLockAcquirePartial() {
    mManager.acquire(WakeLockType.PARTIAL);
  }

  @Rpc(description = "Acquires a bright wake lock (CPU on, screen bright).")
  public void wakeLockAcquireBright() {
    mManager.acquire(WakeLockType.BRIGHT);
  }

  @Rpc(description = "Acquires a dim wake lock (CPU on, screen dim).")
  public void wakeLockAcquireDim() {
    mManager.acquire(WakeLockType.DIM);
  }

  @Rpc(description = "Releases the wake lock.")
  public void wakeLockRelease() {
    mManager.release();
  }

  @Override
  public void shutdown() {
    wakeLockRelease();
  }
}
