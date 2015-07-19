/*
 * Copyright (C) 2010 Google Inc.
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

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Sl4aApplication extends BaseApplication {

  @Override
  public void onCreate() {
    super.onCreate();
    // Analytics.start(this, "UA-158835-13");

    // extract run_pie from asset folder.
    File path = new File(this.getFilesDir(), "run_pie");
    if (!path.isFile()) {
      String run_pie = System.getProperty("os.arch");
      if (run_pie.startsWith("i686")) {
        run_pie = "_x86";
      }
      else if (run_pie.startsWith("mips")) {
        run_pie = "_mips";
      }
      else {
        // FIXME: armv7a binary is not used, it this right?
        Log.v("fallback arch to arm: " + run_pie);
        run_pie = "_armeabi";
      }
      run_pie = "run_pie" + run_pie;

      try {
        AssetManager as = getResources().getAssets();
        InputStream ins = as.open(run_pie);
        byte[] buffer = new byte[ins.available()];
        ins.read(buffer);
        ins.close();
        FileOutputStream fos = this.openFileOutput("run_pie", Context.MODE_PRIVATE);
        fos.write(buffer);
        fos.close();

        path.setExecutable(true);
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onTerminate() {
    // Analytics.stop();
  }
}
