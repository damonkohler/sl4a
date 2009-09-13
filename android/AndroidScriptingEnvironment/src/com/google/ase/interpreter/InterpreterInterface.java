/*
 * Copyright (C) 2009 Google Inc.
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

import java.io.File;

import com.google.ase.AndroidFacade;

/**
 * Represents an interpreter.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public interface InterpreterInterface {

  /**
   * Returns the name of the interpreter.
   */
  String getName();

  /**
   * Returns the filename extension for scripts to be run by the interpreter.
   */
  String getExtension();

  /**
   * Returns a new process for the interpreter.
   */
  InterpreterProcessInterface buildProcess(AndroidFacade facade, String launchScript);

  /**
   * Returns a nicer looking name.
   */
  String getNiceName();

  /**
   * Returns true if the interpreter is installed.
   */
  boolean isInstalled();

  /**
   * Returns a helpful template of typical script content.
   */
  String getContentTemplate();

  /**
   * Returns the URL where the interpreter can be downloaded from.
   *
   * @param interpreterVersion
   */
  String getInterpreterArchiveUrl(String version);

  /**
   * Returns the URL where the interpreter extras can be downloaded from.
   */
  String getInterpreterExtrasArchiveUrl(String version);

  /**
   * Returns the URL where the example scripts can be downloaded from.
   */
  String getScriptsArchiveUrl(String version);

  /**
   * Returns the name of the archive that contains the interpreter.
   */
  String getInterpreterArchiveName(String version);

  /**
   * Returns the name of the archive that contains the interpreter extras.
   */
  String getInterpreterExtrasArchiveName(String version);

  /**
   * Returns the name of the archive that contains the sample scripts.
   */
  String getScriptsArchiveName(String version);

  boolean hasInterpreterArchive();

  boolean hasInterpreterExtrasArchive();

  boolean hasScriptsArchive();

  File getBinary();

}
