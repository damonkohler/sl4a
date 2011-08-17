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

import android.content.ComponentName;

public interface Constants {

  public static final String ACTION_LAUNCH_FOREGROUND_SCRIPT =
      "com.googlecode.android_scripting.action.LAUNCH_FOREGROUND_SCRIPT";
  public static final String ACTION_LAUNCH_BACKGROUND_SCRIPT =
      "com.googlecode.android_scripting.action.LAUNCH_BACKGROUND_SCRIPT";
  public static final String ACTION_LAUNCH_SCRIPT_FOR_RESULT =
      "com.googlecode.android_scripting.action.ACTION_LAUNCH_SCRIPT_FOR_RESULT";
  public static final String ACTION_LAUNCH_INTERPRETER =
      "com.googlecode.android_scripting.action.LAUNCH_INTERPRETER";
  public static final String ACTION_EDIT_SCRIPT =
      "com.googlecode.android_scripting.action.EDIT_SCRIPT";
  public static final String ACTION_SAVE_SCRIPT =
      "com.googlecode.android_scripting.action.SAVE_SCRIPT";
  public static final String ACTION_SAVE_AND_RUN_SCRIPT =
      "com.googlecode.android_scripting.action.SAVE_AND_RUN_SCRIPT";
  public static final String ACTION_KILL_PROCESS =
      "com.googlecode.android_scripting.action.KILL_PROCESS";
  public static final String ACTION_KILL_ALL = "com.googlecode.android_scripting.action.KILL_ALL";
  public static final String ACTION_SHOW_RUNNING_SCRIPTS =
      "com.googlecode.android_scripting.action.SHOW_RUNNING_SCRIPTS";
  public static final String ACTION_CANCEL_NOTIFICATION =
      "com.googlecode.android_scripting.action.CANCEL_NOTIFICAITON";
  public static final String ACTION_ACTIVITY_RESULT =
      "com.googlecode.android_scripting.action.ACTIVITY_RESULT";
  public static final String ACTION_LAUNCH_SERVER =
      "com.googlecode.android_scripting.action.LAUNCH_SERVER";

  public static final String EXTRA_RESULT = "SCRIPT_RESULT";
  public static final String EXTRA_SCRIPT_PATH =
      "com.googlecode.android_scripting.extra.SCRIPT_PATH";
  public static final String EXTRA_SCRIPT_CONTENT =
      "com.googlecode.android_scripting.extra.SCRIPT_CONTENT";
  public static final String EXTRA_INTERPRETER_NAME =
      "com.googlecode.android_scripting.extra.INTERPRETER_NAME";

  public static final String EXTRA_USE_EXTERNAL_IP =
      "com.googlecode.android_scripting.extra.USE_PUBLIC_IP";
  public static final String EXTRA_USE_SERVICE_PORT =
      "com.googlecode.android_scripting.extra.USE_SERVICE_PORT";
  public static final String EXTRA_SCRIPT_TEXT =
      "com.googlecode.android_scripting.extra.SCRIPT_TEXT";
  public static final String EXTRA_RPC_HELP_TEXT =
      "com.googlecode.android_scripting.extra.RPC_HELP_TEXT";
  public static final String EXTRA_API_PROMPT_RPC_NAME =
      "com.googlecode.android_scripting.extra.API_PROMPT_RPC_NAME";
  public static final String EXTRA_API_PROMPT_VALUES =
      "com.googlecode.android_scripting.extra.API_PROMPT_VALUES";
  public static final String EXTRA_PROXY_PORT = "com.googlecode.android_scripting.extra.PROXY_PORT";
  public static final String EXTRA_PROCESS_ID =
      "com.googlecode.android_scripting.extra.SCRIPT_PROCESS_ID";
  public static final String EXTRA_IS_NEW_SCRIPT =
      "com.googlecode.android_scripting.extra.IS_NEW_SCRIPT";
  public static final String EXTRA_TRIGGER_ID =
      "com.googlecode.android_scripting.extra.EXTRA_TRIGGER_ID";
  public static final String EXTRA_LAUNCH_IN_BACKGROUND =
      "com.googlecode.android_scripting.extra.EXTRA_LAUNCH_IN_BACKGROUND";
  public static final String EXTRA_TASK_ID = "com.googlecode.android_scripting.extra.EXTRA_TASK_ID";

  // BluetoothDeviceManager
  public static final String EXTRA_DEVICE_ADDRESS =
      "com.googlecode.android_scripting.extra.device_address";

  public static final ComponentName SL4A_SERVICE_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.ScriptingLayerService");
  public static final ComponentName SL4A_SERVICE_LAUNCHER_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.ScriptingLayerServiceLauncher");
  public static final ComponentName BLUETOOTH_DEVICE_LIST_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.BluetoothDeviceList");
  public static final ComponentName TRIGGER_SERVICE_COMPONENT_NAME = new ComponentName(
      "com.googlecode.android_scripting",
      "com.googlecode.android_scripting.activity.TriggerService");

  // Preference Keys

  public static final String FORCE_BROWSER = "helpForceBrowser";
  public final static String HIDE_NOTIFY = "hideServiceNotifications";
}