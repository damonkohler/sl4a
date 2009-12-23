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

package com.google.ase;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterUtils;

/**
 * Manages creation, deletion, and execution of stored scripts.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptManager extends ListActivity {

  private static final String TAG = "ScriptManager";

  private static enum RequestCode {
    INSTALL_INTERPETER, QRCODE_ADD
  }

  private HashMap<Integer, Interpreter> addMenuIds;

  private static enum MenuId {
    DELETE, EDIT, ADD_SHORTCUT, START_SERVICE, HELP, QRCODE_ADD, INTERPRETER_MANAGER, PREFERENCES;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    setContentView(R.layout.list);
    CustomWindowTitle.buildWindowTitle(this);

    listScripts();
    registerForContextMenu(getListView());

    AseAnalytics.trackActivity(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    listScripts();
  }

  /**
   * Populates the list view with all available scripts.
   */
  private void listScripts() {
    // Get all of the rows from the database and create the item list
    List<File> scriptFiles = ScriptStorageAdapter.listScripts();

    // Build up a simple list of maps. Just one attribute we care about
    // currently.
    List<Map<String, String>> data = new ArrayList<Map<String, String>>();
    for (File f : scriptFiles) {
      Map<String, String> map = new HashMap<String, String>();
      map.put(Constants.EXTRA_SCRIPT_NAME, f.getName());
      data.add(map);
    }

    Collections.sort(data, new Comparator<Map<String, String>>() {
      public int compare(Map<String, String> m1, Map<String, String> m2) {
        return m1.get(Constants.EXTRA_SCRIPT_NAME).compareTo(m2.get(Constants.EXTRA_SCRIPT_NAME));
      }
    });

    // Create an array to specify the fields we want to display in the list
    // (only TITLE)
    String[] from = new String[] { Constants.EXTRA_SCRIPT_NAME };

    // and an array of the fields we want to bind those fields to (in this case
    // just text1)
    int[] to = new int[] { R.id.text1 };

    // Now create a simple cursor adapter and set it to display
    SimpleAdapter scripts = new SimpleAdapter(this, data, R.layout.row, from, to);
    setListAdapter(scripts);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    // TODO(damonkohler): Would be nice if this were lazier and not done every time the menu
    // button is pressed.
    menu.clear();
    buildMenuIdMaps();
    buildAddMenu(menu);
    menu.add(Menu.NONE, MenuId.INTERPRETER_MANAGER.getId(), Menu.NONE, "Interpreters");
    menu.add(Menu.NONE, MenuId.PREFERENCES.getId(), Menu.NONE, "Preferences");
    menu.add(Menu.NONE, MenuId.HELP.getId(), Menu.NONE, "Help");
    return true;
  }

  private void buildMenuIdMaps() {
    addMenuIds = new HashMap<Integer, Interpreter>();
    int i = MenuId.values().length + Menu.FIRST;
    List<Interpreter> installed = InterpreterUtils.getInstalledInterpreters();
    for (Interpreter interpreter : installed) {
      addMenuIds.put(i, interpreter);
      ++i;
    }
  }

  private void buildAddMenu(Menu menu) {
    Menu addMenu = menu.addSubMenu(Menu.NONE, Menu.NONE, Menu.NONE, "Add");
    for (Entry<Integer, Interpreter> entry : addMenuIds.entrySet()) {
      addMenu.add(Menu.NONE, entry.getKey(), Menu.NONE, entry.getValue().getNiceName());
    }
    addMenu.add(Menu.NONE, MenuId.QRCODE_ADD.getId(), Menu.NONE, "Scan Barcode");
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.HELP.getId()) {
      // Show documentation.
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(getString(R.string.wiki_url)));
      startActivity(intent);
    } else if (itemId == MenuId.INTERPRETER_MANAGER.getId()) {
      // Show interpreter manger.
      Intent i = new Intent(this, InterpreterManager.class);
      startActivity(i);
    } else if (addMenuIds.containsKey(itemId)) {
      // Add a new script.
      Intent intent = new Intent(Constants.ACTION_EDIT_SCRIPT);
      Interpreter interpreter = addMenuIds.get(itemId);
      intent.putExtra(Constants.EXTRA_SCRIPT_NAME, interpreter.getExtension());
      intent.putExtra(Constants.EXTRA_SCRIPT_CONTENT, interpreter.getContentTemplate());
      startActivity(intent);
    } else if (itemId == MenuId.QRCODE_ADD.getId()) {
      Intent intent = new Intent("com.google.zxing.client.android.SCAN");
      startActivityForResult(intent, RequestCode.QRCODE_ADD.ordinal());
    } else if (itemId == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, AsePreferences.class));
    }
    return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected void onListItemClick(ListView list, View view, int position, long id) {
    super.onListItemClick(list, view, position, id);
    Map<String, String> item = (Map<String, String>) list.getItemAtPosition(position);
    String scriptName = item.get(Constants.EXTRA_SCRIPT_NAME);
    if (Intent.ACTION_CREATE_SHORTCUT.equals(getIntent().getAction())) {
      Parcelable iconResource =
          Intent.ShortcutIconResource.fromContext(this, R.drawable.ase_logo_48);
      Intent intent = IntentBuilders.buildShortcutIntent(scriptName, iconResource);
      if (intent != null) {
        setResult(RESULT_OK, intent);
      } else {
        setResult(RESULT_CANCELED, null);
      }
      finish();
      return;
    } else if (Intent.ACTION_PICK.equals(getIntent().getAction())) {
      Intent intent = IntentBuilders.buildLaunchIntent(scriptName);
      if (intent != null) {
        setResult(RESULT_OK, intent);
      } else {
        setResult(RESULT_CANCELED, null);
      }
      finish();
      return;
    } else if (com.twofortyfouram.Intent.ACTION_EDIT_SETTING.equals(getIntent().getAction())) {
      Intent intent = new Intent();
      intent.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
      // Set the description of the action.
      if (scriptName.length() > com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH) {
        intent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB,
            scriptName.substring(0, com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH));
      } else {
        intent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, scriptName);
      }
      setResult(RESULT_OK, intent);
      AseLog.v("Returned launch intent for " + scriptName + " to Locale: " + intent.toURI());
      finish();
      return;
    }

    startActivity(IntentBuilders.buildLaunchIntent(scriptName));
  }

  /**
   * Opens the script for editing.
   *
   * @param scriptName
   *          the name of the script to edit
   */
  private void editScript(String scriptName) {
    Intent i = new Intent(Constants.ACTION_EDIT_SCRIPT);
    i.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
    startActivity(i);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    menu.add(Menu.NONE, MenuId.EDIT.getId(), Menu.NONE, "Edit");
    menu.add(Menu.NONE, MenuId.DELETE.getId(), Menu.NONE, "Delete");
    menu.add(Menu.NONE, MenuId.ADD_SHORTCUT.getId(), Menu.NONE, "Add Shortcut");
    menu.add(Menu.NONE, MenuId.START_SERVICE.getId(), Menu.NONE, "Start Service");
  }

  @SuppressWarnings("unchecked")
  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterView.AdapterContextMenuInfo info;
    try {
      info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    } catch (ClassCastException e) {
      Log.e(TAG, "bad menuInfo", e);
      return false;
    }

    Map<String, String> scriptItem = (Map<String, String>) getListAdapter().getItem(info.position);
    if (scriptItem == null) {
      Log.v(TAG, "No script selected.");
      return false;
    }

    final String scriptName = scriptItem.get(Constants.EXTRA_SCRIPT_NAME);
    Log.v(TAG, "Selected: " + scriptName);

    int itemId = item.getItemId();
    if (itemId == MenuId.DELETE.getId()) {
      deleteScript(scriptName);
    } else if (itemId == MenuId.EDIT.getId()) {
      editScript(scriptName);
    } else if (itemId == MenuId.ADD_SHORTCUT.getId()) {
      Parcelable iconResource =
          Intent.ShortcutIconResource.fromContext(this, R.drawable.ase_logo_48);
      Intent i = IntentBuilders.buildShortcutIntent(scriptName, iconResource);
      if (i != null) {
        i.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
        sendBroadcast(i);
        Toast.makeText(this, "Created shortcut to " + scriptName + ".", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(this, "Could not find script.", Toast.LENGTH_SHORT).show();
      }
    } else if (itemId == MenuId.START_SERVICE.getId()) {
      Intent i = new Intent(this, ScriptService.class);
      i.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
      startService(i);
    }
    return true;
  }

  private void deleteScript(final String scriptName) {
    AlertDialog.Builder alert = new AlertDialog.Builder(this);
    alert.setTitle("Delete Script");
    alert.setMessage("Are you sure?");
    alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        ScriptStorageAdapter.deleteScript(scriptName);
        listScripts();
      }
    });
    alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        // Ignore.
      }
    });
    alert.show();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    RequestCode request = RequestCode.values()[requestCode];
    if (resultCode == RESULT_OK) {
      switch (request) {
        case QRCODE_ADD:
          String contents[] = data.getStringExtra("SCAN_RESULT").split("\n", 2);
          String title = contents[0];
          String body = contents[1];
          ScriptStorageAdapter.writeScript(title, body);
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
    listScripts();
  }
}
