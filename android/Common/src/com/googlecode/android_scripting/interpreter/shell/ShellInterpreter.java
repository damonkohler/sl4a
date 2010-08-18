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

import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.language.ShellLanguage;

import java.io.File;

/**
 * Represents the shell.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ShellInterpreter extends Interpreter {
  private final static String SHELL_BIN = "/system/bin/sh";

  public ShellInterpreter() {
    setExtension(".sh");
    setName("sh");
    setNiceName("Shell");
    setBinary(new File(SHELL_BIN));
    setInteractiveCommand("");
    setScriptCommand("%s");
    setLanguage(new ShellLanguage());
    setHasInteractiveMode(true);
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

  public int getVersion() {
    return 0;
  }

  @Override
  public boolean isUninstallable() {
    return false;
  }

  @Override
  public boolean isInstalled() {
    return true;
  }
}
