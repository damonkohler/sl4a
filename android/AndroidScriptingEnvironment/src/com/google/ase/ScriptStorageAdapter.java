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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.util.Log;

/**
 * Manages storage and retrieval of scripts on the file system.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptStorageAdapter {

  private static final String TAG = "ScriptStorageAdapter";

  private ScriptStorageAdapter() {
    // This is a utility class.
  }

  /**
   * Writes data to the script by name and overwrites any existing data.
   */
  // TODO(damonkohler): Sanitize the script name.
  // TODO(damonkohler): Raise exceptions or provide a return value to indicate
  // success/failure.
  public static void writeScript(String name, String data) {
    // TODO(damonkohler): Move this logic to sanitization or GUI.
    if (name.length() == 0) {
      Log.e(TAG, "No script name specified.");
      return;
    }

    File scriptsDirectory = new File(Constants.SCRIPTS_ROOT);
    if (!scriptsDirectory.exists()) {
      Log.v(TAG, "Creating scripts directory: " + Constants.SCRIPTS_ROOT);
      if (!scriptsDirectory.mkdirs()) {
        Log.e(TAG, "Failed to create scripts directory.");
      }
    }

    // This returns the File object even if the script doesn't exist.
    File scriptFile = getScriptFile(name);
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
  // TODO(damonkohler): Sanitize the script name.
  public static void deleteScript(String name) {
    File scriptFile = getScript(name);
    if (scriptFile == null) {
      return;
    }

    Log.v(TAG, "Deleting script: " + scriptFile.getAbsolutePath());
    if (scriptFile.exists()) {
      if (!scriptFile.delete()) {
        Log.e(TAG, "Failed to delete script.");
      }
    } else {
      Log.e(TAG, "Script does not exist.");
    }
  }

  /**
   * Returns a list of all available script {@link File}s.
   */
  public static List<File> listScripts() {
    File dir = new File(Constants.SCRIPTS_ROOT);
    if (dir.exists()) {
      List<File> scripts = Arrays.asList(new File(Constants.SCRIPTS_ROOT).listFiles());
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
  // TODO(damonkohler): Sanitize the script name.
  public static File getScript(String name) {
    File scriptFile = getScriptFile(name);
    if (scriptFile.exists()) {
      return scriptFile;
    }
    return null;
  }

  private static File getScriptFile(String name) {
    // TODO(damonkohler): Check for a working extension. If none exists, prompt
    // the user to choose one.
    File scriptFile = new File(Constants.SCRIPTS_ROOT + name);
    return scriptFile;
  }

  /**
   * Returns the content of the specified script or null if the script does not exist.
   */
  // TODO(damonkohler): There's probably a better way to do this. Maybe Apache
  // IO Utils?
  public static String readScript(String name) throws IOException {
    File scriptFile = getScript(name);
    if (scriptFile == null) {
      return null;
    }

    Log.v(TAG, "Reading script: " + scriptFile.getAbsolutePath());
    FileReader fr = new FileReader(scriptFile);
    BufferedReader br = new BufferedReader(fr);
    StringBuilder out = new StringBuilder();
    String line;
    while ((line = br.readLine()) != null) {
      out.append(line + '\n');
    }
    br.close();
    return out.toString();
  }
}
