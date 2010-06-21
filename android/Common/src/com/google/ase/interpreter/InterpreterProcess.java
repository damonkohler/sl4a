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

package com.google.ase.interpreter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.ase.AseLog;
import com.google.ase.AseProcess;

/**
 * This is a skeletal implementation of an interpreter process.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class InterpreterProcess extends AseProcess {

  protected final static String SHELL_BIN = "/system/bin/sh";

  protected String mLaunchScript;
  protected Map<String, String> mEnvironment = new HashMap<String, String>();

  /**
   * Creates a new {@link InterpreterProcess}.
   * 
   * @param launchScript
   *          the absolute path to a script that should be launched with the interpreter
   * @param port
   *          the port that the AndroidProxy is listening on
   */
  public InterpreterProcess(String launchScript, int port) {
    mLaunchScript = launchScript;
    mEnvironment.put("AP_PORT", Integer.toString(port));
  }

  public void start() {
    super.start(SHELL_BIN, "-", null);

    // Wait until the shell has produced some output before we start writing to it. This prevents
    // misplaced $ prompts in the output.
    try {
      while (!mIn.ready()) {
        Thread.sleep(1);
      }
    } catch (IOException e) {
      AseLog.e("Failed while waiting for mShellFd.", e);
    } catch (InterruptedException e) {
      AseLog.e("Failed while waiting for mShellFd.", e);
    }

    exportEnvironment();
    println(getInterpreterCommand());
  }

  protected void exportEnvironment() {
    buildEnvironment();
    for (Entry<String, String> e : mEnvironment.entrySet()) {
      println(String.format("export %s=\"%s\"", e.getKey(), e.getValue()));
    }
  }

  /**
   * Writes the command to the shell that starts the interpreter.
   */
  // Should normally be overridden. As is, just the shell will pop up.
  protected abstract String getInterpreterCommand(); 



  /**
   * Called before execution to allow interpreters to modify the environment map as necessary.
   */
//Should normally be overridden. As is, the only environment variable will be the AP_PORT.
  protected abstract void buildEnvironment();
    
  
}
