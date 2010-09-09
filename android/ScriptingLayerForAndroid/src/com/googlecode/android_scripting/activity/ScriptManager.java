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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.googlecode.android_scripting.ActivityFlinger;
import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.IntentBuilders;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptListAdapter;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.dialog.Help;
import com.googlecode.android_scripting.dialog.UsageTrackingConfirmation;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration.ConfigurationObserver;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

/**
 * Manages creation, deletion, and execution of stored scripts.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptManager extends ListActivity {

  private final static String EMPTY = "";

  private List<File> mScripts;
  private ScriptManagerAdapter mAdapter;
  private SharedPreferences mPreferences;
  private HashMap<Integer, Interpreter> mAddMenuIds;
  private ScriptListObserver mObserver;
  private InterpreterConfiguration mConfiguration;
  private SearchManager mManager;
  private boolean mInSearchResultMode = false;
  private String mQuery = EMPTY;
  private File mCurrentDir;
  private final File mBaseDir = new File(InterpreterConstants.SCRIPTS_ROOT);
  private final Handler mHandler = new Handler();

  private static enum RequestCode {
    INSTALL_INTERPETER, QRCODE_ADD
  }

  private static enum MenuId {
    DELETE, EDIT, START_SERVICE, HELP, FOLDER_ADD, QRCODE_ADD, INTERPRETER_MANAGER, PREFERENCES,
    LOGCAT_VIEWER, TRIGGER_MANAGER, REFRESH, SEARCH;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CustomizeWindow.requestCustomTitle(this, "Scripts", R.layout.script_manager);
    if (!FileUtils.makeDirectory(mBaseDir)) {
      throw new RuntimeException("Failed to create scripts directory.");
    }
    mCurrentDir = mBaseDir;
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mAdapter = new ScriptManagerAdapter(this);
    mObserver = new ScriptListObserver();
    mAdapter.registerDataSetObserver(mObserver);
    mConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
    updateAndFilterScriptList(mQuery);
    setListAdapter(mAdapter);
    UsageTrackingConfirmation.show(this);
    ActivityFlinger.attachView(getListView(), this);
    ActivityFlinger.attachView(getWindow().getDecorView(), this);
    startService(IntentBuilders.buildTriggerServiceIntent());
    mManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
    handleIntent(getIntent());
    Analytics.trackActivity(this);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    handleIntent(intent);
  }

  @SuppressWarnings("serial")
  private void updateAndFilterScriptList(final String query) {
    List<File> scripts;
    if (mPreferences.getBoolean("show_all_files", false)) {
      scripts = ScriptStorageAdapter.listAllScripts(mCurrentDir);
    } else {
      scripts = ScriptStorageAdapter.listExecutableScripts(mCurrentDir, mConfiguration);
    }
    mScripts = Lists.newArrayList(Collections2.filter(scripts, new Predicate<File>() {
      @Override
      public boolean apply(File file) {
        return file.getName().toLowerCase().contains(query.toLowerCase());
      }
    }));

    synchronized (mQuery) {
      if (!mQuery.equals(query)) {
        if (query == null || query.equals(EMPTY)) {
          ((TextView) findViewById(R.id.left_text)).setText("Scripts");
        } else {
          ((TextView) findViewById(R.id.left_text)).setText(query);
        }
        mQuery = query;
      }
    }

    if (mScripts.size() == 0) {
      ((TextView) findViewById(android.R.id.empty)).setText("No matches found.");
    }

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

  private void handleIntent(Intent intent) {
    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
      mInSearchResultMode = true;
      String query = intent.getStringExtra(SearchManager.QUERY);
      updateAndFilterScriptList(query);
      mAdapter.notifyDataSetChanged();
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && mInSearchResultMode) {
      mInSearchResultMode = false;
      mAdapter.notifyDataSetInvalidated();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  @Override
  public void onStop() {
    super.onStop();
    mConfiguration.unregisterObserver(mObserver);
  }

  @Override
  public void onStart() {
    super.onStart();
    mConfiguration.registerObserver(mObserver);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (!mInSearchResultMode) {
      ((TextView) findViewById(android.R.id.empty)).setText(R.string.no_scripts_message);
    }
    updateAndFilterScriptList(mQuery);
    mAdapter.notifyDataSetChanged();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    menu.clear();
    buildMenuIdMaps();
    buildAddMenu(menu);
    buildSwitchActivityMenu(menu);
    menu.add(Menu.NONE, MenuId.SEARCH.getId(), Menu.NONE, "Search").setIcon(
        R.drawable.ic_menu_search);
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(Menu.NONE, MenuId.REFRESH.getId(), Menu.NONE, "Refresh").setIcon(
        R.drawable.ic_menu_refresh);
    menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help").setIcon(
        android.R.drawable.ic_menu_help);
    return true;
  }

  private void buildSwitchActivityMenu(Menu menu) {
    Menu subMenu =
        menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "View").setIcon(
            android.R.drawable.ic_menu_more);
    subMenu.add(Menu.NONE, MenuId.INTERPRETER_MANAGER.getId(), Menu.NONE, "Interpreters");
    subMenu.add(Menu.NONE, MenuId.TRIGGER_MANAGER.getId(), Menu.NONE, "Triggers");
    subMenu.add(Menu.NONE, MenuId.LOGCAT_VIEWER.getId(), Menu.NONE, "Logcat");
  }

  private void buildMenuIdMaps() {
    mAddMenuIds = new LinkedHashMap<Integer, Interpreter>();
    int i = MenuId.values().length + Menu.FIRST;
    List<Interpreter> installed = mConfiguration.getInstalledInterpreters();
    Collections.sort(installed, new Comparator<Interpreter>() {
      @Override
      public int compare(Interpreter interpreterA, Interpreter interpreterB) {
        return interpreterA.getNiceName().compareTo(interpreterB.getNiceName());
      }
    });
    for (Interpreter interpreter : installed) {
      mAddMenuIds.put(i, interpreter);
      ++i;
    }
  }

  private void buildAddMenu(Menu menu) {
    Menu addMenu =
        menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "Add").setIcon(
            android.R.drawable.ic_menu_add);
    addMenu.add(Menu.NONE, MenuId.FOLDER_ADD.getId(), Menu.NONE, "Folder");
    for (Entry<Integer, Interpreter> entry : mAddMenuIds.entrySet()) {
      addMenu.add(Menu.NONE, entry.getKey(), Menu.NONE, entry.getValue().getNiceName());
    }
    addMenu.add(Menu.NONE, MenuId.QRCODE_ADD.getId(), Menu.NONE, "Scan Barcode");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.HELP.getId()) {
      Help.show(this);
    } else if (itemId == MenuId.INTERPRETER_MANAGER.getId()) {
      // Show interpreter manger.
      Intent i = new Intent(this, InterpreterManager.class);
      startActivity(i);
    } else if (mAddMenuIds.containsKey(itemId)) {
      // Add a new script.
      Intent intent = new Intent(Constants.ACTION_EDIT_SCRIPT);
      Interpreter interpreter = mAddMenuIds.get(itemId);
      intent.putExtra(Constants.EXTRA_SCRIPT_PATH, new File(mCurrentDir.getPath(), interpreter
          .getExtension()).getPath());
      intent.putExtra(Constants.EXTRA_SCRIPT_CONTENT, interpreter.getContentTemplate());
      intent.putExtra(Constants.EXTRA_IS_NEW_SCRIPT, true);
      startActivity(intent);
      synchronized (mQuery) {
        mQuery = EMPTY;
      }
    } else if (itemId == MenuId.QRCODE_ADD.getId()) {
      Intent intent = new Intent("com.google.zxing.client.android.SCAN");
      startActivityForResult(intent, RequestCode.QRCODE_ADD.ordinal());
    } else if (itemId == MenuId.FOLDER_ADD.getId()) {
      addFolder(null);
    } else if (itemId == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (itemId == MenuId.TRIGGER_MANAGER.getId()) {
      startActivity(new Intent(this, TriggerManager.class));
    } else if (itemId == MenuId.LOGCAT_VIEWER.getId()) {
      startActivity(new Intent(this, LogcatViewer.class));
    } else if (itemId == MenuId.REFRESH.getId()) {
      updateAndFilterScriptList(mQuery);
      mAdapter.notifyDataSetChanged();
    } else if (itemId == MenuId.SEARCH.getId()) {
      onSearchRequested();
    }
    return true;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    final File file = mScripts.get(info.position);
    if (!file.isDirectory() || !mCurrentDir.equals(mBaseDir) && info.position == 0) {
      return;
    }
    final QuickAction actionMenu = new QuickAction(info.targetView);

    final ActionItem edit = new ActionItem();
    edit.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
    edit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        renameFolder(file, null);
        dismissQuickActions(actionMenu);
      }
    });

    final ActionItem delete = new ActionItem();
    delete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));
    delete.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteScript(file);
        dismissQuickActions(actionMenu);
      }
    });

    actionMenu.addActionItems(edit, delete);
    actionMenu.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
    actionMenu.show();
  }

  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    final File file = (File) list.getItemAtPosition(position);
    if (file.isDirectory()) {
      mCurrentDir = file;
      mAdapter.notifyDataSetInvalidated();
      return;
    }
    final QuickAction actionMenu = new QuickAction(view);

    ActionItem terminal = new ActionItem();
    terminal.setIcon(getResources().getDrawable(R.drawable.terminal));
    terminal.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ScriptManager.this, ScriptingLayerService.class);
        intent.setAction(Constants.ACTION_LAUNCH_FOREGROUND_SCRIPT);
        intent.putExtra(Constants.EXTRA_SCRIPT_PATH, file.getPath());
        startService(intent);
        dismissQuickActions(actionMenu);
      }
    });

    final ActionItem background = new ActionItem();
    background.setIcon(getResources().getDrawable(R.drawable.background));
    background.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ScriptManager.this, ScriptingLayerService.class);
        intent.setAction(Constants.ACTION_LAUNCH_BACKGROUND_SCRIPT);
        intent.putExtra(Constants.EXTRA_SCRIPT_PATH, file.getPath());
        startService(intent);
        dismissQuickActions(actionMenu);
      }
    });

    final ActionItem edit = new ActionItem();
    edit.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));
    edit.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        editScript(file.getPath());
        dismissQuickActions(actionMenu);
      }
    });

    final ActionItem delete = new ActionItem();
    delete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));
    delete.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        deleteScript(file);
        dismissQuickActions(actionMenu);
      }
    });

    actionMenu.addActionItems(terminal, background, edit, delete);
    actionMenu.setAnimStyle(QuickAction.ANIM_GROW_FROM_CENTER);
    actionMenu.show();

  }

  private void dismissQuickActions(final QuickAction action) {
    mHandler.postDelayed(new Runnable() {
      @Override
      public void run() {
        action.dismiss();
      }
    }, 1);
  }

  /**
   * Opens the script for editing.
   * 
   * @param scriptName
   *          the name of the script to edit
   */
  private void editScript(String script) {
    Intent i = new Intent(Constants.ACTION_EDIT_SCRIPT);
    i.putExtra(Constants.EXTRA_SCRIPT_PATH, script);
    startActivity(i);
  }

  private void deleteScript(final File script) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    if (script.isDirectory()) {
      alert.setTitle("Delete Folder");
    } else {
      alert.setTitle("Delete Script");
    }
    alert.setMessage("Would you like to delete " + script.getName() + "?");
    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        FileUtils.delete(script);
        mScripts.remove(script);
        mAdapter.notifyDataSetChanged();
      }
    });
    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Ignore.
      }
    });
    alert.show();
  }

  private void addFolder(String name) {
    if (name == null) {
      String defaultName = "untitled folder";
      name = defaultName;
      int i = 1;
      for (File f : mScripts) {
        if (f.isDirectory() && f.getName().equals(name)) {
          i++;
          name = String.format("%s %d", defaultName, i);
        }
      }
    }
    final EditText folderName = new EditText(this);
    folderName.setText(name);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Add Folder");
    alert.setView(folderName);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        String name = folderName.getText().toString();
        if (name.length() == 0) {
          showAlert("Folder name is empty.", name);
          return;
        } else {
          for (File f : mScripts) {
            if (f.getName().equals(name)) {
              showAlert(String.format("Directory \"%s\" already exists.", name), name);
              return;
            }
          }
        }
        File dir = new File(mCurrentDir, name);
        if (!FileUtils.makeDirectory(dir)) {
          throw new RuntimeException(String.format("Cannot create directory \"%s\"", dir.getPath()));
        }
        mAdapter.notifyDataSetInvalidated();
      }

      private void showAlert(String message, final String folderName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ScriptManager.this);
        alert.setMessage(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            addFolder(folderName);
          }
        });
        alert.show();
      }
    });
    alert.show();
  }

  private void renameFolder(final File dir, String name) {
    if (name == null) {
      name = dir.getName();
    }
    final EditText folderName = new EditText(this);
    folderName.setText(name);
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Rename Folder");
    alert.setView(folderName);
    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        String name = folderName.getText().toString();
        if (name.length() == 0) {
          showAlert("Folder name is empty.", name);
          return;
        } else {
          for (File f : mScripts) {
            if (f.getName().equals(name)) {
              showAlert(String.format("Directory \"%s\" already exists.", name), name);
              return;
            }
          }
        }
        if (!FileUtils.remane(dir, name)) {
          throw new RuntimeException(String.format("Cannot rename directory \"%s\"", dir.getPath()));
        }
        mAdapter.notifyDataSetInvalidated();
      }

      private void showAlert(String message, final String folderName) {
        AlertDialog.Builder alert = new AlertDialog.Builder(ScriptManager.this);
        alert.setMessage(message);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            renameFolder(dir, folderName);
          }
        });
        alert.show();
      }
    });
    alert.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    RequestCode request = RequestCode.values()[requestCode];
    if (resultCode == RESULT_OK) {
      switch (request) {
      case QRCODE_ADD:
        writeScriptFromBarcode(data);
        break;
      default:
        break;
      }
    } else {
      switch (request) {
      case QRCODE_ADD:
        break;
      default:
        break;
      }
    }
    mAdapter.notifyDataSetInvalidated();
  }

  private void writeScriptFromBarcode(Intent data) {
    String result = data.getStringExtra("SCAN_RESULT");
    if (result == null) {
      Log.e(this, "Invalid QR code content.");
      return;
    }
    String contents[] = result.split("\n", 2);
    if (contents.length != 2) {
      Log.e(this, "Invalid QR code content.");
      return;
    }
    String title = contents[0];
    String body = contents[1];
    File script = new File(mCurrentDir, title);
    ScriptStorageAdapter.writeScript(script, body);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mConfiguration.unregisterObserver(mObserver);
    mManager.setOnCancelListener(null);
  }

  private class ScriptListObserver extends DataSetObserver implements ConfigurationObserver {
    @Override
    public void onInvalidated() {
      updateAndFilterScriptList(EMPTY);
    }

    @Override
    public void onConfigurationChanged() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          updateAndFilterScriptList(mQuery);
          mAdapter.notifyDataSetChanged();
        }
      });
    }
  }

  private class ScriptManagerAdapter extends ScriptListAdapter {
    public ScriptManagerAdapter(Context context) {
      super(context);
    }

    @Override
    protected List<File> getScriptList() {
      return mScripts;
    }
  }
}
