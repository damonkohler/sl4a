package com.googlecode.pythonforandroid;

import com.google.ase.AseLog;
import com.google.ase.activity.InterpreterInstaller;
import com.google.ase.interpreter.InterpreterConstants;

import java.io.File;

public class PythonInstaller extends InterpreterInstaller {

  @Override
  protected boolean setup() {

    File tmp =
        new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName() + "/tmp");
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
}
