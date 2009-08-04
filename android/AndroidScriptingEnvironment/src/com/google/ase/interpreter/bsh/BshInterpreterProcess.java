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

package com.google.ase.interpreter.bsh;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.util.Log;
import bsh.ConsoleInterface;

import com.google.ase.AndroidFacade;
import com.google.ase.interpreter.InterpreterProcessInterface;

// TODO(damonkohler): Typing into BeanShell is a bit choppy. I think it has to do with
// the piped streams. These could be modified to read more often and possible
// also benefit from being synchronized. Look at the BeanShell JConsole code for
// example.
public class BshInterpreterProcess implements InterpreterProcessInterface {

  private static final String TAG = "BshInterpreterProcess";

  private final bsh.Interpreter mBshInterpreter;
  private final PipedInputStream mTermInPipe;
  private final PrintStream mTermOutStream;
  private final BshConsole mBshConsole;
  private final String mLaunchScript;

  /**
   * Collects the text to be written to BeanShell after pressing return. This is necessary because
   * BeanShell parses the text immediately as it's written.
   */
  private final StringBuffer mPrintBuffer;

  private class BshConsole implements ConsoleInterface {

    private final PipedInputStream mBshInPipe;
    private final PrintStream mBshOutStream;

    public BshConsole() throws IOException {
      mBshInPipe = new PipedInputStream();
      mBshOutStream = new PrintStream(new PipedOutputStream(mTermInPipe));
    }

    @Override
    public Reader getIn() {
      return new InputStreamReader(mTermInPipe);
    }

    @Override
    public PrintStream getOut() {
      return mTermOutStream;
    }

    @Override
    public void print(Object obj) {
      obj = termCharAdapter(obj);
      getOut().print(obj);
    }

    @Override
    public void println(Object obj) {
      print(obj);
      print((char) 13);
    }

    @Override
    public PrintStream getErr() {
      return getOut();
    }

    @Override
    public void error(Object o) {
      print(o);
    }

    public PipedInputStream getInPipe() {
      return mBshInPipe;
    }

    public PrintStream getOutStream() {
      return mBshOutStream;
    }
  }

  public BshInterpreterProcess(AndroidFacade facade, String launchScript) throws Exception {
    mLaunchScript = launchScript;
    mPrintBuffer = new StringBuffer();

    // Order is important here. Each stream depends on the previous.
    mTermInPipe = new PipedInputStream();
    mBshConsole = new BshConsole();
    mTermOutStream = new PrintStream(new PipedOutputStream(mBshConsole.getInPipe()));

    mBshInterpreter = new bsh.Interpreter(mBshConsole);
    mBshInterpreter.set("android", facade);
  }

  @Override
  public FileDescriptor getFd() {
    // Because BeanShell runs in the same process, there is no FD for it.
    return null;
  }

  @Override
  public PrintStream getOut() {
    return mBshConsole.getOutStream();
  }

  @Override
  public void print(Object obj) {
    obj = termCharAdapter(obj);
    mTermOutStream.print(obj); // Print to BeanShell.
    mPrintBuffer.append(obj);
    while (true) {
      int newLineIndex = mPrintBuffer.indexOf(Character.valueOf((char) 10).toString());
      if (newLineIndex == -1) {
        break;
      } else {
        String cmd = mPrintBuffer.substring(0, newLineIndex + 1);
        cmd = replaceBackspaceChars(cmd);
        mBshConsole.getOutStream().print(cmd); // Sends the command to BeanShell.
        String remainder = mPrintBuffer.substring(newLineIndex + 1);
        mPrintBuffer.setLength(0);
        mPrintBuffer.append(remainder);
      }
    }
  }

  @Override
  public void println(Object obj) {
    print(obj);
    print((char) 13);
  }

  @Override
  public PrintStream getErr() {
    return getOut();
  }

  @Override
  public void error(Object obj) {
    print(obj);
  }

  @Override
  public Reader getIn() {
    return new InputStreamReader(mBshConsole.getInPipe());
  }

  @Override
  public Integer getPid() {
    return null;
  }

  @Override
  public void kill() {
    // There is no running process to kill.
  }

  @Override
  public void start() {
    if (mLaunchScript != null) {
      new Thread() {
        @Override
        public void run() {
          try {
            mBshInterpreter.source(mLaunchScript);
          } catch (Exception e) {
            Log.e(TAG, "Failed to launch script.", e);
          }
        }
      }.start();
    } else {
      new Thread(mBshInterpreter).start();
    }
  }

  private String replaceBackspaceChars(String cmd) {
    while (true) {
      int bsIndex = cmd.indexOf((char) 8);
      if (bsIndex == -1) {
        break;
      } else {
        cmd = cmd.substring(0, bsIndex - 1) + cmd.substring(bsIndex + 3);
      }
    }
    return cmd;
  }

  private Object termCharAdapter(Object obj) {
    List<Character> buffer = new ArrayList<Character>();
    if (obj instanceof String) {
      for (char c : ((String) obj).toCharArray()) {
        buffer.add(c);
      }
    } else if (obj instanceof Character) {
      buffer.add((Character) obj);
    } else {
      return obj;
    }

    StringBuilder out = new StringBuilder();
    for (char c : buffer) {
      if (c == 127) {
        out.append((char) 8);
        out.append((char) 32);
        out.append((char) 8);
      } else if (c == 13) {
        out.append((char) 13); // CR
        out.append((char) 10); // LF
      } else {
        out.append(c);
      }
    }
    return out.toString();
  }
}
