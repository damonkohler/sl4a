/*
 * Copyright (C) 2016 Google Inc.
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
import android.content.Intent;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.event.Event;

import java.io.File;

/**
 * A trigger implementation that launches a given script when the event occurs.
 *
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class ScriptTrigger implements Trigger {
  private static final long serialVersionUID = 1804599219214041409L;
  private final File mScript;
  private final String mEventName;

  public ScriptTrigger(String eventName, File script) {
    mEventName = eventName;
    mScript = script;
  }

  @Override
  public void handleEvent(Event event, Context context) {
    Intent intent = IntentBuilders.buildStartInBackgroundIntent(mScript);
    // This is required since the script is being started from the TriggerService.
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(intent);
  }

  @Override
  public String getEventName() {
    return mEventName;
  }

  public File getScript() {
    return mScript;
  }
}
