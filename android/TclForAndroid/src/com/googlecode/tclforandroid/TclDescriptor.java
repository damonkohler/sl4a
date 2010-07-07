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

package com.googlecode.tclforandroid;

import com.google.ase.interpreter.AseHostedInterpreter;

public class TclDescriptor extends AseHostedInterpreter {

  private final static String TCL = "tclsh";

  public String getExtension() {
    return ".tcl";
  }

  public String getName() {
    return TCL;
  }

  public String getNiceName() {
    return "Tcl 8.6b2";
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

  public String getBinary() {
    return TCL;
  }

  public int getVersion() {
    return 1;
  }

  public String getEmptyParams() {
    return "";
  }

  public String getExecuteParams() {
    return " %s";
  }

}
