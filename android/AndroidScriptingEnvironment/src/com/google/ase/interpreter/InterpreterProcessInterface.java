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

import java.io.FileDescriptor;
import java.io.PrintStream;
import java.io.Reader;

/**
 * Represents and manages a script subprocess.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public interface InterpreterProcessInterface {

  /**
   * Returns the PID of the subprocess.
   */
  public abstract Integer getPid();

  /**
   * Returns the {@link FileDescriptor} for the shell process.
   */
  public abstract FileDescriptor getFd();

  /**
   * Starts the interpreter subprocess.
   */
  public abstract void start();

  /**
   * Kills the subprocess.
   */
  public abstract void kill();

  PrintStream getOut();

  PrintStream getErr();

  Reader getIn();

  void error(Object obj);

  void print(Object obj);

  void println(Object obj);
}
