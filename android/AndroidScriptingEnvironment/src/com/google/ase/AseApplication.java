package com.google.ase;

import android.app.Application;

public class AseApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    AseAnalytics.start(this);
  }

  @Override
  public void onTerminate() {
    super.onTerminate();
    AseAnalytics.stop();
  }
}
