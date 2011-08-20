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
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.dialog.Help;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A text editor for scripts.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptEditor extends Activity implements OnClickListener {
  private static final int DIALOG_FIND_REPLACE = 2;
  private static final int DIALOG_LINE = 1;
  private EditText mNameText;
  private EditText mContentText;
  private boolean mScheduleMoveLeft;
  private String mLastSavedContent;
  private SharedPreferences mPreferences;
  private InterpreterConfiguration mConfiguration;
  private ContentTextWatcher mWatcher;
  private EditHistory mHistory;
  private File mScript;
  private EditText mLineNo;

  private boolean mIsUndoOrRedo = false;
  private boolean mEnableAutoClose;
  private boolean mAutoIndent;

  private EditText mSearchFind;
  private EditText mSearchReplace;
  private CheckBox mSearchCase;
  private CheckBox mSearchWord;
  private CheckBox mSearchAll;
  private CheckBox mSearchStart;

  private static enum MenuId {
    SAVE, SAVE_AND_RUN, PREFERENCES, API_BROWSER, HELP, SHARE, GOTO;
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

    mScript = new File(getIntent().getStringExtra(Constants.EXTRA_SCRIPT_PATH));
    mNameText.setText(mScript.getName());
    mNameText.setSelected(true);
    // NOTE: This appears to be the only way to get Android to put the cursor to the beginning of
    // the EditText field.
    mNameText.setSelection(1);
    mNameText.extendSelection(0);
    mNameText.setSelection(0);
    mLastSavedContent = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_CONTENT);
    if (mLastSavedContent == null) {
      try {
        mLastSavedContent = FileUtils.readToString(mScript);
      } catch (IOException e) {
        Log.e("Failed to read script.", e);
        mLastSavedContent = "";
      } finally {
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
    mLineNo = new EditText(this);
    mLineNo.setInputType(InputType.TYPE_CLASS_NUMBER);
    Analytics.trackActivity(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    updatePreferences();
  }

  private void updatePreferences() {
    mContentText.setTextSize(readIntPref("editor_fontsize", 10, 30));
    mEnableAutoClose = mPreferences.getBoolean("enableAutoClose", true);
    mAutoIndent = mPreferences.getBoolean("editor_auto_indent", false);
    mContentText.setHorizontallyScrolling(mPreferences.getBoolean("editor_no_wrap", false));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    menu.add(0, MenuId.SAVE.getId(), 0, "Save & Exit").setIcon(android.R.drawable.ic_menu_save);
    menu.add(0, MenuId.SAVE_AND_RUN.getId(), 0, "Save & Run").setIcon(
        android.R.drawable.ic_media_play);
    menu.add(0, MenuId.PREFERENCES.getId(), 0, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    menu.add(0, MenuId.API_BROWSER.getId(), 0, "API Browser").setIcon(
        android.R.drawable.ic_menu_info_details);
    menu.add(0, MenuId.HELP.getId(), 0, "Help").setIcon(android.R.drawable.ic_menu_help);
    menu.add(0, MenuId.SHARE.getId(), 0, "Share").setIcon(android.R.drawable.ic_menu_share);
    menu.add(0, MenuId.GOTO.getId(), 0, "GoTo").setIcon(android.R.drawable.ic_menu_directions);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == MenuId.SAVE.getId()) {
      save();
      finish();
    } else if (item.getItemId() == MenuId.SAVE_AND_RUN.getId()) {
      save();
      Interpreter interpreter =
          mConfiguration.getInterpreterForScript(mNameText.getText().toString());
      if (interpreter != null) { // We may be editing an unknown type.
        Intent intent = new Intent(this, ScriptingLayerService.class);
        intent.setAction(Constants.ACTION_LAUNCH_FOREGROUND_SCRIPT);
        intent.putExtra(Constants.EXTRA_SCRIPT_PATH, mScript.getAbsolutePath());
        startService(intent);
      } else {
        // TODO(damonkohler): Should remove menu option.
        Toast.makeText(this, "Can't run this type.", Toast.LENGTH_SHORT).show();
      }
      finish();
    } else if (item.getItemId() == MenuId.PREFERENCES.getId()) {
      startActivity(new Intent(this, Preferences.class));
    } else if (item.getItemId() == MenuId.API_BROWSER.getId()) {
      Intent intent = new Intent(this, ApiBrowser.class);
      intent.putExtra(Constants.EXTRA_SCRIPT_PATH, mNameText.getText().toString());
      intent.putExtra(Constants.EXTRA_INTERPRETER_NAME,
          mConfiguration.getInterpreterForScript(mNameText.getText().toString()).getName());
      intent.putExtra(Constants.EXTRA_SCRIPT_TEXT, mContentText.getText().toString());
      startActivityForResult(intent, RequestCode.RPC_HELP.ordinal());
    } else if (item.getItemId() == MenuId.HELP.getId()) {
      Help.show(this);
    } else if (item.getItemId() == MenuId.SHARE.getId()) {
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.putExtra(Intent.EXTRA_TEXT, mContentText.getText().toString());
      intent.putExtra(Intent.EXTRA_SUBJECT, "Share " + mNameText.getText().toString());
      intent.setType("text/plain");
      startActivity(Intent.createChooser(intent, "Send Script to:"));
    } else if (item.getItemId() == MenuId.GOTO.getId()) {
      showDialog(DIALOG_LINE);
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
    mScript = new File(mScript.getParent(), mNameText.getText().toString());
    ScriptStorageAdapter.writeScript(mScript, mLastSavedContent);
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
    } else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
      showDialog(DIALOG_FIND_REPLACE);
      return true;
    } else {
      return super.onKeyDown(keyCode, event);
    }
  }

  @Override
  protected Dialog onCreateDialog(int id, Bundle args) {
    AlertDialog.Builder b = new AlertDialog.Builder(this);
    if (id == DIALOG_LINE) {
      b.setTitle("Goto Line");
      b.setView(mLineNo);
      b.setPositiveButton("Ok", new OnClickListener() {

        @Override
        public void onClick(DialogInterface dialog, int which) {
          gotoLine(Integer.parseInt(mLineNo.getText().toString()));
        }
      });
      b.setNegativeButton("Cancel", null);
      return b.create();
    } else if (id == DIALOG_FIND_REPLACE) {
      View v = getLayoutInflater().inflate(R.layout.findreplace, null);
      mSearchFind = (EditText) v.findViewById(R.id.searchFind);
      mSearchReplace = (EditText) v.findViewById(R.id.searchReplace);
      mSearchAll = (CheckBox) v.findViewById(R.id.searchAll);
      mSearchCase = (CheckBox) v.findViewById(R.id.searchCase);
      mSearchStart = (CheckBox) v.findViewById(R.id.searchStart);
      mSearchWord = (CheckBox) v.findViewById(R.id.searchWord);
      b.setTitle("Search and Replace");
      b.setView(v);
      b.setPositiveButton("Find", this);
      b.setNeutralButton("Next", this);
      b.setNegativeButton("Replace", this);
      return b.create();
    }

    return super.onCreateDialog(id, args);
  }

  @Override
  protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
    if (id == DIALOG_LINE) {
      mLineNo.setText(String.valueOf(getLineNo()));
    } else if (id == DIALOG_FIND_REPLACE) {
      mSearchStart.setChecked(false);
    }
    super.onPrepareDialog(id, dialog, args);
  }

  protected int getLineNo() {
    int pos = mContentText.getSelectionStart();
    String text = mContentText.getText().toString();
    int i = 0;
    int n = 1;
    while (i < pos) {
      int j = text.indexOf("\n", i);
      if (j < 0) {
        break;
      }
      i = j + 1;
      if (i < pos) {
        n += 1;
      }
    }
    return n;
  }

  protected void gotoLine(int line) {
    String text = mContentText.getText().toString();
    if (text.length() < 1) {
      return;
    }
    int i = 0;
    int n = 1;
    while (i < text.length() && n < line) {
      int j = text.indexOf("\n", i);
      if (j < 0) {
        break;
      }
      i = j + 1;
      n += 1;
    }
    mContentText.setSelection(Math.min(text.length() - 1, i));
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
        Interpreter ip = mConfiguration.getInterpreterForScript(mNameText.getText().toString());
        String auto = null;
        if (ip != null && mEnableAutoClose) {
          auto = ip.getLanguage().autoClose(source.charAt(start));
        }
        // Auto indent code?
        if (auto == null && source.charAt(start) == '\n' && mAutoIndent) {
          int i = dstart - 1;
          int spaces = 0;
          while ((i >= 0) && dest.charAt(i) != '\n') {
            i -= 1; // Find start of line.
          }
          i += 1;
          while (i < dest.length() && dest.charAt(i++) == ' ') {
            spaces += 1;
          }
          if (spaces > 0) {
            return String.format("\n%" + spaces + "s", " ");
          }
        }
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

  @Override
  public void onClick(DialogInterface dialog, int which) {
    int start = mContentText.getSelectionStart();
    int end = mContentText.getSelectionEnd();
    String original = mContentText.getText().toString();
    if (start == end || which != AlertDialog.BUTTON_NEGATIVE) {
      end = original.length();
    }
    if (which == AlertDialog.BUTTON_NEUTRAL) {
      start += 1;
    }
    if (mSearchStart.isChecked()) {
      start = 0;
      end = original.length();
    }
    String findText = mSearchFind.getText().toString();
    String replaceText = mSearchReplace.getText().toString();
    String search = Pattern.quote(findText);
    int flags = 0;
    if (!mSearchCase.isChecked()) {
      flags |= Pattern.CASE_INSENSITIVE;
    }
    if (mSearchWord.isChecked()) {
      search = "\\b" + search + "\\b";
    }
    Pattern p = Pattern.compile(search, flags);
    Matcher m = p.matcher(original);
    m.region(start, end);
    if (!m.find()) {
      Toast.makeText(this, "Search not found.", Toast.LENGTH_SHORT).show();
      return;
    }
    int foundpos = m.start();
    if (which != AlertDialog.BUTTON_NEGATIVE) { // Find
      mContentText.setSelection(foundpos, foundpos + findText.length());
    } else { // Replace
      String s;
      // Seems to be a bug in the android 2.2 implementation of replace... regions not returning
      // whole string.
      m = p.matcher(original.substring(start, end));
      String replace = Matcher.quoteReplacement(replaceText);
      if (mSearchAll.isChecked()) {
        s = m.replaceAll(replace);
      } else {
        s = m.replaceFirst(replace);
      }
      mContentText.setText(original.substring(0, start) + s + original.substring(end));
      mContentText.setSelection(foundpos, foundpos + replaceText.length());
    }
    mContentText.requestFocus();
  }
}
