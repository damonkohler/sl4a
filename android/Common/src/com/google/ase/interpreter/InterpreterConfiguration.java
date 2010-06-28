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

package com.google.ase.interpreter;

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

import com.google.ase.AseLog;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.shell.ShellInterpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages and provides access to the set of available interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class InterpreterConfiguration {

  private final InterpreterListener mListener;
  private final Set<InterpreterAgent> mInterpreterSet;
  private final Set<ConfigurationObserver> mObserverSet;
  private final Context mContext;

  public interface ConfigurationObserver {
    public void onConfigurationChanged();
  }

  private class InterpreterListener extends BroadcastReceiver {
    private final PackageManager mmPackageManager;
    private final ContentResolver mmResolver;
    private final ExecutorService mmExecutor;
    private final Map<String, InterpreterAgent> mmDiscoveredInterpreters;

    private InterpreterListener(Context context) {
      mmPackageManager = context.getPackageManager();
      mmResolver = context.getContentResolver();
      mmExecutor = Executors.newSingleThreadExecutor();
      mmDiscoveredInterpreters = new HashMap<String, InterpreterAgent>();
    }

    private void discoverAll() {
      mmExecutor.submit(new Runnable() {
        @Override
        public void run() {
          Intent intent = new Intent(InterpreterConstants.ACTION_DISCOVER_INTERPRETERS);
          intent.addCategory(Intent.CATEGORY_LAUNCHER);
          List<InterpreterAgent> discoveredInterpreters =
              new ArrayList<InterpreterAgent>();
          List<ResolveInfo> resolveInfos = mmPackageManager.queryIntentActivities(intent, 0);
          for (ResolveInfo info : resolveInfos) {
            InterpreterAgent interpreter =
                buildInterpreter(info.activityInfo.packageName);
            if (interpreter == null) {
              continue;
            }
            mmDiscoveredInterpreters.put(info.activityInfo.packageName, interpreter);
            discoveredInterpreters.add(interpreter);
          }
          mInterpreterSet.addAll(discoveredInterpreters);
          for (ConfigurationObserver observer : mObserverSet) {
            observer.onConfigurationChanged();
          }
        }
      });
    }

    private void addInterpreter(final String packageName) {
      if (mmDiscoveredInterpreters.containsKey(packageName)) {
        return;
      }
      mmExecutor.submit(new Runnable() {
        @Override
        public void run() {
          InterpreterAgent discoveredInterpreter = buildInterpreter(packageName);
          if (discoveredInterpreter == null) {
            return;
          }
          mmDiscoveredInterpreters.put(packageName, discoveredInterpreter);
          mInterpreterSet.add(discoveredInterpreter);
          for (ConfigurationObserver observer : mObserverSet) {
            observer.onConfigurationChanged();
          }
        }
      });
    }

    private void remove(final String packageName) {
      if (!mmDiscoveredInterpreters.containsKey(packageName)) {
        return;
      }
      mmExecutor.submit(new Runnable() {
        @Override
        public void run() {
          InterpreterAgent interpreter = mmDiscoveredInterpreters.get(packageName);
          if (interpreter == null) {
            return;
          }
          mInterpreterSet.remove(interpreter);
          mmDiscoveredInterpreters.remove(packageName);
          for (ConfigurationObserver observer : mObserverSet) {
            observer.onConfigurationChanged();
          }
        }
      });
    }

    // We require that there's only one interpreter provider per APK
    private InterpreterAgent buildInterpreter(String packageName) {
      PackageInfo packInfo = null;
      try {
        packInfo = mmPackageManager.getPackageInfo(packageName, PackageManager.GET_PROVIDERS);
      } catch (NameNotFoundException e) {
        return null;
      }
      ProviderInfo provider = packInfo.providers[0];

      Map<String, String> interpreterMap = getMap(provider, InterpreterConstants.PROVIDER_BASE);
      if (interpreterMap == null) {
        return null; // Apparently, the interpreter is not installed yet.
      }
      Map<String, String> environmentMap = getMap(provider, InterpreterConstants.PROVIDER_ENV);

      InterpreterAgent interpreter = null;
      try {
        interpreter = new Interpreter(interpreterMap, environmentMap);
      } catch (AseException e) {
        AseLog.e(e.getMessage(), e);
      } catch (Exception e) {
        e.printStackTrace();
      }

      return interpreter;
    }

    private Map<String, String> getMap(ProviderInfo provider, String name) {
      Uri uri = Uri.parse("content://" + provider.authority + "/" + name);
      Cursor cursor = mmResolver.query(uri, null, null, null, null);
      if (cursor == null) {
        return null;
      }
      cursor.moveToFirst();
      int size = cursor.getColumnCount();
      Map<String, String> map = new HashMap<String, String>(size);
      for (int i = 0; i < size; i++) {
        map.put(cursor.getColumnName(i), cursor.getString(i));
      }
      return map;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      String packageName = intent.getData().getSchemeSpecificPart();
      if (action.equals(InterpreterConstants.ACTION_INTERPRETER_ADDED)) {
        addInterpreter(packageName);
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
    mInterpreterSet = new CopyOnWriteArraySet<InterpreterAgent>();
    mInterpreterSet.add(new ShellInterpreter());
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
    mListener.discoverAll();
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
  public List<? extends InterpreterAgent> getSupportedInterpreters() {
    return new ArrayList<InterpreterAgent>(mInterpreterSet);
  }

  /**
   * Returns the list of all installed interpreters.
   */
  public List<InterpreterAgent> getInstalledInterpreters() {
    List<InterpreterAgent> interpreters =
        new ArrayList<InterpreterAgent>();
    for (InterpreterAgent i : mInterpreterSet) {
      if (i.isInstalled(mContext)) {
        interpreters.add(i);
      }
    }
    return interpreters;
  }

  /**
   * Returns the list of all not installed interpreters.
   */
  public List<InterpreterAgent> getNotInstalledInterpreters() {
    List<InterpreterAgent> interpreters =
        new ArrayList<InterpreterAgent>();
    for (InterpreterAgent i : mInterpreterSet) {
      if (!i.isInstalled(mContext)) {
        interpreters.add(i);
      }
    }
    return interpreters;
  }

  /**
   * Returns the interpreter matching the provided name or null if no interpreter was found.
   */
  public InterpreterAgent getInterpreterByName(String interpreterName) {
    for (InterpreterAgent i : mInterpreterSet) {
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
  public InterpreterAgent getInterpreterForScript(String scriptName) {
    int dotIndex = scriptName.lastIndexOf('.');
    if (dotIndex == -1) {
      return null;
    }
    String ext = scriptName.substring(dotIndex);
    for (InterpreterAgent i : mInterpreterSet) {
      if (i.getExtension().equals(ext)) {
        return i;
      }
    }
    return null;
  }

}
