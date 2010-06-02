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
import android.os.Environment;

public class Constants {

  private Constants() {
    // Utitlity class.
  }

  public static final String ACTION_LAUNCH_SCRIPT = "com.google.ase.action.LAUNCH_SCRIPT";
  public static final String ACTION_LAUNCH_TERMINAL = "com.google.ase.action.LAUNCH_TERMINAL";
  public static final String ACTION_EDIT_SCRIPT = "com.google.ase.action.EDIT_SCRIPT";
  public static final String ACTION_SAVE_SCRIPT = "com.google.ase.action.SAVE_SCRIPT";
  public static final String ACTION_SAVE_AND_RUN_SCRIPT =
      "com.google.ase.action.SAVE_AND_RUN_SCRIPT";
  public static final String ACTION_KILL_SERVICE = "com.google.ase.action.KILL_SERVICE";
  public static final String ACTION_CANCEL_NOTIFICATION =
      "com.google.ase.action.CANCEL_NOTIFICAITON";
  public static final String ACTION_ACTIVITY_RESULT = "com.google.ase.action.ACTIVITY_RESULT";
  public static final String ACTION_LAUNCH_SERVER = "com.google.ase.action.LAUNCH_SERVER";
  public static final String ACTION_DISCOVER_INTERPRETERS = "com.google.ase.DISCOVER_INTERPRETERS";

  public static final String EXTRA_SCRIPT_NAME = "com.google.ase.extra.SCRIPT_NAME";
  public static final String EXTRA_SCRIPT_CONTENT = "com.google.ase.extra.SCRIPT_CONTENT";
  public static final String EXTRA_INTERPRETER_NAME = "com.google.ase.extra.INTERPRETER_NAME";
  public static final String EXTRA_INPUT_PATH = "com.google.ase.extra.INPUT_PATH";
  public static final String EXTRA_OUTPUT_PATH = "com.google.ase.extra.OUTPUT_PATH";
  public static final String EXTRA_URL = "com.google.ase.extra.URL";
  public static final String EXTRA_USE_EXTERNAL_IP = "com.google.ase.extra.USE_PUBLIC_IP";
  public static final String EXTRA_SCRIPT_TEXT = "com.google.ase.extra.SCRIPT_TEXT";
  public static final String EXTRA_RPC_HELP_TEXT = "com.google.ase.extra.RPC_HELP_TEXT";
  public static final String EXTRA_API_PROMPT_RPC_NAME = "com.google.ase.extra.API_PROMPT_RPC_NAME";
  public static final String EXTRA_API_PROMPT_VALUES = "com.google.ase.extra.API_PROMPT_VALUES";
  public static final String EXTRA_PROXY_PORT = "com.google.ase.extra.PROXY_PORT";
  public static final String EXTRA_IS_NEW_SCRIPT = "com.google.ase.extra.IS_NEW_SCRIPT";
  public static final String EXTRA_TRIGGER_ID = "com.google.ase.extra.EXTRA_TRIGGER_ID";
  public static final String EXTRA_LAUNCH_IN_BACKGROUND =
      "com.google.ase.extra.EXTRA_LAUNCH_IN_BACKGROUND";

  // BluetoothDeviceManager
  public static final String EXTRA_DEVICE_ADDRESS = "com.google.ase.extra.device_address";

  public static final String SDCARD_ROOT =
      Environment.getExternalStorageDirectory().getAbsolutePath();
  public static final String DOWNLOAD_ROOT = SDCARD_ROOT;
  public static final String SDCARD_ASE_ROOT = SDCARD_ROOT + "ase/";
  public static final String ASE_DALVIK_CACHE_ROOT = SDCARD_ASE_ROOT + "dalvik-cache/";
  public static final String SCRIPTS_ROOT = SDCARD_ASE_ROOT + "scripts/";
  public static final String INTERPRETER_EXTRAS_ROOT = SDCARD_ASE_ROOT + "extras/";
  public static final String BASE_INSTALL_URL = "http://android-scripting.googlecode.com/files/";

  public static final ComponentName ASE_SERVICE_COMPONENT_NAME =
      new ComponentName("com.google.ase", "com.google.ase.activity.AseService");
  public static final ComponentName ASE_SERVICE_LAUNCHER_COMPONENT_NAME =
      new ComponentName("com.google.ase", "com.google.ase.activity.AseServiceLauncher");
  public static final ComponentName BLUETOOTH_DEVICE_LIST_COMPONENT_NAME =
      new ComponentName("com.google.ase", "com.google.ase.activity.BluetoothDeviceList");
  public static final ComponentName TRIGGER_SERVICE_COMPONENT_NAME =
      new ComponentName("com.google.ase", "com.google.ase.activity.TriggerService");

}