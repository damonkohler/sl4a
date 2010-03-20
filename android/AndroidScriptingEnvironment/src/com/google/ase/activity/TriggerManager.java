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
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.ase.ActivityFlinger;
import com.google.ase.AseAnalytics;
import com.google.ase.R;
import com.google.ase.trigger.AseTriggerRepository;
import com.google.ase.trigger.Trigger;

public class TriggerManager extends ListActivity {
  private AseTriggerRepository mTriggerRepository;

  public TriggerManager() {
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mTriggerRepository = new AseTriggerRepository(this);
    setContentView(R.layout.trigger_manager);
    getListView().setFastScrollEnabled(true);
    AseAnalytics.trackActivity(this);
    setResult(RESULT_CANCELED);
    setListAdapter(new TriggerAdapter(this, mTriggerRepository.getAllTriggers()));
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    AseAnalytics.trackActivity(this);
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

}
