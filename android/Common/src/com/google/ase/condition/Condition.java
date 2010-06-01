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

import android.os.Bundle;

import com.google.ase.trigger.ConditionListener;

/**
 * This interace describes the capability of a "condition". A {@link Condition} object observes a
 * particular condition (such as whether the ringer mode was changed to silent) and invokes
 * {@link ConditionListener} callbacks when the truth value of the condition changes. A
 * "BeginListener" is a listener that is invoked when the condition starts to hold (e.g. ringer mode
 * change to silent) and an "EndListener" is a listener that is invoked when the condition ceases to
 * hold (e.g. ringer mode leaves silent setting). When the notion of beginning and ending do not
 * make sense, the BeginListener notifies the listeners that the event has happened.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 * 
 */
public abstract class Condition {
  private ConditionListener mConditionListener;

  /** Adds a listener for the event that the condition starts to hold. */
  public void addListener(ConditionListener listener) {
    mConditionListener = listener;
  }

  /** Invokes the listener with the updated state. */
  protected void updateState(Bundle state) {
    if (mConditionListener != null) {
      mConditionListener.run(state);
    }
  }

  /** Starts observing the condition. */
  public abstract void start();

  /** Stops observing the condition. */
  public abstract void stop();
}
