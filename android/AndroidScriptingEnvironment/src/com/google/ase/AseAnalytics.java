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
