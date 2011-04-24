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

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.future.FutureResult;
import com.googlecode.android_scripting.jsonrpc.JsonBuilder;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcName;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

/**
 * Manage the event queue. <br>
 * <br>
 * <b>Usage Notes:</b><br>
 * EventFacade APIs interact with the Event Queue (a data buffer containing up to 1024 event
 * entries).<br>
 * Events are automatically entered into the Event Queue following API calls such as startSensing()
 * and startLocating().<br>
 * The Event Facade provides control over how events are entered into (and removed from) the Event
 * Queue.<br>
 * The Event Queue provides a useful means of recording background events (such as sensor data) when
 * the phone is busy with foreground activities.
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
  private final CopyOnWriteArrayList<EventObserver> mGlobalEventObservers =
      new CopyOnWriteArrayList<EventObserver>();
  private final Multimap<String, EventObserver> mNamedEventObservers = Multimaps
      .synchronizedListMultimap(ArrayListMultimap.<String, EventObserver> create());
  private EventServer mEventServer = null;
  private final HashMap<String, BroadcastListener> mBroadcastListeners =
      new HashMap<String, BroadcastListener>();
  private final Context mContext;

  public EventFacade(FacadeManager manager) {
    super(manager);
    mContext = manager.getService().getApplicationContext();
  }

  /**
   * Example (python): droid.eventClearBuffer()
   */
  @Rpc(description = "Clears all events from the event buffer.")
  public void eventClearBuffer() {
    mEventQueue.clear();
  }

  /**
   * Registers a listener for a new broadcast signal
   */
  @Rpc(description = "Registers a listener for a new broadcast signal")
  public boolean eventRegisterForBroadcast(
      @RpcParameter(name = "category") String category,
      @RpcParameter(name = "enqueue", description = "Should this events be added to the event queue or only dispatched") @RpcDefault(value = "true") Boolean enqueue) {
    if (mBroadcastListeners.containsKey(category)) {
      return false;
    }

    BroadcastListener b = new BroadcastListener(this, enqueue.booleanValue());
    IntentFilter c = new IntentFilter(category);
    mContext.registerReceiver(b, c);
    mBroadcastListeners.put(category, b);

    return true;
  }

  @Rpc(description = "Stop listening for a broadcast signal")
  public void eventUnregisterForBroadcast(@RpcParameter(name = "category") String category) {
    if (!mBroadcastListeners.containsKey(category)) {
      return;
    }

    mContext.unregisterReceiver(mBroadcastListeners.get(category));
    mBroadcastListeners.remove(category);
  }

  @Rpc(description = "Lists all the broadcast signals we are listening for")
  public Set<String> eventGetBrodcastCategories() {
    return mBroadcastListeners.keySet();
  }

  /**
   * Actual data returned in the map will depend on the type of event.
   * 
   * <pre>
   * Example (python):
   *     import android, time
   *     droid = android.Android()
   *     droid.startSensing()
   *     time.sleep(1)
   *     droid.eventClearBuffer()
   *     time.sleep(1)
   *     e = eventPoll(1).result
   *     event_entry_number = 0
   *     x = e[event_entry_ number]['data']['xforce']
   * </pre>
   * 
   * e has the format:<br>
   * [{u'data': {u'accuracy': 0, u'pitch': -0.48766891956329345, u'xmag': -5.6875, u'azimuth':
   * 0.3312483489513397, u'zforce': 8.3492730000000002, u'yforce': 4.5628165999999997, u'time':
   * 1297072704.813, u'ymag': -11.125, u'zmag': -42.375, u'roll': -0.059393649548292161, u'xforce':
   * 0.42223078000000003}, u'name': u'sensors', u'time': 1297072704813000L}]<br>
   * x has the string value of the x force data (0.42223078000000003) at the time of the event
   * entry. </pre>
   */

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
      @RpcParameter(name = "timeout", description = "the maximum time to wait (in ms)") @RpcOptional Integer timeout)
      throws InterruptedException {
    synchronized (mEventQueue) { // First check to make sure it isn't already there
      for (Event event : mEventQueue) {
        if (event.getName().equals(eventName)) {
          return event;
        }
      }
    }
    final FutureResult<Event> futureEvent = new FutureResult<Event>();
    addNamedEventObserver(eventName, new EventObserver() {
      @Override
      public void onEventReceived(Event event) {
        if (event.getName().equals(eventName)) {
          synchronized (futureEvent) {
            if (!futureEvent.isDone()) {
              futureEvent.set(event);
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

  @Rpc(description = "Blocks until an event occurs. The returned event is removed from the buffer.", returns = "Map of event properties.")
  public Event eventWait(
      @RpcParameter(name = "timeout", description = "the maximum time to wait") @RpcOptional Integer timeout)
      throws InterruptedException {
    final FutureResult<Event> futureEvent = new FutureResult<Event>();
    synchronized (mEventQueue) { // Anything in queue?
      if (mEventQueue.size() > 0) {
        return mEventQueue.poll(); // return it.
      }
    }
    addGlobalEventObserver(new EventObserver() {
      @Override
      public void onEventReceived(Event event) { // set up observer for any events.
        synchronized (futureEvent) {
          if (!futureEvent.isDone()) {
            futureEvent.set(event);
            removeEventObserver(this);
          }
          mEventQueue.remove(event);
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
   * <pre>
   * Example:
   *   import android
   *   from datetime import datetime
   *   droid = android.Android()
   *   t = datetime.now()
   *   droid.eventPost('Some Event', t)
   * </pre>
   */
  @Rpc(description = "Post an event to the event queue.")
  public void eventPost(
      @RpcParameter(name = "name", description = "Name of event") String name,
      @RpcParameter(name = "data", description = "Data contained in event.") String data,
      @RpcParameter(name = "enqueue", description = "Set to False if you don't want your events to be added to the event queue, just dispatched.") @RpcOptional @RpcDefault("false") Boolean enqueue) {
    postEvent(name, data, enqueue.booleanValue());
  }

  /**
   * Post an event and queue it
   */
  public void postEvent(String name, Object data) {
    postEvent(name, data, true);
  }

  /**
   * Posts an event with to the event queue.
   */
  public void postEvent(String name, Object data, boolean enqueue) {
    Event event = new Event(name, data);
    if (enqueue == false) {
      mEventQueue.add(event);
      if (mEventQueue.size() > MAX_QUEUE_SIZE) {
        mEventQueue.remove();
      }
    }
    synchronized (mNamedEventObservers) {
      for (EventObserver observer : mNamedEventObservers.get(name)) {
        observer.onEventReceived(event);
      }
    }
    synchronized (mGlobalEventObservers) {
      for (EventObserver observer : mGlobalEventObservers) {
        observer.onEventReceived(event);
      }
    }
  }

  @RpcDeprecated(value = "eventPost", release = "r4")
  @Rpc(description = "Post an event to the event queue.")
  @RpcName(name = "postEvent")
  public void rpcPostEvent(@RpcParameter(name = "name") String name,
      @RpcParameter(name = "data") String data) {
    postEvent(name, data);
  }

  @RpcDeprecated(value = "eventPoll", release = "r4")
  @Rpc(description = "Returns and removes the oldest event (i.e. location or sensor update, etc.) from the event buffer.", returns = "Map of event properties.")
  public Event receiveEvent() {
    return mEventQueue.poll();
  }

  @RpcDeprecated(value = "eventWaitFor", release = "r4")
  @Rpc(description = "Blocks until an event with the supplied name occurs. The returned event is not removed from the buffer.", returns = "Map of event properties.")
  public Event waitForEvent(
      @RpcParameter(name = "eventName") final String eventName,
      @RpcParameter(name = "timeout", description = "the maximum time to wait") @RpcOptional Integer timeout)
      throws InterruptedException {
    return eventWaitFor(eventName, timeout);
  }

  @Rpc(description = "Opens up a socket where you can read for events posted")
  public int startEventDispatcher(
      @RpcParameter(name = "port", description = "Port to use") @RpcDefault("0") @RpcOptional() Integer port) {
    if (mEventServer == null) {
      mEventServer = new EventServer(port);
      addGlobalEventObserver(mEventServer);
    }
    return mEventServer.getAddress().getPort();
  }

  @Rpc(description = "Stops the event server, you can't read in the port anymore")
  public void stopEventDispatcher() throws RuntimeException {
    if (mEventServer == null) {
      throw new RuntimeException("Not running");
    }
    mEventServer.shutdown();
    removeEventObserver(mEventServer);
    mEventServer = null;
    return;
  }

  @Override
  public void shutdown() {
    try {
      stopEventDispatcher();
    } catch (Exception err) {
    }
    // let others (like webviews) know we're going down
    postEvent("sl4a", "{\"shutdown\": \"event-facade\"}");
  }

  public void addNamedEventObserver(String eventName, EventObserver observer) {
    mNamedEventObservers.put(eventName, observer);
  }

  public void addGlobalEventObserver(EventObserver observer) {
    mGlobalEventObservers.add(observer);
  }

  public void removeEventObserver(EventObserver observer) {
    mNamedEventObservers.removeAll(observer);
    mGlobalEventObservers.remove(observer);
  }

  public interface EventObserver {
    public void onEventReceived(Event event);
  }

  public class BroadcastListener extends android.content.BroadcastReceiver {
    private EventFacade mParent;
    private boolean mEnQueue;

    public BroadcastListener(EventFacade parent, boolean enqueue) {
      mParent = parent;
      mEnQueue = enqueue;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      Bundle data = (Bundle) intent.getExtras().clone();
      data.putString("action", intent.getAction());
      try {
        mParent.eventPost("sl4a", JsonBuilder.build(data).toString(), mEnQueue);
      } catch (JSONException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }
}
