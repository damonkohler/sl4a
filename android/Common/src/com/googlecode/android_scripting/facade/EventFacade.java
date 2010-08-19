/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.facade;

import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This facade exposes the functionality to read from the event queue as an RPC, and the
 * functionality to write to the event queue as a pure java function.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class EventFacade extends RpcReceiver {
  /**
   * The maximum length of the event queue. Old events will be discarded when this limit is
   * exceeded.
   */
  private static final int MAX_QUEUE_SIZE = 1024;
  private final Queue<Event> mEventQueue = new ConcurrentLinkedQueue<Event>();
  private final Queue<EventObserver> mObserverList;

  public EventFacade(FacadeManager manager) {
    super(manager);
    mObserverList = new ConcurrentLinkedQueue<EventObserver>();
  }

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.)", returns = "Map of event properties.")
  public Event receiveEvent() {
    return mEventQueue.poll();
  }

  /**
   * Posts an event with {@link String} data to the event queue.
   */
  public void postEvent(String name, Object data) {
    mEventQueue.add(new Event(name, data));
    if (mEventQueue.size() > MAX_QUEUE_SIZE) {
      mEventQueue.remove();
    }
    for (EventObserver observer : mObserverList) {
      observer.onEventReceived(name, data);
    }
  }

  @Override
  public void shutdown() {
  }

  public void addEventObserver(EventObserver observer) {
    mObserverList.add(observer);
  }

  public void removeEventObserver(EventObserver observer) {
    mObserverList.remove(observer);
  }

  public interface EventObserver {
    public void onEventReceived(String eventName, Object data);
  }
}
