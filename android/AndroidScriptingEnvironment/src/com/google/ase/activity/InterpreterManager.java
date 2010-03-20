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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.ase.ActivityFlinger;
import com.google.ase.AseAnalytics;
import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.dialog.Help;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterConfiguration;

public class InterpreterManager extends ListActivity {

  private InterpreterManagerAdapter mAdapter;
  private List<Interpreter> mInterpreterList;

  private static enum RequestCode {
    INSTALL_INTERPRETER, UNINSTALL_INTERPRETER
  }

  private HashMap<Integer, Interpreter> mInstallerMenuIds;

  private static enum MenuId {
    HELP, ADD, DELETE, NETWORK, PREFERENCES;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, R.layout.interpreter_manager);
    mInterpreterList = InterpreterConfiguration.getInstalledInterpreters();
    mAdapter = new InterpreterManagerAdapter();
    mAdapter.registerDataSetObserver(new InterpreterListObserver());
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    AseAnalytics.trackActivity(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    mAdapter.notifyDataSetInvalidated();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    buildMenuIdMaps();
    buildInstallLanguagesMenu(menu);
    menu.add(Menu.NONE, MenuId.NETWORK.getId(), Menu.NONE, "Start Server").setIcon(
        android.R.drawable.ic_menu_share);
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help").setIcon(
        android.R.drawable.ic_menu_help);
    return super.onPrepareOptionsMenu(menu);
  }

  private void buildMenuIdMaps() {
    mInstallerMenuIds = new HashMap<Integer, Interpreter>();
    int i = MenuId.values().length + Menu.FIRST;
    List<Interpreter> notInstalled = InterpreterConfiguration.getNotInstalledInterpreters();
    for (Interpreter interpreter : notInstalled) {
      mInstallerMenuIds.put(i, interpreter);
      ++i;
    }
  }

  private void buildInstallLanguagesMenu(Menu menu) {
    if (InterpreterConfiguration.getNotInstalledInterpreters().size() > 0) {
      SubMenu installMenu =
          menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "Add").setIcon(
              android.R.drawable.ic_menu_add);
      List<Entry<Integer, Interpreter>> interpreters = new ArrayList<Entry<Integer, Interpreter>>();
      interpreters.addAll(mInstallerMenuIds.entrySet());
      Collections.sort(interpreters, new Comparator<Entry<Integer, Interpreter>>() {
        @Override
        public int compare(Entry<Integer, Interpreter> arg0, Entry<Integer, Interpreter> arg1) {
          return arg0.getValue().getNiceName().compareTo(arg1.getValue().getNiceName());
        }
      });
      for (Entry<Integer, Interpreter> entry : interpreters) {
        installMenu.add(Menu.NONE, entry.getKey(), Menu.NONE, entry.getValue().getNiceName());
      }
    }
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
    } else if (mInstallerMenuIds.containsKey(itemId)) {
      // Install selected interpreter.
      Interpreter interpreter = mInstallerMenuIds.get(itemId);
      installInterpreter(interpreter);
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

  private void installInterpreter(Interpreter interpreter) {
    Intent intent = new Intent(this, InterpreterInstaller.class);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, interpreter.getName());
    startActivityForResult(intent, RequestCode.INSTALL_INTERPRETER.ordinal());
  }

  private void launchTerminal(Interpreter interpreter) {
    Intent intent = new Intent(this, AseService.class);
    intent.setAction(Constants.ACTION_LAUNCH_TERMINAL);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, interpreter.getName());
    startService(intent);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    Interpreter interpreter = (Interpreter) list.getItemAtPosition(position);
    launchTerminal(interpreter);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MenuId.DELETE.getId(), Menu.NONE, "Uninstall");
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      AseLog.e("Bad MenuInfo", e);
      return false;
    }

    Interpreter interpreter = (Interpreter) mAdapter.getItem(info.position);
    if (interpreter == null) {
      AseLog.v(this, "No interpreter selected.");
      return false;
    }

    if (!interpreter.isUninstallable()) {
      AseLog.v(this, "Cannot uninstall " + interpreter.getNiceName());
      return true;
    }

    int itemId = item.getItemId();
    if (itemId == MenuId.DELETE.getId()) {
      Intent intent = new Intent(this, InterpreterUninstaller.class);
      intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, interpreter.getName());
      startActivityForResult(intent, RequestCode.UNINSTALL_INTERPRETER.ordinal());
    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    RequestCode request = RequestCode.values()[requestCode];
    if (resultCode == RESULT_OK) {
      switch (request) {
        case INSTALL_INTERPRETER:
          break;
        case UNINSTALL_INTERPRETER:
          AseLog.v(this, "Uninstallation successful.");
          break;
        default:
          break;
      }
    } else {
      switch (request) {
        case INSTALL_INTERPRETER:
          break;
        case UNINSTALL_INTERPRETER:
          AseLog.v(this, "Uninstallation failed.");
          break;
        default:
          break;
      }
    }
    mAdapter.notifyDataSetInvalidated();
  }

  private class InterpreterListObserver extends DataSetObserver {
    @Override
    public void onInvalidated() {
      mInterpreterList = InterpreterConfiguration.getInstalledInterpreters();
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
