package com.googlecode.android_scripting.trigger;

import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.trigger.TriggerRepository.TriggerRepositoryObserver;

import java.util.Collection;

/**
 * A {@link TriggerRepositoryObserver} that starts and stops the monitoring of events depending on
 * whether or not triggers for the event exist.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class StartEventMonitoringObserver implements TriggerRepositoryObserver {
  private final TriggerRepository mTriggerRepository;
  private final FacadeConfiguration mFacadeConfiguration;

  public StartEventMonitoringObserver(TriggerRepository triggerRepository,
      FacadeConfiguration facadeConfiguration) {
    mTriggerRepository = triggerRepository;
    mFacadeConfiguration = facadeConfiguration;
  }

  @Override
  public void onPut(Trigger trigger) {
    // If we're not already monitoring the events corresponding to this trigger, do so.
    Collection<Trigger> triggers = mTriggerRepository.getAllTriggers().get(trigger.getEventName());
    if (triggers.isEmpty()) {
      startMonitoring(trigger.getEventName());
    }
  }

  @Override
  public void onRemove(Trigger trigger) {
    // If there are no more triggers listening to this event, then we need to stop monitoring.
    Collection<Trigger> triggers = mTriggerRepository.getAllTriggers().get(trigger.getEventName());
    if (triggers.isEmpty()) {
      stopMonitoring(trigger.getEventName());
    }
  }

  private void startMonitoring(String eventName) {

  }

  private void stopMonitoring(String eventName) {

  }
}
