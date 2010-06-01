package com.google.ase.facade;

import android.app.Service;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcParameter;
import com.google.ase.trigger.ConditionTrigger;
import com.google.ase.trigger.TriggerRepository;

public class ConditionManagerFacade implements RpcReceiver {
  private final Service mTriggerService;
  private final TriggerRepository mTriggerRepository;

  public ConditionManagerFacade(Service triggerService, TriggerRepository triggerRepository) {
    mTriggerService = triggerService;
    mTriggerRepository = triggerRepository;
  }

  @Rpc(description = "Schedules a script for execution when the ringer volume is set to silent.")
  public void onRingerSilent(
      @RpcParameter(name = "scriptName", description = "script to execute when the ringer volume is set to silent, or set to anything other than silent") String scriptName) {
    mTriggerRepository.addTrigger(new ConditionTrigger(scriptName, mTriggerRepository
        .getIdProvider(), mTriggerService));
  }

  @Override
  public void shutdown() {
  }
}
