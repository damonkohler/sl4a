package com.googlecode.jrubyforandroid;

import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

import java.util.HashMap;
import java.util.Map;

public class JRubyProvider extends InterpreterProvider {

  private static final String ENV_DATA = "ANDROID_DATA"; // Is it really used somewhere?

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new JRubyDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(1);
    settings.put(ENV_DATA, InterpreterConstants.SDCARD_ASE_ROOT);
    return settings;
  }

}
