
package com.google.ase.jsonrpc;

import com.google.ase.AseLog;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public abstract class  RpcReceiverManager {
  protected final Map<Class<? extends RpcReceiver>, RpcReceiver> mClassObjectMap;
  protected final Class<? extends RpcReceiverManager> mThisClass = getClass();
  
  public RpcReceiverManager(Set<Class<? extends RpcReceiver>> classSet) {
    mClassObjectMap = new HashMap<Class<? extends RpcReceiver>, RpcReceiver>();
    for (Class<? extends RpcReceiver> receiverClass : classSet) {
      mClassObjectMap.put(receiverClass, null);
    }
  }

  public RpcReceiver getReceiver(Class<? extends RpcReceiver> clazz) {
    RpcReceiver object = mClassObjectMap.get(clazz);
    if (object != null) {
      return object;
    }

    Constructor<? extends RpcReceiver> constructor;
    try {
      constructor = clazz.getConstructor(mThisClass);
      object = constructor.newInstance(this);
      mClassObjectMap.put(clazz, object);
    } catch (Exception e) {
      AseLog.e(e);
    }

    return object;
  }
}
