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

import android.content.ComponentName;
import android.content.Intent;
import android.os.Parcelable;

import com.google.ase.activity.AseServiceLauncher;

public class IntentBuilders {

  private IntentBuilders() {
    // Utility class.
  }

  /**
   * Builds an intent that will launch a script in the background.
   *
   * @param scriptName
   *          the script to launch
   * @return the intent that will launch the script
   */
  public static Intent buildStartInBackgroundIntent(String scriptName) {
    final ComponentName componentName = AseServiceLauncher.COMPONENT_NAME;
    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.setAction(Constants.ACTION_LAUNCH_SCRIPT);
    intent.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
    return intent;
  }

  /**
   * Builds an intent that launches a script in a terminal.
   *
   * @param scriptName
   *          the script to launch
   * @return the intent that will launch the script
   */
  public static Intent buildStartInTerminalIntent(String scriptName) {
    final ComponentName componentName = AseServiceLauncher.COMPONENT_NAME;
    Intent intent = new Intent();
    intent.setComponent(componentName);
    intent.setAction(Constants.ACTION_LAUNCH_TERMINAL);
    intent.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
    return intent;
  }

  /**
   * Builds an intent that creates a shortcut to launch the provided script in the background.
   *
   * @param scriptName
   *          the script to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildBackgroundShortcutIntent(String scriptName, Parcelable iconResource) {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, buildStartInBackgroundIntent(scriptName));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, scriptName);
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }

  /**
   * Builds an intent that creates a shortcut to launch the provided script in a terminal.
   *
   * @param scriptName
   *          the script to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildTerminalShortcutIntent(String scriptName, Parcelable iconResource) {
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, buildStartInTerminalIntent(scriptName));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, scriptName);
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }
}
