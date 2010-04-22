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
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AseAnalytics {
  private static GoogleAnalyticsTracker mTracker;
  private static SharedPreferences mPrefs;
  private static String mAseVersion;

  private AseAnalytics() {
    // Utility class.
  }

  public static void start(Context context) {
    mAseVersion = AseVersion.getVersion(context);
    mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    mTracker = GoogleAnalyticsTracker.getInstance();
    mTracker.start("UA-158835-13", context);
  }

  public static void track(String name) {
    if (mPrefs.getBoolean("usagetracking", false)) {
      String url = String.format("/%s/%s", mAseVersion, name);
      AseLog.v("Tracking " + url);
      mTracker.trackPageView(url);
      mTracker.dispatch();
    }
  }

  public static void trackActivity(Activity activity) {
    String name = activity.getClass().getSimpleName();
    track(name);
  }

  public static void stop() {
    mTracker.stop();
  }
}
