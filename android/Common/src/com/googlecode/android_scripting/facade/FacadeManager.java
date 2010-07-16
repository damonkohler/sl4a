package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.trigger.TriggerRepository;

import java.util.Collection;

public class FacadeManager extends RpcReceiverManager {

  private final Service mService;
  private final Intent mIntent;
  private final TriggerRepository mTriggerRepository;

  public FacadeManager(Service service, Intent intent,
      Collection<Class<? extends RpcReceiver>> classList) {
    super(classList);
    mService = service;
    mIntent = intent;
    mTriggerRepository = ((BaseApplication) service.getApplication()).getTriggerRepository();
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
      public int getLogo48() {
        // TODO(Alexey): As an alternative, ask application for resource ids.
        String packageName = mService.getApplication().getPackageName();
        return mService.getResources().getIdentifier("script_logo_48", "drawable", packageName);
        // return R.drawable.ase_logo_48;
      }
    };
  }

}
