package com.googlecode.pythonforandroid;

import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;
import com.google.ase.language.LanguageStrings;

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
    File parent = mContext.getFilesDir().getParentFile();
    File file = new File(parent, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getExtras() {
    File file = new File(Constants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName());
    return file.getAbsolutePath();
  }

  private String getTemp() {
    File tmp = new File(Constants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName() + "/tmp");
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

  @Override
  protected Map<String, String> getLanguageSettings() {
    Map<String, String> settings = new HashMap<String, String>(3);
    settings.put(LanguageStrings.IMPORT, "import android\n");
    settings.put(LanguageStrings.QUOTE, "'");
    settings.put(LanguageStrings.NULL, "None");
    settings.put(LanguageStrings.TRUE, "True");
    settings.put(LanguageStrings.RPC_RECEIVER_DECLARATION, "%s = android.Android()\n");
    return settings;
  }

}
