package com.google.ase.facade;

import com.google.ase.condition.RingerModeEvent;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;
import com.google.ase.trigger.EventTrigger;
import com.google.ase.trigger.TriggerRepository;

public class ConditionManagerFacade implements RpcReceiver {
  private final TriggerRepository mTriggerRepository;

  public ConditionManagerFacade(TriggerRepository triggerRepository) {
    mTriggerRepository = triggerRepository;
  }

  @Rpc(description = "Schedules a script for execution when the ringer volume is set to silent.")
  public void onRingerSilent(
      @RpcParameter(name = "scriptName", description = "script to execute when the ringer volume is set to silent, or set to anything other than silent") String scriptName) {
    mTriggerRepository.addTrigger(new EventTrigger(scriptName, new RingerModeEvent.Factory()));
  }

  @Override
  public void shutdown() {
  }
}
