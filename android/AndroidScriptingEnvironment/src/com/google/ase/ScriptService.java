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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.interpreter.InterpreterUtils;

/**
 * A service that allows scripts to run in the background.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptService extends Service {

  private AndroidProxy mAndroidProxy;
  private InterpreterProcess mProcess;
  private String mScriptName;
  private NotificationManager mNotificationManager;

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    mScriptName = intent.getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    if (mScriptName == null) {
      Toast.makeText(this, "Script not specified.", Toast.LENGTH_SHORT).show();
      stopSelf();
    }

    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    String ticker = "Running " + mScriptName;
    Notification notification = new Notification(R.drawable.ase_logo_48, ticker,
        System.currentTimeMillis());
    Intent notificationIntent = new Intent(this, ScriptKiller.class);
    notificationIntent.setAction(Constants.ACTION_KILL_SERVICE);
    PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
    String message = "Running " + mScriptName + ". Tap to stop.";
    notification.setLatestEventInfo(this, "ASE Script Service", message, contentIntent);
    notification.flags = Notification.FLAG_NO_CLEAR;
    mNotificationManager.notify(0, notification);

    String interpreterName = InterpreterUtils.getInterpreterForScript(mScriptName).getName();
    String scriptPath = ScriptStorageAdapter.getScript(mScriptName).getAbsolutePath();

    mAndroidProxy = new AndroidProxy(this, intent);
    int port = mAndroidProxy.startLocal().getPort();
    mProcess =
        InterpreterUtils.getInterpreterByName(interpreterName).buildProcess(scriptPath, port);
    mProcess.start();
    Toast.makeText(this, mScriptName + " service started.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mProcess != null) {
      mProcess.kill();
    }
    if (mAndroidProxy != null) {
      mAndroidProxy.shutdown();
    }
    if (mNotificationManager != null) {
      mNotificationManager.cancelAll();
    }
    Toast.makeText(this, mScriptName + " service stopped.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
