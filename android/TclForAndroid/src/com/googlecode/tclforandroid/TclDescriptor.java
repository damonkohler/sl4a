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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;
import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class TclDescriptor extends Sl4aHostedInterpreter {

  private static final String TCL = "tclsh";
  private static final String ENV_HOME = "TCL_LIBRARY";
  private static final String ENV_LIB = "TCLLIBPATH";
  private static final String ENV_SCRIPTS = "TCL_SCRIPTS";
  private static final String ENV_TEMP = "TEMP";
  private static final String ENV_HOME_GLOBAL = "HOME";

  public String getExtension() {
    return ".tcl";
  }

  public String getName() {
    return TCL;
  }

  public String getNiceName() {
    return "Tcl 8.6b2";
  }

  public boolean hasInterpreterArchive() {
    return true;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  @Override
  public File getBinary(Context context) {
    return new File(getExtrasPath(context), TCL);
  }

  public int getVersion() {
    return 1;
  }

  private String getExtrasRoot() {
    return InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName()
        + InterpreterConstants.INTERPRETER_EXTRAS_ROOT;
  }

  private String getHome(Context context) {
    File file = InterpreterUtils.getInterpreterRoot(context, getName());
    return file.getAbsolutePath();
  }

  private String getExtras() {
    File file = new File(getExtrasRoot(), getName());
    return file.getAbsolutePath();
  }

  private String getTemp() {
    File tmp = new File(getExtrasRoot(), getName() + "/tmp");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    return tmp.getAbsolutePath();
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context context) {
    Map<String, String> settings = new HashMap<String, String>();
    settings.put(ENV_HOME, getHome(context));
    settings.put(ENV_LIB, getExtras());
    settings.put(ENV_TEMP, getTemp());
    settings.put(ENV_SCRIPTS, InterpreterConstants.SCRIPTS_ROOT);
    settings.put(ENV_HOME_GLOBAL, getClass().getPackage().getName());
    return settings;
  }
}
