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

package com.google.ase.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.google.ase.AndroidProxy;
import com.google.ase.AseApplication;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.ScriptLauncher;
import com.google.ase.ScriptProcess;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.terminal.Terminal;
import com.google.ase.trigger.Trigger;

import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.exception.Sl4aException;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service that allows scripts and the RPC server to run in the background.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class AseService extends Service {

  private final Map<Integer, ScriptProcess> mProcessMap;
  private NotificationManager mNotificationManager;
  private Notification mNotification;
  private final IBinder mBinder;
  private volatile int modCount = 0;

  private static final int mNotificationId = NotificationIdFactory.create();

  public class LocalBinder extends Binder {
    public AseService getService() {
      return AseService.this;
    }
  }

  public AseService() {
    mProcessMap = new ConcurrentHashMap<Integer, ScriptProcess>();
    mBinder = new LocalBinder();
  }

  @Override
  public void onCreate() {
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    createNotification();
    ServiceUtils.setForeground(this, mNotificationId, mNotification);
  }

  private void createNotification() {
    String notificationMessage = "Service is created.";
    mNotification =
        new Notification(R.drawable.ase_logo_48, "ASE is running...", System.currentTimeMillis());
    mNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    mNotification.contentView.setTextViewText(R.id.notification_title, "ASE Service");
    mNotification.contentView.setTextViewText(R.id.notification_message, notificationMessage);
    mNotification.contentView.setTextViewText(R.id.notification_action, null);
    mNotification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    mNotification.contentIntent = PendingIntent.getService(this, 0, null, 0);
  }

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);

    if (intent.getAction().equals(Constants.ACTION_KILL_ALL)) {
      killAll();
      stopSelf(startId);
      return;
    }

    if (intent.getAction().equals(Constants.ACTION_KILL_PROCESS)) {
      killScriptProcess(intent);
      if (mProcessMap.isEmpty()) {
        stopSelf(startId);
      }
      return;
    }

    if (intent.getAction().equals(Constants.ACTION_SHOW_RUNNING_SCRIPTS)) {
      showRunningScripts();
      return;
    }

    AndroidProxy serverProxy = null;
    ScriptLauncher launcher = null;

    if (intent.getAction().equals(Constants.ACTION_LAUNCH_SERVER)) {
      serverProxy = launchServer(intent, false);
    } else if (intent.getAction().equals(Constants.ACTION_LAUNCH_SCRIPT)
        || intent.getAction().equals(Constants.ACTION_LAUNCH_TERMINAL)) {
      serverProxy = launchServer(intent, true);
      try {
        launcher = launchScript(intent, serverProxy);
      } catch (Sl4aException e) {
        Sl4aLog.e(this, e.getMessage(), e);
        serverProxy.shutdown();
        serverProxy = null;
        return;
      }

      if (intent.getAction().equals(Constants.ACTION_LAUNCH_TERMINAL)) {
        launchTerminal(intent, serverProxy.getAddress());
      }
    }

    ScriptProcess scriptProcess = new ScriptProcess(serverProxy, launcher, getTrigger(intent));
    addScriptProcess(scriptProcess);
    scriptProcess.notifyTriggerOfStart(this);
  }

  private AndroidProxy launchServer(Intent intent, boolean requiresHandshake) {
    AndroidProxy androidProxy = new AndroidProxy(this, intent, requiresHandshake);
    boolean usePublicIp = intent.getBooleanExtra(Constants.EXTRA_USE_EXTERNAL_IP, false);
    if (usePublicIp) {
      androidProxy.startPublic();
    } else {
      androidProxy.startLocal();
    }
    return androidProxy;
  }

  private ScriptLauncher launchScript(Intent intent, AndroidProxy proxy) throws Sl4aException {
    InterpreterConfiguration config =
        ((AseApplication) getApplication()).getInterpreterConfiguration();
    ScriptLauncher launcher = new ScriptLauncher(proxy, intent, config);
    final int port = proxy.getAddress().getPort();
    launcher.launch(new Runnable() {
      @Override
      public void run() {
        Intent intent = new Intent(AseService.this, AseService.class);
        intent.setAction(Constants.ACTION_KILL_PROCESS);
        intent.putExtra(Constants.EXTRA_PROXY_PORT, port);
        startService(intent);
      }
    });
    return launcher;
  }

  private void launchTerminal(Intent intent, InetSocketAddress address) {
    Intent i = new Intent(this, Terminal.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtras(intent);
    i.putExtra(Constants.EXTRA_PROXY_PORT, address.getPort());
    startActivity(i);
  }

  private void showRunningScripts() {
    Intent i = new Intent(this, AseMonitor.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  private void addScriptProcess(ScriptProcess process) {
    mProcessMap.put(process.getPort(), process);
    modCount++;
    updateNotification();
  }

  private ScriptProcess removeScriptProcess(int id) {
    ScriptProcess process;
    process = mProcessMap.remove(id);
    if (process == null) {
      return null;
    }
    modCount++;
    updateNotification();
    return process;
  }

  private void killScriptProcess(Intent intent) {
    int processId = intent.getIntExtra(Constants.EXTRA_PROXY_PORT, 0);
    ScriptProcess scriptProcess = removeScriptProcess(processId);
    scriptProcess.notifyTriggerOfShutDown(this);
    if (scriptProcess != null) {
      scriptProcess.kill();
    }
  }

  public int getModCount() {
    return modCount;
  }

  private void killAll() {
    for (ScriptProcess scriptProcess : getScriptProcessesList()) {
      scriptProcess = removeScriptProcess(scriptProcess.getPort());
      scriptProcess.notifyTriggerOfShutDown(this);
      if (scriptProcess != null) {
        scriptProcess.kill();
      }
    }
  }

  public List<ScriptProcess> getScriptProcessesList() {
    ArrayList<ScriptProcess> result = new ArrayList<ScriptProcess>();
    result.addAll(mProcessMap.values());
    return result;
  }

  public ScriptProcess getScriptProcess(int processPort) {
    return mProcessMap.get(processPort);
  }

  private void updateNotification() {
    StringBuffer message = new StringBuffer();
    Intent notificationIntent = new Intent(this, AseService.class);
    mNotification.flags = 0;

    if (mProcessMap.size() == 0) {
      message.append(getText(R.string.no_running_scripts_message));
      notificationIntent = null;
      mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
      mNotification.contentView.setTextViewText(R.id.notification_action, null);
    } else {
      int numProcesses = mProcessMap.size();
      message.append(getText(R.string.script_number_message));
      message.append(numProcesses);
      mNotification.contentView.setTextViewText(R.id.notification_action,
          getText(R.string.notification_action_message));
      notificationIntent.setAction(Constants.ACTION_SHOW_RUNNING_SCRIPTS);
    }
    mNotification.contentView.setTextViewText(R.id.notification_message, message);
    mNotification.contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
    mNotification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    mNotificationManager.notify(mNotificationId, mNotification);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mNotificationManager.cancel(mNotificationId);
  }

  /** Returns the {@link TriggerInfo} for the given intent, or null if none exists. */
  private Trigger getTrigger(Intent intent) {
    final AseApplication application = (AseApplication) getApplication();
    final String triggerIdExtra = intent.getStringExtra(Constants.EXTRA_TRIGGER_ID);
    if (triggerIdExtra == null) {
      return null;
    }

    try {
      final UUID triggerId = UUID.fromString(triggerIdExtra);
      return application.getTriggerRepository().getById(triggerId);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
