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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.ase.AseLog;
import com.google.ase.R;
import com.google.ase.dialog.Help;

public class LogcatViewer extends ListActivity {

  private Handler mHandler;
  private List<String> mLogcatMessages;
  private int mOldLastPosition;
  private LogcatViewerAdapter mAdapter;
  private Process mLogcatProcess;

  private static enum MenuId {
    HELP, PREFERENCES, JUMP_TO_BOTTOM;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private class LogcatWatcher implements Runnable {

    @Override
    public void run() {
      try {
        mLogcatProcess = Runtime.getRuntime().exec("logcat");
        InputStreamReader isr = new InputStreamReader(mLogcatProcess.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          mLogcatMessages.add(line);
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              mAdapter.notifyDataSetInvalidated();
              // This logic performs what transcriptMode="normal" should do. Since that doesn't seem
              // to work, we do it this way.
              int lastVisiblePosition = getListView().getLastVisiblePosition();
              int lastPosition = mLogcatMessages.size() - 1;
              if (lastVisiblePosition == mOldLastPosition || lastVisiblePosition == -1) {
                getListView().setSelection(lastPosition);
              }
              mOldLastPosition = lastPosition;
            }
          });
        }
      } catch (IOException e) {
        AseLog.e("Logcat execution failed.");
      }
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.logcat_viewer);
    mLogcatMessages = new LinkedList<String>();
    mOldLastPosition = 0;
    mAdapter = new LogcatViewerAdapter();
    mHandler = new Handler();
    setListAdapter(mAdapter);
    new ActivityFlinger.Builder()
        .addLeftActivity(this, ScriptManager.class, "Script Manager")
        .addRightActivity(this, InterpreterManager.class, "Interpreter Manager")
        .attachToView(getListView());
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(Menu.NONE, MenuId.JUMP_TO_BOTTOM.getId(), Menu.NONE, "Jump to Bottom").setIcon(
        android.R.drawable.ic_menu_revert);
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
    } else if (itemId == MenuId.JUMP_TO_BOTTOM.getId()) {
      getListView().setSelection(mLogcatMessages.size() - 1);
    } else if (itemId == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, AsePreferences.class));
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStart() {
    mLogcatMessages.clear();
    Thread logcatWatcher = new Thread(new LogcatWatcher());
    logcatWatcher.setPriority(Thread.NORM_PRIORITY - 1);
    logcatWatcher.start();
    super.onStart();
  }

  @Override
  protected void onPause() {
    if (mLogcatProcess != null) {
      AseLog.v("Logcat killed.");
      mLogcatProcess.destroy();
    }
    super.onPause();
  }

  private class LogcatViewerAdapter extends BaseAdapter {

    @Override
    public int getCount() {
      return mLogcatMessages.size();
    }

    @Override
    public Object getItem(int position) {
      return mLogcatMessages.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      TextView view = new TextView(LogcatViewer.this);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
      view.setText(mLogcatMessages.get(position));
      return view;
    }
  }
}