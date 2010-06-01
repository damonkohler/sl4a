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

package com.google.ase.trigger;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.ase.IntentBuilders;
import com.google.ase.condition.Condition;
import com.google.ase.condition.ConditionFactory;

/**
 * A {@link ConditionTrigger} object combines a trigger with a condition. When the condition fires,
 * the trigger is invoked. The {@link ConditionTrigger} object takes care of proper serialization of
 * the condition configuration.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class ConditionTrigger extends Trigger {
  private static final long serialVersionUID = 5415193311156216064L;
  private static final String EXTRA_CONDITION_STATE = "condition_state";

  private final ConditionFactory mConditionFactory;
  private transient Condition mCondition;

  public ConditionTrigger(String scriptName, TriggerRepository.IdProvider idProvider,
      Service service, ConditionFactory conditionFactory) {
    super(scriptName, idProvider);
    mConditionFactory = conditionFactory;
    initializeTransients(service);
  }

  @Override
  public void initializeTransients(final Context context) {
    mCondition = mConditionFactory.create(context);
    mCondition.addListener(new ConditionListener() {
      @Override
      public void run(Bundle state) {
        Intent intent = IntentBuilders.buildStartInBackgroundIntent(getScriptName());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_CONDITION_STATE, state);
        context.startActivity(intent);
      }
    });
  }

  @Override
  public void install() {
    mCondition.start();
  }

  @Override
  public void remove() {
    mCondition.stop();
  }
}
