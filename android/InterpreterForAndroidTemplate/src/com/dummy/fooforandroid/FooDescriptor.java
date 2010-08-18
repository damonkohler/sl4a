package com.dummy.fooforandroid;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

import java.io.File;
import java.util.List;
import java.util.Map;

public class FooDescriptor implements InterpreterDescriptor {

  @Override
  public String getName() {
    return null;
  }

  @Override
  public String getNiceName() {
    return null;
  }

  @Override
  public String getExtension() {
    return null;
  }

  @Override
  public File getBinary(Context context) {
    return null;
  }

  @Override
  public List<String> getArguments(Context context) {
    return null;
  }

  @Override
  public String getInteractiveCommand(Context context) {
    return null;
  }

  @Override
  public String getScriptCommand(Context context) {
    return null;
  }

  @Override
  public int getVersion() {
    return 0;
  }

  @Override
  public boolean hasInterpreterArchive() {
    return false;
  }

  @Override
  public String getInterpreterArchiveName() {
    return null;
  }

  @Override
  public String getInterpreterArchiveUrl() {
    return null;
  }

  @Override
  public boolean hasExtrasArchive() {
    return false;
  }

  @Override
  public String getExtrasArchiveName() {
    return null;
  }

  @Override
  public String getExtrasArchiveUrl() {
    return null;
  }

  @Override
  public boolean hasScriptsArchive() {
    return false;
  }

  @Override
  public String getScriptsArchiveName() {
    return null;
  }

  @Override
  public String getScriptsArchiveUrl() {
    return null;
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context arg0) {
    return null;
  }

  @Override
  public boolean hasInteractiveMode() {
    return false;
  }
}
