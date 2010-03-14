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
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.ase.AseLog;
import com.google.ase.R;

public class LogcatViewer extends ListActivity {
  private List<String> mLogcatMessages;
  private LogcatViewerAdapter mAdapter;
  private Handler mHandler;

  private class LogcatWatcher implements Runnable {
    @Override
    public void run() {
      try {
        Process logcat = Runtime.getRuntime().exec("logcat");
        InputStreamReader isr = new InputStreamReader(logcat.getInputStream());
        BufferedReader br = new BufferedReader(isr);
        String line;
        while ((line = br.readLine()) != null) {
          mLogcatMessages.add(line);
          mHandler.post(new Runnable() {
            @Override
            public void run() {
              mAdapter.notifyDataSetInvalidated();
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
    mAdapter = new LogcatViewerAdapter();
    mHandler = new Handler();
    setListAdapter(mAdapter);
    Thread logcatWatcher = new Thread(new LogcatWatcher());
    logcatWatcher.setPriority(Thread.NORM_PRIORITY - 1);
    logcatWatcher.start();
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