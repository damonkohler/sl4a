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

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.Window;
import android.widget.TextView;

public class CustomWindowTitle {
  private CustomWindowTitle() {
    // Utility class.
  }

  public static void buildWindowTitle(Activity activity) {
    String versionName = "";
    try {
      PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
      versionName = info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      AseLog.e("Package name not found", e);
    }
    activity.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);
    ((TextView) activity.findViewById(R.id.right_text)).setText(versionName);
  }
}
