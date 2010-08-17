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

package com.googlecode.android_scripting.jsonrpc;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class RpcReceiverManager {

  private final Map<Class<? extends RpcReceiver>, RpcReceiver> mReceivers;

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, MethodDescriptor> mKnownRpcs = new HashMap<String, MethodDescriptor>();

  public RpcReceiverManager(Collection<Class<? extends RpcReceiver>> classList) {
    mReceivers = new HashMap<Class<? extends RpcReceiver>, RpcReceiver>();
    for (Class<? extends RpcReceiver> receiverClass : classList) {
      mReceivers.put(receiverClass, null);
      Collection<MethodDescriptor> methodList = MethodDescriptor.collectFrom(receiverClass);
      for (MethodDescriptor m : methodList) {
        if (mKnownRpcs.containsKey(m.getName())) {
          // We already know an RPC of the same name. We don't catch this anywhere because this is a
          // programming error.
          throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
        }
        mKnownRpcs.put(m.getName(), m);
      }
    }
  }

  public Collection<Class<? extends RpcReceiver>> getRpcReceiverClasses() {
    return mReceivers.keySet();
  }

  private RpcReceiver get(Class<? extends RpcReceiver> clazz) {
    RpcReceiver object = mReceivers.get(clazz);
    if (object != null) {
      return object;
    }

    Constructor<? extends RpcReceiver> constructor;
    try {
      constructor = clazz.getConstructor(getClass());
      object = constructor.newInstance(this);
      mReceivers.put(clazz, object);
    } catch (Exception e) {
      Log.e(e);
    }

    return object;
  }

  public <T extends RpcReceiver> T getReceiver(Class<T> clazz) {
    RpcReceiver receiver = get(clazz);
    return clazz.cast(receiver);
  }

  public MethodDescriptor getMethodDescriptor(String methodName) {
    return mKnownRpcs.get(methodName);
  }

  public Object invoke(Class<? extends RpcReceiver> clazz, Method method, Object[] args)
      throws Exception {
    RpcReceiver object = get(clazz);
    return method.invoke(object, args);
  }

  public void shutdown() {
    for (RpcReceiver receiver : mReceivers.values()) {
      if (receiver != null) {
        receiver.shutdown();
      }
    }
  }
}
