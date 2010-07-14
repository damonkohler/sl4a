package com.googlecode.bshforandroid;

import java.util.HashMap;
import java.util.Map;


import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

public class BshProvider extends InterpreterProvider {

  private static final String ENV_DATA = "ANDROID_DATA";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new BshDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(1);
    settings.put(ENV_DATA, InterpreterConstants.SDCARD_ASE_ROOT);
    return settings;
  }

}
