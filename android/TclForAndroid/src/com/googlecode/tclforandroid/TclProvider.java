/*
 * Copyright (C) 2009 Google Inc.
 * Copyright (C) 2010 Pat Thoyts
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

package com.googlecode.tclforandroid;


import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TclProvider extends InterpreterProvider {

  private final static String ENV_HOME = "TCL_LIBRARY";
  private final static String ENV_LIB = "TCLLIBPATH";
  private final static String ENV_SCRIPTS = "TCL_SCRIPTS";
  private final static String ENV_TEMP = "TEMP";
  private final static String ENV_HOME_GLOBAL = "HOME";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new TclDescriptor();
  }

  private String getHome() {
    File parent = mContext.getFilesDir().getParentFile();
    File file = new File(parent, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getExtras() {
    File file = new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getTemp() {
    File tmp =
        new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName() + "/tmp");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    return tmp.getAbsolutePath();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(5);
    settings.put(ENV_HOME, getHome());
    settings.put(ENV_LIB, getExtras());
    settings.put(ENV_TEMP, getTemp());
    settings.put(ENV_SCRIPTS, InterpreterConstants.SCRIPTS_ROOT);
    settings.put(ENV_HOME_GLOBAL, InterpreterConstants.SDCARD_SL4A_ROOT);
    return settings;
  }

}
