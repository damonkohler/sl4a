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

package com.google.ase.interpreter;

import android.os.Environment;

/**
 * A collection of constants required for installation/removal of an interpreter.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterConstants {

  public static final String SDCARD_ROOT =
      Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
  public static final String DOWNLOAD_ROOT = SDCARD_ROOT;

  public static final String SDCARD_ASE_ROOT = SDCARD_ROOT + "ase/";

  public static final String ASE_DALVIK_CACHE_ROOT = SDCARD_ASE_ROOT + "dalvik-cache/";

  public static final String INTERPRETER_EXTRAS_ROOT = SDCARD_ASE_ROOT + "extras/";

  public static final String SCRIPTS_ROOT = SDCARD_ASE_ROOT + "scripts/";

  // Interpreters discovery mechanism
  public static final String ACTION_DISCOVER_INTERPRETERS = "com.google.ase.DISCOVER_INTERPRETERS";
  // Interpreters broadcasts
  public static final String ACTION_INTERPRETER_ADDED = "com.google.ase.INTERPRETER_ADDED";
  public static final String ACTION_INTERPRETER_REMOVED = "com.google.ase.INTERPRETER_REMOVED";
  // Interpreter content provider
  public static final String PROVIDER_BASE = "com.google.ase.base";
  public static final String PROVIDER_ENV = "com.google.ase.env";

  public static final String INSTALL_PREF = "ASE.interpreter.installed";

}
