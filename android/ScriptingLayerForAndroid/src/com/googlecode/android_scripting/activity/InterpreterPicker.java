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
import android.os.Parcelable;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration.ConfigurationObserver;

import java.util.List;

/**
 * Presents available scripts and returns the selected one.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class InterpreterPicker extends ListActivity {

  private List<Interpreter> mInterpreterList;
  private InterpreterPickerAdapter mAdapter;
  private InterpreterConfiguration mConfiguration;
  private ScriptListObserver mObserver;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Interpreters", R.layout.script_manager);
    mObserver = new ScriptListObserver();
    mConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
    mInterpreterList = mConfiguration.getInstalledInterpreters();
    mConfiguration.registerObserver(mObserver);
    mAdapter = new InterpreterPickerAdapter();
    mAdapter.registerDataSetObserver(mObserver);
    setListAdapter(mAdapter);
    Analytics.trackActivity(this);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final Interpreter interpreter = (Interpreter) list.getItemAtPosition(position);
    if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
      int icon =
          FeaturedInterpreters.getInterpreterIcon(InterpreterPicker.this, interpreter
              .getExtension());
      if (icon == 0) {
        icon = R.drawable.sl4a_logo_48;
      }
      Parcelable iconResource =
          Intent.ShortcutIconResource.fromContext(InterpreterPicker.this, icon);
      Intent intent = IntentBuilders.buildInterpreterShortcutIntent(interpreter, iconResource);
      setResult(RESULT_OK, intent);
    }
    finish();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mConfiguration.unregisterObserver(mObserver);
  }

  private class ScriptListObserver extends DataSetObserver implements ConfigurationObserver {
    @Override
    public void onInvalidated() {
      mInterpreterList = mConfiguration.getInstalledInterpreters();
    }

    @Override
    public void onConfigurationChanged() {
      mAdapter.notifyDataSetInvalidated();
    }
  }

  private class InterpreterPickerAdapter extends BaseAdapter {

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
      TextView view = new TextView(InterpreterPicker.this);
      view.setPadding(2, 2, 2, 2);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
      view.setText(mInterpreterList.get(position).getNiceName());
      return view;
    }
  }
}
