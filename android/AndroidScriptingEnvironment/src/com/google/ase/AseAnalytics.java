package com.google.ase;

import android.app.Activity;

public class AseAnalytics {
  private AseAnalytics() {
    // Utility class.
  }

  public static void trackActivity(Activity activity, String name) {
    AseLog.v("Tracked " + name);
    ((AseApplication) activity.getApplication()).track(name);
  }

  public static void trackActivity(Activity activity) {
    String name = activity.getClass().getSimpleName();
    trackActivity(activity, name);
  }
}
