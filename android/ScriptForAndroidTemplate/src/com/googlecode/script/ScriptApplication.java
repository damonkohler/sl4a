package com.googlecode.script;

import android.content.Intent;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration.ConfigurationObserver;

public class ScriptApplication extends BaseApplication implements ConfigurationObserver {

  private volatile boolean receivedConfigUpdate = false;

  @Override
  public void onCreate() {
    mConfiguration = new InterpreterConfiguration(this);
    mConfiguration.registerObserver(this);
    mConfiguration.startDiscovering();
  }

  @Override
  public void onConfigurationChanged() {
    if (!receivedConfigUpdate) {
      receivedConfigUpdate = true;
      startService(new Intent(this, ScriptService.class));
    }

  }
}
