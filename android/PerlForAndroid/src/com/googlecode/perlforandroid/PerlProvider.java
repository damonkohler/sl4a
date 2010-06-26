package com.googlecode.perlforandroid;

import java.util.Map;

import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

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
