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

/**
 * A collection of string for querying InterpreterProvider.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterStrings {

  /**
   * Unique name of the interpreter.
   */
  public static final String NAME = "name";

  /**
   * Display name of the interpreter.
   */
  public static final String NICE_NAME = "niceName";

  /**
   * Supported script file extension.
   */
  public static final String EXTENSION = "extension";

  /**
   * Name (and path within working directory) of the interpreter executable.
   */
  public static final String BIN = "binary";

  /**
   * Path to the working directory of the interpreter.
   */
  public static final String PATH = "path";

  /**
   * Specifies command line arguments when script name is not provided.
   */
  public static final String EMPTY_PARAMS = "emptyParams";

  /**
   * Specifies command line arguments to execute a script.
   */
  public static final String EXECUTE_PARAMS = "executeParams";

  /**
   * Execution command.
   */
  public static final String EXECUTE = "execute";

  /**
   * Execution arguments.
   */
  public static final String ARGS = "arguments";

}
