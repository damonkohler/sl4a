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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.common.base.Preconditions;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.ForegroundService;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.event.Event;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.facade.EventFacade.EventObserver;
import com.googlecode.android_scripting.trigger.EventGenerationControllingObserver;
import com.googlecode.android_scripting.trigger.Trigger;
import com.googlecode.android_scripting.trigger.TriggerRepository;
import com.googlecode.android_scripting.trigger.TriggerRepository.TriggerRepositoryObserver;

/**
 * The trigger service takes care of installing triggers serialized to the preference storage.
 * 
 * <p>
 * The service also installs an alarm that keeps it running, unless the user force-quits the
 * service.
 * 
 * <p>
 * When no triggers are installed the service shuts down silently as to not consume resources
 * unnecessarily.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class TriggerService extends ForegroundService {
  private static final int NOTIFICATION_ID = NotificationIdFactory.create();
  private static final long PING_MILLIS = 10 * 1000 * 60;

  private final IBinder mBinder;
  private TriggerRepository mTriggerRepository;
  private FacadeManager mFacadeManager;
  private EventFacade mEventFacade;

  public class LocalBinder extends Binder {
    public TriggerService getService() {
      return TriggerService.this;
    }
  }

  public TriggerService() {
    super(NOTIFICATION_ID);
    mBinder = new LocalBinder();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

  @Override
  public void onCreate() {
    super.onCreate();

    mFacadeManager =
        new FacadeManager(FacadeConfiguration.getSdkLevel(), this, null, FacadeConfiguration
            .getFacadeClasses());
    mEventFacade = mFacadeManager.getReceiver(EventFacade.class);

    mTriggerRepository = ((BaseApplication) getApplication()).getTriggerRepository();
    mTriggerRepository.bootstrapObserver(new RepositoryObserver());
    mTriggerRepository.bootstrapObserver(new EventGenerationControllingObserver(mFacadeManager));
    installAlarm();
  }

  @Override
  public void onStart(Intent intent, int startId) {
    if (mTriggerRepository.isEmpty()) {
      stopSelfResult(startId);
      return;
    }
  }

  /** Returns the notification to display whenever the service is running. */
  @Override
  protected Notification createNotification() {
    Notification notification =
        new Notification(R.drawable.sl4a_logo_48, "SL4A Trigger Service started.", System
            .currentTimeMillis());
    notification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    notification.contentView.setTextViewText(R.id.notification_title, "SL4A Trigger Service");
    notification.contentView.setTextViewText(R.id.notification_action, "Tap to view triggers.");
    Intent notificationIntent = new Intent(this, TriggerManager.class);
    notification.contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    notification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    return notification;
  }

  private class TriggerEventObserver implements EventObserver {
    private final Trigger mTrigger;

    public TriggerEventObserver(Trigger trigger) {
      mTrigger = trigger;
    }

    @Override
    public void onEventReceived(Event event) {
      mTrigger.handleEvent(event, TriggerService.this);
    }
  }

  private class RepositoryObserver implements TriggerRepositoryObserver {
    int mTriggerCount = 0;

    @Override
    public void onPut(Trigger trigger) {
      mTriggerCount++;
      mEventFacade.addNamedEventObserver(trigger.getEventName(), new TriggerEventObserver(trigger));
    }

    @Override
    public void onRemove(Trigger trigger) {
      Preconditions.checkArgument(mTriggerCount > 0);
      // TODO(damonkohler): Tear down EventObserver associated with trigger.
      if (--mTriggerCount == 0) {
        // TODO(damonkohler): Use stopSelfResult() which would require tracking startId.
        stopSelf();
      }
    }
  }

  private void installAlarm() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + PING_MILLIS,
        PING_MILLIS, IntentBuilders.buildTriggerServicePendingIntent(this));
  }

  private void uninstallAlarm() {
    AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
    alarmManager.cancel(IntentBuilders.buildTriggerServicePendingIntent(this));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    uninstallAlarm();
  }
}
