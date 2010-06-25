package com.googlecode.luaforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterDescriptor;

public class LuaInstaller extends InterpreterInstaller {

  public LuaInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    return true;
  }
}
