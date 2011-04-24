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

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;
import com.googlecode.android_scripting.interpreter.html.HtmlActivityTask;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;

import java.io.File;

public class ScriptLauncher {

  private ScriptLauncher() {
    // Utility class.
  }

  public static HtmlActivityTask launchHtmlScript(File script, Service service, Intent intent,
      InterpreterConfiguration config) {
    if (!script.exists()) {
      throw new RuntimeException("No such script to launch.");
    }
    HtmlInterpreter interpreter =
        (HtmlInterpreter) config.getInterpreterByName(HtmlInterpreter.HTML);
    if (interpreter == null) {
      throw new RuntimeException("HtmlInterpreter is not available.");
    }
    final FacadeManager manager =
        new FacadeManager(FacadeConfiguration.getSdkLevel(), service, intent,
            FacadeConfiguration.getFacadeClasses());
    FutureActivityTaskExecutor executor =
        ((BaseApplication) service.getApplication()).getTaskExecutor();
    final HtmlActivityTask task =
        new HtmlActivityTask(manager, interpreter.getAndroidJsSource(),
            interpreter.getJsonSource(), script.getAbsolutePath(), true);
    executor.execute(task);
    return task;
  }

  public static InterpreterProcess launchInterpreter(final AndroidProxy proxy, Intent intent,
      InterpreterConfiguration config, Runnable shutdownHook) {
    Interpreter interpreter;
    String interpreterName;
    interpreterName = intent.getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
    interpreter = config.getInterpreterByName(interpreterName);
    InterpreterProcess process = new InterpreterProcess(interpreter, proxy);
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

  public static ScriptProcess launchScript(File script, InterpreterConfiguration configuration,
      final AndroidProxy proxy, Runnable shutdownHook) {
    if (!script.exists()) {
      throw new RuntimeException("No such script to launch.");
    }
    ScriptProcess process = new ScriptProcess(script, configuration, proxy);
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