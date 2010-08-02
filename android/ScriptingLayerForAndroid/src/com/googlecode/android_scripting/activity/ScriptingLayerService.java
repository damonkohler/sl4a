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

package com.googlecode.android_scripting.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.ScriptProcess;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;
import com.googlecode.android_scripting.interpreter.shell.ShellInterpreter;
import com.googlecode.android_scripting.terminal.Terminal;
import com.googlecode.android_scripting.trigger.Trigger;

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
public class ScriptingLayerService extends Service {

  private final Map<Integer, InterpreterProcess> mProcessMap;
  private NotificationManager mNotificationManager;
  private Notification mNotification;
  private final IBinder mBinder;
  private volatile int modCount = 0;
  private InterpreterConfiguration mInterpreterConfiguration;

  private static final int mNotificationId = NotificationIdFactory.create();

  public class LocalBinder extends Binder {
    public ScriptingLayerService getService() {
      return ScriptingLayerService.this;
    }
  }

  public ScriptingLayerService() {
    mProcessMap = new ConcurrentHashMap<Integer, InterpreterProcess>();
    mBinder = new LocalBinder();
  }

  @Override
  public void onCreate() {
    mInterpreterConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
    mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    createNotification();
    ServiceUtils.setForeground(this, mNotificationId, mNotification);
  }

  private void createNotification() {
    mNotification =
        new Notification(R.drawable.sl4a_logo_48, "SL4A Service is running...", System
            .currentTimeMillis());
    mNotification.contentView = new RemoteViews(getPackageName(), R.layout.notification);
    mNotification.contentView.setTextViewText(R.id.notification_title, "SL4A Service");
    mNotification.contentView.setTextViewText(R.id.notification_action,
        "Tap to view running scripts.");
    mNotification.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
    Intent notificationIntent = new Intent(this, ScriptingLayerService.class);
    notificationIntent.setAction(Constants.ACTION_SHOW_RUNNING_SCRIPTS);
    mNotification.contentIntent = PendingIntent.getService(this, 0, notificationIntent, 0);
  }

  private void updateNotification() {
    StringBuilder message = new StringBuilder();
    message.append(getText(R.string.script_number_message));
    message.append(mProcessMap.size());
    mNotification.contentView.setTextViewText(R.id.notification_message, message);
    mNotificationManager.notify(mNotificationId, mNotification);
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
      killProcess(intent);
      if (mProcessMap.isEmpty()) {
        stopSelf(startId);
      }
      return;
    }

    if (intent.getAction().equals(Constants.ACTION_SHOW_RUNNING_SCRIPTS)) {
      showRunningScripts();
      return;
    }

    AndroidProxy proxy = null;
    InterpreterProcess interpreterProcess = null;

    if (intent.getAction().equals(Constants.ACTION_LAUNCH_SERVER)) {
      proxy = launchServer(intent, false);
      // TODO(damonkohler): This is just to make things easier. Really, we shouldn't need to start
      // an interpreter when all we want is a server.
      interpreterProcess = new InterpreterProcess(new ShellInterpreter(), proxy);
    } else {
      proxy = launchServer(intent, true);
      if (intent.getAction().equals(Constants.ACTION_LAUNCH_FOREGROUND_SCRIPT)) {
        launchTerminal(intent, proxy.getAddress());
        interpreterProcess = launchScript(intent, proxy, getTrigger(intent));
        ((ScriptProcess) interpreterProcess).notifyTriggerOfStart(this);
      } else if (intent.getAction().equals(Constants.ACTION_LAUNCH_BACKGROUND_SCRIPT)) {
        interpreterProcess = launchScript(intent, proxy, getTrigger(intent));
        ((ScriptProcess) interpreterProcess).notifyTriggerOfStart(this);
      } else if (intent.getAction().equals(Constants.ACTION_LAUNCH_INTERPRETER)) {
        launchTerminal(intent, proxy.getAddress());
        interpreterProcess = launchInterpreter(intent, proxy);
      }
    }
    addProcess(interpreterProcess);
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

  private ScriptProcess launchScript(Intent intent, AndroidProxy proxy, Trigger trigger) {
    final int port = proxy.getAddress().getPort();
    return ScriptLauncher.launchScript(mInterpreterConfiguration, proxy, intent, trigger,
        new Runnable() {
          @Override
          public void run() {
            // TODO(damonkohler): This action actually kills the script rather than notifying the
            // service that script exited on its own. We should distinguish between these two cases.
            Intent intent = new Intent(ScriptingLayerService.this, ScriptingLayerService.class);
            intent.setAction(Constants.ACTION_KILL_PROCESS);
            intent.putExtra(Constants.EXTRA_PROXY_PORT, port);
            startService(intent);
          }
        });
  }

  private InterpreterProcess launchInterpreter(Intent intent, AndroidProxy proxy) {
    InterpreterConfiguration config =
        ((BaseApplication) getApplication()).getInterpreterConfiguration();
    final int port = proxy.getAddress().getPort();
    return ScriptLauncher.launchInterpreter(proxy, intent, config, new Runnable() {
      @Override
      public void run() {
        // TODO(damonkohler): This action actually kills the script rather than notifying the
        // service that script exited on its own. We should distinguish between these two cases.
        Intent intent = new Intent(ScriptingLayerService.this, ScriptingLayerService.class);
        intent.setAction(Constants.ACTION_KILL_PROCESS);
        intent.putExtra(Constants.EXTRA_PROXY_PORT, port);
        startService(intent);
      }
    });
  }

  private void launchTerminal(Intent intent, InetSocketAddress address) {
    Intent i = new Intent(this, Terminal.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.putExtras(intent);
    i.putExtra(Constants.EXTRA_PROXY_PORT, address.getPort());
    startActivity(i);
  }

  private void showRunningScripts() {
    Intent i = new Intent(this, ScriptProcessMonitor.class);
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(i);
  }

  private void addProcess(InterpreterProcess process) {
    mProcessMap.put(process.getPort(), process);
    modCount++;
    updateNotification();
  }

  private InterpreterProcess removeProcess(int port) {
    InterpreterProcess process = mProcessMap.remove(port);
    if (process == null) {
      return null;
    }
    modCount++;
    updateNotification();
    return process;
  }

  private void killProcess(Intent intent) {
    int processId = intent.getIntExtra(Constants.EXTRA_PROXY_PORT, 0);
    InterpreterProcess process = removeProcess(processId);
    if (process != null) {
      if (process instanceof ScriptProcess) {
        ((ScriptProcess) process).notifyTriggerOfShutDown(this);
      }
      process.kill();
    }
  }

  public int getModCount() {
    return modCount;
  }

  private void killAll() {
    for (InterpreterProcess process : getScriptProcessesList()) {
      process = removeProcess(process.getPort());
      if (process instanceof ScriptProcess) {
        ((ScriptProcess) process).notifyTriggerOfShutDown(this);
      }
      if (process != null) {
        process.kill();
      }
    }
  }

  public List<InterpreterProcess> getScriptProcessesList() {
    ArrayList<InterpreterProcess> result = new ArrayList<InterpreterProcess>();
    result.addAll(mProcessMap.values());
    return result;
  }

  public InterpreterProcess getProcess(int port) {
    return mProcessMap.get(port);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mNotificationManager.cancel(mNotificationId);
  }

  /** Returns the {@link TriggerInfo} for the given intent, or null if none exists. */
  private Trigger getTrigger(Intent intent) {
    final BaseApplication application = (BaseApplication) getApplication();
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
