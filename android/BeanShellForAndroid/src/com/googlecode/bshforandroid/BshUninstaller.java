package com.googlecode.bshforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterUninstaller;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class BshUninstaller extends InterpreterUninstaller {

  public BshUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean cleanup() {
    return true;
  }
}
