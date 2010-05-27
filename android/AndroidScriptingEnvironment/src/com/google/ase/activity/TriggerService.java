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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.ase.AseApplication;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.trigger.ConditionTrigger;
import com.google.ase.trigger.Trigger;
import com.google.ase.trigger.TriggerRepository;

/**
 * The trigger service takes care of installing triggers serialized to the preference storage.
 * 
 * @author Felix Arends (felix.arends@gmail.com) Damon Kohler (damonkohler@gmail.com)
 */
public class TriggerService extends Service {
  private TriggerRepository mTriggerRepository;
  private static final int mTriggerServiceNotificationId = NotificationIdFactory.createId();

  public TriggerService() {
  }

  private void initializeTriggers() {
    for (Trigger trigger : mTriggerRepository.getAllTriggers()) {
      if (trigger instanceof ConditionTrigger) {
        trigger.install();
      }
    }
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    AseApplication application = (AseApplication) this.getApplication();
    mTriggerRepository = application.getTriggerRepository();
    initializeTriggers();

    ((AseApplication) getApplication()).setTriggerService(this);

    setForeground(true);

    Notification notification =
        new Notification(R.drawable.ase_logo_48, "ASE is running...", System.currentTimeMillis());
    notification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    notification.contentView.setTextViewText(R.id.notification_title, "ASE Trigger Service");
    Intent notificationIntent = new Intent(this, AseService.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    notification.contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    NotificationManager manager =
        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(mTriggerServiceNotificationId, notification);
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
