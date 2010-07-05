package com.dummy.fooforandroid;

import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterProvider;

import java.util.Map;

public class FooProvider extends InterpreterProvider {

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new FooDescriptor();
  }

  @Override
  protected Map<String, String> getEnvironmentSettings() {
    // TODO Auto-generated method stub
    return null;
  }

}
