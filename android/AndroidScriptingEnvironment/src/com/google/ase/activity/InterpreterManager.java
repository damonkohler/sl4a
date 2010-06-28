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

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ase.ActivityFlinger;
import com.google.ase.Analytics;
import com.google.ase.AseApplication;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.dialog.Help;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.interpreter.InterpreterAgent;
import com.google.ase.interpreter.InterpreterConfiguration.ConfigurationObserver;

public class InterpreterManager extends ListActivity {

  private InterpreterManagerAdapter mAdapter;
  private InterpreterListObserver mObserver;
  private List<InterpreterAgent> mInterpreterList;
  private InterpreterConfiguration mConfiguration;

  private static enum MenuId {
    HELP, NETWORK, PREFERENCES;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Interpreters", R.layout.interpreter_manager);
    mConfiguration = ((AseApplication) getApplication()).getInterpreterConfiguration();
    mInterpreterList = new ArrayList<InterpreterAgent>();
    mAdapter = new InterpreterManagerAdapter();
    mObserver = new InterpreterListObserver();
    mAdapter.registerDataSetObserver(mObserver);
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    Analytics.trackActivity(this);
  }

  @Override
  public void onStart() {
    super.onStart();
    mConfiguration.registerObserver(mObserver);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAdapter.notifyDataSetInvalidated();
  }

  @Override
  public void onStop() {
    super.onStop();
    mConfiguration.unregisterObserver(mObserver);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(Menu.NONE, MenuId.NETWORK.getId(), Menu.NONE, "Start Server").setIcon(
        android.R.drawable.ic_menu_share);
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help").setIcon(
        android.R.drawable.ic_menu_help);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.HELP.getId()) {
      Help.show(this);
    } else if (itemId == MenuId.NETWORK.getId()) {
      AlertDialog.Builder dialog = new AlertDialog.Builder(this);
      dialog.setItems(new CharSequence[] { "Public", "Private" }, new OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          launchService(which == 0 /* usePublicIp */);
        }
      });
      dialog.show();
    } else if (itemId == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, AsePreferences.class));
    }
    return true;
  }

  private void launchService(boolean usePublicIp) {
    Intent intent = new Intent(this, AseService.class);
    intent.setAction(Constants.ACTION_LAUNCH_SERVER);
    intent.putExtra(Constants.EXTRA_USE_EXTERNAL_IP, usePublicIp);
    startService(intent);
  }

  private void launchTerminal(InterpreterAgent interpreter) {
    Intent intent = new Intent(this, AseService.class);
    intent.setAction(Constants.ACTION_LAUNCH_TERMINAL);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, interpreter.getName());
    startService(intent);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    InterpreterAgent interpreter =
        (InterpreterAgent) list.getItemAtPosition(position);
    launchTerminal(interpreter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mConfiguration.unregisterObserver(mObserver);
  }

  private class InterpreterListObserver extends DataSetObserver implements ConfigurationObserver {
    @Override
    public void onInvalidated() {
      mInterpreterList = mConfiguration.getInstalledInterpreters();
    }

    @Override
    public void onChanged() {
      mInterpreterList = mConfiguration.getInstalledInterpreters();
    }

    @Override
    public void onConfigurationChanged() {
      mAdapter.notifyDataSetChanged();
    }
  }

  private class InterpreterManagerAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return mInterpreterList.size();
    }

    @Override
    public Object getItem(int position) {
      return mInterpreterList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView view = new TextView(InterpreterManager.this);
      view.setPadding(2, 2, 2, 2);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
      view.setText(mInterpreterList.get(position).getNiceName());
      return view;
    }
  }
}
