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
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.savedInstanceState See the License for the specific language
 * governing permissions and limitations under the License.
 */

package com.googlecode.android_scripting.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.dialog.Help;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

/**
 * A text editor for scripts.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptEditor extends Activity {

  private EditText mNameText;
  private EditText mContentText;
  private boolean mScheduleMoveLeft;
  private String mLastSavedContent;
  private SharedPreferences mPreferences;
  private InterpreterConfiguration mConfiguration;
  private ContentTextWatcher mWatcher;
  private EditHistory mHistory;
  private String mPath;

  private boolean mIsUndoOrRedo = false;

  private static enum MenuId {
    SAVE, SAVE_AND_RUN, PREFERENCES, API_BROWSER, HELP;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private static enum RequestCode {
    RPC_HELP
  }

  private int readIntPref(String key, int defaultValue, int maxValue) {
    int val;
    try {
      val = Integer.parseInt(mPreferences.getString(key, Integer.toString(defaultValue)));
    } catch (NumberFormatException e) {
      val = defaultValue;
    }
    val = Math.max(0, Math.min(val, maxValue));
    return val;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.script_editor);
    mNameText = (EditText) findViewById(R.id.script_editor_title);
    mContentText = (EditText) findViewById(R.id.script_editor_body);
    mHistory = new EditHistory();
    mWatcher = new ContentTextWatcher(mHistory);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    updatePreferences();
    String script = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_PATH);

    if (script != null) {
      File file = new File(script);
      String name = file.getName();
      mPath = file.getParent();
      mNameText.setText(name);
      mNameText.setSelected(true);
      // NOTE: This appears to be the only way to get Android to put the cursor to the beginning of
      // the EditText field.
      mNameText.setSelection(1);
      mNameText.extendSelection(0);
      mNameText.setSelection(0);
      mLastSavedContent = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_CONTENT);
      if (mLastSavedContent == null) {
        try {
          mLastSavedContent = ScriptStorageAdapter.readScript(script);
        } catch (IOException e) {
          Log.e("Failed to read script.", e);
          mLastSavedContent = "";
        } finally {
        }
      }
    }

    mContentText.setText(mLastSavedContent);

    InputFilter[] oldFilters = mContentText.getFilters();
    List<InputFilter> filters = new ArrayList<InputFilter>(oldFilters.length + 1);
    filters.addAll(Arrays.asList(oldFilters));
    filters.add(new ContentInputFilter());
    mContentText.setFilters(filters.toArray(oldFilters));
    mContentText.addTextChangedListener(mWatcher);
    mConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
    // Disables volume key beep.
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
    Analytics.trackActivity(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updatePreferences();
  }

  private void updatePreferences() {
    mContentText.setTextSize(readIntPref("editor_fontsize", 10, 30));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MenuId.SAVE.getId(), 0, "Save").setIcon(android.R.drawable.ic_menu_save);
    menu.add(0, MenuId.SAVE_AND_RUN.getId(), 0, "Save & Run").setIcon(
        android.R.drawable.ic_media_play);
    menu.add(0, MenuId.PREFERENCES.getId(), 0, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(0, MenuId.API_BROWSER.getId(), 0, "API Browser").setIcon(
        android.R.drawable.ic_menu_info_details);
    menu.add(0, MenuId.HELP.getId(), 0, "Help").setIcon(android.R.drawable.ic_menu_help);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == MenuId.SAVE.getId()) {
      save();
      finish();
    } else if (item.getItemId() == MenuId.SAVE_AND_RUN.getId()) {
      save();
      Intent intent = new Intent(this, ScriptingLayerService.class);
      intent.setAction(Constants.ACTION_LAUNCH_FOREGROUND_SCRIPT);
      intent.putExtra(Constants.EXTRA_SCRIPT_PATH, new File(mPath, mNameText.getText().toString())
          .getPath());
      startService(intent);
      finish();
    } else if (item.getItemId() == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (item.getItemId() == MenuId.API_BROWSER.getId()) {
      Intent intent = new Intent(this, ApiBrowser.class);
      intent.putExtra(Constants.EXTRA_SCRIPT_PATH, mNameText.getText().toString());
      intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, mConfiguration.getInterpreterForScript(
          mNameText.getText().toString()).getName());
      intent.putExtra(Constants.EXTRA_SCRIPT_TEXT, mContentText.getText().toString());
      startActivityForResult(intent, RequestCode.RPC_HELP.ordinal());
    } else if (item.getItemId() == MenuId.HELP.getId()) {
      Help.show(this);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    RequestCode request = RequestCode.values()[requestCode];

    if (resultCode == RESULT_OK) {
      switch (request) {
      case RPC_HELP:
        String rpcText = data.getStringExtra(Constants.EXTRA_RPC_HELP_TEXT);
        insertContent(rpcText);
        break;
      default:
        break;
      }
    } else {
      switch (request) {
      case RPC_HELP:
        break;
      default:
        break;
      }
    }
  }

  private void save() {
    mLastSavedContent = mContentText.getText().toString();
    String destination = new File(mPath, mNameText.getText().toString()).getPath();
    ScriptStorageAdapter.writeScript(destination, mLastSavedContent);
    Toast.makeText(this, "Saved " + mNameText.getText().toString(), Toast.LENGTH_SHORT).show();
  }

  private void insertContent(String text) {
    int selectionStart = Math.min(mContentText.getSelectionStart(), mContentText.getSelectionEnd());
    int selectionEnd = Math.max(mContentText.getSelectionStart(), mContentText.getSelectionEnd());
    mContentText.getEditableText().replace(selectionStart, selectionEnd, text);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK && hasContentChanged()) {
      AlertDialog.Builder alert = new AlertDialog.Builder(this);
      setVolumeControlStream(AudioManager.STREAM_MUSIC);
      alert.setCancelable(false);
      alert.setTitle("Confirm exit");
      alert.setMessage("Would you like to save?");
      alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          save();
          finish();
        }
      });
      alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
          finish();
        }
      });
      alert.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int whichButton) {
        }
      });
      alert.show();
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
      redo();
      return true;
    } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
      undo();
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  protected void onUserLeaveHint() {
    if (hasContentChanged()) {
      save();
    }
  }

  private boolean hasContentChanged() {
    return !mLastSavedContent.equals(mContentText.getText().toString());
  }

  private final class ContentInputFilter implements InputFilter {
    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart,
        int dend) {
      if (end - start == 1) {
        String auto =
            mConfiguration.getInterpreterForScript(mNameText.getText().toString()).getLanguage()
                .autoClose(source.charAt(start));
        if (auto != null) {
          mScheduleMoveLeft = true;
          return auto;
        }
      }
      return null;
    }
  }

  private final class ContentTextWatcher implements TextWatcher {
    private final EditHistory mmEditHistory;
    private CharSequence mmBeforeChange;
    private CharSequence mmAfterChange;

    private ContentTextWatcher(EditHistory history) {
      mmEditHistory = history;
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      if (!mIsUndoOrRedo) {
        mmAfterChange = s.subSequence(start, start + count);
        mmEditHistory.add(new EditItem(start, mmBeforeChange, mmAfterChange));
      }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      if (!mIsUndoOrRedo) {
        mmBeforeChange = s.subSequence(start, start + count);
      }
    }

    @Override
    public void afterTextChanged(Editable s) {
      if (mScheduleMoveLeft) {
        mScheduleMoveLeft = false;
        Selection.moveLeft(mContentText.getText(), mContentText.getLayout());
      }
    }
  }

  /**
   * Keeps track of all the edit history of a text.
   */
  private final class EditHistory {
    int mmPosition = 0;
    private final Vector<EditItem> mmHistory = new Vector<EditItem>();

    /**
     * Adds a new edit operation to the history at the current position. If executed after a call to
     * getPrevious() removes all the future history (elements with positions >= current history
     * position).
     * 
     */
    private void add(EditItem item) {
      mmHistory.setSize(mmPosition);
      mmHistory.add(item);
      mmPosition++;
    }

    /**
     * Traverses the history backward by one position, returns and item at that position.
     */
    private EditItem getPrevious() {
      if (mmPosition == 0) {
        return null;
      }
      mmPosition--;
      return mmHistory.get(mmPosition);
    }

    /**
     * Traverses the history forward by one position, returns and item at that position.
     */
    private EditItem getNext() {
      if (mmPosition == mmHistory.size()) {
        return null;
      }
      EditItem item = mmHistory.get(mmPosition);
      mmPosition++;
      return item;
    }
  }

  /**
   * Represents a single edit operation.
   */
  private final class EditItem {
    private final int mmIndex;
    private final CharSequence mmBefore;
    private final CharSequence mmAfter;

    /**
     * Constructs EditItem of a modification that was applied at position start and replaced
     * CharSequence before with CharSequence after.
     */
    public EditItem(int start, CharSequence before, CharSequence after) {
      mmIndex = start;
      mmBefore = before;
      mmAfter = after;
    }
  }

  private void undo() {
    EditItem edit = mHistory.getPrevious();
    if (edit == null) {
      return;
    }
    Editable text = mContentText.getText();
    int start = edit.mmIndex;
    int end = start + (edit.mmAfter != null ? edit.mmAfter.length() : 0);
    mIsUndoOrRedo = true;
    text.replace(start, end, edit.mmBefore);
    mIsUndoOrRedo = false;
    // This will get rid of underlines inserted when editor tries to come up with a suggestion.
    for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
      text.removeSpan(o);
    }
    Selection.setSelection(text, edit.mmBefore == null ? start : (start + edit.mmBefore.length()));
  }

  private void redo() {
    EditItem edit = mHistory.getNext();
    if (edit == null) {
      return;
    }
    Editable text = mContentText.getText();
    int start = edit.mmIndex;
    int end = start + (edit.mmBefore != null ? edit.mmBefore.length() : 0);
    mIsUndoOrRedo = true;
    text.replace(start, end, edit.mmAfter);
    mIsUndoOrRedo = false;
    for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
      text.removeSpan(o);
    }
    Selection.setSelection(text, edit.mmAfter == null ? start : (start + edit.mmAfter.length()));
  }

}
