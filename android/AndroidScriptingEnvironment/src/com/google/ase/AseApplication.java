package com.google.ase;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AseApplication extends Application {

  private GoogleAnalyticsTracker mTracker;
  private SharedPreferences mPrefs;

  @Override
  public void onCreate() {
    mTracker = GoogleAnalyticsTracker.getInstance();
    mTracker.start("UA-158835-13", this);
    mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
  }

  public void track(String name) {
    if (mPrefs.getBoolean("usagetracking", false)) {
      AseLog.v("Tracking /" + name);
      mTracker.trackPageView("/" + name);
      mTracker.dispatch();
    }
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    mTracker.stop();
  }

}
