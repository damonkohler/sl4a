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

package com.google.ase.interpreter.python;

import com.google.ase.AndroidFacade;
import com.google.ase.AndroidProxy;
import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.jsonrpc.JsonRpcServer;

public class PythonInterpreterProcess extends InterpreterProcess {

  private final static String PYTHON_HOME = "/data/data/com.google.ase/python";
  private final static String PYTHON_EXTRAS = Constants.SDCARD_ASE_ROOT + "extras/python/";

  private final AndroidProxy mAndroidProxy;
  private final int mAndroidProxyPort;

  public PythonInterpreterProcess(AndroidFacade facade, String launchScript) {
    super(facade, launchScript);
    mAndroidProxy = new AndroidProxy(facade);
    mAndroidProxyPort = new JsonRpcServer(mAndroidProxy).startLocal();
    mEnvironment.put("AP_PORT", Integer.toString(mAndroidProxyPort));
    mEnvironment.put("PYTHONHOME", PYTHON_HOME);
    mEnvironment.put("PYTHONPATH", PYTHON_EXTRAS + ":" + Constants.SCRIPTS_ROOT);
    mEnvironment.put("TEMP", PYTHON_EXTRAS + "tmp/");
  }

  @Override
  protected void writeInterpreterCommand() {
    PythonInterpreter interpreter = new PythonInterpreter();
    print(interpreter.getBinary().getAbsolutePath());
    if (mLaunchScript != null) {
      print(" " + mLaunchScript);
    }
    print("\n");
  }
}
