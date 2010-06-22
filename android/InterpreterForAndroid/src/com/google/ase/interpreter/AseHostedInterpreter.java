package com.google.ase.interpreter;

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

}
