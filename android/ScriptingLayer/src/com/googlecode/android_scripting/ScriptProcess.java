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

package com.googlecode.android_scripting;

import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;

import java.io.File;

public class ScriptProcess extends InterpreterProcess {

  private final File mScript;

  public ScriptProcess(File script, InterpreterConfiguration configuration, AndroidProxy proxy) {
    super(configuration.getInterpreterForScript(script.getName()), proxy);
    mScript = script;
    String scriptName = script.getName();
    setName(scriptName);
    Interpreter interpreter = configuration.getInterpreterForScript(scriptName);
    setCommand(String.format(interpreter.getScriptCommand(), script.getAbsolutePath()));
  }

  public String getPath() {
    return mScript.getPath();
  }

}
