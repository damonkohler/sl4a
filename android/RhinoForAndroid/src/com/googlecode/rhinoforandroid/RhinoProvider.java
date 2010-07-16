package com.googlecode.rhinoforandroid;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

import java.util.HashMap;
import java.util.Map;

public class RhinoProvider extends InterpreterProvider {

  private static final String ENV_DATA = "ANDROID_DATA";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new RhinoDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(1);
    settings.put(ENV_DATA, InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName());
    return settings;
  }

}
