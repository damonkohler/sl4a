package com.google.ase.interpreter;

public interface InterpreterDescriptor {

  public String getName();

  public String getNiceName();

  public String getExtension();

  public int getVersion();

  public String getBinary();
  
  public String getEmptyCommand();
  
  public String getExecuteParams();

  public boolean hasInterpreterArchive();

  public boolean hasExtrasArchive();

  public boolean hasScriptsArchive();
  
  public String getInterpreterArchiveName();

  public String getExtrasArchiveName();

  public String getScriptsArchiveName();
  
  public String getInterpreterArchiveUrl();

  public String getScriptsArchiveUrl();
  
  public String getExtrasArchiveUrl();

}
