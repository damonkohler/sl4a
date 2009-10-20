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

package com.google.ase.interpreter.sh;

import java.io.File;

import com.google.ase.AndroidFacade;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;

/**
 * Represents the shell.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ShInterpreter extends Interpreter {

  @Override
  public String getExtension() {
    return ".sh";
  }

  @Override
  public String getName() {
    return "sh";
  }

  @Override
  public InterpreterProcess buildProcess(AndroidFacade facade, String scriptName) {
    return new ShInterpreterProcess(facade, scriptName);
  }

  @Override
  public String getNiceName() {
    return "Shell";
  }

  @Override
  public boolean hasInterpreterArchive() {
    return false;
  }

  @Override
  public boolean hasInterpreterExtrasArchive() {
    return false;
  }

  @Override
  public boolean hasScriptsArchive() {
    return false;
  }

  @Override
  public File getBinary() {
    return null;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public boolean isUninstallable() {
    return false;
  }
}
