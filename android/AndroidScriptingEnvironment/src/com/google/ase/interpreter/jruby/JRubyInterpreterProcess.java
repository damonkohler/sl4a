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

package com.google.ase.interpreter.jruby;

import java.io.BufferedInputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jruby.Ruby;
import org.jruby.RubyInstanceConfig;
import org.jruby.ast.executable.Script;
import org.jruby.internal.runtime.GlobalVariables;
import org.jruby.runtime.GlobalVariable;

import android.util.Log;
import bsh.ConsoleInterface;

import com.google.ase.AndroidFacade;
import com.google.ase.AndroidProxy;
import com.google.ase.interpreter.InterpreterProcessInterface;
import com.google.ase.jsonrpc.JsonRpcServer;

public class JRubyInterpreterProcess implements InterpreterProcessInterface{


  private static final String TAG = "JRubyInterpreterProcess";

  private final Ruby mJRubyInterpreter;
  private final PipedInputStream mTermInPipe;
  private final PrintStream mTermOutStream;
  private JRubyConsole mJRubyConsole;
  private final String mLaunchScript;

  private AndroidProxy mAndroidProxy;
  private int mAndroidProxyPort;

  /**
   * Collects the text to be written to BeanShell after pressing return. This is necessary because
   * BeanShell parses the text immediately as it's written.
   */
  private final StringBuffer mPrintBuffer;

  private class JRubyConsole implements ConsoleInterface {

    private final PipedInputStream mJRubyInPipe;
    private final PrintStream mJRubyOutStream;

    public JRubyConsole() throws IOException {
      mJRubyInPipe = new PipedInputStream();
      mJRubyOutStream = new PrintStream(new PipedOutputStream(mTermInPipe));
    }

    @Override
    public Reader getIn() {
      return new InputStreamReader(mTermInPipe);
    }

    public InputStream getInStream() {
      return mTermInPipe;
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
      return mJRubyInPipe;
    }

    public PrintStream getOutStream() {
      return mJRubyOutStream;
    }
  }

  public JRubyInterpreterProcess(AndroidFacade facade, String launchScript) throws Exception {
    mLaunchScript = launchScript;
    mPrintBuffer = new StringBuffer();

    mAndroidProxy = new AndroidProxy(facade);
    mAndroidProxyPort = new JsonRpcServer(mAndroidProxy).start();

    // Order is important here. Each stream depends on the previous.
    mTermInPipe = new PipedInputStream();
    mJRubyConsole = new JRubyConsole();
    mTermOutStream = new PrintStream(new PipedOutputStream(mJRubyConsole.getInPipe()));
    final RubyInstanceConfig config = new RubyInstanceConfig() {
      {
        setInput(mJRubyConsole.getInStream());
        setOutput(mJRubyConsole.getOut());
        setError(mJRubyConsole.getErr());
        setObjectSpaceEnabled(false); // Does not work without readline.
        setArgv(new String[0]); // TODO(psycho)!!!!!
        Map<String, String> env = new HashMap<String, String>();
        env.put("AP_PORT", "" + mAndroidProxyPort);
        setEnvironment(env);
      }
    };
    mJRubyInterpreter = Ruby.newInstance(config);
  }

  @Override
  public FileDescriptor getFd() {
    // Because JRuby runs in the same process, there is no FD for it.
    return null;
  }

  @Override
  public PrintStream getOut() {
    return mJRubyConsole.getOutStream();
  }

  @Override
  public void print(Object obj) {
    obj = termCharAdapter(obj);
    mTermOutStream.print(obj); // Print to Ruby.
    mPrintBuffer.append(obj);
    while (true) {
      int newLineIndex = mPrintBuffer.indexOf(Character.valueOf((char) 10).toString());
      if (newLineIndex == -1) {
        break;
      } else {
        String cmd = mPrintBuffer.substring(0, newLineIndex + 1);
        cmd = replaceBackspaceChars(cmd);
        mJRubyConsole.getOutStream().print(cmd); // Sends the command to Ruby.
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
    return new InputStreamReader(mJRubyConsole.getInPipe());
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
    Runnable r = new Runnable() {
      @Override
      public void run() {
        try {
          if (mLaunchScript == null) {
            mJRubyInterpreter
                .evalScriptlet("puts $LOAD_PATH.inspect;" +
                               "require 'irb';" +
                               "IRB.conf[:USE_READLINE] = false; " +
                               "IRB.start");
          } else {
            BufferedInputStream code =
                new BufferedInputStream(new FileInputStream(mLaunchScript));
            mJRubyInterpreter.runFromMain(code, "main");
          }
        } catch (Exception e) {
          Log.e(TAG, "Failed to launch script.", e);
        }
      }
    };
    new Thread(null, r, "jruby-interpreter", 1024 * 1024).start();
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
