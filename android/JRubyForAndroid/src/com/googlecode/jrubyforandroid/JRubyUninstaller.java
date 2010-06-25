package com.googlecode.jrubyforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterDescriptor;

public class JRubyUninstaller extends InterpreterUninstaller {

  public JRubyUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean cleanup() {
    return true;
  }
}
