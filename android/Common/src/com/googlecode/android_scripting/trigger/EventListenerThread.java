package com.googlecode.android_scripting.trigger;

import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;

/**
 * A thread that listens for a named event on the event queue and executes a trigger when such an
 * event occurs.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class EventListenerThread extends Thread {
  private final String mEventName;
  private final FacadeManager mFacadeManager;
  private final Trigger mTrigger;

  /**
   * Creates a new {@link EventListenerThread}
   * 
   * @param facadeManager
   *          FacadeManager that contains all available facades
   * @param eventName
   *          Name of the event to listen for
   * @param trigger
   *          Trigger to execute upon appearance of the event
   */
  public EventListenerThread(FacadeManager facadeManager, String eventName, Trigger trigger) {
    mEventName = eventName;
    mFacadeManager = facadeManager;
    mTrigger = trigger;
  }

  @Override
  public void run() {
    EventFacade eventFacade = mFacadeManager.getReceiver(EventFacade.class);

    while (!interrupted()) {
      try {
        Event event = eventFacade.eventWaitFor(mEventName, null);
        mTrigger.handleEvent(event, null);
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }
}
