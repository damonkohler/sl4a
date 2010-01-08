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

package com.google.ase;

import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.content.Intent;

/**
 * {@link ActivityLauncher} objects are responsible for running an activity and blocking until the
 * result is available. They rely on the underlying parent activity for calling the {@code
 * onActivityResult} method of the launcher object. The calling code can supply a request code that
 * the particular activity launcher uses to identify the results that are returned by a running
 * activity.
 * 
 * If concurrent threads launch activities using the same request code as this launcher, then the
 * behavior is not well-defined. In other words: MAKE SURE THE REQUEST CODE IS UNIQUE with respect
 * to the parent activity.
 * 
 * This class is intended to be thread-safe.
 * 
 * @author Felix Arendsd (felix.arends@gmail.com)
 * 
 */
public class ActivityLauncher {
  /** The request code used to identify activities launched by this launcher. */
  private final int requestCode;

  /** Reference to the parent activity. Used to spawn the new activity. */
  private final Activity mParentActivity;

  /**
   * A countdown latch that is non-null while waiting for the result of an ongoing activity. When
   * this is non-null, some thread is inside of {@code getActivityResult} and therefore holds this
   * object's lock. The {@code getActivityResult} method is the only code writing this variable. The
   * countDown function is called only from within {@code onActivityResult}.
   */
  private CountDownLatch mActivityResultLatch = null;
  private Intent mActivityData = null;
  private int mActivityResultCode = 0;

  public ActivityLauncher(final Activity parentActivity, final int requestCode) {
    this.mParentActivity = parentActivity;
    this.requestCode = requestCode;
  }

  /**
   * Launches an activity and blocks until the result is available.
   * 
   * @param intent the intent to launch
   * @return the {@link Intent} object containing the result of the launched
   *         activity.
   */
  public synchronized Intent getActivityResult(final Intent intent) {
    // The countdown latch that we use to communicate activity completion is a final local variable
    // so that we can be sure that the reference is correct.
    final CountDownLatch latch = new CountDownLatch(1);

    mActivityResultLatch = latch;

    // Tell the parent activity to launch our new activity.
    mParentActivity.startActivityForResult(intent, requestCode);

    try {
      // onActivityResult counts down this latch as soon as the data has become available.
      latch.await();

      if (mActivityResultCode != Activity.RESULT_OK) {
        throw new RuntimeException("Activity Result: " + mActivityResultCode);
      }

      return mActivityData;
    } catch (InterruptedException e) {
      // We don't really know how to respond to this, so we just interrupt the thread again.
      Thread.currentThread().interrupt();
      return null;
    } finally {
      // Make sure that the latch is really set to null: in case onActivityResult wasn't called yet,
      // but this thread gets interrupted.
      synchronized(mActivityResultLatch) {
        mActivityResultLatch = null;
        mActivityData = null;
        mActivityResultCode = 0;
      }
    }
  }

  /**
   * Processes an incoming activity result. This method must be invoked by the parent activity to
   * notify the launcher of incoming activity results.
   * 
   * Parameters: see {@link Activity.onActivityResult}.
   */
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == this.requestCode) {
      synchronized (mActivityResultLatch) {
        // The lock mActivityResultLatch prevents write after read race conditions (countdown could
        // be invoked on a null-object, and even worse getActivityResult could be seeing
        // inconsistent data inside mActivityResultCode and mActivityData.
        if (mActivityResultLatch != null) {
          mActivityData = data;
          mActivityResultCode = resultCode;
          mActivityResultLatch.countDown();
        }
      }
    }
  }
}
