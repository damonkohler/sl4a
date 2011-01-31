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

package com.googlecode.android_scripting.trigger;

import android.content.Context;

import com.googlecode.android_scripting.event.Event;

import java.io.Serializable;

/**
 * Interface implemented by objects listening to events on the event queue inside of the
 * {@link SerivceManager}.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public interface Trigger extends Serializable {
  /**
   * Handles an event from the event queue.
   * 
   * @param event
   *          Event to handle
   * @param context
   *          TODO
   */
  void handleEvent(Event event, Context context);

  /**
   * Returns the event name that this {@link Trigger} is interested in.
   */
  // TODO(damonkohler): This could be removed by maintaining a reverse mapping from Trigger to event
  // name in the TriggerRespository.
  String getEventName();
}
