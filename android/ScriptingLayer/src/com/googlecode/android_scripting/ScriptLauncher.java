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
    Interpreter interpreter;
    String interpreterName;
    interpreterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
    interpreter = config.getInterpreterByName(interpreterName);
    InterpreterProcess process =
        new InterpreterProcess(interpreter, proxy.getAddress().getHostName(), proxy.getAddress()
            .getPort(), proxy.getSecret());
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

  public static ScriptProcess launchScript(InterpreterConfiguration configuration,
      final AndroidProxy proxy, Intent intent, Trigger trigger, Runnable shutdownHook) {
    String scriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    File script = ScriptStorageAdapter.getExistingScript(scriptName);
    if (script == null) {
      throw new RuntimeException("No such script to launch.");
    }
    ScriptProcess process = new ScriptProcess(configuration, scriptName, proxy, trigger);
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

  public static ScriptProcess launchScript(InterpreterConfiguration configuration,
      final AndroidProxy proxy, File script, Trigger trigger, Runnable shutdownHook) {
    Intent intent = new Intent();
    intent.putExtra(Constants.EXTRA_SCRIPT_NAME, script.getName());
    return launchScript(configuration, proxy, intent, trigger, shutdownHook);
  }
}