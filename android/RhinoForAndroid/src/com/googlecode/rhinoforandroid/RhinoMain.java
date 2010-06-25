package com.googlecode.rhinoforandroid;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.InterpreterUninstaller;
import com.google.ase.activity.Main;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterDescriptor;



public class RhinoMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new RhinoDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws AseException {
    return new RhinoInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws AseException {
    return new RhinoUninstaller(descriptor, context, listener);
  }

  
}