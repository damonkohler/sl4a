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

package com.googlecode.android_scripting.interpreter;

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

  public static final String SDCARD_SL4A_ROOT = SDCARD_ROOT + "sl4a/";

  public static final String SCRIPTS_ROOT = SDCARD_SL4A_ROOT + "scripts/";

  public static final String SDCARD_SL4A_DOC = SDCARD_SL4A_ROOT + "doc/";

  public static final String SL4A_DALVIK_CACHE_ROOT = "/dalvik-cache/";

  public static final String INTERPRETER_EXTRAS_ROOT = "/extras/";

  // Interpreters discovery mechanism.
  public static final String ACTION_DISCOVER_INTERPRETERS =
      "com.googlecode.android_scripting.DISCOVER_INTERPRETERS";

  // Interpreters broadcasts.
  public static final String ACTION_INTERPRETER_ADDED =
      "com.googlecode.android_scripting.INTERPRETER_ADDED";
  public static final String ACTION_INTERPRETER_REMOVED =
      "com.googlecode.android_scripting.INTERPRETER_REMOVED";

  // Interpreter content provider.
  public static final String PROVIDER_PROPERTIES = "com.googlecode.android_scripting.base";
  public static final String PROVIDER_ENVIRONMENT_VARIABLES =
      "com.googlecode.android_scripting.env";
  public static final String PROVIDER_ARGUMENTS = "com.googlecode.android_scripting.args";

  public static final String INSTALLED_PREFERENCE_KEY = "SL4A.interpreter.installed";

  public static final String MIME = "script/";
}
