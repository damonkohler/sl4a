package com.googlecode.tclforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.activity.Main;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterDescriptor;

public class TclMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new TclDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws AseException {
    return new TclInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws AseException {
    return new TclUninstaller(descriptor, context, listener);
  }

}