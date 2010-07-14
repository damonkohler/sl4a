package com.googlecode.perlforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.activity.Main;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class PerlMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PerlDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PerlInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PerlUninstaller(descriptor, context, listener);
  }

}