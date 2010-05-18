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

package com.google.ase.interpreter.jruby;

import java.io.File;

import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;

public class JRubyInterpreterProcess extends InterpreterProcess {

  private final static String JRUBY_BIN = "dalvikvm -Xss128k " +
      "-classpath /sdcard/ase/extras/jruby/jruby-complete-1.4.jar org.jruby.Main -X-C " +
      // Fix include path.
      "-e \"\\$LOAD_PATH.push('file:/sdcard/ase/extras/jruby/jruby-complete-1.4.jar!/META-INF/jruby.home/lib/ruby/1.8'); " +
      "require 'android';";
  
  public JRubyInterpreterProcess(String launchScript, int port) {
    super(launchScript, port);
  }

  @Override
  protected void buildEnvironment() {
    File dalvikCache = new File(Constants.ASE_DALVIK_CACHE_ROOT);
    if (!dalvikCache.exists()) {
      dalvikCache.mkdirs();
    }
    mEnvironment.put("ANDROID_DATA", Constants.SDCARD_ASE_ROOT);
  }

  @Override
  protected void writeInterpreterCommand() {
    print(JRUBY_BIN);
    if (mLaunchScript != null) {
      print("load('" + mLaunchScript + "')\"");
    } else {
      // Start IRB for interactive terminal.
      print("require 'irb'; IRB.conf[:USE_READLINE] = false; IRB.start\"");
    }
    print("\n");
  }
}
