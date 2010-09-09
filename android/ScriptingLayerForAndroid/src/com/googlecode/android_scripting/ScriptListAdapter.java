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

package com.googlecode.android_scripting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public abstract class ScriptListAdapter extends BaseAdapter {

  protected final Context mContext;
  protected final LayoutInflater mInflater;

  public ScriptListAdapter(Context context) {
    mContext = context;
    mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public int getCount() {
    return getScriptList().size();
  }

  @Override
  public Object getItem(int position) {
    return getScriptList().get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    LinearLayout container;
    File script = getScriptList().get(position);

    if (convertView == null) {
      container = (LinearLayout) mInflater.inflate(R.layout.list_item, null);
    } else {
      container = (LinearLayout) convertView;
    }

    ImageView img = (ImageView) container.findViewById(R.id.list_item_icon);
    int imgId = 0;
    if (script.isDirectory()) {
      imgId = R.drawable.folder;
      img.setBackgroundDrawable(null);
      img.setPadding(0, 0, 0, 0);
    } else {
      img.setBackgroundResource(R.drawable.file_bg);
      img.setPadding(4, 4, 8, 8);
      imgId = FeaturedInterpreters.getInterpreterIcon(mContext, script.getName());
      if (imgId == 0) {
        imgId = R.drawable.sl4a_logo_32;
      }
    }
    img.setImageResource(imgId);

    TextView text = (TextView) container.findViewById(R.id.list_item_title);
    text.setText(getScriptList().get(position).getName());
    return container;
  }

  protected abstract List<File> getScriptList();
}
