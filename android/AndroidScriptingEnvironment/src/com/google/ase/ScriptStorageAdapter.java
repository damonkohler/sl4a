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

package com.google.ase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterAgent;

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
  public static void writeScript(String name, String data) {
    if (name.length() == 0) {
      AseLog.e("No script name specified.");
      return;
    }

    File scriptsDirectory = new File(InterpreterConstants.SCRIPTS_ROOT);
    if (!scriptsDirectory.exists()) {
      AseLog.v("Creating scripts directory: " + InterpreterConstants.SCRIPTS_ROOT);
      if (!scriptsDirectory.mkdirs()) {
        AseLog.e("Failed to create scripts directory.");
      }
    }

    File scriptFile = getScript(name);
    try {
      FileWriter stream = new FileWriter(scriptFile, false /* overwrite */);
      BufferedWriter out = new BufferedWriter(stream);
      out.write(data);
      out.close();
    } catch (IOException e) {
      Log.e("Script Storage", "Failed to write script file.", e);
    }
  }

  /**
   * Deletes the specified script by name.
   */
  public static void deleteScript(String name) {
    File scriptFile = getExistingScript(name);
    if (scriptFile == null) {
      return;
    }
    if (scriptFile.exists()) {
      if (!scriptFile.delete()) {
        AseLog.e("Failed to delete script.");
      }
    } else {
      AseLog.e("Script does not exist.");
    }
  }

  /**
   * Returns a list of all available script {@link File}s.
   */
  public static List<File> listAllScripts() {
    File dir = new File(InterpreterConstants.SCRIPTS_ROOT);
    if (dir.exists()) {
      List<File> scripts = Arrays.asList(new File(InterpreterConstants.SCRIPTS_ROOT).listFiles());
      Collections.sort(scripts);
      return scripts;
    }
    return new ArrayList<File>();
  }

  /**
   * Returns a list of all script {@link File}s for which there is an interpreter installed.
   */
  public static List<File> listExecutableScripts(Context context, InterpreterConfiguration config) {
    File dir = new File(InterpreterConstants.SCRIPTS_ROOT);
    if (dir.exists()) {
      // NOTE(damonkohler): Creating a LinkedList here is necessary in order to be able to filter it
      // later.
      List<File> scripts =
          new LinkedList<File>(Arrays.asList(new File(InterpreterConstants.SCRIPTS_ROOT)
              .listFiles()));
      // Filter out any files that don't have interpreters installed.
      for (Iterator<File> it = scripts.iterator(); it.hasNext();) {
        InterpreterAgent interpreter =
            config.getInterpreterForScript(it.next().getName());
        if (interpreter == null || !interpreter.isInstalled(context)) {
          it.remove();
        }
      }
      Collections.sort(scripts);
      return scripts;
    }
    return new ArrayList<File>();
  }

  /**
   * Returns the {@link File} object for the script or null if the script does not exist.
   * 
   * @param name
   *          the name of the script to access
   */
  public static File getExistingScript(String name) {
    File scriptFile = getScript(name);
    if (scriptFile.exists()) {
      return scriptFile;
    }
    return null;
  }

  private static File getScript(String name) {
    return new File(InterpreterConstants.SCRIPTS_ROOT, name);
  }

  /**
   * Returns the content of the specified script or null if the script does not exist.
   */
  public static String readScript(String name) throws IOException {
    File scriptFile = getExistingScript(name);
    if (scriptFile == null) {
      return null;
    }
    FileReader reader = new FileReader(scriptFile);
    StringBuilder out = new StringBuilder();
    char[] buffer = new char[1024 * 4];
    int numRead = 0;
    while ((numRead = reader.read(buffer)) > -1) {
      out.append(String.valueOf(buffer, 0, numRead));
    }
    reader.close();
    return out.toString();
  }
}