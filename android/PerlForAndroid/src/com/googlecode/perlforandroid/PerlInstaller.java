package com.googlecode.perlforandroid;

import android.content.Context;


import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.InterpreterInstaller;
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
