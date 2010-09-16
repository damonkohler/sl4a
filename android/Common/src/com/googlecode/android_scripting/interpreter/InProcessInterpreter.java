package com.googlecode.android_scripting.interpreter;

import java.io.FileDescriptor;

public interface InProcessInterpreter {
  public FileDescriptor getStdOut();

  public FileDescriptor getStdIn();

  public boolean runInteractive();

  public void runScript(String filename);
}
