/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @author modified by raaar
 *
 */

package org.connectbot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.ScriptProcess;
import com.googlecode.android_scripting.activity.Preferences;
import com.googlecode.android_scripting.activity.ScriptingLayerService;

import de.mud.terminal.VDUBuffer;
import de.mud.terminal.vt320;

import org.connectbot.service.PromptHelper;
import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;
import org.connectbot.util.PreferenceConstants;
import org.connectbot.util.SelectionArea;

public class ConsoleActivity extends Activity {

  protected static final int REQUEST_EDIT = 1;

  private static final int CLICK_TIME = 250;
  private static final float MAX_CLICK_DISTANCE = 25f;
  private static final int KEYBOARD_DISPLAY_TIME = 1250;

  // Direction to shift the ViewFlipper
  private static final int SHIFT_LEFT = 0;
  private static final int SHIFT_RIGHT = 1;

  protected ViewFlipper flip = null;
  protected TerminalManager manager = null;
  protected ScriptingLayerService mService = null;
  protected LayoutInflater inflater = null;

  private SharedPreferences prefs = null;

  private PowerManager.WakeLock wakelock = null;

  protected Integer processID;

  protected ClipboardManager clipboard;

  private RelativeLayout booleanPromptGroup;
  private TextView booleanPrompt;
  private Button booleanYes, booleanNo;

  private Animation slide_left_in, slide_left_out, slide_right_in, slide_right_out,
      fade_stay_hidden, fade_out_delayed;

  private Animation keyboard_fade_in, keyboard_fade_out;
  private ImageView keyboardButton;
  private float lastX, lastY;

  private int mTouchSlopSquare;

  private InputMethodManager inputManager;

  protected TerminalBridge copySource = null;
  private int lastTouchRow, lastTouchCol;

  private boolean forcedOrientation;

  private Handler handler = new Handler();

  private static enum MenuId {
    EDIT, PREFS, EMAIL, RESIZE, COPY, PASTE;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  private final ServiceConnection mConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      mService = ((ScriptingLayerService.LocalBinder) service).getService();
      manager = mService.getTerminalManager();
      // let manager know about our event handling services
      manager.setDisconnectHandler(disconnectHandler);

      Log.d(String.format("Connected to TerminalManager and found bridges.size=%d", manager
          .getBridgeList().size()));

      manager.setResizeAllowed(true);

      // clear out any existing bridges and record requested index
      flip.removeAllViews();

      int requestedIndex = 0;

      TerminalBridge requestedBridge = manager.getConnectedBridge(processID);

      // If we didn't find the requested connection, try opening it
      if (processID != null && requestedBridge == null) {
        try {
          Log.d(String.format(
              "We couldnt find an existing bridge with id = %d, so creating one now", processID));
          requestedBridge = manager.openConnection(processID);
        } catch (Exception e) {
          Log.e("Problem while trying to create new requested bridge", e);
        }
      }

      // create views for all bridges on this service
      for (TerminalBridge bridge : manager.getBridgeList()) {

        final int currentIndex = addNewTerminalView(bridge);

        // check to see if this bridge was requested
        if (bridge == requestedBridge) {
          requestedIndex = currentIndex;
        }
      }

