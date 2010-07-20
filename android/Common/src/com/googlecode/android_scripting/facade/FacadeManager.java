package com.googlecode.android_scripting.facade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.activity.NotificationIdFactory;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.RpcDepreciated;
import com.googlecode.android_scripting.trigger.TriggerRepository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

  @Override
  public Object invoke(Class<? extends RpcReceiver> clazz, Method method, Object[] args)
      throws Exception {
    try {
      if (method.isAnnotationPresent(RpcDepreciated.class)) {
        String replacedBy = method.getAnnotation(RpcDepreciated.class).value();
        String title = method.getName() + " is depreciated";
        displayNotification(title, title, String.format("Please use %s instead.", replacedBy));
      }
      return super.invoke(clazz, method, args);
    } catch (InvocationTargetException e) {
      if (e.getCause() instanceof SecurityException) {
        displayNotification("RPC invoke failed...", mService.getPackageName(), e.getCause()
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

  private void displayNotification(String title, String contentTitle, String message) {
    String packageName = mService.getApplication().getPackageName();
    int iconId = mService.getResources().getIdentifier("stat_sys_warning", "drawable", packageName);
    NotificationManager notificationManager =
        (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification note = new Notification(iconId > 0 ? iconId : -1, title, 0);
    note.setLatestEventInfo(mService, contentTitle, message, PendingIntent.getService(mService, 0,
        null, 0));
    notificationManager.notify(NotificationIdFactory.create(), note);
  }

}
