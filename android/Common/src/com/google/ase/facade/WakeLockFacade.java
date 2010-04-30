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

package com.google.ase.facade;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;

/**
 * A facade exposing some of the functionality of the PowerManager, in particular wake locks.
 * 
 * @author Felix Arends (felixarends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class WakeLockFacade implements RpcReceiver {

  private final static String WAKE_LOCK_TAG = "com.google.ase.facade.PowerManagerFacade";

  private enum WakeLockType {
    FULL, PARTIAL, BRIGHT, DIM
  }

  private class WakeLockManager {
    private final PowerManager mmPowerManager;
    private final Map<WakeLockType, WakeLock> mmLocks = new HashMap<WakeLockType, WakeLock>();

    public WakeLockManager(Service service) {
      mmPowerManager = (PowerManager) service.getSystemService(Context.POWER_SERVICE);
      WakeLock full =
          mmPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE,
              WAKE_LOCK_TAG);
      full.setReferenceCounted(false);
      WakeLock partial = mmPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
      partial.setReferenceCounted(false);
      WakeLock bright =
          mmPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
              | PowerManager.ON_AFTER_RELEASE, WAKE_LOCK_TAG);
      bright.setReferenceCounted(false);
      WakeLock dim =
          mmPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK
              | PowerManager.ON_AFTER_RELEASE, WAKE_LOCK_TAG);
      dim.setReferenceCounted(false);
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

  public WakeLockFacade(Service service) {
    mManager = new WakeLockManager(service);
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
