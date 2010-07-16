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

/**
 * A description of the interpreters hosted by the SL4A project.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class Sl4aHostedInterpreter implements InterpreterDescriptor {

  public static final String BASE_INSTALL_URL = "http://android-scripting.googlecode.com/files/";
  public static final String DALVIKVM = "/system/bin/dalvikvm";

  public int getInterpreterVersion() {
    return getVersion();
  }

  public int getExtrasVersion() {
    return getVersion();
  }

  public int getScriptsVersion() {
    return getVersion();
  }

  public String getInterpreterArchiveName() {
    return String.format("%s_r%s.zip", getName(), getInterpreterVersion());
  }

  public String getExtrasArchiveName() {
    return String.format("%s_extras_r%s.zip", getName(), getExtrasVersion());
  }

  public String getScriptsArchiveName() {
    return String.format("%s_scripts_r%s.zip", getName(), getScriptsVersion());
  }

  public String getInterpreterArchiveUrl() {
    return BASE_INSTALL_URL + getInterpreterArchiveName();
  }

  public String getExtrasArchiveUrl() {
    return BASE_INSTALL_URL + getExtrasArchiveName();
  }

  public String getScriptsArchiveUrl() {
    return BASE_INSTALL_URL + getScriptsArchiveName();
  }

  public String getPath(Context context) {
    if (!hasInterpreterArchive() && hasExtrasArchive()) {
      return new File(InterpreterConstants.SDCARD_ROOT + this.getClass().getPackage().getName()
          + InterpreterConstants.INTERPRETER_EXTRAS_ROOT, getName()).getAbsolutePath();
    }
    if (context == null) {
      return null;
    }
    return InterpreterUtils.getInterpreterRoot(context, getName()).getAbsolutePath();
  }

  public String getExecuteCommand(Context context) {
    return String.format("%1$s/%2$s", getPath(context), getBinary());
  }

  public String getEmptyParams(Context context) {
    return null;
  }

  public String getExecuteParams(Context context) {
    return "%s";
  }

  @Override
  public String[] getExecuteArgs(Context context) {
    return null;
  }

}
