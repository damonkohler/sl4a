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

import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;

public class TclInterpreterProcess extends InterpreterProcess {

  private final static String TCL_HOME = "/data/data/com.google.ase/files/tclsh";
  private final static String TCL_EXTRAS = Constants.SDCARD_ASE_ROOT + "extras/tcl/";

  public TclInterpreterProcess(String launchScript, int port) {
    super(launchScript, port);
  }

  @Override
  protected void buildEnvironment() {
    mEnvironment.put("TCL_LIBRARY", TCL_HOME);
    mEnvironment.put("TCLLIBPATH", TCL_EXTRAS);
    mEnvironment.put("TCL_SCRIPTS", Constants.SCRIPTS_ROOT);
    mEnvironment.put("HOME", Constants.SDCARD_ROOT);
    File tmp = new File(TCL_EXTRAS + "tmp/");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    mEnvironment.put("TEMP", tmp.getAbsolutePath());
  }

  @Override
  protected void writeInterpreterCommand() {
    TclInterpreter interpreter = new TclInterpreter();
    print(interpreter.getBinary().getAbsolutePath());
    if (mLaunchScript != null) {
      print(" " + mLaunchScript);
    }
    print("\n");
  }
}
