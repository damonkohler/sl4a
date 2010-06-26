package com.googlecode.rhinoforandroid;

import java.io.File;

import android.content.Context;

import com.google.ase.AseLog;
import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;

public class RhinoInstaller extends InterpreterInstaller {

  public RhinoInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    File dalvikCache = new File(InterpreterConstants.ASE_DALVIK_CACHE_ROOT);
    if (!dalvikCache.isDirectory()) {
      try {
        dalvikCache.mkdir();
      } catch (SecurityException e) {
        AseLog.e(mContext, "Setup failed.", e);
        return false;
      }
    }
    return true;
  }
}
