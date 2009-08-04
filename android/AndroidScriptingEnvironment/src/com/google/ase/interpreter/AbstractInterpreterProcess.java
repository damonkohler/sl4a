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

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Exec;
import android.os.Process;
import android.util.Log;

import com.google.ase.AndroidFacade;
import com.google.ase.AndroidProxy;
import com.google.ase.interpreter.lua.LuaInterpreterProcess;

/**
 * This is a skeletal implementation of an interpreter process.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
// TODO(damonkohler): Possibly use ProcessBuilder?
public abstract class AbstractInterpreterProcess implements InterpreterProcessInterface {

  protected final static String SHELL_BIN = "/system/bin/sh";
  protected static final String TAG = "InterpreterInterface";

  protected String mLaunchScript;
  protected Map<String, String> mEnvironment = new HashMap<String, String>();

  protected Integer mShellPid;
  protected FileDescriptor mShellFd;
  protected FileOutputStream mShellOut;
  protected FileInputStream mShellIn;

  protected PrintStream mOut;
  protected Reader mIn;

  /**
   * Creates a new {@link LuaInterpreterProcess}.
   * 
   * @param ap an instance of {@link AndroidProxy} for the script to connect to
   * @param launchScript the absolute path to a script that should be launched
   *        with the interpreter
   */
  public AbstractInterpreterProcess(AndroidFacade facade, String launchScript) {
    mLaunchScript = launchScript;
  }

  @Override
  public Integer getPid() {
    return mShellPid;
  }

  @Override
  public FileDescriptor getFd() {
    return mShellFd;
  }

  @Override
  public PrintStream getOut() {
    return mOut;
  }

  @Override
  public PrintStream getErr() {
    return getOut();
  }

  @Override
  public Reader getIn() {
    return mIn;
  }

  @Override
  public void error(Object obj) {
    println(obj);
  }

  @Override
  public void print(Object obj) {
    getOut().print(obj);
  }

  @Override
  public void println(Object obj) {
    getOut().println(obj);
  }

  @Override
  public void start() {
    int[] pid = new int[1];
    mShellFd = Exec.createSubprocess(SHELL_BIN, "-", null, pid);
    mShellPid = pid[0];

    mShellOut = new FileOutputStream(mShellFd);
    mShellIn = new FileInputStream(mShellFd);

    mOut = new PrintStream(mShellOut, true /* autoflush */);
    mIn = new BufferedReader(new InputStreamReader(mShellIn));

    exportEnvironment();
    writeInterpreterCommand();

    // TODO(damonkohler): Possibly allow clients to have a death listener.
    new Thread(new Runnable() {
      public void run() {
        Log.i(TAG, "Waiting for: " + mShellPid);
        int result = Exec.waitFor(mShellPid);
        Log.i(TAG, "Subprocess exited: " + result);
      }
    }).start();
  }

  @Override
  public void kill() {
    if (mShellPid != null) {
      Process.killProcess(mShellPid);
    }
  }

  protected void exportEnvironment() {
    StringBuilder exports = new StringBuilder();
    for (Entry<String, String> e : mEnvironment.entrySet()) {
      exports.append("export ");
      exports.append(e.getKey());
      exports.append("=\"");
      exports.append(e.getValue());
      exports.append("\"\n");
    }
    print(exports.toString());
  }

  /**
   * Writes the command to the shell that starts the interpreter.
   */
  protected void writeInterpreterCommand() {
    // Should normally be overridden. As is, just the shell will pop up.
  }
}
