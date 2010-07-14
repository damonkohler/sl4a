package com.googlecode.perlforandroid;

import java.util.Map;

import com.google.ase.interpreter.InterpreterProvider;

import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class PerlProvider extends InterpreterProvider {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PerlDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    return null;
  }

}
