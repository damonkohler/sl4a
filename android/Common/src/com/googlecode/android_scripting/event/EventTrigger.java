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

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.trigger.Trigger;

import java.io.File;

/**
 * A {@link EventTrigger} object combines a trigger with a condition. When the condition fires, the
 * trigger is invoked. The {@link EventTrigger} object takes care of proper serialization of the
 * condition configuration.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class EventTrigger extends Trigger {
  private static final long serialVersionUID = 5415193311156216064L;

  private final EventFactory mEventFactory;
  private transient EventListener mEventListener;

  public EventTrigger(File script, EventFactory eventFactory) {
    super(script);
    mEventFactory = eventFactory;
  }

  @Override
  public void install(final Service service) {
    mEventListener = mEventFactory.create(service);
    mEventListener.registerObserver(new EventObserver() {
      @Override
      public void run(Event event) {
        Intent intent = IntentBuilders.buildStartInBackgroundIntent(getScript());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        service.startActivity(intent);
      }
    });
    mEventListener.start();
  }

  @Override
  public void remove() {
    mEventListener.stop();
  }
}
