

package com.googlecode.script;

import android.content.Intent;

import com.google.ase.AseApplication;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.interpreter.InterpreterConfiguration.ConfigurationObserver;

public class ScriptApplication extends AseApplication implements ConfigurationObserver {

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
