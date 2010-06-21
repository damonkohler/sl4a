package com.googlecode.pythonforandroid;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.activity.InterpreterInstaller;

import java.io.File;

public class PythonInstaller extends InterpreterInstaller {

  @Override
  protected boolean setup() {

    File tmp = new File(Constants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName() + "/tmp");
    if (!tmp.isDirectory()) {
      try{
        tmp.mkdir();
      }catch(SecurityException e){
        AseLog.e(this, "Setup failed.", e);
        return false;
      }
    }
    return true;
  }

  @Override
  protected boolean isInstalled() {
    String packageName = getClass().getPackage().getName();
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    return preferences.getBoolean(packageName, false);
  }

}
