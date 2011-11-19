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
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptListAdapter;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;

import java.io.File;
import java.util.List;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

/**
 * Presents available scripts and returns the selected one.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptPicker extends ListActivity {

  private List<File> mScripts;
  private ScriptPickerAdapter mAdapter;
  private InterpreterConfiguration mConfiguration;
  private File mCurrentDir;
  private final File mBaseDir = new File(InterpreterConstants.SCRIPTS_ROOT);

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Scripts", R.layout.script_manager);
    mCurrentDir = mBaseDir;
    mConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
    mScripts = ScriptStorageAdapter.listExecutableScripts(null, mConfiguration);
    mAdapter = new ScriptPickerAdapter(this);
    mAdapter.registerDataSetObserver(new ScriptListObserver());
    setListAdapter(mAdapter);
    Analytics.trackActivity(this);
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final File script = (File) list.getItemAtPosition(position);

    if (script.isDirectory()) {
      mCurrentDir = script;
      mAdapter.notifyDataSetInvalidated();
      return;
    }

    QuickAction actionMenu = new QuickAction(view);
    ActionItem terminal = new ActionItem();
    terminal.setIcon(getResources().getDrawable(R.drawable.terminal));
    ActionItem background = new ActionItem();
    background.setIcon(getResources().getDrawable(R.drawable.background));

    actionMenu.addActionItems(terminal, background);

    if (Intent.ACTION_PICK.equals(getIntent().getAction())) {
      terminal.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = IntentBuilders.buildStartInTerminalIntent(script);
          setResult(RESULT_OK, intent);
          finish();
        }
      });

      background.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = IntentBuilders.buildStartInBackgroundIntent(script);
          setResult(RESULT_OK, intent);
          finish();
        }
      });
    } else if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
      int icon = FeaturedInterpreters.getInterpreterIcon(ScriptPicker.this, script.getName());
      if (icon == 0) {
        icon = R.drawable.sl4a_logo_48;
      }

      final Parcelable iconResource =
          Intent.ShortcutIconResource.fromContext(ScriptPicker.this, icon);

      terminal.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = IntentBuilders.buildTerminalShortcutIntent(script, iconResource);
          setResult(RESULT_OK, intent);
          finish();
        }
      });

      background.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = IntentBuilders.buildBackgroundShortcutIntent(script, iconResource);
          setResult(RESULT_OK, intent);
          finish();
        }
      });
    } else if (com.twofortyfouram.locale.platform.Intent.ACTION_EDIT_SETTING.equals(getIntent()
        .getAction())) {
      final Intent intent = new Intent();
      final Bundle storeAndForwardExtras = new Bundle();
      storeAndForwardExtras.putString(Constants.EXTRA_SCRIPT_PATH, script.getPath());

      intent.putExtra(com.twofortyfouram.locale.platform.Intent.EXTRA_STRING_BLURB,
          script.getName());

      terminal.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          storeAndForwardExtras.putBoolean(Constants.EXTRA_LAUNCH_IN_BACKGROUND, false);
          intent.putExtra(com.twofortyfouram.locale.platform.Intent.EXTRA_BUNDLE,
              storeAndForwardExtras);
          setResult(RESULT_OK, intent);
          finish();
        }
      });

      background.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          storeAndForwardExtras.putBoolean(Constants.EXTRA_LAUNCH_IN_BACKGROUND, true);
          intent.putExtra(com.twofortyfouram.locale.platform.Intent.EXTRA_BUNDLE,
              storeAndForwardExtras);
          setResult(RESULT_OK, intent);
          finish();
        }
      });
    }

    actionMenu.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
    actionMenu.show();
  }

  private class ScriptListObserver extends DataSetObserver {
    @SuppressWarnings("serial")
    @Override
    public void onInvalidated() {
      mScripts = ScriptStorageAdapter.listExecutableScripts(mCurrentDir, mConfiguration);
      // TODO(damonkohler): Extending the File class here seems odd.
      if (!mCurrentDir.equals(mBaseDir)) {
        mScripts.add(0, new File(mCurrentDir.getParent()) {
          @Override
          public boolean isDirectory() {
            return true;
          }

          @Override
          public String getName() {
            return "..";
          }
        });
      }
    }
  }

  private class ScriptPickerAdapter extends ScriptListAdapter {

    public ScriptPickerAdapter(Context context) {
      super(context);
    }

    @Override
    protected List<File> getScriptList() {
      return mScripts;
    }

  }
}
