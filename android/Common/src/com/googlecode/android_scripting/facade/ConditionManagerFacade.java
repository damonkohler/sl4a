package com.googlecode.android_scripting.facade;

import com.googlecode.android_scripting.event.EventTrigger;
import com.googlecode.android_scripting.event.RingerModeEventListener;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.googlecode.android_scripting.trigger.TriggerRepository;

import java.io.File;

public class ConditionManagerFacade extends RpcReceiver {
  private final TriggerRepository mTriggerRepository;

  public ConditionManagerFacade(FacadeManager manager) {
    super(manager);
    mTriggerRepository = manager.getTriggerRepository();
  }

  @Rpc(description = "Schedules a script for execution when the ringer volume is set to silent.")
  public void onRingerSilent(
      @RpcParameter(name = "script", description = "The path to a script to execute when the ringer mode changes.") String script) {
    mTriggerRepository.addTrigger(new EventTrigger(new File(script),
        new RingerModeEventListener.Factory()));
  }

  @Override
  public void shutdown() {
  }
}
