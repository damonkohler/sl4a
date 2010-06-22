/*
 * 
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

package com.google.ase.interpreter.bsh;

import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterProcess;

import java.io.File;

public class BshInterpreterProcess extends InterpreterProcess {

  public BshInterpreterProcess(String launchScript, int port) {
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
  protected String getInterpreterCommand() {
    BshInterpreter interpreter = new BshInterpreter();
    String str = interpreter.getBinary() + "%s";
    return String.format(str, (mLaunchScript == null) ? "" : " " + mLaunchScript);
  }
}
