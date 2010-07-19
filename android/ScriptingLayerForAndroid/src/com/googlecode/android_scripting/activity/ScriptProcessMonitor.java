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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptProcess;
import com.googlecode.android_scripting.terminal.Terminal;

/**
 * An activity that allows to monitor running scripts.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class ScriptProcessMonitor extends ListActivity {

  private final static int UPDATE_INTERVAL_SECS = 1;

  private List<ScriptProcess> mProcessList;
  private ScriptMonitorAdapter mAdapter;
  private volatile ScriptingLayerService mService;
  private boolean isConnected = false;

  private final Timer mTimer = new Timer();
  private ScriptListAdapter mUpdater;

  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mService = ((ScriptingLayerService.LocalBinder) service).getService();
      mUpdater = new ScriptListAdapter();
      mTimer.scheduleAtFixedRate(mUpdater, 0, UPDATE_INTERVAL_SECS * 1000);
      isConnected = true;
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService = null;
      isConnected = false;
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bindService(new Intent(this, ScriptingLayerService.class), mConnection, 0);
    CustomizeWindow.requestCustomTitle(this, "Script Monitor", R.layout.script_monitor);
    mAdapter = new ScriptMonitorAdapter();
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    Analytics.trackActivity(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    mUpdater.cancel();
    mTimer.purge();
  }

  @Override
  public void onResume() {
    super.onResume();
    if (isConnected) {
      try {
        mUpdater = new ScriptListAdapter();
        mTimer.scheduleAtFixedRate(mUpdater, 0, UPDATE_INTERVAL_SECS * 1000);
      } catch (IllegalStateException e) {
        Log.e(e.getMessage(), e);
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mTimer.cancel();
    unbindService(mConnection);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final ScriptProcess script = (ScriptProcess) list.getItemAtPosition(position);
    Intent intent = new Intent(ScriptProcessMonitor.this, Terminal.class);
    intent.putExtra(Constants.EXTRA_PROXY_PORT, script.getPort());
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    try {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
      ScriptProcess script = (ScriptProcess) mAdapter.getItem(info.position);
      script.getScriptName();
    } catch (ClassCastException e) {
      Log.e(e);
    }
    menu.add(Menu.NONE, 0, Menu.NONE, "Stop");
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      Log.e("Bad menuInfo", e);
      return false;
    }

    final ScriptProcess script = (ScriptProcess) mAdapter.getItem(info.position);
    if (script == null) {
      Log.v("No script selected.");
      return false;
    }

    Intent intent = new Intent(ScriptProcessMonitor.this, ScriptingLayerService.class);
    intent.setAction(Constants.ACTION_KILL_PROCESS);
    intent.putExtra(Constants.EXTRA_PROXY_PORT, script.getPort());
    startService(intent);

    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    // TODO(damonkohler): How could mProcessList ever be null?
    if (mProcessList != null && !mProcessList.isEmpty()) {
      menu.add(Menu.NONE, 0, Menu.NONE, "Stop All Scripts").setIcon(
          android.R.drawable.ic_menu_close_clear_cancel);
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    Intent intent = new Intent(this, ScriptingLayerService.class);
    intent.setAction(Constants.ACTION_KILL_ALL);
    startService(intent);
    return true;
  }

  private class ScriptListAdapter extends TimerTask {
    private int mmExpectedModCount = 0;
    private volatile List<ScriptProcess> mmList;

    @Override
    public void run() {
      if (mService == null) {
        mmList.clear();
        mTimer.cancel();
      } else {
        int freshModCount = mService.getModCount();
        if (freshModCount != mmExpectedModCount) {
          mmExpectedModCount = freshModCount;
          mmList = mService.getScriptProcessesList();
        }
      }
      runOnUiThread(new Runnable() {
        public void run() {
          mProcessList = mUpdater.getFreshProcessList();
          mAdapter.notifyDataSetChanged();
        }
      });
    }

    private List<ScriptProcess> getFreshProcessList() {
      return mmList;
    }
  }

  private class ScriptMonitorAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      if (mProcessList == null) {
        return 0;
      }
      return mProcessList.size();
    }

    @Override
    public Object getItem(int position) {
      return mProcessList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View itemView;
      if (convertView == null) {
        LayoutInflater inflater =
            (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(R.layout.script_monitor_list_item, parent, false);
      } else {
        itemView = convertView;
      }
      ScriptProcess process = mProcessList.get(position);
      ((TextView) itemView.findViewById(R.id.process_title)).setText(process.getScriptName());
      ((TextView) itemView.findViewById(R.id.process_age)).setText(process.getUptime());
      ((TextView) itemView.findViewById(R.id.process_details)).setText(process.getServerName()
          + ":" + process.getPort());
      ((TextView) itemView.findViewById(R.id.process_status)).setText(process.getState() + "("
          + process.getPID() + ")");
      return itemView;
    }
  }
}
