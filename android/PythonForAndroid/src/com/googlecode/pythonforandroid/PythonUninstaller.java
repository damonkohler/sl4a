package com.googlecode.pythonforandroid;

import com.google.ase.activity.InterpreterUninstaller;

public class PythonUninstaller extends InterpreterUninstaller {

  @Override
  protected boolean cleanup() {
    return true;
  }
}
