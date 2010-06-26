package com.googlecode.tclforandroid;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

public class TclProvider extends InterpreterProvider {

  private final static String ENV_HOME = "TCL_LIBRARY";
  private final static String ENV_LIB = "TCLLIBPATH";
  private final static String ENV_SCRIPTS = "TCL_SCRIPTS";
  private final static String ENV_TEMP = "TEMP";
  private final static String ENV_HOME_GLOBAL = "HOME";

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new TclDescriptor();
  }

  private String getHome() {
    File parent = mContext.getFilesDir().getParentFile();
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
    Map<String, String> settings = new HashMap<String, String>(5);
    settings.put(ENV_HOME, getHome());
    settings.put(ENV_LIB, getExtras());
    settings.put(ENV_TEMP, getTemp());
    settings.put(ENV_SCRIPTS, InterpreterConstants.SCRIPTS_ROOT);
    settings.put(ENV_HOME_GLOBAL, InterpreterConstants.SDCARD_ASE_ROOT);
    return settings;
  }

}
