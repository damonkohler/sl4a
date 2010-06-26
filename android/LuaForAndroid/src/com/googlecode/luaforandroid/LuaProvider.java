package com.googlecode.luaforandroid;

import java.util.Map;

import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

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
