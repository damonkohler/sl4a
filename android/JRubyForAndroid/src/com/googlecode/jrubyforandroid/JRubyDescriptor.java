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

package com.googlecode.jrubyforandroid;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class JRubyDescriptor extends Sl4aHostedInterpreter {

  private static final String JRUBY_PREFIX =
      "-e $LOAD_PATH.push('file:%1$s!/META-INF/jruby.home/lib/ruby/1.8'); require 'android'; %2$s";
  private static final String JRUBY_JAR = "jruby-complete-1.4.jar";
  private static final String ENV_DATA = "ANDROID_DATA";

  public String getExtension() {
    return ".rb";
  }

  public String getName() {
    return "jruby";
  }

  public String getNiceName() {
    return "JRuby-1.4";
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
    return 2;
  }

  @Override
  public int getScriptsVersion() {
    return 1;
  }

  @Override
  public File getBinary(Context context) {
    return new File(DALVIKVM);
  }

  @Override
  public String getInteractiveCommand(Context context) {
    String absolutePathToJar = new File(getExtrasPath(context), JRUBY_JAR).getAbsolutePath();
    return String.format(JRUBY_PREFIX, absolutePathToJar,
        "require 'irb'; IRB.conf[:USE_READLINE] = false; IRB.start");
  }

  @Override
  public String getScriptCommand(Context context) {
    String absolutePathToJar = new File(getExtrasPath(context), JRUBY_JAR).getAbsolutePath();
    return String.format(JRUBY_PREFIX, absolutePathToJar, "load('%s')");
  }

  @Override
  public List<String> getArguments(Context context) {
    String absolutePathToJar = new File(getExtrasPath(context), JRUBY_JAR).getAbsolutePath();
    return Arrays.asList("-Xbootclasspath:/system/framework/core.jar", "-Xss128k", "-classpath",
        absolutePathToJar, "org.jruby.Main", "-X-C");
  }

  @Override
  public Map<String, String> getEnvironmentVariables(Context unused) {
    Map<String, String> values = new HashMap<String, String>(1);
    values.put(ENV_DATA, InterpreterConstants.SDCARD_ROOT + getClass().getPackage().getName());
    return values;
  }
}
