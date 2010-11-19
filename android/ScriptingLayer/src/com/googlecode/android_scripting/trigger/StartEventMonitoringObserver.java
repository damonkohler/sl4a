package com.googlecode.android_scripting.trigger;

import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.trigger.TriggerRepository.TriggerRepositoryObserver;

import java.util.Collection;
import java.util.Map;

import org.json.JSONArray;

/**
 * A {@link TriggerRepositoryObserver} that starts and stops the monitoring of events depending on
 * whether or not triggers for the event exist.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class StartEventMonitoringObserver implements TriggerRepositoryObserver {
  private final TriggerRepository mTriggerRepository;
  private final FacadeManager mFacadeManager;
  private final Map<String, MethodDescriptor> mEventGeneratingMethodDescriptors;

  /**
   * Creates a new StartEventMonitoringObserver for the given trigger repository.
   * 
   * @param facadeManager
   * @param triggerRepository
   */
  public StartEventMonitoringObserver(FacadeManager facadeManager,
      TriggerRepository triggerRepository) {
    mFacadeManager = facadeManager;
    mTriggerRepository = triggerRepository;
    mEventGeneratingMethodDescriptors =
        FacadeConfiguration.collectEventGeneratingMethodDescriptors();
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
    MethodDescriptor eventGeneratingMethod = mEventGeneratingMethodDescriptors.get(eventName);
    try {
      eventGeneratingMethod.invoke(mFacadeManager, new JSONArray());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private void stopMonitoring(String eventName) {
    // TODO(felix.arends@gmail.com): stop monitoring the event for "eventName" here.
  }
}
