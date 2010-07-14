package com.googlecode.rhinoforandroid;

import android.content.Context;


import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.InterpreterUninstaller;
import com.googlecode.android_scripting.activity.Main;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class RhinoMain extends Main {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new RhinoDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new RhinoInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new RhinoUninstaller(descriptor, context, listener);
  }

}