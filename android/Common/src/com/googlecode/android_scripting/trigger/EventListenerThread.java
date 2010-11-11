package com.googlecode.android_scripting.trigger;

import com.googlecode.android_scripting.facade.EventFacade;

public class EventListenerThread extends Thread {
  private final String mEventName;
  private final EventFacade mEventFacade;
  private final EventHandler mEventHandler;

  public interface EventHandler {
    void handleEvent(EventFacade eventFacade);
  }

  public EventListenerThread(EventFacade eventFacade, String eventName, EventHandler eventHandler) {
    mEventName = eventName;
    mEventFacade = eventFacade;
    mEventHandler = eventHandler;
  }

  @Override
  public void run() {
    while (!interrupted()) {
      try {
        mEventFacade.waitForEvent(mEventName, null);
        mEventHandler.handleEvent(mEventFacade);
      } catch (InterruptedException e) {
        interrupt();
      }
    }
  }
}
