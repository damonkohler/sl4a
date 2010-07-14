package com.googlecode.luaforandroid;


import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

import java.util.HashMap;
import java.util.Map;

public class LuaProvider extends InterpreterProvider {

  private final static String LUA_PATH = "LUA_PATH";
  private final static String LUA_CPATH = "LUA_CPATH";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new LuaDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(1);
    String root = mDescriptor.getPath(mContext);
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
