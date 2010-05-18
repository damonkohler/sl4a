/*
 * Copyright 2009 Brice Lambson
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.interpreter.rhino;

import java.io.File;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.language.JavaScriptLanguage;

public class RhinoInterpreter extends Interpreter {

  public RhinoInterpreter() {
    super(new JavaScriptLanguage());
  }
  
	@Override
	public String getExtension() {
		return ".js";
	}

	@Override
	public String getName() {
		return "rhino";
	}

	@Override
	public String getNiceName() {
		return "Rhino 1.7R2";
	}

	@Override
  public InterpreterProcess buildProcess(String scriptName, int port) {
    return new RhinoInterpreterProcess(scriptName, port);
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
		return 0;
	}
}
