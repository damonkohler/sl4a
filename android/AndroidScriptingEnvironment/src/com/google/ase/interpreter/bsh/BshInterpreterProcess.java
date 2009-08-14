/*

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

import java.io.File;

import com.google.ase.AndroidFacade;
import com.google.ase.AndroidProxy;
import com.google.ase.Constants;
import com.google.ase.interpreter.AbstractInterpreterProcess;
import com.google.ase.jsonrpc.JsonRpcServer;

public class BshInterpreterProcess extends AbstractInterpreterProcess {

  private final static String BSH_BIN =
      "dalvikvm -classpath /sdcard/ase/extras/bsh/bsh-2.0b4-dx.jar bsh.Interpreter";
  private final AndroidProxy mAndroidProxy;
  private final int mAndroidProxyPort;

  public BshInterpreterProcess(AndroidFacade facade, String launchScript) {
    super(facade, launchScript);
    mAndroidProxy = new AndroidProxy(facade);
    mAndroidProxyPort = new JsonRpcServer(mAndroidProxy).start();
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
    print(BSH_BIN);
    if (mLaunchScript != null) {
      print(" " + mLaunchScript);
    }
    print("\n");
  }

}