      setDisplayedTerminal(requestedIndex);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      manager = null;
      mService = null;
    }
  };

  protected Handler promptHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      // someone below us requested to display a prompt
      updatePromptVisible();
    }
  };

  protected Handler disconnectHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      Log.d("Someone sending HANDLE_DISCONNECT to parentHandler");
      TerminalBridge bridge = (TerminalBridge) msg.obj;
      closeBridge(bridge);
    }
  };

  /**
   * @param bridge
   */
  private void closeBridge(final TerminalBridge bridge) {
    synchronized (flip) {
      final int flipIndex = getFlipIndex(bridge);

      if (flipIndex >= 0) {
        if (flip.getDisplayedChild() == flipIndex) {
          shiftCurrentTerminal(SHIFT_LEFT);
        }
        flip.removeViewAt(flipIndex);

        /*
         * TODO Remove this workaround when ViewFlipper is fixed to listen to view removals. Android
         * Issue 1784
         */
        final int numChildren = flip.getChildCount();
        if (flip.getDisplayedChild() >= numChildren && numChildren > 0) {
          flip.setDisplayedChild(numChildren - 1);
        }
      }

      // If we just closed the last bridge, go back to the previous activity.
      if (flip.getChildCount() == 0) {
        finish();
      }
    }
  }

  protected View findCurrentView(int id) {
    View view = flip.getCurrentView();
    if (view == null) {
      return null;
    }
    return view.findViewById(id);
  }

  protected PromptHelper getCurrentPromptHelper() {
    View view = findCurrentView(R.id.console_flip);
    if (!(view instanceof TerminalView)) {
      return null;
    }
    return ((TerminalView) view).bridge.getPromptHelper();
  }

  protected void hideAllPrompts() {
    booleanPromptGroup.setVisibility(View.GONE);
  }

  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    this.setContentView(R.layout.act_console);

    clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // hide status bar if requested by user
    if (prefs.getBoolean(PreferenceConstants.FULLSCREEN, false)) {
      getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
          WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    // TODO find proper way to disable volume key beep if it exists.
    setVolumeControlStream(AudioManager.STREAM_MUSIC);

    PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    wakelock = manager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, getPackageName());

    // handle requested console from incoming intent
    int id = getIntent().getIntExtra(Constants.EXTRA_PROXY_PORT, -1);

    if (id > 0) {
      processID = id;
    }

    inflater = LayoutInflater.from(this);

    flip = (ViewFlipper) findViewById(R.id.console_flip);
    booleanPromptGroup = (RelativeLayout) findViewById(R.id.console_boolean_group);
    booleanPrompt = (TextView) findViewById(R.id.console_prompt);

    booleanYes = (Button) findViewById(R.id.console_prompt_yes);
    booleanYes.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PromptHelper helper = getCurrentPromptHelper();
        if (helper == null) {
          return;
        }
        helper.setResponse(Boolean.TRUE);
        updatePromptVisible();
      }
    });

    booleanNo = (Button) findViewById(R.id.console_prompt_no);
    booleanNo.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        PromptHelper helper = getCurrentPromptHelper();
        if (helper == null) {
          return;
        }
        helper.setResponse(Boolean.FALSE);
        updatePromptVisible();
      }
    });

    // preload animations for terminal switching
    slide_left_in = AnimationUtils.loadAnimation(this, R.anim.slide_left_in);
    slide_left_out = AnimationUtils.loadAnimation(this, R.anim.slide_left_out);
    slide_right_in = AnimationUtils.loadAnimation(this, R.anim.slide_right_in);
    slide_right_out = AnimationUtils.loadAnimation(this, R.anim.slide_right_out);

    fade_out_delayed = AnimationUtils.loadAnimation(this, R.anim.fade_out_delayed);
    fade_stay_hidden = AnimationUtils.loadAnimation(this, R.anim.fade_stay_hidden);

    // Preload animation for keyboard button
    keyboard_fade_in = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_in);
    keyboard_fade_out = AnimationUtils.loadAnimation(this, R.anim.keyboard_fade_out);

    inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    keyboardButton = (ImageView) findViewById(R.id.keyboard_button);
    keyboardButton.setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        View flip = findCurrentView(R.id.console_flip);
        if (flip == null) {
          return;
        }

        inputManager.showSoftInput(flip, InputMethodManager.SHOW_FORCED);
        keyboardButton.setVisibility(View.GONE);
      }
    });
    if (prefs.getBoolean(PreferenceConstants.HIDE_KEYBOARD, false)) {
      // Force hidden keyboard.
      getWindow().setSoftInputMode(
          WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
              | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
    final ViewConfiguration configuration = ViewConfiguration.get(this);
    int touchSlop = configuration.getScaledTouchSlop();
    mTouchSlopSquare = touchSlop * touchSlop;

    // detect fling gestures to switch between terminals
    final GestureDetector detect =
        new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
          private float totalY = 0;

          @Override
          public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            final float distx = e2.getRawX() - e1.getRawX();
            final float disty = e2.getRawY() - e1.getRawY();
            final int goalwidth = flip.getWidth() / 2;

            // need to slide across half of display to trigger console change
            // make sure user kept a steady hand horizontally
            if (Math.abs(disty) < (flip.getHeight() / 4)) {
              if (distx > goalwidth) {
                shiftCurrentTerminal(SHIFT_RIGHT);
                return true;
              }

              if (distx < -goalwidth) {
                shiftCurrentTerminal(SHIFT_LEFT);
                return true;
              }

            }

            return false;
          }

          @Override
          public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

            // if copying, then ignore
            if (copySource != null && copySource.isSelectingForCopy()) {
              return false;
            }

            if (e1 == null || e2 == null) {
              return false;
            }

            // if releasing then reset total scroll
            if (e2.getAction() == MotionEvent.ACTION_UP) {
              totalY = 0;
            }

            // activate consider if within x tolerance
            if (Math.abs(e1.getX() - e2.getX()) < ViewConfiguration.getTouchSlop() * 4) {

              View flip = findCurrentView(R.id.console_flip);
              if (flip == null) {
                return false;
              }
              TerminalView terminal = (TerminalView) flip;

              // estimate how many rows we have scrolled through
              // accumulate distance that doesn't trigger immediate scroll
              totalY += distanceY;
              final int moved = (int) (totalY / terminal.bridge.charHeight);

              VDUBuffer buffer = terminal.bridge.getVDUBuffer();

              // consume as scrollback only if towards right half of screen
              if (e2.getX() > flip.getWidth() / 2) {
                if (moved != 0) {
                  int base = buffer.getWindowBase();
                  buffer.setWindowBase(base + moved);
                  totalY = 0;
                  return true;
                }
              } else {
                // otherwise consume as pgup/pgdown for every 5 lines
                if (moved > 5) {
                  ((vt320) buffer).keyPressed(vt320.KEY_PAGE_DOWN, ' ', 0);
                  terminal.bridge.tryKeyVibrate();
                  totalY = 0;
                  return true;
                } else if (moved < -5) {
                  ((vt320) buffer).keyPressed(vt320.KEY_PAGE_UP, ' ', 0);
                  terminal.bridge.tryKeyVibrate();
                  totalY = 0;
                  return true;
                }

              }

            }

            return false;
          }

        });

    flip.setOnCreateContextMenuListener(this);

    flip.setOnTouchListener(new OnTouchListener() {

      public boolean onTouch(View v, MotionEvent event) {

        // when copying, highlight the area
        if (copySource != null && copySource.isSelectingForCopy()) {
          int row = (int) Math.floor(event.getY() / copySource.charHeight);
          int col = (int) Math.floor(event.getX() / copySource.charWidth);

          SelectionArea area = copySource.getSelectionArea();

          switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            // recording starting area
            if (area.isSelectingOrigin()) {
              area.setRow(row);
              area.setColumn(col);
              lastTouchRow = row;
              lastTouchCol = col;
              copySource.redraw();
            }
            return true;
          case MotionEvent.ACTION_MOVE:
            /*
             * ignore when user hasn't moved since last time so we can fine-tune with directional
             * pad
             */
            if (row == lastTouchRow && col == lastTouchCol) {
              return true;
            }
            // if the user moves, start the selection for other corner
            area.finishSelectingOrigin();

            // update selected area
            area.setRow(row);
            area.setColumn(col);
            lastTouchRow = row;
            lastTouchCol = col;
            copySource.redraw();
            return true;
          case MotionEvent.ACTION_UP:
            /*
             * If they didn't move their finger, maybe they meant to select the rest of the text
             * with the directional pad.
             */
            if (area.getLeft() == area.getRight() && area.getTop() == area.getBottom()) {
              return true;
            }

            // copy selected area to clipboard
            String copiedText = area.copyFrom(copySource.getVDUBuffer());

            clipboard.setText(copiedText);
            Toast.makeText(ConsoleActivity.this,
                getString(R.string.terminal_copy_done, copiedText.length()), Toast.LENGTH_LONG)
                .show();
            // fall through to clear state

          case MotionEvent.ACTION_CANCEL:
            // make sure we clear any highlighted area
            area.reset();
            copySource.setSelectingForCopy(false);
            copySource.redraw();
            return true;
          }
        }

        Configuration config = getResources().getConfiguration();

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          lastX = event.getX();
          lastY = event.getY();
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
          final int deltaX = (int) (lastX - event.getX());
          final int deltaY = (int) (lastY - event.getY());
          int distance = (deltaX * deltaX) + (deltaY * deltaY);
          if (distance > mTouchSlopSquare) {
            // If currently scheduled long press event is not canceled here,
            // GestureDetector.onScroll is executed, which takes a while, and by the time we are
            // back in the view's dispatchTouchEvent
            // mPendingCheckForLongPress is already executed
            flip.cancelLongPress();
          }
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
          // Same as above, except now GestureDetector.onFling is called.
          flip.cancelLongPress();
          if (config.hardKeyboardHidden != Configuration.KEYBOARDHIDDEN_NO
              && keyboardButton.getVisibility() == View.GONE
              && event.getEventTime() - event.getDownTime() < CLICK_TIME
              && Math.abs(event.getX() - lastX) < MAX_CLICK_DISTANCE
              && Math.abs(event.getY() - lastY) < MAX_CLICK_DISTANCE) {
            keyboardButton.startAnimation(keyboard_fade_in);
            keyboardButton.setVisibility(View.VISIBLE);

            handler.postDelayed(new Runnable() {
              public void run() {
                if (keyboardButton.getVisibility() == View.GONE) {
                  return;
                }

                keyboardButton.startAnimation(keyboard_fade_out);
                keyboardButton.setVisibility(View.GONE);
              }
            }, KEYBOARD_DISPLAY_TIME);

            return false;
          }
        }
        // pass any touch events back to detector
        return detect.onTouchEvent(event);
      }

    });

  }

  private void configureOrientation() {
    String rotateDefault;
    if (getResources().getConfiguration().keyboard == Configuration.KEYBOARD_NOKEYS) {
      rotateDefault = PreferenceConstants.ROTATION_PORTRAIT;
    } else {
      rotateDefault = PreferenceConstants.ROTATION_LANDSCAPE;
    }

    String rotate = prefs.getString(PreferenceConstants.ROTATION, rotateDefault);
    if (PreferenceConstants.ROTATION_DEFAULT.equals(rotate)) {
      rotate = rotateDefault;
    }

    // request a forced orientation if requested by user
    if (PreferenceConstants.ROTATION_LANDSCAPE.equals(rotate)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
      forcedOrientation = true;
    } else if (PreferenceConstants.ROTATION_PORTRAIT.equals(rotate)) {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      forcedOrientation = true;
    } else {
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
      forcedOrientation = false;
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);
    getMenuInflater().inflate(R.menu.terminal, menu);
    menu.setQwertyMode(true);
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    setVolumeControlStream(AudioManager.STREAM_NOTIFICATION);
    TerminalBridge bridge = ((TerminalView) findCurrentView(R.id.console_flip)).bridge;
    boolean sessionOpen = bridge.isSessionOpen();
    menu.findItem(R.id.terminal_menu_resize).setEnabled(sessionOpen);
    if (bridge.getProcess() instanceof ScriptProcess) {
      menu.findItem(R.id.terminal_menu_exit_and_edit).setEnabled(true);
    }
    bridge.onPrepareOptionsMenu(menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.terminal_menu_resize:
      doResize();
      break;
    case R.id.terminal_menu_preferences:
      doPreferences();
      break;
    case R.id.terminal_menu_send_email:
      doEmailTranscript();
      break;
    case R.id.terminal_menu_exit_and_edit:
      TerminalView terminalView = (TerminalView) findCurrentView(R.id.console_flip);
      TerminalBridge bridge = terminalView.bridge;
      if (manager != null) {
        manager.closeConnection(bridge, true);
      } else {
        Intent intent = new Intent(this, ScriptingLayerService.class);
        intent.setAction(Constants.ACTION_KILL_PROCESS);
        intent.putExtra(Constants.EXTRA_PROXY_PORT, bridge.getId());
        startService(intent);
        Message.obtain(disconnectHandler, -1, bridge).sendToTarget();
      }
      Intent intent = new Intent(Constants.ACTION_EDIT_SCRIPT);
      ScriptProcess process = (ScriptProcess) bridge.getProcess();
      intent.putExtra(Constants.EXTRA_SCRIPT_PATH, process.getPath());
      startActivity(intent);
      finish();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onOptionsMenuClosed(Menu menu) {
    super.onOptionsMenuClosed(menu);
    setVolumeControlStream(AudioManager.STREAM_MUSIC);
  }

  private void doResize() {
    closeOptionsMenu();
    final TerminalView terminalView = (TerminalView) findCurrentView(R.id.console_flip);
    final View resizeView = inflater.inflate(R.layout.dia_resize, null, false);
    new AlertDialog.Builder(ConsoleActivity.this).setView(resizeView).setPositiveButton(
        R.string.button_resize, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            int width, height;
            try {
              width =
                  Integer.parseInt(((EditText) resizeView.findViewById(R.id.width)).getText()
                      .toString());
              height =
                  Integer.parseInt(((EditText) resizeView.findViewById(R.id.height)).getText()
                      .toString());
            } catch (NumberFormatException nfe) {
              return;
            }
            terminalView.forceSize(width, height);
          }
        }).setNegativeButton(android.R.string.cancel, null).create().show();
  }

  private void doPreferences() {
    startActivity(new Intent(this, Preferences.class));
  }

  private void doEmailTranscript() {
    // Don't really want to supply an address, but currently it's required,
    // otherwise we get an exception.
    TerminalView terminalView = (TerminalView) findCurrentView(R.id.console_flip);
    TerminalBridge bridge = terminalView.bridge;
    // TODO(raaar): Replace with process log.
    VDUBuffer buffer = bridge.getVDUBuffer();
    int height = buffer.getRows();
    int width = buffer.getColumns();
    StringBuilder string = new StringBuilder();
    for (int i = 0; i < height; i++) {
      for (int j = 0; j < width; j++) {
        string.append(buffer.getChar(j, i));
      }
    }
    String addr = "user@example.com";
    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + addr));
    intent.putExtra("body", string.toString().trim());
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    TerminalBridge bridge = ((TerminalView) findCurrentView(R.id.console_flip)).bridge;
    boolean sessionOpen = bridge.isSessionOpen();
    menu.add(Menu.NONE, MenuId.COPY.getId(), Menu.NONE, R.string.terminal_menu_copy);
    if (clipboard.hasText() && sessionOpen) {
      menu.add(Menu.NONE, MenuId.PASTE.getId(), Menu.NONE, R.string.terminal_menu_paste);
    }
    bridge.onCreateContextMenu(menu, view, menuInfo);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == MenuId.COPY.getId()) {
      TerminalView terminalView = (TerminalView) findCurrentView(R.id.console_flip);
      copySource = terminalView.bridge;
      SelectionArea area = copySource.getSelectionArea();
      area.reset();
      area.setBounds(copySource.getVDUBuffer().getColumns(), copySource.getVDUBuffer().getRows());
      copySource.setSelectingForCopy(true);
      // Make sure we show the initial selection
      copySource.redraw();
      Toast.makeText(ConsoleActivity.this, getString(R.string.terminal_copy_start),
          Toast.LENGTH_LONG).show();
      return true;
    } else if (itemId == MenuId.PASTE.getId()) {
      TerminalView terminalView = (TerminalView) findCurrentView(R.id.console_flip);
      TerminalBridge bridge = terminalView.bridge;
      // pull string from clipboard and generate all events to force down
      String clip = clipboard.getText().toString();
      bridge.injectString(clip);
      return true;
    }
    return false;
  }

  @Override
  public void onStart() {
    super.onStart();
    // connect with manager service to find all bridges
    // when connected it will insert all views
    bindService(new Intent(this, ScriptingLayerService.class), mConnection, 0);
  }

  @Override
  public void onPause() {
    super.onPause();
    Log.d("onPause called");

    // Allow the screen to dim and fall asleep.
    if (wakelock != null && wakelock.isHeld()) {
      wakelock.release();
    }

    if (forcedOrientation && manager != null) {
      manager.setResizeAllowed(false);
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.d("onResume called");

    // Make sure we don't let the screen fall asleep.
    // This also keeps the Wi-Fi chipset from disconnecting us.
    if (wakelock != null && prefs.getBoolean(PreferenceConstants.KEEP_ALIVE, true)) {
      wakelock.acquire();
    }

    configureOrientation();

    if (forcedOrientation && manager != null) {
      manager.setResizeAllowed(true);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see android.app.Activity#onNewIntent(android.content.Intent)
   */
  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);

    Log.d("onNewIntent called");

    int id = intent.getIntExtra(Constants.EXTRA_PROXY_PORT, -1);

    if (id > 0) {
      processID = id;
    }

    if (processID == null) {
      Log.e("Got null intent data in onNewIntent()");
      return;
    }

    if (manager == null) {
      Log.e("We're not bound in onNewIntent()");
      return;
    }

    TerminalBridge requestedBridge = manager.getConnectedBridge(processID);
    int requestedIndex = 0;

    synchronized (flip) {
      if (requestedBridge == null) {
        // If we didn't find the requested connection, try opening it

        try {
          Log.d(String.format("We couldnt find an existing bridge with id = %d,"
              + "so creating one now", processID));
          requestedBridge = manager.openConnection(processID);
        } catch (Exception e) {
          Log.e("Problem while trying to create new requested bridge", e);
        }

        requestedIndex = addNewTerminalView(requestedBridge);
      } else {
        final int flipIndex = getFlipIndex(requestedBridge);
        if (flipIndex > requestedIndex) {
          requestedIndex = flipIndex;
        }
      }

      setDisplayedTerminal(requestedIndex);
    }
  }

  @Override
  public void onStop() {
    super.onStop();
    unbindService(mConnection);
  }

  protected void shiftCurrentTerminal(final int direction) {
    View overlay;
    synchronized (flip) {
      boolean shouldAnimate = flip.getChildCount() > 1;

      // Only show animation if there is something else to go to.
      if (shouldAnimate) {
        // keep current overlay from popping up again
        overlay = findCurrentView(R.id.terminal_overlay);
        if (overlay != null) {
          overlay.startAnimation(fade_stay_hidden);
        }

        if (direction == SHIFT_LEFT) {
          flip.setInAnimation(slide_left_in);
          flip.setOutAnimation(slide_left_out);
          flip.showNext();
        } else if (direction == SHIFT_RIGHT) {
          flip.setInAnimation(slide_right_in);
          flip.setOutAnimation(slide_right_out);
          flip.showPrevious();
        }
      }

      if (shouldAnimate) {
        // show overlay on new slide and start fade
        overlay = findCurrentView(R.id.terminal_overlay);
        if (overlay != null) {
          overlay.startAnimation(fade_out_delayed);
        }
      }

      updatePromptVisible();
    }
  }

  /**
   * Show any prompts requested by the currently visible {@link TerminalView}.
   */
  protected void updatePromptVisible() {
    // check if our currently-visible terminalbridge is requesting any prompt services
    View view = findCurrentView(R.id.console_flip);

    // Hide all the prompts in case a prompt request was canceled
    hideAllPrompts();

    if (!(view instanceof TerminalView)) {
      // we dont have an active view, so hide any prompts
      return;
    }

    PromptHelper prompt = ((TerminalView) view).bridge.getPromptHelper();

    if (Boolean.class.equals(prompt.promptRequested)) {
      booleanPromptGroup.setVisibility(View.VISIBLE);
      booleanPrompt.setText(prompt.promptHint);
      booleanYes.requestFocus();
    } else {
      hideAllPrompts();
      view.requestFocus();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    Log.d(String.format(
        "onConfigurationChanged; requestedOrientation=%d, newConfig.orientation=%d",
        getRequestedOrientation(), newConfig.orientation));
    if (manager != null) {
      if (forcedOrientation
          && (newConfig.orientation != Configuration.ORIENTATION_LANDSCAPE && getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
          || (newConfig.orientation != Configuration.ORIENTATION_PORTRAIT && getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)) {
        manager.setResizeAllowed(false);
      } else {
        manager.setResizeAllowed(true);
      }

      manager
          .setHardKeyboardHidden(newConfig.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_YES);
    }
  }

  /**
   * Adds a new TerminalBridge to the current set of views in our ViewFlipper.
   * 
   * @param bridge
   *          TerminalBridge to add to our ViewFlipper
   * @return the child index of the new view in the ViewFlipper
   */
  private int addNewTerminalView(TerminalBridge bridge) {
    // let them know about our prompt handler services
    bridge.getPromptHelper().setHandler(promptHandler);

    // inflate each terminal view
    RelativeLayout view = (RelativeLayout) inflater.inflate(R.layout.item_terminal, flip, false);

    // set the terminal overlay text
    TextView overlay = (TextView) view.findViewById(R.id.terminal_overlay);
    overlay.setText(bridge.getName());

    // and add our terminal view control, using index to place behind overlay
    TerminalView terminal = new TerminalView(ConsoleActivity.this, bridge);
    terminal.setId(R.id.console_flip);
    view.addView(terminal, 0);

    synchronized (flip) {
      // finally attach to the flipper
      flip.addView(view);
      return flip.getChildCount() - 1;
    }
  }

  private int getFlipIndex(TerminalBridge bridge) {
    synchronized (flip) {
      final int children = flip.getChildCount();
      for (int i = 0; i < children; i++) {
        final View view = flip.getChildAt(i).findViewById(R.id.console_flip);

        if (view == null || !(view instanceof TerminalView)) {
          // How did that happen?
          continue;
        }

        final TerminalView tv = (TerminalView) view;

        if (tv.bridge == bridge) {
          return i;
        }
      }
    }

    return -1;
  }

  /**
   * Displays the child in the ViewFlipper at the requestedIndex and updates the prompts.
   * 
   * @param requestedIndex
   *          the index of the terminal view to display
   */
  private void setDisplayedTerminal(int requestedIndex) {
    synchronized (flip) {
      try {
        // show the requested bridge if found, also fade out overlay
        flip.setDisplayedChild(requestedIndex);
        flip.getCurrentView().findViewById(R.id.terminal_overlay).startAnimation(fade_out_delayed);
      } catch (NullPointerException npe) {
        Log.d("View went away when we were about to display it", npe);
      }
      updatePromptVisible();
    }
  }
}
