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

import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.interpreter.InterpreterAgent;
import com.google.ase.interpreter.InterpreterProcess;

public class ScriptLauncher {

  private final String mScriptName;
  private final String mInterpreterName;
  private final InterpreterAgent mInterpreter;
  private final InetSocketAddress mAddress;
  private InterpreterProcess mProcess;

  public ScriptLauncher(Intent intent, InetSocketAddress address, InterpreterConfiguration config) {
    mScriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    if (mScriptName != null) {
      mInterpreter = config.getInterpreterForScript(mScriptName);
      mInterpreterName = mInterpreter.getName();
    } else {
      mInterpreterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
      mInterpreter = config.getInterpreterByName(mInterpreterName);
    }
    mAddress = address;
  }

  public void launch() throws AseException {
    if (mScriptName == null && mInterpreter == null) {
      throw new AseException("Must specify either script or interpreter.");
    }
    String scriptPath = null;
    if (mScriptName != null) {
      File script = ScriptStorageAdapter.getExistingScript(mScriptName);
      if (script == null) {
        throw new AseException("No such script to launch.");
      }
      scriptPath = script.getAbsolutePath();
    }
    mProcess = mInterpreter.buildProcess(scriptPath, mAddress.getPort());
    mProcess.start();
    Analytics.track(mInterpreterName);
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
    return mInterpreterName;
  }

  public InterpreterProcess getProcess() {
    return mProcess;
  }

  public int getPid() {
    return mProcess.getPid();
  }

  public int getProxyPort() {
    return mAddress.getPort();
  }
}