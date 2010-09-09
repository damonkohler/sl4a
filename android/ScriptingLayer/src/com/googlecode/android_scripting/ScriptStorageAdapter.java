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

package com.googlecode.android_scripting;

import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages storage and retrieval of scripts on the file system.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptStorageAdapter {

  private ScriptStorageAdapter() {
    // Utility class.
  }

  /**
   * Writes data to the script by name and overwrites any existing data.
   */
  public static void writeScript(File script, String data) {
    if (script.getParent() == null) {
      script = new File(InterpreterConstants.SCRIPTS_ROOT, script.getPath());
    }
    try {
      FileWriter stream = new FileWriter(script, false /* overwrite */);
      BufferedWriter out = new BufferedWriter(stream);
      out.write(data);
      out.close();
    } catch (IOException e) {
      Log.e("Failed to write script.", e);
    }
  }

  /**
   * Returns a list of all available script {@link File}s.
   */
  public static List<File> listAllScripts(File dir) {
    if (dir == null) {
      dir = new File(InterpreterConstants.SCRIPTS_ROOT);
    }
    if (dir.exists()) {
      List<File> scripts = Arrays.asList(dir.listFiles());
      Collections.sort(scripts, new Comparator<File>() {
        @Override
        public int compare(File file1, File file2) {
          if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
          } else if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
          }
          return file1.compareTo(file2);
        }
      });
      return scripts;
    }
    return new ArrayList<File>();
  }

  /**
   * Returns a list of script {@link File}s from the given folder for which there is an interpreter
   * installed.
   */
  public static List<File> listExecutableScripts(File directory, InterpreterConfiguration config) {
    // NOTE(damonkohler): Creating a LinkedList here is necessary in order to be able to filter it
    // later.
    List<File> scripts = new LinkedList<File>(listAllScripts(directory));
    // Filter out any files that don't have interpreters installed.
    for (Iterator<File> it = scripts.iterator(); it.hasNext();) {
      File script = it.next();
      if (script.isDirectory()) {
        continue;
      }
      Interpreter interpreter = config.getInterpreterForScript(script.getName());
      if (interpreter == null || !interpreter.isInstalled()) {
        it.remove();
      }
    }
    return scripts;
  }

  /**
   * Returns a list of all (including subfolders) script {@link File}s for which there is an
   * interpreter installed.
   */
  public static List<File> listExecutableScriptsRecursively(File directory,
      InterpreterConfiguration config) {
    // NOTE(damonkohler): Creating a LinkedList here is necessary in order to be able to filter it
    // later.
    List<File> scripts = new LinkedList<File>();
    List<File> files = listAllScripts(directory);

    // Filter out any files that don't have interpreters installed.
    for (Iterator<File> it = files.iterator(); it.hasNext();) {
      File file = it.next();
      if (file.isDirectory()) {
        scripts.addAll(listExecutableScriptsRecursively(file, config));
      }
      Interpreter interpreter = config.getInterpreterForScript(file.getName());
      if (interpreter != null && interpreter.isInstalled()) {
        scripts.add(file);
      }
    }
    Collections.sort(scripts);
    return scripts;
  }
}