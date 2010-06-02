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

import java.io.Serializable;
import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * The definition of the interface implemented by triggers. A trigger combines a script name with
 * the description of an event that causes the trigger to fire.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public abstract class Trigger implements Serializable {
  private static final long serialVersionUID = 5190219422732210378L;
  private final String mScriptName;
  private final UUID mId;

  public Trigger(String scriptName) {
    mScriptName = scriptName;
    mId = UUID.randomUUID();
  }

  /** Invoked just after the trigger is invoked */
  public void afterTrigger(Service service) {
  }

  /** Invoked before the trigger is invoked */
  public void beforeTrigger(Service service) {
  }

  /** Returns the name of the script to execute */
  public final String getScriptName() {
    return mScriptName;
  }

  /** Returns this trigger's id. */
  public final UUID getId() {
    return mId;
  }

  /** 
   * Installs the trigger.
   * 
   * @param service the {@link Service} owning the trigger
   */
  public abstract void install(Service service);

  /** Removes the trigger. This does not remove the trigger from the repository. */
  public abstract void remove();
  
  /** Initializes the transient variables of the trigger. */
  public abstract void initializeTransients(final Context context);

  /** Creates a view to display this trigger in the trigger manager. */
  public View getView(Context context) {
    TextView view = new TextView(context);
    view.setPadding(2, 2, 2, 2);
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
    view.setText(getScriptName());
    return view;
  }
}
