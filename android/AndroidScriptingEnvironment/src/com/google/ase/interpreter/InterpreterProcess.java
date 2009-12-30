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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.os.Process;
import android.util.Log;

import com.google.ase.AseLog;
import com.google.ase.Exec;
import com.google.ase.RpcFacade;
import com.google.ase.jsonrpc.JsonRpcServer;

/**
 * This is a skeletal implementation of an interpreter process.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class InterpreterProcess {

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

  protected final int mAndroidProxyPort;
  private final JsonRpcServer mRpcServer;

  /**
   * Creates a new {@link InterpreterProcess}.
   *
   * @param ap an instance of {@link AndroidProxy} for the script to connect to
   * @param launchScript the absolute path to a script that should be launched
   *        with the interpreter
   */
  public InterpreterProcess(String launchScript, RpcFacade... facades) {
    mLaunchScript = launchScript;
    mRpcServer = JsonRpcServer.create(facades);
    mAndroidProxyPort = mRpcServer.startLocal().getPort();
    mEnvironment.put("AP_PORT", Integer.toString(mAndroidProxyPort));
  }

  public Integer getPid() {
    return mShellPid;
  }

  public FileDescriptor getFd() {
    return mShellFd;
  }

  public PrintStream getOut() {
    return mOut;
  }

  public PrintStream getErr() {
    return getOut();
  }

  public Reader getIn() {
    return mIn;
  }

  public void error(Object obj) {
    println(obj);
  }

  public void print(Object obj) {
    getOut().print(obj);
  }

  public void println(Object obj) {
    getOut().println(obj);
  }

  public void start() {
    int[] pid = new int[1];
    mShellFd = Exec.createSubprocess(SHELL_BIN, "-", null, pid);
    mShellPid = pid[0];

    mShellOut = new FileOutputStream(mShellFd);
    mShellIn = new FileInputStream(mShellFd);

    mOut = new PrintStream(mShellOut, true /* autoflush */);
    mIn = new BufferedReader(new InputStreamReader(mShellIn));

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

  public void kill() {
    shutdown();

    if (mShellPid != null) {
      Process.killProcess(mShellPid);
    }
  }

  protected void exportEnvironment() {
    for (Entry<String, String> e : mEnvironment.entrySet()) {
      println(String.format("export %s=\"%s\"", e.getKey(), e.getValue()));
    }
  }

  /**
   * Writes the command to the shell that starts the interpreter.
   */
  protected void writeInterpreterCommand() {
    // Should normally be overridden. As is, just the shell will pop up.
  }

  /**
   * Called just before the interpreter process is shut down.
   */
  private void shutdown() {
    mRpcServer.shutdown();
  }
}
