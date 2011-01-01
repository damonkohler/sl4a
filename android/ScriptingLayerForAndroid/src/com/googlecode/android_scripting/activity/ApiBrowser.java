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

package com.googlecode.android_scripting.activity;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.dialog.Help;
import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.language.SupportedLanguages;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.ParameterDescriptor;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcMinSdk;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiBrowser extends ListActivity {

  private boolean searchResultMode = false;

  private static enum RequestCode {
    RPC_PROMPT
  }

  private static enum MenuId {
    EXPAND_ALL, COLLAPSE_ALL, SEARCH;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum ContextMenuId {
    INSERT_TEXT, PROMPT_PARAMETERS, HELP;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private List<MethodDescriptor> mMethodDescriptors;
  private Set<Integer> mExpandedPositions;
  private ApiBrowserAdapter mAdapter;
  private boolean mIsLanguageSupported;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "API Browser", R.layout.api_browser);
    getListView().setFastScrollEnabled(true);
    mExpandedPositions = new HashSet<Integer>();
    updateAndFilterMethodDescriptors(null);
    String scriptName = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_PATH);
    mIsLanguageSupported = SupportedLanguages.checkLanguageSupported(scriptName);
    mAdapter = new ApiBrowserAdapter();
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    Analytics.trackActivity(this);
    setResult(RESULT_CANCELED);
  }

  private void updateAndFilterMethodDescriptors(final String query) {
    mMethodDescriptors =
        Lists.newArrayList(Collections2.filter(FacadeConfiguration.collectMethodDescriptors(),
            new Predicate<MethodDescriptor>() {
              @Override
              public boolean apply(MethodDescriptor descriptor) {
                Method method = descriptor.getMethod();
                if (method.isAnnotationPresent(RpcDeprecated.class)) {
                  return false;
                } else if (method.isAnnotationPresent(RpcMinSdk.class)) {
                  int requiredSdkLevel = method.getAnnotation(RpcMinSdk.class).value();
                  if (FacadeConfiguration.getSdkLevel() < requiredSdkLevel) {
                    return false;
                  }
                }
                if (query == null) {
                  return true;
                }
                return descriptor.getName().toLowerCase().contains(query.toLowerCase());
              }
            }));
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      searchResultMode = true;
      final String query = intent.getStringExtra(SearchManager.QUERY);
      ((TextView) findViewById(R.id.left_text)).setText(query);
      updateAndFilterMethodDescriptors(query);
      if (mMethodDescriptors.size() == 1) {
        mExpandedPositions.add(0);
      } else {
        mExpandedPositions.clear();
      }
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && searchResultMode) {
      searchResultMode = false;
      mExpandedPositions.clear();
      ((TextView) findViewById(R.id.left_text)).setText("API Browser");
      updateAndFilterMethodDescriptors("");
      mAdapter.notifyDataSetChanged();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.clear();
    menu.add(Menu.NONE, MenuId.EXPAND_ALL.getId(), Menu.NONE, "Expand All").setIcon(
        android.R.drawable.ic_menu_add);
    menu.add(Menu.NONE, MenuId.COLLAPSE_ALL.getId(), Menu.NONE, "Collapse All").setIcon(
        android.R.drawable.ic_menu_close_clear_cancel);
    menu.add(Menu.NONE, MenuId.SEARCH.getId(), Menu.NONE, "Search").setIcon(
        R.drawable.ic_menu_search);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    int itemId = item.getItemId();
    if (itemId == MenuId.EXPAND_ALL.getId()) {
      for (int i = 0; i < mMethodDescriptors.size(); i++) {
        mExpandedPositions.add(i);
      }
    } else if (itemId == MenuId.COLLAPSE_ALL.getId()) {
      mExpandedPositions.clear();
    } else if (itemId == MenuId.SEARCH.getId()) {
      onSearchRequested();
    }

    mAdapter.notifyDataSetInvalidated();
    return true;
  }

  @Override
  protected void onListItemClick(ListView l, View v, int position, long id) {
    if (mExpandedPositions.contains(position)) {
      mExpandedPositions.remove(position);
    } else {
      mExpandedPositions.add(position);
    }
    mAdapter.notifyDataSetInvalidated();
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    if (!mIsLanguageSupported) {
      return;
    }
    menu.add(Menu.NONE, ContextMenuId.INSERT_TEXT.getId(), Menu.NONE, "Insert");
    menu.add(Menu.NONE, ContextMenuId.PROMPT_PARAMETERS.getId(), Menu.NONE, "Prompt");
    if (Help.checkApiHelp(this)) {
      menu.add(Menu.NONE, ContextMenuId.HELP.getId(), Menu.NONE, "Help");
    }
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

    MethodDescriptor rpc = (MethodDescriptor) getListAdapter().getItem(info.position);
    if (rpc == null) {
      Log.v("No RPC selected.");
      return false;
    }

    if (item.getItemId() == ContextMenuId.INSERT_TEXT.getId()) {
      // There's no activity to track calls to insert (like there is for prompt) so we track it
      // here instead.
      Analytics.track("ApiInsert");
      insertText(rpc, new String[0]);
    } else if (item.getItemId() == ContextMenuId.PROMPT_PARAMETERS.getId()) {
      Intent intent = new Intent(this, ApiPrompt.class);
      intent.putExtra(Constants.EXTRA_API_PROMPT_RPC_NAME, rpc.getName());
      ParameterDescriptor[] parameters = rpc.getParameterValues(new String[0]);
      String[] values = new String[parameters.length];
      int index = 0;
      for (ParameterDescriptor parameter : parameters) {
        values[index++] = parameter.getValue();
      }
      intent.putExtra(Constants.EXTRA_API_PROMPT_VALUES, values);
      startActivityForResult(intent, RequestCode.RPC_PROMPT.ordinal());
    } else if (item.getItemId() == ContextMenuId.HELP.getId()) {
      String help = rpc.getDeclaringClass().getSimpleName() + ".html#" + rpc.getName();
      Help.showApiHelp(this, help);

    }
    return true;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    RequestCode request = RequestCode.values()[requestCode];
    if (resultCode == RESULT_OK) {
      switch (request) {
      case RPC_PROMPT:
        MethodDescriptor rpc =
            FacadeConfiguration.getMethodDescriptor(data
                .getStringExtra(Constants.EXTRA_API_PROMPT_RPC_NAME));
        String[] values = data.getStringArrayExtra(Constants.EXTRA_API_PROMPT_VALUES);
        insertText(rpc, values);
        break;
      default:
        break;
      }
    } else {
      switch (request) {
      case RPC_PROMPT:
        break;
      default:
        break;
      }
    }
  }

  private void insertText(MethodDescriptor rpc, String[] values) {
    String scriptText = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_TEXT);
    InterpreterConfiguration config =
        ((BaseApplication) getApplication()).getInterpreterConfiguration();

    Interpreter interpreter =
        config.getInterpreterByName(getIntent().getStringExtra(Constants.EXTRA_INTERPRETER_NAME));
    String rpcHelpText = interpreter.getRpcText(scriptText, rpc, values);

    Intent intent = new Intent();
    intent.putExtra(Constants.EXTRA_RPC_HELP_TEXT, rpcHelpText);
    setResult(RESULT_OK, intent);
    finish();
  }

  private class ApiBrowserAdapter extends BaseAdapter implements SectionIndexer {

    private final AlphabetIndexer mIndexer;
    private final MatrixCursor mCursor;

    public ApiBrowserAdapter() {
      mCursor = new MatrixCursor(new String[] { "NAME" });
      for (MethodDescriptor info : mMethodDescriptors) {
        mCursor.addRow(new String[] { info.getName() });
      }
      mIndexer = new AlphabetIndexer(mCursor, 0, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Override
    public int getCount() {
      return mMethodDescriptors.size();
    }

    @Override
    public Object getItem(int position) {
      return mMethodDescriptors.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      TextView view;
      if (convertView == null) {
        view = new TextView(ApiBrowser.this);
      } else {
        view = (TextView) convertView;
      }
      view.setPadding(4, 4, 4, 4);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
      if (mExpandedPositions.contains(position)) {
        view.setText(mMethodDescriptors.get(position).getHelp());
      } else {
        view.setText(mMethodDescriptors.get(position).getName());
      }
      return view;
    }

    @Override
    public int getPositionForSection(int section) {
      return mIndexer.getPositionForSection(section);
    }

    @Override
    public int getSectionForPosition(int position) {
      return mIndexer.getSectionForPosition(position);
    }

    @Override
    public Object[] getSections() {
      return mIndexer.getSections();
    }
  }
}
