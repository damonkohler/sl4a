package com.googlecode.luaforandroid;

import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

import java.util.Map;

public class LuaProvider extends InterpreterProvider {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new LuaDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    return null;
  }

}
