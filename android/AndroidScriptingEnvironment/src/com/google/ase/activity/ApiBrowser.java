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

import android.app.ListActivity;
import android.content.Intent;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.ContextMenu;
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

import com.google.ase.Analytics;
import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.R;
import com.google.ase.facade.FacadeConfiguration;
import com.google.ase.interpreter.InterpreterAgent;
import com.google.ase.interpreter.InterpreterConfiguration;
import com.google.ase.language.SupportedLanguages;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.ParameterDescriptor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ApiBrowser extends ListActivity {

  private static enum RequestCode {
    RPC_PROMPT
  }

  private static enum MenuId {
    EXPAND_ALL, COLLAPSE_ALL;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum ContextMenuId {
    INSERT_TEXT, PROMPT_PARAMETERS;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private List<MethodDescriptor> mRpcDescriptors;
  private Set<Integer> mExpandedPositions;
  private ApiBrowserAdapter mAdapter;
  private boolean mIsLanguageSupported;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.api_browser);
    getListView().setFastScrollEnabled(true);
    mExpandedPositions = new HashSet<Integer>();
    mRpcDescriptors = FacadeConfiguration.collectRpcDescriptors();
    String scriptName = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    mIsLanguageSupported = SupportedLanguages.checkLanguageSupported(scriptName);
    mAdapter = new ApiBrowserAdapter();
    setListAdapter(mAdapter);
    registerForContextMenu(getListView());
    Analytics.trackActivity(this);
    setResult(RESULT_CANCELED);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.clear();
    menu.add(Menu.NONE, MenuId.EXPAND_ALL.getId(), Menu.NONE, "Expand All").setIcon(
        android.R.drawable.ic_menu_add);
    menu.add(Menu.NONE, MenuId.COLLAPSE_ALL.getId(), Menu.NONE, "Collapse All").setIcon(
        android.R.drawable.ic_menu_close_clear_cancel);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    super.onOptionsItemSelected(item);
    int itemId = item.getItemId();
    if (itemId == MenuId.EXPAND_ALL.getId()) {
      for (int i = 0; i < mRpcDescriptors.size(); i++) {
        mExpandedPositions.add(i);
      }
    } else if (itemId == MenuId.COLLAPSE_ALL.getId()) {
      mExpandedPositions.clear();
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
    if(!mIsLanguageSupported){
      return;
    }
    menu.add(Menu.NONE, ContextMenuId.INSERT_TEXT.getId(), Menu.NONE, "Insert");
    menu.add(Menu.NONE, ContextMenuId.PROMPT_PARAMETERS.getId(), Menu.NONE, "Prompt");
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

    MethodDescriptor rpc = (MethodDescriptor) getListAdapter().getItem(info.position);
    if (rpc == null) {
      AseLog.v("No RPC selected.");
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
        ((AseApplication) getApplication()).getInterpreterConfiguration();

    InterpreterAgent interpreter =
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
      for (MethodDescriptor info : mRpcDescriptors) {
        mCursor.addRow(new String[] { info.getName() });
      }
      mIndexer = new AlphabetIndexer(mCursor, 0, " ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    @Override
    public int getCount() {
      return mRpcDescriptors.size();
    }

    @Override
    public Object getItem(int position) {
      return mRpcDescriptors.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
      TextView view = new TextView(ApiBrowser.this);
      view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
      if (mExpandedPositions.contains(position)) {
        view.setText(mRpcDescriptors.get(position).getHelp());
      } else {
        view.setText(mRpcDescriptors.get(position).getName());
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
