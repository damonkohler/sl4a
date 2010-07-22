package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Intent;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.RpcDepreciated;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.trigger.TriggerRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

public class FacadeManager extends RpcReceiverManager {

  private final Service mService;
  private final Intent mIntent;
  private final TriggerRepository mTriggerRepository;
  private int mSdkLevel;

  public FacadeManager(int sdkLevel, Service service, Intent intent,
      Collection<Class<? extends RpcReceiver>> classList) {
    super(classList);
    mSdkLevel = sdkLevel;
    mService = service;
    mIntent = intent;
    mTriggerRepository = ((BaseApplication) service.getApplication()).getTriggerRepository();
  }

  public int getSdkLevel() {
    return mSdkLevel;
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

  @Override
  public Object invoke(Class<? extends RpcReceiver> clazz, Method method, Object[] args)
      throws Exception {
    try {
      if (method.isAnnotationPresent(RpcDepreciated.class)) {
        String replacedBy = method.getAnnotation(RpcDepreciated.class).value();
        String title = method.getName() + " is depreciated";
        Log.notify(mService, title, title, String.format("Please use %s instead.", replacedBy));
      } else if (method.isAnnotationPresent(RpcMinSdk.class)) {
        int requiredSdkLevel = method.getAnnotation(RpcMinSdk.class).value();
        if (mSdkLevel < requiredSdkLevel) {
          throw new Sl4aException(String.format("%s requires API level %d, current level is %d",
              method.getName(), requiredSdkLevel, mSdkLevel));
        }
      }
      return super.invoke(clazz, method, args);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof SecurityException) {
        Log.notify(mService, "RPC invoke failed...", mService.getPackageName(), e.getCause()
            .getMessage());
      }
      throw e;
    }
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
