package com.googlecode.android_scripting.trigger;

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.facade.FacadeManager;

import java.io.File;

/**
 * A trigger implementation that launches a given script when the event occurs.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class ScriptTrigger implements Trigger {
  private static final long serialVersionUID = 1804599219214041409L;
  private final File mScript;
  private final Service mService;

  public ScriptTrigger(Service service, File script) {
    mScript = script;
    mService = service;
  }

  @Override
  public void handleEvent(Event event, FacadeManager facadeManager) {
    Intent intent = IntentBuilders.buildStartInBackgroundIntent(mScript);
    // This is required since the context is not an activity.
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mService.startActivity(intent);
  }
}
