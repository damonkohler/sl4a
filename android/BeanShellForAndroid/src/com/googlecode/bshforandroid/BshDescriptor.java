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

package com.googlecode.bshforandroid;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class BshDescriptor extends Sl4aHostedInterpreter {

  private final static String BSH_BIN = "bsh-2.0b4-dx.jar";

  public String getExtension() {
    return ".bsh";
  }

  public String getName() {
    return "bsh";
  }

  public String getNiceName() {
    return "BeanShell 2.0b4";
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  public int getVersion() {
    return 2;
  }

  @Override
  public int getScriptsVersion() {
    return 3;
  }

  public String getBinary() {
    return BSH_BIN;
  }

  @Override
  public String getExecuteCommand(Context context) {
    return DALVIKVM;
  }

  @Override
  public String[] getExecuteArgs(Context context) {
    String[] args =
        { "-Xbootclasspath:/system/framework/core.jar", "-classpath",
          super.getExecuteCommand(context), "bsh.Interpreter" };
    return args;
  }

}
