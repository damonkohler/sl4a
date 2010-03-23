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

package com.google.ase.facade;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;

/**
 * This facade exposes the functionality to read from the event queue as an RPC, and the
 * functionality to write to the event queue as a pure java function.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class EventFacade implements RpcReceiver {
  final Queue<Bundle> mEventQueue = new ConcurrentLinkedQueue<Bundle>();
  final Context mService;

  public EventFacade(final Service service) {
    mService = service;
  }

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.", returns = "Map of event properties.")
  public Bundle receiveEvent() {
    return mEventQueue.poll();
  }

  /**
   * Posts an event on the event queue. This method is supposed to be used from other facades to
   * post events.
   */
  void postEvent(String name, Bundle bundle) {
    Bundle event = new Bundle(bundle);
    event.putString("name", name);
    mEventQueue.add(event);
  }

  @Override
  public void shutdown() {
  }
}
