package com.googlecode.jrubyforandroid;


import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

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
    settings.put(ENV_DATA, InterpreterConstants.SDCARD_SL4A_ROOT);
    return settings;
  }

}
