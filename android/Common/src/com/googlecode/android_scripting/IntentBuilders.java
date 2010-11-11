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

package com.googlecode.android_scripting;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.googlecode.android_scripting.interpreter.Interpreter;

import java.io.File;

public class IntentBuilders {
  /** An arbitrary value that is used to identify pending intents for executing scripts. */
  private static final int EXECUTE_SCRIPT_REQUEST_CODE = 0x12f412a;

  private IntentBuilders() {
    // Utility class.
  }

  public static Intent buildTriggerServiceIntent() {
    Intent intent = new Intent();
    intent.setComponent(Constants.TRIGGER_SERVICE_COMPONENT_NAME);
    return intent;
  }

  /**
   * Builds an intent that will launch a script in the background.
   * 
   * @param script
   *          the script to launch
   * @return the intent that will launch the script
   */
  public static Intent buildStartInBackgroundIntent(File script) {
    final ComponentName componentName = Constants.SL4A_SERVICE_LAUNCHER_COMPONENT_NAME;
    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.setAction(Constants.ACTION_LAUNCH_BACKGROUND_SCRIPT);
    intent.putExtra(Constants.EXTRA_SCRIPT_PATH, script.getAbsolutePath());
    return intent;
  }

  /**
   * Builds an intent that launches a script in a terminal.
   * 
   * @param script
   *          the script to launch
   * @return the intent that will launch the script
   */
  public static Intent buildStartInTerminalIntent(File script) {
    final ComponentName componentName = Constants.SL4A_SERVICE_LAUNCHER_COMPONENT_NAME;
    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.setAction(Constants.ACTION_LAUNCH_FOREGROUND_SCRIPT);
    intent.putExtra(Constants.EXTRA_SCRIPT_PATH, script.getAbsolutePath());
    return intent;
  }

  /**
   * Builds an intent that launches an interpreter.
   * 
   * @param interpreterName
   *          the interpreter to launch
   * @return the intent that will launch the interpreter
   */
  public static Intent buildStartInterpreterIntent(String interpreterName) {
    final ComponentName componentName = Constants.SL4A_SERVICE_LAUNCHER_COMPONENT_NAME;
    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.setAction(Constants.ACTION_LAUNCH_INTERPRETER);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, interpreterName);
    return intent;
  }

  /**
   * Builds an intent that creates a shortcut to launch the provided interpreter.
   * 
   * @param interpreter
   *          the interpreter to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildInterpreterShortcutIntent(Interpreter interpreter,
      Parcelable iconResource) {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT,
        buildStartInterpreterIntent(interpreter.getName()));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, interpreter.getNiceName());
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }

  /**
   * Builds an intent that creates a shortcut to launch the provided script in the background.
   * 
   * @param script
   *          the script to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildBackgroundShortcutIntent(File script, Parcelable iconResource) {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, buildStartInBackgroundIntent(script));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, script.getName());
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }

  /**
   * Builds an intent that creates a shortcut to launch the provided script in a terminal.
   * 
   * @param script
   *          the script to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildTerminalShortcutIntent(File script, Parcelable iconResource) {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, buildStartInTerminalIntent(script));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, script.getName());
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }

  /**
   * Creates a pending intent that can be used to start the trigger service.
   * 
   * @param context
   *          the context under whose authority to launch the intent
   * 
   * @return {@link PendingIntent} object for running the trigger service
   */
  public static PendingIntent buildTriggerServicePendingIntent(Context context) {
    final Intent intent = buildTriggerServiceIntent();
    return PendingIntent.getService(context, EXECUTE_SCRIPT_REQUEST_CODE, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
  }
}
