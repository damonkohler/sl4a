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

package com.googlecode.android_scripting.activity;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.trigger.Trigger;
import com.googlecode.android_scripting.trigger.TriggerRepository;
import com.googlecode.android_scripting.trigger.TriggerRepository.AddTriggerListener;

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

  private final AddTriggerListener mAddTriggerListener = new AddTriggerListener() {
    @Override
    public void onAddTrigger(Trigger trigger) {
      trigger.install(TriggerService.this);
    }
  };

  public TriggerService() {
  }

  private void initializeTriggers() {
    for (Trigger trigger : mTriggerRepository.getAllTriggers()) {
      trigger.install(this);
    }
  }

  @Override
  public void onCreate() {
    super.onCreate();

    mTriggerServiceNotificationId = NotificationIdFactory.create();
    BaseApplication application = (BaseApplication) getApplication();
    mTriggerRepository = application.getTriggerRepository();
    mTriggerRepository.registerAddTriggerListener(mAddTriggerListener);

    if (mTriggerRepository.isEmpty()) {
      stopSelf();
    } else {
      initializeTriggers();
      ServiceUtils.setForeground(this, mTriggerServiceNotificationId, createNotification());
      installAlarm();
    }
  }

  /** Returns the notification to display whenever the service is running. */
  private Notification createNotification() {
    Notification notification =
        new Notification(R.drawable.sl4a_logo_48, "SL4A Trigger Service is running...", System
            .currentTimeMillis());
    notification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    notification.contentView.setTextViewText(R.id.notification_title, "SL4A Trigger Service");
    notification.contentView.setTextViewText(R.id.notification_action, "Tap to view triggers.");
    Intent notificationIntent = new Intent(this, TriggerManager.class);
    notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    return notification;
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    if (intent.getAction() != null
        && Constants.ACTION_KILL_PROCESS.compareTo(intent.getAction()) == 0) {
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

    mTriggerRepository.unregisterAddListener(mAddTriggerListener);
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
