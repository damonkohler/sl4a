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

package com.google.ase.interpreter.perl;

import java.io.File;

import com.google.ase.RpcFacade;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;

/**
 * Represents the Perl interpreter.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class PerlInterpreter extends Interpreter {

  private final static String PERL_BIN = "/data/data/com.google.ase/perl/perl";

  @Override
  public String getExtension() {
    return ".pl";
  }

  @Override
  public String getName() {
    return "perl";
  }

  @Override
  public InterpreterProcess buildProcess(String scriptName, RpcFacade... facades) {
    return new PerlInterpreterProcess(scriptName, facades);
  }

  @Override
  public String getNiceName() {
    return "Perl 5.10.0";
  }

  @Override
  public String getContentTemplate() {
    return "use Android;\n\nmy $droid = Android->new();";
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
    return new File(PERL_BIN);
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
