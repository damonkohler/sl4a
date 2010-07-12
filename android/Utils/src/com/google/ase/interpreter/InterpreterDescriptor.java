/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.ase.interpreter;

import android.content.Context;

/**
 * Provides interpreter-specific info for execution/installation/removal purposes.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterDescriptor {

  // The following methods should be implemented to provide general interpreter information:

  /**
   * Returns unique name of the interpreter.
   */
  public String getName();

  /**
   * Returns display name of the interpreter.
   */
  public String getNiceName();

  /**
   * Returns supported script-file extension.interpreter
   */
  public String getExtension();

  /**
   * Returns interpreter version number.
   */
  public int getVersion();

  // The following methods provide execution-related information:

  /**
   * Returns name (and path within working directory) of the interpreter executable.
   */
  public String getBinary();

  /**
   * Returns path to the interpreter working directory, i.e. path to the installation directory.
   */
  public String getPath(Context context);

  /**
   * Returns execution command as a format string. The string can contain up to three specifiers,
   * where first refers to the path (as returned by the getPath()), second - to the binary (as
   * returned by the getBinary()), and third - to the execution parameters (constructed using
   * getEmptyParams() or getExecuteParams()).
   */
  public String getExecuteCommand(Context context);

  /**
   * Returns execution parameters in case when script name is not provided;
   */
  public String getEmptyParams(Context context);

  /**
   * Returns command line arguments to execute a with a given script (format string with one
   * argument).
   */
  public String getExecuteParams(Context context);

  /**
   * TODO(Alexey): Update Javadoc.
   */
  public String[] getExecuteArgs(Context context);

  // The following methods are required for installation:

  /**
   * Returns true if interpreter has an archive.
   */
  public boolean hasInterpreterArchive();

  /**
   * Returns true if interpreter has an extras archive.
   */
  public boolean hasExtrasArchive();

  /**
   * Returns true if interpreter comes with a scripts archive.
   */
  public boolean hasScriptsArchive();

  /**
   * Returns file name of the interpreter archive.
   */
  public String getInterpreterArchiveName();

  /**
   * Returns file name of the extras archive.
   */
  public String getExtrasArchiveName();

  /**
   * Returns file name of the scripts archive.
   */
  public String getScriptsArchiveName();

  /**
   * Returns URL location of the interpreter archive.
   */
  public String getInterpreterArchiveUrl();

  /**
   * Returns URL location of the scripts archive.
   */
  public String getScriptsArchiveUrl();

  /**
   * Returns URL location of the extras archive.
   */
  public String getExtrasArchiveUrl();

}
