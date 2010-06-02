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

package com.google.ase.activity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.IntentBuilders;
import com.google.ase.R;
import com.google.ase.trigger.EventTrigger;
import com.google.ase.trigger.Trigger;
import com.google.ase.trigger.TriggerRepository;
import com.google.ase.trigger.TriggerRepository.AddTriggerListener;

/**
 * The trigger service takes care of installing triggers serialized to the preference storage.
 * 
 * The service also installs an alarm that keeps it running, unless the user force-quits the
 * service.
 * 
 * When no triggers are installed the service shuts down silently as to not consume resources
 * unnecessarily.
 * 
 * @author Felix Arends (felix.arends@gmail.com) Damon Kohler (damonkohler@gmail.com)
 */
public class TriggerService extends Service {
  private TriggerRepository mTriggerRepository;
  private static int mTriggerServiceNotificationId;
  private static final long TRIGGER_SERVICE_PING_MILLIS = 10 * 1000 * 60;

  private final AddTriggerListener addTriggerListener = new AddTriggerListener() {
    @Override
    public void onAddTrigger(Trigger trigger) {
      trigger.install(TriggerService.this);
    }
  };

  public TriggerService() {
  }

  private void initializeTriggers() {
    for (Trigger trigger : mTriggerRepository.getAllTriggers()) {
      if (trigger instanceof EventTrigger) {
        trigger.install(this);
      }
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();

    AseApplication application = (AseApplication) this.getApplication();
    mTriggerServiceNotificationId = application.getNewNotificationId();
    mTriggerRepository = application.getTriggerRepository();
    mTriggerRepository.registerAddTriggerListener(addTriggerListener);

    initializeTriggers();

    setForeground();

    installAlarm();
  }

  /**
   * Marks the service as a foreground service. This uses reflection to figure out whether the new
   * APIs for marking a service as a foreground service are available. If not, it falls back to the
   * old {@link #setForeground(boolean)} call.
   */
  private void setForeground() {
    final Class<?>[] startForegroundSignature = new Class[] { int.class, Notification.class };
    Method startForeground = null;
    try {
      startForeground = getClass().getMethod("startForeground", startForegroundSignature);

      try {
        startForeground.invoke(this, new Object[] { Integer.valueOf(mTriggerServiceNotificationId),
          createNotification() });
      } catch (IllegalArgumentException e) {
        // Should not happen!
        AseLog.e("Could not set TriggerService to foreground mode.", e);
      } catch (IllegalAccessException e) {
        // Should not happen!
        AseLog.e("Could not set TriggerService to foreground mode.", e);
      } catch (InvocationTargetException e) {
        // Should not happen!
        AseLog.e("Could not set TriggerService to foreground mode.", e);
      }

    } catch (NoSuchMethodException e) {
      // Fall back on old API.
      setForeground(true);

      NotificationManager manager =
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(mTriggerServiceNotificationId, createNotification());
    }
  }

  /** Returns the notificaiton to display whenever the service is running. */
  private Notification createNotification() {
    Notification notification =
        new Notification(R.drawable.ase_logo_48, "ASE Trigger Service is running...", System
            .currentTimeMillis());
    notification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    notification.contentView.setTextViewText(R.id.notification_title, "ASE Trigger Service");
    Intent notificationIntent = new Intent(this, TriggerService.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    notification.contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    // NotificationManager manager =
    //    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    // manager.notify(mTriggerServiceNotificationId, notification);
    return notification;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    if (intent.getAction() != null
        && Constants.ACTION_KILL_SERVICE.compareTo(intent.getAction()) == 0) {
      uninstallAlarm();
      stopSelf();
      return;
    }
  }

  private void installAlarm() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
        + TRIGGER_SERVICE_PING_MILLIS, TRIGGER_SERVICE_PING_MILLIS, IntentBuilders
        .buildTriggerServicePendingIntent(this));
  }

  private void uninstallAlarm() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(IntentBuilders.buildTriggerServicePendingIntent(this));
  }

  @Override
  public void onDestroy() {
    NotificationManager manager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.cancel(mTriggerServiceNotificationId);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
