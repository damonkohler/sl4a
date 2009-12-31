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

package com.google.ase.interpreter.bsh;

import java.io.File;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;

public class BshInterpreter extends Interpreter {

  @Override
  public String getExtension() {
    return ".bsh";
  }

  @Override
  public String getName() {
    return "bsh";
  }

  @Override
  public String getNiceName() {
    return "BeanShell 2.0b4";
  }

  @Override
  public String getContentTemplate() {
    return "source(\"/sdcard/ase/extras/bsh/android.bsh\");\ndroid = Android();\n";
  }

  @Override
  public InterpreterProcess buildProcess(String scriptName, int port) {
    return new BshInterpreterProcess(scriptName, port);
  }

  @Override
  public boolean hasInterpreterArchive() {
    return false;
  }

  @Override
  public boolean hasInterpreterExtrasArchive() {
    return true;
  }

  @Override
  public boolean hasScriptsArchive() {
    return true;
  }

  @Override
  public File getBinary() {
    return null;
  }

  @Override
  public int getVersion() {
    return 1;
  }

}
