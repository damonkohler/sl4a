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

package com.googlecode.pythonforandroid;

import com.google.ase.interpreter.AseHostedInterpreter;

public class PythonDescriptor extends AseHostedInterpreter {

  private static final String PYTHON_BIN = "bin/python";

  public String getExtension() {
    return ".py";
  }

  public String getName() {
    return "python";
  }

  public String getNiceName() {
    return "Python 2.6.2";
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

  public int getVersion() {
    return 7;
  }

  public String getBinary() {
    return PYTHON_BIN;
  }

  public String getEmptyParams() {
    return "";
  }

  public String getExecuteParams() {
    return " %s";
  }

}
