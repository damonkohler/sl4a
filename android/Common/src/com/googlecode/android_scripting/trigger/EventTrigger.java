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

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.condition.Event;
import com.googlecode.android_scripting.condition.EventFactory;

/**
 * A {@link EventTrigger} object combines a trigger with a condition. When the condition fires, the
 * trigger is invoked. The {@link EventTrigger} object takes care of proper serialization of the
 * condition configuration.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class EventTrigger extends Trigger {
  private static final long serialVersionUID = 5415193311156216064L;
  private static final String EXTRA_CONDITION_STATE = "condition_state";

  private final EventFactory mConditionFactory;
  private transient Event mCondition;

  public EventTrigger(String scriptName, EventFactory conditionFactory) {
    super(scriptName);
    mConditionFactory = conditionFactory;
  }

  @Override
  public void install(final Service service) {
    mCondition = mConditionFactory.create(service);
    mCondition.addListener(new EventListener() {
      @Override
      public void run(Bundle state) {
        Intent intent = IntentBuilders.buildStartInBackgroundIntent(getScriptName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CONDITION_STATE, state);
        service.startActivity(intent);
      }
    });
    mCondition.start();
  }

  @Override
  public void remove() {
    mCondition.stop();
  }
}
