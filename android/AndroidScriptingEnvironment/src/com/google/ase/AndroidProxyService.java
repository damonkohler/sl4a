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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.ase.jsonrpc.JsonRpcServer;

public class AndroidProxyService extends Service {
  private NotificationManager mNotificationManager;
  private JsonRpcServer mRpcServer;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    boolean usePublicIp = intent.getBooleanExtra(Constants.EXTRA_USE_EXTERNAL_IP, false);
    mRpcServer = AndroidProxyFactory.create(this, intent);
    final InetSocketAddress address = usePublicIp ?
        mRpcServer.startPublic() : mRpcServer.startLocal();

    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String ticker = String.format("ASE running on %s:%d", address.getHostName(), address.getPort());
    Notification notification =
        new Notification(R.drawable.ase_logo_48, ticker, System.currentTimeMillis());
    Intent notificationIntent = new Intent(this, ScriptKiller.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    String message =
        String.format("%s:%d - Tap to stop.", address.getHostName(), address.getPort());
    notification.setLatestEventInfo(this, "ASE Network Service", message, contentIntent);
    notification.flags = Notification.FLAG_NO_CLEAR;
    mNotificationManager.notify(0, notification);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mRpcServer.shutdown();
    mNotificationManager.cancelAll();
    Toast.makeText(this, "ASE network service stopped.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
