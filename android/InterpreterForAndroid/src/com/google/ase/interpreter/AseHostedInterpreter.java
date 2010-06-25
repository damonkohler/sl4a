package com.google.ase.interpreter;

import android.content.Context;

import java.io.File;

public abstract class AseHostedInterpreter implements InterpreterDescriptor {

  public static final String BASE_INSTALL_URL = "http://android-scripting.googlecode.com/files/";

  public String getInterpreterArchiveName() {
    return String.format("%s_r%s.zip", getName(), getVersion());
  }

  public String getExtrasArchiveName() {
    return String.format("%s_extras_r%s.zip", getName(), getVersion());
  }

  public String getScriptsArchiveName() {
    return String.format("%s_scripts_r%s.zip", getName(), getVersion());
  }

  public String getInterpreterArchiveUrl() {
    return BASE_INSTALL_URL + getInterpreterArchiveName();
  }

  public String getExtrasArchiveUrl() {
    return BASE_INSTALL_URL + getExtrasArchiveName();
  }

  public String getScriptsArchiveUrl() {
    return BASE_INSTALL_URL + getScriptsArchiveName();
  }

  public String getPath(Context context) {
    if (!hasInterpreterArchive() && hasExtrasArchive()) {
      return new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, getName()).getAbsolutePath();
    }
    if (context == null) {
      return null;
    }
    return InterpreterUtils.getInterpreterRoot(context, getName()).getAbsolutePath();
  }

  public String getExecuteCommand() {
    return "%1$s%2$s%3$s";
  }

}
