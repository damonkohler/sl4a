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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Analytics {
  private static GoogleAnalyticsTracker mTracker;
  private static SharedPreferences mPrefs;
  private static String mAseVersion;
  private static ExecutorService mWorkPool;
  private static volatile boolean started = false;

  private Analytics() {
    // Utility class.
  }

  public static void start(Context context, String analyticsID) {
    mAseVersion = AseVersion.getVersion(context);
    mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    mTracker = GoogleAnalyticsTracker.getInstance();
    mTracker.start(analyticsID, 10, context);
    mWorkPool = Executors.newSingleThreadExecutor();
    started = true;
  }

  private static class PageNameBuilder {
    private final StringBuilder mmName = new StringBuilder();

    void add(String pathPart) {
      mmName.append("/");
      mmName.append(pathPart);
    }

    String build() {
      return mmName.toString();
    }
  }

  public static void track(final String... nameParts) {
    if (started && mPrefs.getBoolean("usagetracking", false)) {
      mWorkPool.submit(new Runnable() {
        public void run() {
          PageNameBuilder builder = new PageNameBuilder();
          builder.add(mAseVersion);
          for (String part : nameParts) {
            builder.add(part);
          }
          String name = builder.build();
          mTracker.trackPageView(name);
        }
      });
    }
  }

  public static void trackActivity(Activity activity) {
    String name = activity.getClass().getSimpleName();
    track(name);
  }

  public static void stop() {
    if (started) {
      started = false;
      mWorkPool.shutdownNow();
      mTracker.stop();
    }
  }
}
