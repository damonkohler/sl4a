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

package com.google.ase.interpreter.lua;

import java.io.File;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.language.LuaLanguage;

/**
 * Represents the Lua interpreter.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class LuaInterpreter extends Interpreter {

  private final static String LUA_BIN = "/data/data/com.google.ase/lua/bin/lua";
  
  public LuaInterpreter() {
    super(new LuaLanguage());
  }

  @Override
  public String getExtension() {
    return ".lua";
  }

  @Override
  public String getName() {
    return "lua";
  }

  @Override
  public String getNiceName() {
    return "Lua 5.1.4";
  }

  @Override
  public InterpreterProcess buildProcess(String scriptName, int port) {
    return new LuaInterpreterProcess(scriptName, port);
  }

  @Override
  public boolean hasInterpreterArchive() {
    return true;
  }

  @Override
  public boolean hasInterpreterExtrasArchive() {
    return false;
  }

  @Override
  public boolean hasScriptsArchive() {
    return true;
  }

  @Override
  public File getBinary() {
    return new File(LUA_BIN);
  }

  @Override
  public int getVersion() {
    return 0;
  }
}
