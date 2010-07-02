package com.google.ase.facade;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.app.Service;
import android.content.Intent;

import com.google.ase.AseApplication;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.jsonrpc.RpcReceiverManager;
import com.google.ase.trigger.TriggerRepository;

public class FacadeManager extends RpcReceiverManager {

  private final Service mService;
  private final Intent mIntent;
  private final TriggerRepository mTriggerRepository;
  private final Map<Class<? extends RpcReceiver>, RpcReceiver> mReceivers;

  public FacadeManager(Service service, Intent intent,
      Collection<Class<? extends RpcReceiver>> classList) {
    super(classList);
    mService = service;
    mIntent = intent;
    mTriggerRepository = ((AseApplication) service.getApplication()).getTriggerRepository();
    mReceivers = new HashMap<Class<? extends RpcReceiver>, RpcReceiver>();
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

  public <T extends RpcReceiver> T getFacade(Class<T> clazz) {
    if (mReceivers.containsKey(clazz)) {
      return clazz.cast(mReceivers.get(clazz));
    }
    RpcReceiver receiver = getReceiver(clazz);
    mReceivers.put(clazz, receiver);
    return clazz.cast(receiver);
  }
}
