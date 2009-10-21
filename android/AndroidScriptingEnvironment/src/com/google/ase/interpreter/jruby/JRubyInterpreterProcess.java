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

import com.google.ase.AndroidFacade;
import com.google.ase.AndroidProxy;
import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.jsonrpc.JsonRpcServer;

public class JRubyInterpreterProcess extends InterpreterProcess {

  private final static String JRUBY_BIN = "dalvikvm -Xss128k " +
      "-classpath /sdcard/ase/extras/jruby/jruby-complete-1.2.0RC1-dex.jar org.jruby.Main -X-C";

  private final AndroidProxy mAndroidProxy;
  private final int mAndroidProxyPort;

  public JRubyInterpreterProcess(AndroidFacade facade, String launchScript) {
    super(facade, launchScript);
    mAndroidProxy = new AndroidProxy(facade);
    mAndroidProxyPort = new JsonRpcServer(mAndroidProxy).startLocal().getPort();
    buildEnvironment();
  }

  private void buildEnvironment() {
    File dalvikCache = new File(Constants.ASE_DALVIK_CACHE_ROOT);
    if (!dalvikCache.exists()) {
      dalvikCache.mkdirs();
    }
    mEnvironment.put("ANDROID_DATA", Constants.SDCARD_ASE_ROOT);
    mEnvironment.put("AP_PORT", Integer.toString(mAndroidProxyPort));
  }

  @Override
  protected void writeInterpreterCommand() {
    print(JRUBY_BIN);
    if (mLaunchScript != null) {
      print(" " + mLaunchScript);
    } else {
      // Start IRB for interactive terminal.
      print(" -e \"require 'irb'; IRB.conf[:USE_READLINE] = false; IRB.start\"");
    }
    print("\n");
  }

}
