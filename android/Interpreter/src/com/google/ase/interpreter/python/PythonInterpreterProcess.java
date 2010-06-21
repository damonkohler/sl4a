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

package com.google.ase.interpreter.python;

import java.io.File;

import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;

public class PythonInterpreterProcess extends InterpreterProcess {

  public final static String PYTHON_HOME = "/data/data/com.google.ase/python";
  public final static String PYTHON_EXTRAS = Constants.INTERPRETER_EXTRAS_ROOT + "python";

  public PythonInterpreterProcess(String launchScript, int port) {
    super(launchScript, port);
  }

  @Override
  protected void buildEnvironment() {
    mEnvironment.put("PYTHONHOME", PYTHON_HOME);
    mEnvironment.put("PYTHONPATH", PYTHON_EXTRAS + ":" + Constants.SCRIPTS_ROOT);
    File tmp = new File(PYTHON_EXTRAS + "tmp/");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    mEnvironment.put("TEMP", tmp.getAbsolutePath());
  }

  @Override
  protected String getInterpreterCommand() {
    PythonInterpreter interpreter = new PythonInterpreter();
    String str = interpreter.getBinary()+"%s";
    return String.format(str, (mLaunchScript == null)?"":" "+mLaunchScript);
  }
}
