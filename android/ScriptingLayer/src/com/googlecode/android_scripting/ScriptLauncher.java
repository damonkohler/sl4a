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

package com.googlecode.android_scripting;

import android.content.Intent;


import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterAgent;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;

import java.io.File;

public class ScriptLauncher {

  private final String mScriptName;
  private final String mInterpreterName;
  private final InterpreterAgent mInterpreter;
  private InterpreterProcess mProcess;
  private final AndroidProxy mProxy;

  public ScriptLauncher(AndroidProxy proxy, File script, InterpreterConfiguration config) {
    mProxy = proxy;
    mScriptName = script.getName();
    if (mScriptName == null) {
      // throw exception
    }
    mInterpreter = config.getInterpreterForScript(mScriptName);
    mInterpreterName = mInterpreter.getName();
  }

  public ScriptLauncher(AndroidProxy proxy, Intent intent, InterpreterConfiguration config) {
    mProxy = proxy;
    mScriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    if (mScriptName != null) {
      mInterpreter = config.getInterpreterForScript(mScriptName);
      mInterpreterName = mInterpreter.getName();
    } else {
      mInterpreterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
      mInterpreter = config.getInterpreterByName(mInterpreterName);
    }
  }

  public void launch() throws Sl4aException {
    launch((new Runnable() {
      @Override
      public void run() {
        mProxy.shutdown();
      }
    }));
  }

  public void launch(final Runnable shutdownHook) throws Sl4aException {
    if (mScriptName == null && mInterpreter == null) {
      throw new Sl4aException("Must specify either script or interpreter.");
    }
    String scriptPath = null;
    if (mScriptName != null) {
      File script = ScriptStorageAdapter.getExistingScript(mScriptName);
      if (script == null) {
        throw new Sl4aException("No such script to launch.");
      }
      scriptPath = script.getAbsolutePath();
    }
    mProcess =
        mInterpreter.buildProcess(scriptPath, mProxy.getAddress().getHostName(), mProxy
            .getAddress().getPort(), mProxy.getSecret());
    mProcess.start(shutdownHook);
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

}