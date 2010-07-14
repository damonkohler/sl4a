package com.googlecode.rhinoforandroid;

import java.io.File;

import android.content.Context;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;

import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class RhinoInstaller extends InterpreterInstaller {

  public RhinoInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    File dalvikCache = new File(InterpreterConstants.ASE_DALVIK_CACHE_ROOT);
    if (!dalvikCache.isDirectory()) {
      try {
        dalvikCache.mkdir();
      } catch (SecurityException e) {
        Sl4aLog.e(mContext, "Setup failed.", e);
        return false;
      }
    }
    return true;
  }
}
