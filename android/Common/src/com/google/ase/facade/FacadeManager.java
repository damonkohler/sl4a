package com.google.ase.facade;

import android.app.Service;
import android.content.Intent;

import com.google.ase.AseApplication;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.jsonrpc.RpcReceiverManager;
import com.google.ase.trigger.TriggerRepository;

import java.util.List;

public class FacadeManager extends RpcReceiverManager {

  private final Service mService;
  private final Intent mIntent;
  private final TriggerRepository mTriggerRepository;

  public FacadeManager(Service service, Intent intent, List<Class<? extends RpcReceiver>> classList) {
    super(classList);

    mService = service;
    mIntent = intent;
    mTriggerRepository = ((AseApplication) service.getApplication()).getTriggerRepository();
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
    RpcReceiver facade = getReceiver(clazz);
    return clazz.cast(facade);
  }

}
