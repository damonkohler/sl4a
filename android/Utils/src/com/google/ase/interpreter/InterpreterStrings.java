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
 * A collection of string for querying InterpreterProvider:
 * 
 * NAME - unique name of the interpreter;<br>
 * NICE_NAME - display name of the interpreter;<br>
 * EXTENSION - supported script file extension;<br>
 * BIN - name (and path within working directory) of the interpreter executable;<br>
 * PATH - path to the working directory of the interpreter;<br>
 * EMPTY_PARAMS - specifies command line arguments when script name is not provided;<br>
 * EXECUTE_PARAMS - specifies command line arguments to execute a script;<br>
 * EXECUTE - execution command.<br>
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterStrings {

  public static final String NAME = "name";

  public static final String NICE_NAME = "niceName";

  public static final String EXTENSION = "extension";

  public static final String BIN = "binary";

  public static final String PATH = "path";

  public static final String EMPTY_PARAMS = "emptyParams";

  public static final String EXECUTE_PARAMS = "executeParams";

  public static final String EXECUTE = "execute";

}
