/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.googlecode.android_scripting.terminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptProcess;
import com.googlecode.android_scripting.activity.Preferences;
import com.googlecode.android_scripting.activity.ScriptingLayerService;
import com.googlecode.android_scripting.interpreter.InterpreterProcess;

/**
 * A terminal emulator activity.
 */
public class Terminal extends Activity {

  /**
   * Set to true to add debugging code and logging.
   */
  public static final boolean DEBUG = false;

  /**
   * Set to true to log each character received from the remote process to the android log, which
   * makes it easier to debug some kinds of problems with emulating escape sequences and control
   * codes.
   */
  public static final boolean LOG_CHARACTERS_FLAG = DEBUG;

  /**
   * Set to true to log unknown escape sequences.
   */
  public static final boolean LOG_UNKNOWN_ESCAPE_SEQUENCES = DEBUG;

  /**
   * The tag we use when logging, so that our messages can be distinguished from other messages in
   * the log. Public because it's used by several classes.
   */
  public static final String TAG = "Terminal";

  /**
   * Our main view. Displays the emulated terminal screen.
   */
  private EmulatorView mEmulatorView;

  /**
   * A key listener that tracks the modifier keys and allows the full ASCII character set to be
   * entered.
   */
  private TermKeyListener mKeyListener;

  /**
   * The name of our emulator view in the view resource.
   */
  private static final int EMULATOR_VIEW = R.id.emulatorView;

  private static final int DEFAULT_FONT_SIZE = 10;
  private static final String FONTSIZE_KEY = "fontsize";

  public static final int WHITE = 0xffffffff;
  public static final int BLACK = 0xff000000;
  public static final int BLUE = 0xff344ebd;

  private static final int DEFAULT_COLOR_SCHEME = 1;
  private static final String COLOR_KEY = "color";

  private static final int[][] COLOR_SCHEMES =
      { { BLACK, WHITE }, { WHITE, BLACK }, { WHITE, BLUE } };

  private static final String CONTROLKEY_KEY = "controlkey";
  private int mControlKeyId = 0;

  private static final int[] CONTROL_KEY_SCHEMES =
      { KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_AT, KeyEvent.KEYCODE_ALT_LEFT,
        KeyEvent.KEYCODE_ALT_RIGHT };

  private static final String[] CONTROL_KEY_NAME = { "Ball", "@", "Left-Alt", "Right-Alt" };

  private int mControlKeyCode;

  private SharedPreferences mPreferences;

  private StringBuffer mBuffer = new StringBuffer();
  private InterpreterProcess mInterpreterProcess;
  private ScriptingLayerService mService;
  private int mProcessPort;

  private final ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mService = ((ScriptingLayerService.LocalBinder) service).getService();
      startTerminal();
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mService = null;
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    bindService(new Intent(this, ScriptingLayerService.class), mConnection, 0);

