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

package com.googlecode.luaforandroid;

import java.io.File;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class LuaDescriptor extends Sl4aHostedInterpreter {

  private final static String LUA_BIN = "bin/lua";

  public String getExtension() {
    return ".lua";
  }

  public String getName() {
    return "lua";
  }

  public String getNiceName() {
    return "Lua 5.1.4";
  }

  public boolean hasInterpreterArchive() {
    return true;
  }

  public boolean hasExtrasArchive() {
    return false;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  public int getVersion() {
    return 1;
  }

  @Override
  public File getBinary(Context context) {
    return new File(getExtrasPath(context), LUA_BIN);
  }
}
