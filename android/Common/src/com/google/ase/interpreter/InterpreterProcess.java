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

import com.google.ase.AseProcess;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This is a skeletal implementation of an interpreter process.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public abstract class InterpreterProcess extends AseProcess {

  protected String mLaunchScript;
  protected Map<String, String> mEnvironment = new HashMap<String, String>();
  protected int mId;

  /**
   * Creates a new {@link InterpreterProcess}.
   * 
   * @param launchScript
   *          the absolute path to a script that should be launched with the interpreter
   * @param port
   *          the port that the AndroidProxy is listening on
   */
  public InterpreterProcess(String launchScript, String host, int port, String handshake) {
    mId = port;
    mLaunchScript = launchScript;
    mEnvironment.putAll(System.getenv());
    mEnvironment.put("AP_PORT", Integer.toString(port));
    mEnvironment.put("AP_HOST", host);
    if (handshake != null) {
      mEnvironment.put("AP_HANDSHAKE", handshake);
    }
    mLog = new StringBuffer();
  }

  public void start(final Runnable shutdownHook) {
    String command = getInterpreterCommand();
    String[] arguments = getInterpreterArguments();
    String[] environmentVariables = getEnvironmentVariables();
    super.start(command, arguments, environmentVariables, shutdownHook);
  }

  protected String[] getEnvironmentVariables() {
    buildEnvironment();
    String[] vars = new String[mEnvironment.size()];
    int i = 0;
    for (Entry<String, String> e : mEnvironment.entrySet()) {
      vars[i++] = String.format("%s=%s", e.getKey(), e.getValue());
    }
    return vars;
  }

  /**
   * Writes the command to the shell that starts the interpreter.
   */
  protected abstract String getInterpreterCommand();

  protected abstract String[] getInterpreterArguments();

  /**
   * Called before execution to allow interpreters to modify the environment map as necessary.
   */
  // Should normally be overridden. As is, the only environment variable will be the AP_PORT.
  protected abstract void buildEnvironment();

  @Override
  public BufferedReader getIn() {
    return new LoggingBufferedReader(mIn, 8192);
  }

  private final StringBuffer mLog;

  private volatile int mLogLength = 0;

  // TODO(Alexey): Add Javadoc.
  private class LoggingBufferedReader extends BufferedReader {
    private boolean mmSkipLF = false;

    // TODO(Alexey): Use persistent storage
    // replace with circular log, see ConnectBot
    private int mmPos = 0;

    public LoggingBufferedReader(Reader in) {
      super(in);
    }

    public LoggingBufferedReader(Reader in, int size) {
      super(in, size);
    }

    @Override
    public int read() throws IOException {
      if (mmPos == mLogLength) {
        return read1();
      } else {
        int c = mLog.charAt(mmPos);
        mmPos++;
        return c;
      }
    }

    private int read1() throws IOException {
      int c;
      synchronized (lock) {
        // check again
        if (mmPos < mLogLength) {
          c = mLog.charAt(mmPos);
        } else {
          c = (char) super.read();
          mLog.append(Character.valueOf((char) c));
          mLogLength++;
        }
      }
      mmPos++;
      return c;
    }

    @Override
    public int read(char cbuf[], int off, int len) throws IOException {
      if (mmPos == mLogLength) {
        return read1(cbuf, off, len);
      } else {
        int count = 0;
        int end = Math.min(mLogLength, mmPos + len);
        mLog.getChars(mmPos, end, cbuf, off);
        count = end - mmPos;
        len -= count;
        off += count;
        mmPos += count;
        if (len > 0) {
          int read = read1(cbuf, off, len);
          if (read < 0) {
            return read;
          }
          count += read;
        }
        return count;
      }
    }

    private int read1(char cbuf[], int off, int len) throws IOException {
      int count = 0;
      synchronized (lock) {
        if (mmPos < mLogLength) {
          int end = Math.min(mLogLength, mmPos + len);
          mLog.getChars(mmPos, end, cbuf, off);
          count = end - mmPos;
          len -= count;
          off += count;
          mmPos += count;
        }
        if (len > 0) {
          int read = super.read(cbuf, off, len);
          if (read < 0) {
            return read;
          }
          mLog.append(cbuf, off, read);
          mLogLength += read;
          mmPos += read;
          count += read;
        }
      }
      return count;
    }

    @Override
    public String readLine() throws IOException {
      if (mmPos == mLogLength) {
        return readLine1();
      } else {
        StringBuffer buffer = new StringBuffer();
        while (mmPos < mLogLength) {
          char nextChar = mLog.charAt(mmPos);
          mmPos++;
          if (mmSkipLF) {
            mmSkipLF = false;
            if (nextChar == '\n') {
              continue;
            }
          }
          buffer.append(nextChar);
          if (nextChar == '\n') {
            return buffer.toString();
          }
          if (nextChar == '\r') {
            mmSkipLF = true;
            return buffer.toString();
          }
        }
        buffer.append(readLine1());
        return buffer.toString();
      }
    }

    private String readLine1() throws IOException {
      StringBuffer buffer = new StringBuffer();
      synchronized (lock) {
        while (mmPos < mLogLength) {
          char nextChar = mLog.charAt(mmPos);
          mmPos++;
          if (mmSkipLF) {
            mmSkipLF = false;
            if (nextChar == '\n') {
              continue;
            }
          }
          buffer.append(nextChar);
          if (nextChar == '\n') {
            return buffer.toString();
          }
          if (nextChar == '\r') {
            mmSkipLF = true;
            return buffer.toString();
          }
        }
        String str = super.readLine();
        if (mmSkipLF && str.length() == 1 && str.charAt(0) == '\n') {
          mmSkipLF = false;
          str = super.readLine();
        }
        return buffer.append(str).toString();
      }
    }
  }

}
