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

package com.googlecode.android_scripting.interpreter;

import android.content.Context;

/**
 * Provides interpreter-specific info for execution/installation/removal purposes.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterDescriptor {

  /**
   * Returns unique name of the interpreter.
   */
  public String getName();

  /**
   * Returns display name of the interpreter.
   */
  public String getNiceName();

  /**
   * Returns supported script-file extension.
   */
  public String getExtension();

  /**
   * Returns interpreter version number.
   */
  public int getVersion();

  // The following methods provide execution-related information:

  /**
   * Returns name (and path within working directory) of the interpreter binary.
   */
  public String getBinary();

  /**
   * Returns path to the interpreter working directory, i.e. path to the installation directory. If
   * interpreter binaries provided as a part of interpreter archive, by default they will be
   * extracted in /data/data/package_name/files/ - as returned by
   * InterpreterUtils.getInterpreterRoot(mContext).getAbsolutePath(). Therefore, this should return
   * InterpreterUtils.getInterpreterRoot(context, interpreter_name).getAbsolutePath(), where
   * interpreter_name is the name of the interpreter folder in the archive (which by default should
   * be the same as returned by getName()). If interpreter binaries provided as a part of extras
   * archive, by default they will be extracted in /sdcard/package_name/extras/. In this case, this
   * should return /sdcard/package_name/extras/interpreter_name/.
   * 
   */
  public String getPath(Context context);

  /**
   * Returns the command (path and name of the interpreter's executable - in the most common case it
   * can return String.format("%1$s/%2$s", getPath(context), getBinary())).
   */
  public String getExecuteCommand(Context context);

  /**
   * Returns execution parameters in case when script name is not provided (when interpreter is
   * started in a shell mode);
   */
  public String getEmptyParams(Context context);

  /**
   * Returns command line arguments to execute a with a given script (format string with one
   * argument).
   */
  public String getExecuteParams(Context context);

  /**
   * Returns an array of command line arguments required to execute the interpreter (it's essential
   * that the order in the array is consistent with order of arguments in the command line).
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
