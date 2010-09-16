package com.googlecode.pythonforandroid;

import com.googlecode.android_scripting.interpreter.InProcessInterpreter;

import java.io.FileDescriptor;

public class PythonInProcessInterpreter implements InProcessInterpreter {

  static {
    System.loadLibrary("python2.6");
    System.loadLibrary("pythonInProcess");
  }

  @Override
  public FileDescriptor getStdIn() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public FileDescriptor getStdOut() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean runInteractive() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public native void runScript(String filename);
}
