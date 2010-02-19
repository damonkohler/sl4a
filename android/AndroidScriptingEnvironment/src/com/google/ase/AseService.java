/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase;

import java.net.InetSocketAddress;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.ase.terminal.Terminal;

/**
 * A service that allows scripts and the RPC server to run in the background.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class AseService extends Service {

  private AndroidProxy mAndroidProxy;
  private ScriptLauncher mLauncher;
  private NotificationManager mNotificationManager;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    // Handle onStart events that should only occur if the service was already started.
    if (intent.getAction().equals(Constants.ACTION_ACTIVITY_RESULT)) {
      mAndroidProxy.onActivityResult(intent.getIntExtra("requestCode", 0), intent.getIntExtra(
          "resultCode", Activity.RESULT_CANCELED), intent.<Intent> getParcelableExtra("data"));
      return;
    }
    if (intent.getAction().equals(Constants.ACTION_KILL_SERVICE)) {
      stopSelf();
      return;
    }

    // Handle initial onStart events.
    StringBuilder notificationMessage = new StringBuilder();
    // Start proxy.
    mAndroidProxy = new AndroidProxy(this, intent);
    boolean usePublicIp = intent.getBooleanExtra(Constants.EXTRA_USE_EXTERNAL_IP, false);
    InetSocketAddress address =
        usePublicIp ? mAndroidProxy.startPublic() : mAndroidProxy.startLocal();
    notificationMessage.append(String.format("Running network service on: %s:%d",
        address.getHostName(), address.getPort()));

    // Launch script in the background.
    if (intent.getAction().equals(Constants.ACTION_LAUNCH_SCRIPT)) {
      mLauncher = new ScriptLauncher(intent, address);
      try {
        mLauncher.launch();
      } catch (AseException e) {
        AseLog.e(this, e.getMessage(), e);
        stopSelf();
        return;
      }
      notificationMessage.append("\nRunning script service: " + mLauncher.getScriptName());
    }

    showNotification("ASE is running...", "ASE Service", notificationMessage.toString());

    // Launch script in a terminal.
    if (intent.getAction().equals(Constants.ACTION_LAUNCH_TERMINAL)) {
      Intent i = new Intent(this, Terminal.class);
      i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      i.putExtras(intent);
      i.putExtra(Constants.EXTRA_PROXY_PORT, mAndroidProxy.getAddress().getPort());
      startActivity(i);
    }
  }

  private void showNotification(String ticker, String title, String message) {
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    Notification notification =
        new Notification(R.drawable.ase_logo_48, ticker, System.currentTimeMillis());
    notification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    notification.contentView.setTextViewText(R.id.notification_title, title);
    notification.contentView.setTextViewText(R.id.notification_message, message);

    Intent notificationIntent = new Intent(this, AseService.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    notification.contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

    notification.flags = Notification.FLAG_NO_CLEAR;
    notification.flags = Notification.FLAG_ONGOING_EVENT;
    mNotificationManager.notify(0, notification);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mLauncher != null) {
      mLauncher.getProcess().kill();
    }
    if (mAndroidProxy != null) {
      mAndroidProxy.shutdown();
    }
    if (mNotificationManager != null) {
      mNotificationManager.cancelAll();
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
