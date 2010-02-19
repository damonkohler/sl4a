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

package com.google.ase;

import java.io.File;
import java.net.InetSocketAddress;

import android.content.Intent;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.interpreter.InterpreterUtils;

public class ScriptLauncher {

  private final String mScriptName;
  private final String mInterpeterName;
  private final InetSocketAddress mAddress;
  private InterpreterProcess mProcess;

  public ScriptLauncher(Intent intent, InetSocketAddress address) {
    mScriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    mInterpeterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
    mAddress = address;
  }

  public void launch() throws AseException {
    if (mScriptName == null && mInterpeterName == null) {
      throw new AseException("Must specify either script or interpreter.");
    }
    Interpreter interpreter;
    // TODO(damonkohler): Relying on a null parameter feels broken.
    String scriptPath = null;
    if (mScriptName != null) {
      interpreter = InterpreterUtils.getInterpreterForScript(mScriptName);
      if (interpreter == null) {
        throw new AseException("No compatible interpreter installed.");
      }
      File script = ScriptStorageAdapter.getScript(mScriptName);
      if (script == null) {
        throw new AseException("No such script to launch.");
      }
      scriptPath = script.getAbsolutePath();
    } else {
      interpreter = InterpreterUtils.getInterpreterByName(mInterpeterName);
    }
    mProcess =
        InterpreterUtils.getInterpreterByName(interpreter.getName()).buildProcess(scriptPath,
            mAddress.getPort());
    mProcess.start();
  }

  public void kill() {
    if (mProcess != null) {
      mProcess.kill();
    }
  }

  public String getScriptName() {
    return mScriptName;
  }

  public String getInterpreterName() {
    return mInterpeterName;
  }

  public InterpreterProcess getProcess() {
    return mProcess;
  }
}
