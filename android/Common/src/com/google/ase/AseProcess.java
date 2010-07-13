/*
 * Copyright (C) 2010 Google Inc.
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

import android.os.Process;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;

public class AseProcess {

  private static final int DEFAULT_BUFFER_SIZE = 8192;

  protected Integer mPid;
  protected FileDescriptor mFd;

  protected PrintStream mOut;
  protected Reader mIn;

  public AseProcess() {
  }

  public Integer getPid() {
    return mPid;
  }

  public FileDescriptor getFd() {
    return mFd;
  }

  public PrintStream getOut() {
    return mOut;
  }

  public PrintStream getErr() {
    return getOut();
  }

  public BufferedReader getIn() {
    return new BufferedReader(mIn, DEFAULT_BUFFER_SIZE);
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

  public void start(String binary, String[] args, String[] envvars, final Runnable shutdownHook) {
    int[] pid = new int[1];
    mFd = Exec.createSubprocess(binary, args, envvars, pid);
    mPid = pid[0];
    mOut = new PrintStream(new FileOutputStream(mFd), true /* autoflush */);
    mIn = new InputStreamReader(new FileInputStream(mFd));

    new Thread(new Runnable() {
      public void run() {
        AseLog.v("Waiting for " + mPid);
        int result = Exec.waitFor(mPid);
        AseLog.v("Subprocess exited with result code " + result);
        mOut.close();
        try {
          mIn.close();
        } catch (IOException e) {
          AseLog.e(e);
        }
        if (shutdownHook != null) {
          shutdownHook.run();
        }
      }
    }).start();
  }

  public void kill() {
    if (mPid != null) {
      Process.killProcess(mPid);
      AseLog.v("Killed process " + mPid);
    }
  }
}
