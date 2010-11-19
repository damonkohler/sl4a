package com.googlecode.android_scripting.trigger;

import com.google.common.collect.Maps;
import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.trigger.TriggerRepository.TriggerRepositoryObserver;

import java.util.Map;

import org.json.JSONArray;

/**
 * A {@link TriggerRepositoryObserver} that starts and stops the monitoring of events depending on
 * whether or not triggers for the event exist.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class EventGenerationControllingObserver implements TriggerRepositoryObserver {
  private final FacadeManager mFacadeManager;
  private final Map<String, MethodDescriptor> mStartEventGeneratingMethodDescriptors;
  private final Map<String, MethodDescriptor> mStopEventGeneratingMethodDescriptors;
  private final Map<String, Integer> mEventTriggerRefCounts = Maps.newHashMap();

  /**
   * Creates a new StartEventMonitoringObserver for the given trigger repository.
   * 
   * @param facadeManager
   * @param triggerRepository
   */
  public EventGenerationControllingObserver(FacadeManager facadeManager) {
    mFacadeManager = facadeManager;
    mStartEventGeneratingMethodDescriptors =
        FacadeConfiguration.collectStartEventMethodDescriptors();
    mStopEventGeneratingMethodDescriptors = FacadeConfiguration.collectStopEventMethodDescriptors();
  }

  private synchronized int incrementAndGetRefCount(String eventName) {
    int refCount =
        (mEventTriggerRefCounts.containsKey(eventName)) ? mEventTriggerRefCounts.get(eventName) : 0;
    refCount++;
    mEventTriggerRefCounts.put(eventName, refCount);
    return refCount;
  }

  private synchronized int decrementAndGetRefCount(String eventName) {
    int refCount =
        (mEventTriggerRefCounts.containsKey(eventName)) ? mEventTriggerRefCounts.get(eventName) : 0;
    refCount--;
    mEventTriggerRefCounts.put(eventName, refCount);
    return refCount;
  }

  @Override
  public synchronized void onPut(Trigger trigger) {
    // If we're not already monitoring the events corresponding to this trigger, do so.
    if (incrementAndGetRefCount(trigger.getEventName()) == 1) {
      startMonitoring(trigger.getEventName());
    }
  }

  @Override
  public synchronized void onRemove(Trigger trigger) {
    // If there are no more triggers listening to this event, then we need to stop monitoring.
    if (decrementAndGetRefCount(trigger.getEventName()) == 1) {
      stopMonitoring(trigger.getEventName());
    }
  }

  private void startMonitoring(String eventName) {
    MethodDescriptor startEventGeneratingMethod =
        mStartEventGeneratingMethodDescriptors.get(eventName);
    try {
      startEventGeneratingMethod.invoke(mFacadeManager, new JSONArray());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private void stopMonitoring(String eventName) {
    MethodDescriptor stopEventGeneratingMethod =
        mStopEventGeneratingMethodDescriptors.get(eventName);
    try {
      stopEventGeneratingMethod.invoke(mFacadeManager, new JSONArray());
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }
}
