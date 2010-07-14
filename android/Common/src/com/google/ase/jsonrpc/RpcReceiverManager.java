package com.google.ase.jsonrpc;

import com.googlecode.android_scripting.Sl4aLog;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class RpcReceiverManager {

  private final Map<Class<? extends RpcReceiver>, RpcReceiver> mReceivers;

  public RpcReceiverManager(Collection<Class<? extends RpcReceiver>> classList) {
    mReceivers = new HashMap<Class<? extends RpcReceiver>, RpcReceiver>();
    for (Class<? extends RpcReceiver> receiverClass : classList) {
      mReceivers.put(receiverClass, null);
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
      Sl4aLog.e(e);
    }

    return object;
  }

  public <T extends RpcReceiver> T getReceiver(Class<T> clazz) {
    RpcReceiver receiver = get(clazz);
    return clazz.cast(receiver);
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
