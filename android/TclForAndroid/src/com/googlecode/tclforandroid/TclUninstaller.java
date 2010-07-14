package com.googlecode.tclforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterUninstaller;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class TclUninstaller extends InterpreterUninstaller {

  public TclUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean cleanup() {
    return true;
  }
}
