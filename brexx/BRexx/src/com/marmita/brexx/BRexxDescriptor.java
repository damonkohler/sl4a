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

package com.marmita.brexx;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides interpreter-specific info for execution/installation/removal purposes.
 *
 * @author Vasilis Vlachoudis (Vasilis.Vlachoudis@cern.ch)
 */
public class BRexxDescriptor implements InterpreterDescriptor {

  private static final String REXX_BIN        = "brexx/bin/rexx";
  private static final String ENV_HOME        = "HOME";
  private static final String ENV_LIB         = "RXLIB";
  private static final String ENV_TEMP        = "TEMP";
  private static final String ENV_LD          = "LD_LIBRARY_PATH";
  public static final String BASE_INSTALL_URL = "http://pceet075.cern.ch/bnv/brexx/";

  /**
   * Returns interpreter version number.
   */
  @Override
  public int getVersion() {
    return 2;
  }

  /**
   * Returns unique name of the interpreter.
   */
  @Override
  public String getName() {
    return "brexx";
  }

  /**
   * Returns display name of the interpreter.
   */
  @Override
  public String getNiceName() {
    return "BRexx 2.1.9";
  }

  /**
   * Returns supported script-file extension.
   */
  @Override
  public String getExtension() {
    return ".r";
  }

  /**
   * Returns the binary as a File object. Context is the InterpreterProvider's {@link Context} and
   * is provided to find the interpreter installation directory.
   */
  @Override
  public File getBinary(Context context) {
    return new File(context.getFilesDir(), REXX_BIN);
  }

 /**
   * Returns execution parameters in case when script name is not provided (when interpreter is
   * started in a shell mode);
   */
  @Override
  public String getInteractiveCommand(Context context) {
    return "-i";
  }

  /**
   * Returns command line arguments to execute a with a given script (format string with one
   * argument).
   */
  @Override
  public String getScriptCommand(Context context) {
    return "%s";
  }

  /**
   * Returns an array of command line arguments required to execute the interpreter (it's essential
   * that the order in the array is consistent with order of arguments in the command line).
   */
  @Override
  public List<String> getArguments(Context context) {
    return new ArrayList<String>();
  }

  /**
   * Should return a map of environment variables names and their values (or null if interpreter
   * does not require any environment variables).
   */
  @Override
  public Map<String, String> getEnvironmentVariables(Context context) {
    Map<String, String> values = new HashMap<String, String>();
    values.put(ENV_HOME, getHome(context));
    values.put(ENV_LD,  new File(getHome(context), String.format("%s/lib",getName())).getAbsolutePath());
//    values.put(ENV_LIB, new File(getHome(context), "lib/python2.6/python.zip/python") + ":"
//        + new File(getHome(context), "lib/python2.6/python.zip/python/site-packages") + ":"
//        + new File(getHome(context), "lib/python2.6/lib-dynload"));
      values.put(ENV_LIB, String.format("%s%s/lib",getExtrasRoot(),getName()));
    values.put(ENV_TEMP, getTemp());
    return values;
  }

  /**
   * Returns true if interpreter has an archive.
   */
  @Override
  public boolean hasInterpreterArchive() {
    return true;
  }

  /**
   * Returns true if interpreter has an extras archive.
   */
  @Override
  public boolean hasExtrasArchive() {
    return true;
  }

  /**
   * Returns true if interpreter comes with a scripts archive.
   */
  @Override
  public boolean hasScriptsArchive() {
    return true;
  }

  /**
   * Returns file name of the interpreter archive.
   */
  @Override
  public String getInterpreterArchiveName() {
    return String.format("%s.zip", getName());
  }

  /**
   * Returns file name of the scripts archive.
   */
  @Override
  public String getExtrasArchiveName() {
    return String.format("%s_extras.zip", getName());
  }

  /**
   * Returns file name of the scripts archive.
   */
  @Override
  public String getScriptsArchiveName() {
    return String.format("%s_scripts.zip", getName());
  }

  /**
   * Returns URL location of the interpreter archive.
   */
  @Override
  public String getInterpreterArchiveUrl() {
    return BASE_INSTALL_URL + getInterpreterArchiveName();
  }

  /**
   * Returns URL location of the extras archive.
   */
  @Override
  public String getExtrasArchiveUrl() {
    return BASE_INSTALL_URL + getExtrasArchiveName();
  }

  /**
   * Returns URL location of the scripts archive.
   */
  @Override
  public String getScriptsArchiveUrl() {
    return BASE_INSTALL_URL + getScriptsArchiveName();
  }

  /**
   * Returns true if interpreter can be executed in interactive mode.
   */
   @Override
  public boolean hasInteractiveMode() {
    return true;
  }

  public File getExtrasPath(Context context) {
    if (!hasInterpreterArchive() && hasExtrasArchive()) {
      return new File(InterpreterConstants.SDCARD_ROOT + this.getClass().getPackage().getName()
          + InterpreterConstants.INTERPRETER_EXTRAS_ROOT, getName());
    }
    return InterpreterUtils.getInterpreterRoot(context, getName());
  }

  private String getExtrasRoot() {
    return InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName()
        + InterpreterConstants.INTERPRETER_EXTRAS_ROOT;
  }

  private String getHome(Context context) {
    return context.getFilesDir().getAbsolutePath();
  }

  private String getTemp() {
    File tmp = new File(getExtrasRoot(),"tmp");
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    return tmp.getAbsolutePath();
  }
}
