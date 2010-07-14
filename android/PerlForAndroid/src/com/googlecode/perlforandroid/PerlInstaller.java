package com.googlecode.perlforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class PerlInstaller extends InterpreterInstaller {

  public PerlInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    return true;
  }
}
