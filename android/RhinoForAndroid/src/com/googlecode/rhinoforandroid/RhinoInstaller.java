package com.googlecode.rhinoforandroid;

import android.content.Context;

import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

import java.io.File;

public class RhinoInstaller extends InterpreterInstaller {

  public RhinoInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {
    super(descriptor, context, listener);
  }

  @Override
  protected boolean setup() {
    File dalvikCache = new File(mInterpreterRoot + InterpreterConstants.SL4A_DALVIK_CACHE_ROOT);
    if (!dalvikCache.isDirectory()) {
      try {
        dalvikCache.mkdir();
      } catch (SecurityException e) {
        Log.e(mContext, "Setup failed.", e);
        return false;
      }
    }
    return true;
  }
}
