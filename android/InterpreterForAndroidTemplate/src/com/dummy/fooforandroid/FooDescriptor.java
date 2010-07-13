package com.dummy.fooforandroid;

import android.content.Context;

import com.google.ase.interpreter.InterpreterDescriptor;

public class FooDescriptor implements InterpreterDescriptor {

  public String getName() {
    return null;
  }

  public String getNiceName() {
    return null;
  }

  public String getExtension() {
    return null;
  }

  public String getBinary() {
    return null;
  }

  public String getPath(Context arg0) {
    return null;
  }

  public int getVersion() {
    return 0;
  }

  public String getEmptyParams(Context context) {
    return null;
  }

  public String getExecuteCommand(Context context) {
    return null;
  }

  public String getExecuteParams(Context context) {
    return null;
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public String getInterpreterArchiveName() {
    return null;
  }

  public String getInterpreterArchiveUrl() {
    return null;
  }

  public boolean hasExtrasArchive() {
    return false;
  }

  public String getExtrasArchiveName() {
    return null;
  }

  public String getExtrasArchiveUrl() {
    return null;
  }

  public boolean hasScriptsArchive() {
    return false;
  }

  public String getScriptsArchiveName() {
    return null;
  }

  public String getScriptsArchiveUrl() {
    return null;
  }

  public String[] getExecuteArgs(Context context) {
    return null;
  }

}
