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

import android.content.Context;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A description of the interpreters hosted by the SL4A project.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class Sl4aHostedInterpreter implements InterpreterDescriptor {

  public static final String BASE_INSTALL_URL = "http://android-scripting.googlecode.com/files/";
  public static final String DALVIKVM = "/system/bin/dalvikvm";

  // TODO(damonkohler): Remove getVersion() and pull these three version methods up in to the base
  // class.

  public String getBaseInstallUrl() {
    return BASE_INSTALL_URL;
  }

  public int getInterpreterVersion() {
    return getVersion();
  }

  public int getExtrasVersion() {
    return getVersion();
  }

  public int getScriptsVersion() {
    return getVersion();
  }

  @Override
  public String getInterpreterArchiveName() {
    return String.format("%s_r%s.zip", getName(), getInterpreterVersion());
  }

  @Override
  public String getExtrasArchiveName() {
    return String.format("%s_extras_r%s.zip", getName(), getExtrasVersion());
  }

  @Override
  public String getScriptsArchiveName() {
    return String.format("%s_scripts_r%s.zip", getName(), getScriptsVersion());
  }

  @Override
  public String getInterpreterArchiveUrl() {
    return getBaseInstallUrl() + getInterpreterArchiveName();
  }

  @Override
  public String getExtrasArchiveUrl() {
    return getBaseInstallUrl() + getExtrasArchiveName();
  }

  @Override
  public String getScriptsArchiveUrl() {
    return getBaseInstallUrl() + getScriptsArchiveName();
  }

  @Override
  public String getInteractiveCommand(Context context) {
    return "";
  }

  @Override
  public boolean hasInteractiveMode() {
    return true;
  }

  @Override
  public String getScriptCommand(Context context) {
    return "%s";
  }

  @Override
  public List<String> getArguments(Context context) {
    return new ArrayList<String>();
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context context) {
    return new HashMap<String, String>();
  }

  // TODO(damonkohler): This shouldn't be public.
  public File getExtrasPath(Context context) {
    if (!hasInterpreterArchive() && hasExtrasArchive()) {
      return new File(InterpreterConstants.SDCARD_ROOT + this.getClass().getPackage().getName()
          + InterpreterConstants.INTERPRETER_EXTRAS_ROOT, getName());
    }
    return InterpreterUtils.getInterpreterRoot(context, getName());
  }
}
