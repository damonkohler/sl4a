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

import com.google.ase.RpcFacade;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;

public class RhinoInterpreter extends Interpreter {
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
	public String getContentTemplate() {
		return "load(\"/sdcard/ase/extras/rhino/android.js\");\nvar droid = new Android();\n";
	}

	@Override
	public InterpreterProcess buildProcess(String scriptName, RpcFacade... facades) {
		return new RhinoInterpreterProcess(scriptName, facades);
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
