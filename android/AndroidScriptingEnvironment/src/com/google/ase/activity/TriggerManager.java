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

import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
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

import com.google.ase.ActivityFlinger;
import com.google.ase.AseAnalytics;
import com.google.ase.AseLog;
import com.google.ase.R;
import com.google.ase.dialog.DurationPickerDialog;
import com.google.ase.dialog.Help;
import com.google.ase.dialog.DurationPickerDialog.DurationPickedListener;
import com.google.ase.trigger.AlarmTrigger;
import com.google.ase.trigger.AlarmTriggerManager;
import com.google.ase.trigger.Trigger;
import com.google.ase.trigger.TriggerRepository;

public class TriggerManager extends ListActivity {
  private TriggerRepository mTriggerRepository;
  private AlarmTriggerManager mAlarmTriggerManager;
  private TriggerAdapter mAdapter;

  private static enum ContextMenuId {
    REMOVE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum MenuId {
    SCHEDULE_REPEATING, HELP, SCHEDULE_INEXACT_REPEATING;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  public TriggerManager() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.trigger_manager);
    mTriggerRepository = new TriggerRepository(this);
    mAdapter = new TriggerAdapter(this, mTriggerRepository.getAllTriggers());
    setListAdapter(mAdapter);
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    AseAnalytics.trackActivity(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    SubMenu subMenu = menu.addSubMenu("Add");
    subMenu.setIcon(android.R.drawable.ic_menu_add);
    subMenu.add(Menu.NONE, MenuId.SCHEDULE_REPEATING.getId(), Menu.NONE, "Repeating");
    subMenu.add(Menu.NONE, MenuId.SCHEDULE_INEXACT_REPEATING.getId(), Menu.NONE,
        "Power Efficient Repeating");
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.HELP.getId()) {
      Help.show(this);
    } else if (itemId == MenuId.SCHEDULE_REPEATING.getId()) {
      Intent intent = new Intent(this, ScriptManager.class);
      intent.setAction(Intent.ACTION_PICK);
      startActivityForResult(intent, 0);
    } else if (itemId == MenuId.SCHEDULE_INEXACT_REPEATING.getId()) {
      Intent intent = new Intent(this, ScriptManager.class);
      intent.setAction(Intent.ACTION_PICK);
      startActivityForResult(intent, 1);
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
      AseLog.e("Bad menuInfo", e);
      return false;
    }

    Trigger trigger = (Trigger) mAdapter.getItem(info.position);
    if (trigger == null) {
      AseLog.v("No trigger selected.");
      return false;
    }

    if (item.getItemId() == ContextMenuId.REMOVE.getId()) {
      if (trigger instanceof AlarmTrigger) {
        mAlarmTriggerManager.cancelRepeating(((AlarmTrigger) trigger).getScriptName());
      } else {
        throw new RuntimeException("Unknown trigger type.");
      }
    }
    return true;
  }

  private static class TriggerAdapter extends BaseAdapter {
    private final List<Trigger> triggers;
    private final Context context;

    public TriggerAdapter(Context context, List<Trigger> triggers) {
      this.triggers = triggers;
      this.context = context;
    }

    @Override
    public int getCount() {
      return triggers.size();
    }

    @Override
    public Object getItem(int position) {
      return triggers.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      return triggers.get(position).getView(context);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    DurationPickerDialog.getDurationFromDialog(this, "Repeat every", new DurationPickedListener() {
      @Override
      public void onSet(double duration) {

      }

      @Override
      public void onCancel() {

      }
    });
  }

}
