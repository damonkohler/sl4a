package com.googlecode.luaforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class LuaInstaller extends InterpreterInstaller {

  public LuaInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    return true;
  }
}
