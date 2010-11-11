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

import com.google.common.collect.Lists;
import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.future.FutureResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

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

  @Rpc(description = "Clears all events from the event buffer.")
  public void eventClearBuffer() {
    mEventQueue.clear();
  }

  @Rpc(description = "Returns and removes the oldest n events (i.e. location or sensor update, etc.) from the event buffer.", returns = "A List of Maps of event properties.")
  public List<Event> eventPoll(
      @RpcParameter(name = "number_of_events") @RpcDefault("1") Integer number_of_events) {
    List<Event> events = Lists.newArrayList();
    for (int i = 0; i < number_of_events; i++) {
      Event event = mEventQueue.poll();
      if (event == null) {
        break;
      }
      events.add(event);
    }
    return events;
  }

  @Rpc(description = "Blocks until an event with the supplied name occurs. The returned event is not removed from the buffer.", returns = "Map of event properties.")
  public Event eventWaitFor(
      @RpcParameter(name = "eventName") final String eventName,
      @RpcParameter(name = "timeout", description = "the maximum time to wait") @RpcOptional Integer timeout)
      throws InterruptedException {
    final FutureResult<Event> futureEvent = new FutureResult<Event>();
    addEventObserver(new EventObserver() {
      @Override
      public void onEventReceived(String name, Object data) {
        if (name.equals(eventName)) {
          synchronized (futureEvent) {
            if (!futureEvent.isDone()) {
              futureEvent.set(new Event(name, data));
              removeEventObserver(this);
            }
          }
        }
      }
    });
    if (timeout != null) {
      return futureEvent.get(timeout, TimeUnit.MILLISECONDS);
    } else {
      return futureEvent.get();
    }
  }

  /**
   * Posts an event with to the event queue.
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

  @Rpc(description = "Post an event to the event queue.")
  public void eventPost(@RpcParameter(name = "name") String name,
      @RpcParameter(name = "data") String data) {
    postEvent(name, data);
  }

  @RpcDeprecated("eventPost")
  @Rpc(description = "Post an event to the event queue.")
  public void postEvent(@RpcParameter(name = "name") String name,
      @RpcParameter(name = "data") String data) {
    postEvent(name, data);
  }

  @RpcDeprecated(value = "eventPoll")
  @Rpc(description = "Returns and removes the oldest event (i.e. location or sensor update, etc.) from the event buffer.", returns = "Map of event properties.")
  public Event receiveEvent() {
    return mEventQueue.poll();
  }

  @RpcDeprecated("eventWaitFor")
  @Rpc(description = "Blocks until an event with the supplied name occurs. The returned event is not removed from the buffer.", returns = "Map of event properties.")
  public Event waitForEvent(
      @RpcParameter(name = "eventName") final String eventName,
      @RpcParameter(name = "timeout", description = "the maximum time to wait") @RpcOptional Integer timeout)
      throws InterruptedException {
    return eventWaitFor(eventName, timeout);
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
