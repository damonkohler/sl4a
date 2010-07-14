package com.googlecode.perlforandroid;

import java.util.Map;


import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

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
