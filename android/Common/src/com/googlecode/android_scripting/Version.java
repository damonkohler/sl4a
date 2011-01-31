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

package com.googlecode.android_scripting;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class Version {

  private Version() {
    // Utility class.
  }

  public static String getVersion(Context context) {
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e("Package name not found", e);
    }
    return "?";
  }

}
