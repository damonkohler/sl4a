package com.google.ase;

import android.app.Application;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AseApplication extends Application {

  private GoogleAnalyticsTracker mTracker;

  @Override
  public void onCreate() {
    mTracker = GoogleAnalyticsTracker.getInstance();
    mTracker.start("UA-158835-13", this);
  }

  public void track(String name) {
    mTracker.trackPageView("/" + name);
  }

}
