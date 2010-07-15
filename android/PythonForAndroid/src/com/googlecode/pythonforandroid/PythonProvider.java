package com.googlecode.pythonforandroid;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterProvider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PythonProvider extends InterpreterProvider {

  private static final String ENV_HOME = "PYTHONHOME";
  private static final String ENV_PATH = "PYTHONPATH";
  private static final String ENV_TEMP = "TEMP";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PythonDescriptor();
  }

  private String getHome() {
    File parent = mContext.getFilesDir();
    File file = new File(parent, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getExtras() {
    File file = new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getTemp() {
    File tmp =
        new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName() + "/tmp");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    return tmp.getAbsolutePath();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    Map<String, String> settings = new HashMap<String, String>(3);
    settings.put(ENV_HOME, getHome());
    settings.put(ENV_PATH, getExtras());
    settings.put(ENV_TEMP, getTemp());
    return settings;
  }

}
