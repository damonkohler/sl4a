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

package com.googlecode.bshforandroid;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BshDescriptor extends Sl4aHostedInterpreter {

  private final static String BSH_JAR = "bsh-2.0b4-dx.jar";
  private static final String ENV_DATA = "ANDROID_DATA";

  public String getExtension() {
    return ".bsh";
  }

  public String getName() {
    return "bsh";
  }

  public String getNiceName() {
    return "BeanShell 2.0b4";
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  public int getVersion() {
    return 3;
  }

  @Override
  public File getBinary(Context context) {
    return new File(DALVIKVM);
  }

  @Override
  public List<String> getArguments(Context context) {
    String absolutePathToJar = new File(getExtrasPath(context), BSH_JAR).getAbsolutePath();

    List<String> result =
        new ArrayList<String>(Arrays.asList("-classpath", absolutePathToJar,
            "com.android.internal.util.WithFramework", "bsh.Interpreter"));
    try {
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
      if (preferences != null) {
        int heapsize = Integer.parseInt(preferences.getString("heapsize", "0"), 10);
        if (heapsize > 0) {
          result.add(0, "-Xmx" + heapsize + "m");
        }
      }
    } catch (Exception e) {
      Log.e(e);
    }
    return result;
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context unused) {
    Map<String, String> values = new HashMap<String, String>();
    values.put(ENV_DATA, InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName());
    return values;
  }
}
