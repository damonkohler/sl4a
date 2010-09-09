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

import android.app.ListActivity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
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

import com.googlecode.android_scripting.ActivityFlinger;
import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.dialog.DurationPickerDialog;
import com.googlecode.android_scripting.dialog.Help;
import com.googlecode.android_scripting.dialog.DurationPickerDialog.DurationPickedListener;
import com.googlecode.android_scripting.event.EventTrigger;
import com.googlecode.android_scripting.event.RingerModeEventListener;
import com.googlecode.android_scripting.trigger.AlarmTriggerManager;
import com.googlecode.android_scripting.trigger.Trigger;
import com.googlecode.android_scripting.trigger.TriggerRepository;

import java.util.List;

public class TriggerManager extends ListActivity {
  private TriggerRepository mTriggerRepository;
  private AlarmTriggerManager mAlarmTriggerManager;
  private TriggerAdapter mAdapter;
  private List<Trigger> mTriggerList;

  private static enum ContextMenuId {
    REMOVE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum MenuId {
    SCHEDULE_REPEATING, SCHEDULE_INEXACT_REPEATING, RINGER_MODE_CONDITION, PREFERENCES, HELP;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Triggers", R.layout.trigger_manager);
    mTriggerRepository = ((BaseApplication) getApplication()).getTriggerRepository();
    mAlarmTriggerManager = new AlarmTriggerManager(this, mTriggerRepository);
    mTriggerList = mTriggerRepository.getAllTriggers();
    mAdapter = new TriggerAdapter();
    mAdapter.registerDataSetObserver(new TriggerListObserver());
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    Analytics.trackActivity(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    SubMenu addRepeating = menu.addSubMenu("Add Repeating");
    addRepeating.setIcon(android.R.drawable.ic_menu_add);
    addRepeating.add(Menu.NONE, MenuId.SCHEDULE_REPEATING.getId(), Menu.NONE, "Repeating");
    addRepeating.add(Menu.NONE, MenuId.SCHEDULE_INEXACT_REPEATING.getId(), Menu.NONE,
        "Power Efficient Repeating");
    SubMenu addCondition = menu.addSubMenu("Add Condition");
    addCondition.setIcon(android.R.drawable.ic_menu_add);
    addCondition.add(Menu.NONE, MenuId.RINGER_MODE_CONDITION.getId(), Menu.NONE, "Ringer Mode");
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help").setIcon(
        android.R.drawable.ic_menu_help);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.HELP.getId()) {
      Help.show(this);
    } else if (itemId == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (itemId != Menu.NONE) {
      Intent intent = new Intent(this, ScriptPicker.class);
      intent.setAction(Intent.ACTION_PICK);
      startActivityForResult(intent, itemId);
    }
    return true;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, ContextMenuId.REMOVE.getId(), Menu.NONE, "Remove");
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

    Trigger trigger = mAdapter.getItem(info.position);
    if (trigger == null) {
      Log.v("No trigger selected.");
      return false;
    }

    if (item.getItemId() == ContextMenuId.REMOVE.getId()) {
      mAlarmTriggerManager.cancelById(trigger.getId());
    }

    mAdapter.notifyDataSetInvalidated();
    return true;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    mAdapter.notifyDataSetInvalidated();
  }

  private class TriggerListObserver extends DataSetObserver {
    @Override
    public void onInvalidated() {
      mTriggerList = mTriggerRepository.getAllTriggers();
    }
  }

  private class TriggerAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return mTriggerList.size();
    }

    @Override
    public Trigger getItem(int position) {
      return mTriggerList.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      return mTriggerList.get(position).getView(TriggerManager.this);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == RESULT_OK) {
      final String script = data.getStringExtra(Constants.EXTRA_SCRIPT);
      if (requestCode == MenuId.SCHEDULE_REPEATING.getId()) {
        DurationPickerDialog.getDurationFromDialog(this, "Repeat every",
            new DurationPickedListener() {
              @Override
              public void onSet(double duration) {
                mAlarmTriggerManager.scheduleRepeating(duration, script, true);
                mAdapter.notifyDataSetInvalidated();
              }

              @Override
              public void onCancel() {
              }
            });
      } else if (requestCode == MenuId.SCHEDULE_INEXACT_REPEATING.getId()) {
        DurationPickerDialog.getDurationFromDialog(this, "Repeat every",
            new DurationPickedListener() {
              @Override
              public void onSet(double duration) {
                mAlarmTriggerManager.scheduleInexactRepeating(duration, script, true);
                mAdapter.notifyDataSetInvalidated();
              }

              @Override
              public void onCancel() {
              }
            });
      } else if (requestCode == MenuId.RINGER_MODE_CONDITION.getId()) {
        mTriggerRepository.addTrigger(new EventTrigger(script,
            new RingerModeEventListener.Factory()));
        mAdapter.notifyDataSetInvalidated();
      }
    }
  }
}