    // TODO(damonkohler): Until we are able to save and return state, it's better to just die.
    if (savedInstanceState != null) {
      Log.e("Attempted to restore previous state. Aborting.");
      finish();
      return;
    }
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mProcessPort = getIntent().getIntExtra(Constants.EXTRA_PROXY_PORT, 0);
    if (mProcessPort == 0) {
      Log.e(this, "No proxy port specified.");
      finish();
      return;
    }
    Analytics.trackActivity(this);
  }

  private void startTerminal() {
    mInterpreterProcess = mService.getProcess(mProcessPort);

    if (mInterpreterProcess == null) {
      Log.e(String.format("Process (%d) does not exist.", mProcessPort));
      finish();
      return;
    }

    setContentView(R.layout.term);
    if (mBuffer.length() != 0) {
      mInterpreterProcess.print(mBuffer.toString());
      mBuffer.setLength(0);
    }
    mEmulatorView = (EmulatorView) findViewById(EMULATOR_VIEW);
    mEmulatorView.attachProcess(mInterpreterProcess);
    mEmulatorView.setOnPollingThreadExit(new Runnable() {
      @Override
      public void run() {
        Terminal.this.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            hideKeyboard();
            Toast.makeText(Terminal.this, mInterpreterProcess.getName() + " exited.",
                Toast.LENGTH_SHORT).show();
          }
        });
      }
    });
    mKeyListener = new TermKeyListener();
    updatePreferences();
  }

  private void updatePreferences() {
    mEmulatorView.setTextSize(readIntPref(FONTSIZE_KEY, DEFAULT_FONT_SIZE, 30));
    int[] scheme =
        COLOR_SCHEMES[readIntPref(COLOR_KEY, DEFAULT_COLOR_SCHEME, COLOR_SCHEMES.length - 1)];
    mEmulatorView.setColors(scheme[0], scheme[1]);
    mControlKeyId = readIntPref(CONTROLKEY_KEY, mControlKeyId, CONTROL_KEY_SCHEMES.length - 1);
    mControlKeyCode = CONTROL_KEY_SCHEMES[mControlKeyId];
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
  public void onResume() {
    super.onResume();
    if (mInterpreterProcess != null && !mInterpreterProcess.isAlive()) {
      hideKeyboard();
    }
    // Typically, onResume is called after we update our preferences.
    if (mEmulatorView != null) {
      updatePreferences();
      mEmulatorView.update();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mEmulatorView.update();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (handleControlKey(keyCode, true)) {
      return true;
    } else if (event.isSystem()) {
      // Don't intercept the system keys.
      return super.onKeyDown(keyCode, event);
    } else if (handleDPad(keyCode, true)) {
      return true;
    }

    // Translate the keyCode into an ASCII character.
    int letter = mKeyListener.keyDown(keyCode, event);
    if (letter > -1) {
      print((char) letter);
    }
    return true;
  }

  @Override
  public boolean onKeyUp(int keyCode, KeyEvent event) {
    if (handleControlKey(keyCode, false)) {
      return true;
    } else if (event.isSystem()) {
      // Don't intercept the system keys.
      return super.onKeyUp(keyCode, event);
    } else if (handleDPad(keyCode, false)) {
      return true;
    }
    mKeyListener.keyUp(keyCode);
    return true;
  }

  private boolean handleControlKey(int keyCode, boolean down) {
    if (keyCode == mControlKeyCode) {
      mKeyListener.handleControlKey(down);
      return true;
    }
    return false;
  }

  private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(mEmulatorView.getWindowToken(), 0);
  }

  /**
   * Handle dpad left-right-up-down events. Don't handle dpad-center, that's our control key.
   * 
   * @param keyCode
   * @param down
   */
  private boolean handleDPad(int keyCode, boolean down) {
    if (keyCode < KeyEvent.KEYCODE_DPAD_UP || keyCode > KeyEvent.KEYCODE_DPAD_CENTER) {
      // keyCode does not correspond to the dpad.
      return false;
    }

    if (down) {
      if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
        // TODO(damonkohler): If center is our control key, why are we printing \r?
        print('\r');
      } else {
        char code;
        switch (keyCode) {
        case KeyEvent.KEYCODE_DPAD_UP:
          code = 'A';
          break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
          code = 'B';
          break;
        case KeyEvent.KEYCODE_DPAD_LEFT:
          code = 'D';
          break;
        default:
        case KeyEvent.KEYCODE_DPAD_RIGHT:
          code = 'C';
          break;
        }
        print((char) 27); // ESC
        if (mEmulatorView.getKeypadApplicationMode()) {
          print('O');
        } else {
          print('[');
        }
        print(code);
      }
    }
    return true;
  }

  private void print(char c) {
    if (mInterpreterProcess != null) {
      mInterpreterProcess.print(c);
    } else {
      mBuffer.append(c);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.terminal, menu);
    if (mInterpreterProcess instanceof ScriptProcess) {
      menu.removeItem(R.id.terminal_menu_exit_and_edit);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.terminal_menu_preferences:
      doPreferences();
      break;
    case R.id.terminal_menu_send_email:
      doEmailTranscript();
      break;
    case R.id.terminal_menu_special_keys:
      doDocumentKeys();
      break;
    case R.id.terminal_menu_exit_and_edit:
      Intent intent = new Intent(Constants.ACTION_EDIT_SCRIPT);
      intent.putExtra(Constants.EXTRA_SCRIPT_NAME, mInterpreterProcess.getName());
      startActivity(intent);
      finish();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  private void doPreferences() {
    startActivity(new Intent(this, Preferences.class));
  }

  private void doEmailTranscript() {
    // Don't really want to supply an address, but currently it's required, otherwise we get an
    // exception.
    String addr = "user@example.com";
    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + addr));
    intent.putExtra("body", mEmulatorView.getTranscriptText());
    startActivity(intent);
  }

  private void doDocumentKeys() {
    String controlKey = CONTROL_KEY_NAME[mControlKeyId];
    new AlertDialog.Builder(this).setTitle("Press " + controlKey + " and Key").setMessage(
        controlKey + " Space ==> Control-@ (NUL)\n" + controlKey + " A..Z ==> Control-A..Z\n"
            + controlKey + " 1 ==> Control-[ (ESC)\n" + controlKey + " 5 ==> Control-_\n"
            + controlKey + " . ==> Control-\\\n" + controlKey + " 0 ==> Control-]\n" + controlKey
            + " 6 ==> Control-^").show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(mConnection);
  }
}
