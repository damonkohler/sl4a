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
import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class LuaDescriptor extends Sl4aHostedInterpreter {

  private static final String LUA_BIN = "bin/lua";
  private static final String LUA_PATH = "LUA_PATH";
  private static final String LUA_CPATH = "LUA_CPATH";

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
    return 2;
  }

  @Override
  public int getScriptsVersion() {
    return 1;
  }

  @Override
  public File getBinary(Context context) {
    return new File(getExtrasPath(context), LUA_BIN);
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context context) {
    Map<String, String> settings = new HashMap<String, String>(1);
    String root = getExtrasPath(context).getAbsolutePath();
    String ldir = root + "/share/lua/5.1/";
    String cdir = root + "/lib/lua/5.1/";
    String lua_path =
        "./?.lua;" + ldir + "?/?.lua;" + ldir + "?.lua;" + ldir + "?/init.lua;" + cdir + "?.lua;"
            + cdir + "?/init.lua;" + InterpreterConstants.SCRIPTS_ROOT + "/?.lua;";
    String lua_cpath =
        "./?.so;" + cdir + "?.so;" + cdir + "loadall.so;" + cdir + "?/init.sl;" + cdir + "?/?.so;";
    settings.put(LUA_PATH, lua_path);
    settings.put(LUA_CPATH, lua_cpath);
    return settings;
  }
}
