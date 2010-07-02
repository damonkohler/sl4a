package com.google.ase.facade;

import android.app.Service;
import android.content.Intent;

import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.jsonrpc.RpcReceiverManager;
import com.google.ase.trigger.TriggerRepository;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacadeManager implements RpcReceiverManager {

  private final Map<Class<? extends RpcReceiverFacade>, RpcReceiverFacade> mFacadeObjectMap;
  private final Service mService;
  private final Intent mIntent;
  private final TriggerRepository mTriggerRepository;
  private final Class<FacadeManager> mThisClass = FacadeManager.class;

  public FacadeManager(Service service, Intent intent,
      List<Class<? extends RpcReceiverFacade>> classList) {
    mService = service;
    mIntent = intent;

    mTriggerRepository = ((AseApplication) service.getApplication()).getTriggerRepository();

    mFacadeObjectMap = new HashMap<Class<? extends RpcReceiverFacade>, RpcReceiverFacade>();
    for (Class<? extends RpcReceiverFacade> receiverClass : classList) {
      mFacadeObjectMap.put(receiverClass, null);
    }
  }

  public Service getService() {
    return mService;
  }

  public Intent getIntent() {
    return mIntent;
  }

  public TriggerRepository getTriggerRepository() {
    return mTriggerRepository;
  }

  public AndroidFacade.Resources getAndroidFacadeResources() {
    return new AndroidFacade.Resources() {
      @Override
      public int getAseLogo48() {
        // TODO(Alexey): As an alternative, ask application for resource ids.
        String packageName = mService.getApplication().getPackageName();
        return mService.getResources().getIdentifier("script_logo_48", "drawable", packageName);
        // return R.drawable.ase_logo_48;
      }
    };
  }

  private RpcReceiverFacade getFacadeInstance(Class<? extends RpcReceiverFacade> facadeClass) {
    RpcReceiverFacade facadeObject = mFacadeObjectMap.get(facadeClass);
    if (facadeObject != null) {
      return facadeObject;
    }

    Constructor<? extends RpcReceiverFacade> facadeConstructor;
    try {
      facadeConstructor = facadeClass.getConstructor(mThisClass);
      facadeObject = facadeConstructor.newInstance(this);
      mFacadeObjectMap.put(facadeClass, facadeObject);
    } catch (Exception e) {
      AseLog.e(e);
    }

    return facadeObject;
  }

  public <T extends RpcReceiverFacade> T getFacade(Class<T> clazz) {
    RpcReceiverFacade facade = getFacadeInstance(clazz);
    return clazz.cast(facade);
  }


  public RpcReceiver getReceiverInstance(Class<? extends RpcReceiver> receiverClass) {

    RpcReceiver receiverObject = null;
    if (RpcReceiverFacade.class.isAssignableFrom(receiverClass)) {

      Class<? extends RpcReceiverFacade> facadeClass =
          receiverClass.asSubclass(RpcReceiverFacade.class);

      receiverObject = getFacadeInstance(facadeClass);
    }

    return receiverObject;
  }


}
