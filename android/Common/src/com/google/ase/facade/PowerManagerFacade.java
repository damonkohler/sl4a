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

import android.app.Service;
import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;

/**
 * A facade exposing some of the functionality of the PowerManager, in
 * particular wake locks.
 * 
 * @author Felix Arends (felixarends@gmail.com)
 * 
 */
public class PowerManagerFacade implements RpcReceiver {
  private final static String WAKE_LOCK_TAG = "com.google.ase.facade.PowerManagerFacade";
  private final PowerManager mPowerManager;
  private WakeLock mWakeLock = null;

  PowerManagerFacade(Service service) {
    mPowerManager = (PowerManager) service
        .getSystemService(Context.POWER_SERVICE);
  }

  @Rpc(description = "Acquires a full wake lock. "
      + "Releases previously held lock.")
  public void acquireFullWakeLock() {
    final WakeLock oldLock = mWakeLock;
    mWakeLock = mPowerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK
        | PowerManager.ON_AFTER_RELEASE, WAKE_LOCK_TAG);
    mWakeLock.acquire();
    maybeRelease(oldLock);
  }

  @Rpc(description = "Acquires the CPU wake lock. "
      + "Releases previously held lock.")
  public void acquirePartialWakeLock() {
    final WakeLock oldLock = mWakeLock;
    mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
        WAKE_LOCK_TAG);
    mWakeLock.acquire();
    maybeRelease(oldLock);
  }

  @Rpc(description = "Acquires a wake lock that keeps the screen bright. "
      + "Releases previously held lock.")
  public void acquireBrightWakeLock() {
    final WakeLock oldLock = mWakeLock;
    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
        WAKE_LOCK_TAG);
    mWakeLock.acquire();
    maybeRelease(oldLock);
  }

  @Rpc(description = "Acquires a wake lock that keeps the screen dimmed. "
      + "Releases previously held lock.")
  public void acquireDimWakeLock() {
    final WakeLock oldLock = mWakeLock;
    mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
        WAKE_LOCK_TAG);
    mWakeLock.acquire();
    maybeRelease(oldLock);
  }

  @Rpc(description = "Releases the most recently obtained wake lock.")
  public void releaseWakeLock() {
    maybeRelease(mWakeLock);
    mWakeLock = null;
  }

  @Override
  public void shutdown() {
    releaseWakeLock();
  }

  /** Releases lock, if it's not null */
  private void maybeRelease(WakeLock lock) {
    if (lock != null) {
      lock.release();
    }
  }
}
