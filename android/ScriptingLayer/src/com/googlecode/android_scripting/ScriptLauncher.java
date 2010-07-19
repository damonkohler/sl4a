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

import java.io.File;

import android.content.Intent;

import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;
import com.googlecode.android_scripting.trigger.Trigger;

public class ScriptLauncher {

  private ScriptLauncher() {
    // Utility class.
  }

  public static InterpreterProcess launchInterpreter(final AndroidProxy proxy, Intent intent,
      InterpreterConfiguration config, Runnable shutdownHook) {
    return launch(proxy, intent, config, null, shutdownHook);
  }

  public static ScriptProcess launchScript(final AndroidProxy proxy, Intent intent,
      InterpreterConfiguration config, Trigger trigger, Runnable shutdownHook) {
    return (ScriptProcess) launch(proxy, intent, config, trigger, shutdownHook);
  }

  public static ScriptProcess launchScript(final AndroidProxy proxy, File script,
      InterpreterConfiguration config, Trigger trigger, Runnable shutdownHook) {
    Intent intent = new Intent();
    intent.putExtra(Constants.EXTRA_SCRIPT_NAME, script.getName());
    return (ScriptProcess) launch(proxy, intent, config, trigger, shutdownHook);
  }

  private static InterpreterProcess launch(final AndroidProxy proxy, Intent intent,
      InterpreterConfiguration config, Trigger trigger, Runnable shutdownHook) {
    String scriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    Interpreter interpreter;
    String interpreterName;
    if (scriptName != null) {
      interpreter = config.getInterpreterForScript(scriptName);
      interpreterName = interpreter.getName();
    } else {
      interpreterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
      interpreter = config.getInterpreterByName(interpreterName);
    }
    if (scriptName == null && interpreter == null) {
      throw new RuntimeException("Must specify either script or interpreter.");
    }
    InterpreterProcess process;
    String scriptPath = null;
    if (scriptName != null) {
      File script = ScriptStorageAdapter.getExistingScript(scriptName);
      if (script == null) {
        throw new RuntimeException("No such script to launch.");
      }
      scriptPath = script.getAbsolutePath();
      process = new ScriptProcess(scriptName, proxy, trigger);
    } else {
      process =
          interpreter.buildProcess(scriptPath, proxy.getAddress().getHostName(), proxy.getAddress()
              .getPort(), proxy.getSecret());
    }
    if (shutdownHook == null) {
      process.start(new Runnable() {
        @Override
        public void run() {
          proxy.shutdown();
        }
      });
    } else {
      process.start(shutdownHook);
    }
    return process;
  }
}