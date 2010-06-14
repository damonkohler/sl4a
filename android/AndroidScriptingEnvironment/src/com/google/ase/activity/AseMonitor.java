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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ase.Analytics;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.ScriptProcess;


import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class AseMonitor extends ListActivity {
  
  private final static int UPDATE_INTERVAL = 1;
  
  private List<ScriptProcess> mProcessList;
  private ScriptMonitorAdapter mAdapter;
  private AseService mService;
  
  private final Handler mHandler = new Handler();
  private final Timer mTimer = new Timer();
  private final ScriptListAdapter mUpdater = new ScriptListAdapter();
  
  
  private ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mService = ((AseService.LocalBinder) service).getService();
      mTimer.scheduleAtFixedRate(mUpdater, 0, UPDATE_INTERVAL * 1000);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService = null;
    }
  };

  private final Runnable mNotifier = new Runnable() {
    public void run() {
      mProcessList = mUpdater.getFreshProcessList();
      mAdapter.notifyDataSetChanged();
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bindService(new Intent(this, AseService.class), mConnection, 0);
    CustomizeWindow.requestCustomTitle(this, "Script Monitor", R.layout.script_monitor);
    mAdapter = new ScriptMonitorAdapter();
    setListAdapter(mAdapter);
    Analytics.trackActivity(this);
  }
  
  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final ScriptProcess script = (ScriptProcess) list.getItemAtPosition(position);

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setItems(new CharSequence[] { "Open in Terminal", "Stop Script" },
          new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              Intent intent = null;
              if (which == 0) {
                // TODO(Alexey): attach a terminal to a runnign script
              } else {
                intent = new Intent(AseMonitor.this, AseService.class);
                intent.setAction(Constants.ACTION_KILL_PROCESS);
                intent.putExtra(Constants.EXTRA_PROXY_PORT, script.getPort());
                startService(intent);
              }
              finish();
            }
          });
      builder.show();
  }

  @Override
  public void onDestroy(){
    super.onDestroy();
    mTimer.cancel();
    unbindService(mConnection);
  }
  

  private class ScriptListAdapter extends TimerTask{
    private int mmExpectedModCount = 0;
    private volatile List<ScriptProcess> mmList;
    @Override
    public void run() {
      if (mService == null) {
        return;
      }
      int freshModCount = mService.getModCount();
      if (freshModCount != mmExpectedModCount) {
        mmExpectedModCount = freshModCount;
        mmList = mService.getScriptProcessesList();
      }
      mHandler.post(mNotifier);
    }
    
    private List<ScriptProcess> getFreshProcessList(){
      return mmList;
    }
  }
  
  private class ScriptMonitorAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      if(mProcessList == null){
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
        LayoutInflater inflater = (LayoutInflater) AseMonitor.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        itemView = inflater.inflate(R.layout.script_monitor_list_item, parent, false);
      } else {
        itemView = convertView;
      }
      ScriptProcess process = mProcessList.get(position);
      ((TextView) itemView.findViewById(R.id.process_title)).setText(process.getScriptName());
      ((TextView) itemView.findViewById(R.id.process_age)).setText(process.getUptime());
      ((TextView) itemView.findViewById(R.id.process_details)).setText(process.getServerName()+":"+process.getPort());
      ((TextView) itemView.findViewById(R.id.process_status)).setText(process.getState()+"("+process.getPID()+")");
      return itemView;
    }
  }
}
