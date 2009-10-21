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

public class Constants {
  private Constants() {
    // Utitlity class.
  }

  public static final String ACTION_LAUNCH_SCRIPT = "com.google.ase.action.LAUNCH_SCRIPT";
  public static final String ACTION_EDIT_SCRIPT = "com.google.ase.action.EDIT_SCRIPT";
  public static final String ACTION_SAVE_SCRIPT = "com.google.ase.action.SAVE_SCRIPT";
  public static final String ACTION_SAVE_AND_RUN_SCRIPT =
      "com.google.ase.action.SAVE_AND_RUN_SCRIPT";
  public static final String ACTION_KILL_SERVICE = "com.google.ase.action.KILL_SERVICE";
  public static final String ACTION_CANCEL_NOTIFICATION =
      "com.google.ase.action.CANCEL_NOTIFICAITON";

  public static final String EXTRA_SCRIPT_NAME = "com.google.ase.extra.SCRIPT_NAME";
  public static final String EXTRA_SCRIPT_CONTENT = "com.google.ase.extra.SCRIPT_CONTENT";
  public static final String EXTRA_INTERPRETER_NAME = "com.google.ase.extra.INTERPRETER_NAME";
  public static final String EXTRA_INPUT_PATH = "com.google.ase.extra.INPUT_PATH";
  public static final String EXTRA_OUTPUT_PATH = "com.google.ase.extra.OUTPUT_PATH";
  public static final String EXTRA_URL = "com.google.ase.extra.URL";
  public static final String EXTRA_USE_PUBLIC_IP = "com.google.ase.extra.USE_PUBLIC_IP";

  public static final String SDCARD_ROOT = "/sdcard/";
  public static final String DOWNLOAD_ROOT = SDCARD_ROOT;
  public static final String SDCARD_ASE_ROOT = SDCARD_ROOT + "ase/";
  public static final String ASE_DALVIK_CACHE_ROOT = SDCARD_ASE_ROOT + "dalvik-cache/";
  public static final String SCRIPTS_ROOT = SDCARD_ASE_ROOT + "scripts/";
  public static final String INTERPRETER_EXTRAS_ROOT = SDCARD_ASE_ROOT + "extras/";
  public static final String INTERPRETER_ROOT = "/data/data/com.google.ase/";
  public static final String BASE_INSTALL_URL = "http://android-scripting.googlecode.com/files/";
}