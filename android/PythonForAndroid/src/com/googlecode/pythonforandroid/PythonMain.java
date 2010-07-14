package com.googlecode.pythonforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.activity.Main;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class PythonMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PythonDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PythonInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PythonUninstaller(descriptor, context, listener);
  }
}