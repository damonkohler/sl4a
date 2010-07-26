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

package com.googlecode.android_scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Process {

  private static final int DEFAULT_BUFFER_SIZE = 8192;

  private final List<String> mArguments;
  private final Map<String, String> mEnvironment;

  private File mBinary;
  private long mStartTime;

  protected String mName;
  protected Integer mPid;
  protected FileDescriptor mFd;
  protected PrintStream mOut;
  protected Reader mIn;

  public Process() {
    mArguments = new ArrayList<String>();
    mEnvironment = new HashMap<String, String>();
  }

  public void addArgument(String argument) {
    mArguments.add(argument);
  }

  public void addAllArguments(List<String> arguments) {
    mArguments.addAll(arguments);
  }

  public void putAllEnvironmentVariables(Map<String, String> environment) {
    mEnvironment.putAll(environment);
  }

  public void putEnvironmentVariable(String key, String value) {
    mEnvironment.put(key, value);
  }

  public void setBinary(File binary) {
    if (!binary.exists()) {
      throw new RuntimeException("Binary " + binary + " does not exist!");
    }
    mBinary = binary;
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

  public void start(final Runnable shutdownHook) {
    if (isAlive()) {
      throw new RuntimeException("Attempted to start process that is already running.");
    }

    int[] pid = new int[1];
    Log.v("Executing " + mBinary.getAbsolutePath() + " with arguments " + mArguments
        + " and with environment " + mEnvironment.toString());
    mFd =
        Exec.createSubprocess(mBinary.getAbsolutePath(), mArguments.toArray(new String[mArguments
            .size()]), getEnvironmentArray(), pid);
    mPid = pid[0];
    mOut = new PrintStream(new FileOutputStream(mFd), true /* autoflush */);
    mIn = new InputStreamReader(new FileInputStream(mFd));
    mStartTime = System.currentTimeMillis();

    new Thread(new Runnable() {
      public void run() {
        int result = Exec.waitFor(mPid);
        Log.v("Process " + mPid + " exited with result code " + result + ".");
        mPid = null;
        mOut.close();
        try {
          mIn.close();
        } catch (IOException e) {
          Log.e(e);
        }
        if (shutdownHook != null) {
          shutdownHook.run();
        }
      }
    }).start();
  }

  private String[] getEnvironmentArray() {
    List<String> environmentVariables = new ArrayList<String>();
    for (Entry<String, String> entry : mEnvironment.entrySet()) {
      environmentVariables.add(entry.getKey() + "=" + entry.getValue());
    }
    String[] environment = environmentVariables.toArray(new String[environmentVariables.size()]);
    return environment;
  }

  public void kill() {
    if (isAlive()) {
      android.os.Process.killProcess(mPid);
      Log.v("Killed process " + mPid);
    }
  }

  public boolean isAlive() {
    return mPid != null;
  }

  public String getUptime() {
    if (!isAlive()) {
      return "";
    }
    long ms = System.currentTimeMillis() - mStartTime;
    StringBuffer buffer = new StringBuffer();
    int days = (int) (ms / (1000 * 60 * 60 * 24));
    int hours = (int) (ms % (1000 * 60 * 60 * 24)) / 3600000;
    int minutes = (int) (ms % 3600000) / 60000;
    int seconds = (int) (ms % 60000) / 1000;
    if (days != 0) {
      buffer.append(String.format("%02d:%02d:", days, hours));
    } else if (hours != 0) {
      buffer.append(String.format("%02d:", hours));
    }
    buffer.append(String.format("%02d:%02d", minutes, seconds));
    return buffer.toString();
  }

  public String getName() {
    return mName;
  }

  public void setName(String name) {
    mName = name;
  }
}
