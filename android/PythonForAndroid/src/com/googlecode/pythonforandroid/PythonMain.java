package com.googlecode.pythonforandroid;

import com.google.ase.activity.InterpreterInstaller;
import com.google.ase.activity.InterpreterUninstaller;
import com.google.ase.activity.Main;
import com.google.ase.interpreter.InterpreterDescriptor;



public class PythonMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PythonDescriptor();
  }

  @Override
  protected Class<? extends InterpreterInstaller> getInstallerClass() {
    return PythonInstaller.class;
  }

  @Override
  protected Class<? extends InterpreterUninstaller> getUnstallerClass() {
    return PythonUninstaller.class;
  }
  
}