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
import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.UUID;

/**
 * The definition of the interface implemented by triggers. A trigger combines a script name with
 * the description of an event that causes the trigger to fire.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public abstract class Trigger implements Serializable {

  private static final long serialVersionUID = 5190219422732210378L;

  private final File mScript;
  private final UUID mId;

  /**
   * This is set to false in the constructor: this way we know that we are being deserialized if the
   * value is still true.
   */
  private transient boolean mIsDeserializing = true;

  public Trigger(File script) {
    mScript = script;
    mId = UUID.randomUUID();
    mIsDeserializing = false;
  }

  /** Invoked just after the trigger is invoked */
  public void afterTrigger(Service service) {
  }

  /** Invoked before the trigger is invoked */
  public void beforeTrigger(Service service) {
  }

  /** Returns the name and path of the script to execute */
  public final File getScript() {
    return mScript;
  }

  /** Returns the name of the script to execute */
  public final String getScriptName() {
    return mScript.getName();
  }

  /** Returns this trigger's id. */
  public final UUID getId() {
    return mId;
  }

  /**
   * Installs the trigger.
   * 
   * @param service
   *          the {@link Service} owning the trigger
   */
  public abstract void install(Service service);

  /** Removes the trigger. This does not remove the trigger from the repository. */
  public abstract void remove();

  /**
   * Retruns true iff this object is being deserialized. This method can be used by trigger
   * implementations to figure out whether or not this is the first time that the install method is
   * called. For alarms, e.g., this is useful, because they persist across multiple instantiations
   * of the {@link TriggerService} service.
   */
  protected boolean isDeserializing() {
    return mIsDeserializing;
  }

  /** Creates a view to display this trigger in the trigger manager. */
  public View getView(Context context) {
    // TODO(damonkohler): Change this to use XML layout.
    TextView view = new TextView(context);
    view.setPadding(4, 4, 4, 4);
    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
    view.setText(getScriptName());
    return view;
  }
}
