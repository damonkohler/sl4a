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
  private transient final Service mService;
  private final File mScript;
  private final String mEventName;

  public ScriptTrigger(Service service, String eventName, File script) {
    mEventName = eventName;
    mScript = script;
    mService = service;
  }

  @Override
  public void handleEvent(Event event, FacadeManager facadeManager) {
    Intent intent = IntentBuilders.buildStartInBackgroundIntent(mScript);
    // This is required since the script is being started from the TriggerService.
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    // TODO(damonkohler): Inject the facadeManager into the script's context.
    mService.startActivity(intent);
  }

  @Override
  public String getEventName() {
    return mEventName;
  }
}
