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

package com.google.ase.interpreter.tcl;

import java.io.File;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.language.TclLanguage;

/**
 * Represents the Tcl interpreter.
 *
 * @author Pat Thoyts (patthoyts@users.sourceforge.net)
 */
public class TclInterpreter extends Interpreter {

  private final static String TCL_BINARY = "/data/data/com.google.ase/tclsh/tclsh";
  
  public TclInterpreter() {
    super(new TclLanguage());
  }

  @Override
  public String getExtension() {
    return ".tcl";
  }

  @Override
  public String getName() {
    return "tclsh";
  }

  @Override
  public InterpreterProcess buildProcess(String scriptName, int port) {
    return new TclInterpreterProcess(scriptName, port);
  }

  @Override
  public String getNiceName() {
    return "Tcl 8.6b2";
  }

  @Override
  public boolean hasInterpreterArchive() {
    return true;
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
    return new File(TCL_BINARY);
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
