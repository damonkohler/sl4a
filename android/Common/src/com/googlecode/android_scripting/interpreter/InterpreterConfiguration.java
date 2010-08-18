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

package com.googlecode.android_scripting.interpreter;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.SingleThreadExecutor;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;
import com.googlecode.android_scripting.interpreter.shell.ShellInterpreter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;

/**
 * Manages and provides access to the set of available interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class InterpreterConfiguration {

  private final InterpreterListener mListener;
  private final Set<Interpreter> mInterpreterSet;
  private final Set<ConfigurationObserver> mObserverSet;
  private final Context mContext;
  private volatile boolean mIsDiscoveryComplete = false;

  public interface ConfigurationObserver {
    public void onConfigurationChanged();
  }

  private class InterpreterListener extends BroadcastReceiver {
    private final PackageManager mmPackageManager;
    private final ContentResolver mmResolver;
    private final ExecutorService mmExecutor;
    private final Map<String, Interpreter> mmDiscoveredInterpreters;

    private InterpreterListener(Context context) {
      mmPackageManager = context.getPackageManager();
      mmResolver = context.getContentResolver();
      mmExecutor = new SingleThreadExecutor();
      mmDiscoveredInterpreters = new HashMap<String, Interpreter>();
    }

    private void discoverForType(final String mime) {
      mmExecutor.execute(new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(InterpreterConstants.ACTION_DISCOVER_INTERPRETERS);
          intent.addCategory(Intent.CATEGORY_LAUNCHER);
          intent.setType(mime);
          List<ResolveInfo> resolveInfos = mmPackageManager.queryIntentActivities(intent, 0);
          for (ResolveInfo info : resolveInfos) {
            addInterpreter(info.activityInfo.packageName);
          }
          mIsDiscoveryComplete = true;
          notifyConfigurationObservers();
        }
      });
    }

    private void discoverAll() {
      mmExecutor.execute(new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(InterpreterConstants.ACTION_DISCOVER_INTERPRETERS);
          intent.addCategory(Intent.CATEGORY_LAUNCHER);
          intent.setType(InterpreterConstants.MIME + "*");
          List<ResolveInfo> resolveInfos = mmPackageManager.queryIntentActivities(intent, 0);
          for (ResolveInfo info : resolveInfos) {
            addInterpreter(info.activityInfo.packageName);
          }
          mIsDiscoveryComplete = true;
          notifyConfigurationObservers();
        }
      });
    }

    private void notifyConfigurationObservers() {
      for (ConfigurationObserver observer : mObserverSet) {
        observer.onConfigurationChanged();
      }
    }

    private void addInterpreter(final String packageName) {
      if (mmDiscoveredInterpreters.containsKey(packageName)) {
        return;
      }
      Interpreter discoveredInterpreter = buildInterpreter(packageName);
      if (discoveredInterpreter == null) {
        return;
      }
      mmDiscoveredInterpreters.put(packageName, discoveredInterpreter);
      mInterpreterSet.add(discoveredInterpreter);
      Log.v("Interpreter discovered: " + packageName + "\nBinary: "
          + discoveredInterpreter.getBinary());
    }

    private void remove(final String packageName) {
      if (!mmDiscoveredInterpreters.containsKey(packageName)) {
        return;
      }
      mmExecutor.execute(new Runnable() {
        @Override
        public void run() {
          Interpreter interpreter = mmDiscoveredInterpreters.get(packageName);
          if (interpreter == null) {
            Log.v("Interpreter for " + packageName + " not installed.");
            return;
          }
          mInterpreterSet.remove(interpreter);
          mmDiscoveredInterpreters.remove(packageName);
          notifyConfigurationObservers();
        }
      });
    }

    // We require that there's only one interpreter provider per APK.
    private Interpreter buildInterpreter(String packageName) {
      PackageInfo packInfo;
      try {
        packInfo = mmPackageManager.getPackageInfo(packageName, PackageManager.GET_PROVIDERS);
      } catch (NameNotFoundException e) {
        throw new RuntimeException("Package '" + packageName + "' not found.");
      }
      ProviderInfo provider = packInfo.providers[0];

      Map<String, String> interpreterMap =
          getMap(provider, InterpreterConstants.PROVIDER_PROPERTIES);
      if (interpreterMap == null) {
        Log.e("Null interpreter map for: " + packageName);
        return null;
      }
      Map<String, String> environmentMap =
          getMap(provider, InterpreterConstants.PROVIDER_ENVIRONMENT_VARIABLES);
      if (environmentMap == null) {
        throw new RuntimeException("Null environment map for: " + packageName);
      }
      Map<String, String> argumentsMap = getMap(provider, InterpreterConstants.PROVIDER_ARGUMENTS);
      if (argumentsMap == null) {
        throw new RuntimeException("Null arguments map for: " + packageName);
      }
      return Interpreter.buildFromMaps(interpreterMap, environmentMap, argumentsMap);
    }

    private Map<String, String> getMap(ProviderInfo provider, String name) {
      Uri uri = Uri.parse("content://" + provider.authority + "/" + name);
      Cursor cursor = mmResolver.query(uri, null, null, null, null);
      if (cursor == null) {
        return null;
      }
      cursor.moveToFirst();
      // Use LinkedHashMap so that order is maintained (important for position CLI arguments).
      Map<String, String> map = new LinkedHashMap<String, String>();
      for (int i = 0; i < cursor.getColumnCount(); i++) {
        map.put(cursor.getColumnName(i), cursor.getString(i));
      }
      return map;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      final String packageName = intent.getData().getSchemeSpecificPart();
      if (action.equals(InterpreterConstants.ACTION_INTERPRETER_ADDED)) {
        mmExecutor.execute(new Runnable() {
          @Override
          public void run() {
            addInterpreter(packageName);
            notifyConfigurationObservers();
          }
        });
      } else if (action.equals(InterpreterConstants.ACTION_INTERPRETER_REMOVED)
          || action.equals(Intent.ACTION_PACKAGE_REMOVED)
          || action.equals(Intent.ACTION_PACKAGE_REPLACED)
          || action.equals(Intent.ACTION_PACKAGE_DATA_CLEARED)) {
        remove(packageName);
      }
    }

  }

  public InterpreterConfiguration(Context context) {
    mContext = context;
    mInterpreterSet = new CopyOnWriteArraySet<Interpreter>();
    mInterpreterSet.add(new ShellInterpreter());
    try {
      mInterpreterSet.add(new HtmlInterpreter(mContext));
    } catch (IOException e) {
      Log.e("Failed to instantiate HtmlInterpreter.", e);
    }
    mObserverSet = new CopyOnWriteArraySet<ConfigurationObserver>();
    IntentFilter filter = new IntentFilter();
    filter.addAction(InterpreterConstants.ACTION_INTERPRETER_ADDED);
    filter.addAction(InterpreterConstants.ACTION_INTERPRETER_REMOVED);
    filter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
    filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
    filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
    filter.addDataScheme("package");
    mListener = new InterpreterListener(mContext);
    mContext.registerReceiver(mListener, filter);
  }

  public void startDiscovering() {
    mListener.discoverAll();
  }

  public void startDiscovering(String mime) {
    mListener.discoverForType(mime);
  }

  public boolean isDiscoveryComplete() {
    return mIsDiscoveryComplete;
  }

  public void registerObserver(ConfigurationObserver observer) {
    if (observer != null) {
      mObserverSet.add(observer);
    }
  }

  public void unregisterObserver(ConfigurationObserver observer) {
    if (observer != null) {
      mObserverSet.remove(observer);
    }
  }

  /**
   * Returns the list of all known interpreters.
   */
  public List<? extends Interpreter> getSupportedInterpreters() {
    return new ArrayList<Interpreter>(mInterpreterSet);
  }

  /**
   * Returns the list of all installed interpreters.
   */
  public List<Interpreter> getInstalledInterpreters() {
    List<Interpreter> interpreters = new ArrayList<Interpreter>();
    for (Interpreter i : mInterpreterSet) {
      if (i.isInstalled()) {
        interpreters.add(i);
      }
    }
    return interpreters;
  }

  /**
   * Returns the list of interpreters that support interactive mode execution.
   */
  public List<Interpreter> getInteractiveInterpreters() {
    List<Interpreter> interpreters = new ArrayList<Interpreter>();
    for (Interpreter i : mInterpreterSet) {
      if (i.isInstalled() && i.hasInteractiveMode()) {
        interpreters.add(i);
      }
    }
    return interpreters;
  }

  /**
   * Returns the interpreter matching the provided name or null if no interpreter was found.
   */
  public Interpreter getInterpreterByName(String interpreterName) {
    for (Interpreter i : mInterpreterSet) {
      if (i.getName().equals(interpreterName)) {
        return i;
      }
    }
    return null;
  }

  /**
   * Returns the correct interpreter for the provided script name based on the script's extension or
   * null if no interpreter was found.
   */
  public Interpreter getInterpreterForScript(String scriptName) {
    int dotIndex = scriptName.lastIndexOf('.');
    if (dotIndex == -1) {
      return null;
    }
    String ext = scriptName.substring(dotIndex);
    for (Interpreter i : mInterpreterSet) {
      if (i.getExtension().equals(ext)) {
        return i;
      }
    }
    return null;
  }
}
