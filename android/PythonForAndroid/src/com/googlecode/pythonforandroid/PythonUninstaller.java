package com.googlecode.pythonforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterDescriptor;

public class PythonUninstaller extends InterpreterUninstaller {

  public PythonUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean cleanup() {
    return true;
  }
}
