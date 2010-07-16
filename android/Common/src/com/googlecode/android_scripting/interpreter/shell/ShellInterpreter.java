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

package com.googlecode.android_scripting.interpreter.shell;

import com.googlecode.android_scripting.interpreter.InterpreterAgent;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;
import com.googlecode.android_scripting.language.Language;
import com.googlecode.android_scripting.language.ShellLanguage;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

/**
 * Represents the shell.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ShellInterpreter implements InterpreterAgent {
  private final static String SHELL_BIN = "/system/bin/sh";
  private final Language mShellLanguage;

  public ShellInterpreter() {
    mShellLanguage = new ShellLanguage();
  }

  public String getExtension() {
    return ".sh";
  }

  public String getName() {
    return "sh";
  }

  public InterpreterProcess buildProcess(String scriptName, String host, int port, String handshake) {
    return new ShellInterpreterProcess(scriptName, host, port, handshake);
  }

  public String getNiceName() {
    return "Shell";
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public boolean hasExtrasArchive() {
    return false;
  }

  public boolean hasScriptsArchive() {
    return false;
  }

  public String getBinary() {
    return "";
  }

  public int getVersion() {
    return 0;
  }

  public boolean isUninstallable() {
    return false;
  }

  public String getContentTemplate() {
    return mShellLanguage.getContentTemplate();
  }

  public Language getLanguage() {
    return mShellLanguage;
  }

  public String getPath() {
    return null;
  }

  public String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return mShellLanguage.getRpcText(content, rpc, values);
  }

  public boolean isInstalled() {
    return true;
  }

  private class ShellInterpreterProcess extends InterpreterProcess {

    public ShellInterpreterProcess(String launchScript, String host, int port, String handshake) {
      super(launchScript, host, port, handshake);
    }

    @Override
    protected void buildEnvironment() {
      // TODO(damonkohler): Add bin directories for all interpreters to the
      // path.
    }

    @Override
    protected String getInterpreterCommand() {
      return SHELL_BIN;
    }

    @Override
    protected String[] getInterpreterArguments() {
      return null;
    }
  }

}
