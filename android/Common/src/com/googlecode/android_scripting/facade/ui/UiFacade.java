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

package com.googlecode.android_scripting.facade.ui;

import android.app.ProgressDialog;
import android.app.Service;
import android.util.AndroidRuntimeException;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.FacadeManager;
import com.googlecode.android_scripting.interpreter.html.HtmlActivityTask;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * User Interface Facade. <br>
 * <br>
 * <b>Usage Notes</b><br>
 * <br>
 * The UI facade provides access to a selection of dialog boxes for general user interaction, and
 * also hosts the {@link #webViewShow} call which allows interactive use of html pages.<br>
 * The general use of the dialog functions is as follows:<br>
 * <ol>
 * <li>Create a dialog using one of the following calls:
 * <ul>
 * <li>{@link #dialogCreateInput}
 * <li>{@link #dialogCreateAlert}
 * <li>{@link #dialogCreateDatePicker}
 * <li>{@link #dialogCreateHorizontalProgress}
 * <li>{@link #dialogCreatePassword}
 * <li>{@link #dialogCreateSeekBar}
 * <li>{@link #dialogCreateSpinnerProgress}
 * </ul>
 * <li>Set additional features to your dialog
 * <ul>
 * <li>{@link #dialogSetItems} Set a list of items. Used like a menu.
 * <li>{@link #dialogSetMultiChoiceItems} Set a multichoice list of items.
 * <li>{@link #dialogSetSingleChoiceItems} Set a single choice list of items.
 * <li>{@link #dialogSetPositiveButtonText}
 * <li>{@link #dialogSetNeutralButtonText}
 * <li>{@link #dialogSetNegativeButtonText}
 * <li>{@link #dialogSetMaxProgress} Set max progress for your progress bar.
 * </ul>
 * <li>Display the dialog using {@link #dialogShow}
 * <li>Update dialog information if needed
 * <ul>
 * <li>{@link #dialogSetCurrentProgress}
 * </ul>
 * <li>Get the results
 * <ul>
 * <li>Using {@link #dialogGetResponse}, which will wait until the user performs an action to close
 * the dialog box, or
 * <li>Use eventPoll to wait for a "dialog" event.
 * <li>You can find out which list items were selected using {@link #dialogGetSelectedItems}, which
 * returns an array of numeric indices to your list. For a single choice list, there will only ever
 * be one of these.
 * </ul>
 * <li>Once done, use {@link #dialogDismiss} to remove the dialog.
 * </ol>
 * <br>
 * You can also manipulate menu options. The menu options are available for both {@link #dialogShow}
 * and {@link #fullShow}.
 * <ul>
 * <li>{@link #clearOptionsMenu}
 * <li>{@link #addOptionsMenuItem}
 * </ul>
 * <br>
 * <b>Some notes:</b><br>
 * Not every dialogSet function is relevant to every dialog type, ie, dialogSetMaxProgress obviously
 * only applies to dialogs created with a progress bar. Also, an Alert Dialog may have a message or
 * items, not both. If you set both, items will take priority.<br>
 * In addition to the above functions, {@link #dialogGetInput} and {@link #dialogGetPassword} are
 * convenience functions that create, display and return the relevant dialogs in one call.<br>
 * There is only ever one instance of a dialog. Any dialogCreate call will cause the existing dialog
 * to be destroyed.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class UiFacade extends RpcReceiver {
  // This value should not be used for menu groups outside this class.
  private static final int MENU_GROUP_ID = Integer.MAX_VALUE;

  private final Service mService;
  private final FutureActivityTaskExecutor mTaskQueue;
  private DialogTask mDialogTask;
  private FullScreenTask mFullScreenTask;

  private final List<UiMenuItem> mContextMenuItems;
  private final List<UiMenuItem> mOptionsMenuItems;
  private final AtomicBoolean mMenuUpdated;

  private final EventFacade mEventFacade;

  public UiFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mTaskQueue = ((BaseApplication) mService.getApplication()).getTaskExecutor();
    mContextMenuItems = new CopyOnWriteArrayList<UiMenuItem>();
    mOptionsMenuItems = new CopyOnWriteArrayList<UiMenuItem>();
    mEventFacade = manager.getReceiver(EventFacade.class);
    mMenuUpdated = new AtomicBoolean(false);
  }

  /**
   * For inputType, see <a
   * href="http://developer.android.com/reference/android/R.styleable.html#TextView_inputType"
   * >InputTypes</a>. Some useful ones are text, number, and textUri. Multiple flags can be
   * supplied, seperated by "|", ie: "textUri|textAutoComplete"
   */
  @Rpc(description = "Create a text input dialog.")
  public void dialogCreateInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Value") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message,
      @RpcParameter(name = "defaultText", description = "text to insert into the input box") @RpcOptional final String text,
      @RpcParameter(name = "inputType", description = "type of input data, ie number or text") @RpcOptional final String inputType)
      throws InterruptedException {
    dialogDismiss();
    mDialogTask = new AlertDialogTask(title, message);
    ((AlertDialogTask) mDialogTask).setTextInput(text);
    if (inputType != null) {
      ((AlertDialogTask) mDialogTask).setEditInputType(inputType);
    }
  }

  @Rpc(description = "Create a password input dialog.")
  public void dialogCreatePassword(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Password") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message) {
    dialogDismiss();
    mDialogTask = new AlertDialogTask(title, message);
    ((AlertDialogTask) mDialogTask).setPasswordInput();
  }

  /**
   * The result is the user's input, or None (null) if cancel was hit. <br>
   * Example (python)
   * 
   * <pre>
   * import android
   * droid=android.Android()
   * 
   * print droid.dialogGetInput("Title","Message","Default").result
   * </pre>
   * 
   */
  @SuppressWarnings("unchecked")
  @Rpc(description = "Queries the user for a text input.")
  public String dialogGetInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("Value") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message,
      @RpcParameter(name = "defaultText", description = "text to insert into the input box") @RpcOptional final String text)
      throws InterruptedException {
    dialogCreateInput(title, message, text, "text");
    dialogSetNegativeButtonText("Cancel");
    dialogSetPositiveButtonText("Ok");
    dialogShow();
    Map<String, Object> response = (Map<String, Object>) dialogGetResponse();
    if ("positive".equals(response.get("which"))) {
      return (String) response.get("value");
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  @Rpc(description = "Queries the user for a password.")
  public String dialogGetPassword(
      @RpcParameter(name = "title", description = "title of the password box") @RpcDefault("Password") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message)
      throws InterruptedException {
    dialogCreatePassword(title, message);
    dialogSetNegativeButtonText("Cancel");
    dialogSetPositiveButtonText("Ok");
    dialogShow();
    Map<String, Object> response = (Map<String, Object>) dialogGetResponse();
    if ("positive".equals(response.get("which"))) {
      return (String) response.get("value");
    } else {
      return null;
    }
  }

  @Rpc(description = "Create a spinner progress dialog.")
  public void dialogCreateSpinnerProgress(@RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message,
      @RpcParameter(name = "maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new ProgressDialogTask(ProgressDialog.STYLE_SPINNER, max, title, message, true);
  }

  @Rpc(description = "Create a horizontal progress dialog.")
  public void dialogCreateHorizontalProgress(
      @RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message,
      @RpcParameter(name = "maximum progress") @RpcDefault("100") Integer max) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask =
        new ProgressDialogTask(ProgressDialog.STYLE_HORIZONTAL, max, title, message, true);
  }

  /**
   * <b>Example (python)</b>
   * 
   * <pre>
   *   import android
   *   droid=android.Android()
   *   droid.dialogCreateAlert("I like swords.","Do you like swords?")
   *   droid.dialogSetPositiveButtonText("Yes")
   *   droid.dialogSetNegativeButtonText("No")
   *   droid.dialogShow()
   *   response=droid.dialogGetResponse().result
   *   droid.dialogDismiss()
   *   if response.has_key("which"):
   *     result=response["which"]
   *     if result=="positive":
   *       print "Yay! I like swords too!"
   *     elif result=="negative":
   *       print "Oh. How sad."
   *   elif response.has_key("canceled"): # Yes, I know it's mispelled.
   *     print "You can't even make up your mind?"
   *   else:
   *     print "Unknown response=",response
   * 
   *   print "Done"
   * </pre>
   */
  @Rpc(description = "Create alert dialog.")
  public void dialogCreateAlert(@RpcParameter(name = "title") @RpcOptional String title,
      @RpcParameter(name = "message") @RpcOptional String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new AlertDialogTask(title, message);
  }

  /**
   * Will produce "dialog" events on change, containing:
   * <ul>
   * <li>"progress" - Position chosen, between 0 and max
   * <li>"which" = "seekbar"
   * <li>"fromuser" = true/false change is from user input
   * </ul>
   * Response will contain a "progress" element.
   */
  @Rpc(description = "Create seek bar dialog.")
  public void dialogCreateSeekBar(
      @RpcParameter(name = "starting value") @RpcDefault("50") Integer progress,
      @RpcParameter(name = "maximum value") @RpcDefault("100") Integer max,
      @RpcParameter(name = "title") String title, @RpcParameter(name = "message") String message) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new SeekBarDialogTask(progress, max, title, message);
  }

  @Rpc(description = "Create time picker dialog.")
  public void dialogCreateTimePicker(
      @RpcParameter(name = "hour") @RpcDefault("0") Integer hour,
      @RpcParameter(name = "minute") @RpcDefault("0") Integer minute,
      @RpcParameter(name = "is24hour", description = "Use 24 hour clock") @RpcDefault("false") Boolean is24hour) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new TimePickerDialogTask(hour, minute, is24hour);
  }

  @Rpc(description = "Create date picker dialog.")
  public void dialogCreateDatePicker(@RpcParameter(name = "year") @RpcDefault("1970") Integer year,
      @RpcParameter(name = "month") @RpcDefault("1") Integer month,
      @RpcParameter(name = "day") @RpcDefault("1") Integer day) {
    dialogDismiss(); // Dismiss any existing dialog.
    mDialogTask = new DatePickerDialogTask(year, month, day);
  }

  @Rpc(description = "Dismiss dialog.")
  public void dialogDismiss() {
    if (mDialogTask != null) {
      mDialogTask.dismissDialog();
      mDialogTask = null;
    }
  }

  @Rpc(description = "Show dialog.")
  public void dialogShow() throws InterruptedException {
    if (mDialogTask != null && mDialogTask.getDialog() == null) {
      mDialogTask.setEventFacade(mEventFacade);
      mTaskQueue.execute(mDialogTask);
      mDialogTask.getShowLatch().await();
    } else {
      throw new RuntimeException("No dialog to show.");
    }
  }

  @Rpc(description = "Set progress dialog current value.")
  public void dialogSetCurrentProgress(@RpcParameter(name = "current") Integer current) {
    if (mDialogTask != null && mDialogTask instanceof ProgressDialogTask) {
      ((ProgressDialog) mDialogTask.getDialog()).setProgress(current);
    } else {
      throw new RuntimeException("No valid dialog to assign value to.");
    }
  }

  @Rpc(description = "Set progress dialog maximum value.")
  public void dialogSetMaxProgress(@RpcParameter(name = "max") Integer max) {
    if (mDialogTask != null && mDialogTask instanceof ProgressDialogTask) {
      ((ProgressDialog) mDialogTask.getDialog()).setMax(max);
    } else {
      throw new RuntimeException("No valid dialog to set maximum value of.");
    }
  }

  @Rpc(description = "Set alert dialog positive button text.")
  public void dialogSetPositiveButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setPositiveButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof SeekBarDialogTask) {
      ((SeekBarDialogTask) mDialogTask).setPositiveButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNegativeButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setNegativeButtonText(text);
    } else if (mDialogTask != null && mDialogTask instanceof SeekBarDialogTask) {
      ((SeekBarDialogTask) mDialogTask).setNegativeButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  @Rpc(description = "Set alert dialog button text.")
  public void dialogSetNeutralButtonText(@RpcParameter(name = "text") String text) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setNeutralButtonText(text);
    } else {
      throw new AndroidRuntimeException("No dialog to add button to.");
    }
  }

  // TODO(damonkohler): Make RPC layer translate between JSONArray and List<Object>.
  /**
   * This effectively creates list of options. Clicking on an item will immediately return an "item"
   * element, which is the index of the selected item.
   */
  @Rpc(description = "Set alert dialog list items.")
  public void dialogSetItems(@RpcParameter(name = "items") JSONArray items) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setItems(items);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  /**
   * This creates a list of radio buttons. You can select one item out of the list. A response will
   * not be returned until the dialog is closed, either with the Cancel key or a button
   * (positive/negative/neutral). Use {@link #dialogGetSelectedItems()} to find out what was
   * selected.
   */
  @Rpc(description = "Set dialog single choice items and selected item.")
  public void dialogSetSingleChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "selected item index") @RpcDefault("0") Integer selected) {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setSingleChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  /**
   * This creates a list of check boxes. You can select multiple items out of the list. A response
   * will not be returned until the dialog is closed, either with the Cancel key or a button
   * (positive/negative/neutral). Use {@link #dialogGetSelectedItems()} to find out what was
   * selected.
   */

  @Rpc(description = "Set dialog multiple choice items and selection.")
  public void dialogSetMultiChoiceItems(
      @RpcParameter(name = "items") JSONArray items,
      @RpcParameter(name = "selected", description = "list of selected items") @RpcOptional JSONArray selected)
      throws JSONException {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      ((AlertDialogTask) mDialogTask).setMultiChoiceItems(items, selected);
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  @Rpc(description = "Returns dialog response.")
  public Object dialogGetResponse() {
    try {
      return mDialogTask.getResult();
    } catch (Exception e) {
      throw new AndroidRuntimeException(e);
    }
  }

  @Rpc(description = "This method provides list of items user selected.", returns = "Selected items")
  public Set<Integer> dialogGetSelectedItems() {
    if (mDialogTask != null && mDialogTask instanceof AlertDialogTask) {
      return ((AlertDialogTask) mDialogTask).getSelectedItems();
    } else {
      throw new AndroidRuntimeException("No dialog to add list to.");
    }
  }

  /**
   * See <a href=http://code.google.com/p/android-scripting/wiki/UsingWebView>wiki page</a> for more
   * detail.
   */
  @Rpc(description = "Display a WebView with the given URL.")
  public void webViewShow(
      @RpcParameter(name = "url") String url,
      @RpcParameter(name = "wait", description = "block until the user exits the WebView") @RpcOptional Boolean wait)
      throws IOException {
    String jsonSrc = FileUtils.readFromAssetsFile(mService, HtmlInterpreter.JSON_FILE);
    String AndroidJsSrc = FileUtils.readFromAssetsFile(mService, HtmlInterpreter.ANDROID_JS_FILE);
    HtmlActivityTask task = new HtmlActivityTask(mManager, AndroidJsSrc, jsonSrc, url, false);
    mTaskQueue.execute(task);
    if (wait != null && wait) {
      try {
        task.getResult();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Context menus are used primarily with {@link #webViewShow}
   */
  @Rpc(description = "Adds a new item to context menu.")
  public void addContextMenuItem(
      @RpcParameter(name = "label", description = "label for this menu item") String label,
      @RpcParameter(name = "event", description = "event that will be generated on menu item click") String event,
      @RpcParameter(name = "eventData") @RpcOptional Object data) {
    mContextMenuItems.add(new UiMenuItem(label, event, data, null));
  }

  /**
   * <b>Example (python)</b>
   * 
   * <pre>
   * import android
   * droid=android.Android()
   * 
   * droid.addOptionsMenuItem("Silly","silly",None,"star_on")
   * droid.addOptionsMenuItem("Sensible","sensible","I bet.","star_off")
   * droid.addOptionsMenuItem("Off","off",None,"ic_menu_revert")
   * 
   * print "Hit menu to see extra options."
   * print "Will timeout in 10 seconds if you hit nothing."
   * 
   * while True: # Wait for events from the menu.
   *   response=droid.eventWait(10000).result
   *   if response==None:
   *     break
   *   print response
   *   if response["name"]=="off":
   *     break
   * print "And done."
   * 
   * </pre>
   */
  @Rpc(description = "Adds a new item to options menu.")
  public void addOptionsMenuItem(
      @RpcParameter(name = "label", description = "label for this menu item") String label,
      @RpcParameter(name = "event", description = "event that will be generated on menu item click") String event,
      @RpcParameter(name = "eventData") @RpcOptional Object data,
      @RpcParameter(name = "iconName", description = "Android system menu icon, see http://developer.android.com/reference/android/R.drawable.html") @RpcOptional String iconName) {
    mOptionsMenuItems.add(new UiMenuItem(label, event, data, iconName));
    mMenuUpdated.set(true);
  }

  @Rpc(description = "Removes all items previously added to context menu.")
  public void clearContextMenu() {
    mContextMenuItems.clear();
  }

  @Rpc(description = "Removes all items previously added to options menu.")
  public void clearOptionsMenu() {
    mOptionsMenuItems.clear();
    mMenuUpdated.set(true);
  }

  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    for (UiMenuItem item : mContextMenuItems) {
      MenuItem menuItem = menu.add(item.mmTitle);
      menuItem.setOnMenuItemClickListener(item.mmListener);
    }
  }

  public boolean onPrepareOptionsMenu(Menu menu) {
    if (mMenuUpdated.getAndSet(false)) {
      menu.removeGroup(MENU_GROUP_ID);
      for (UiMenuItem item : mOptionsMenuItems) {
        MenuItem menuItem = menu.add(MENU_GROUP_ID, Menu.NONE, Menu.NONE, item.mmTitle);
        if (item.mmIcon != null) {
          menuItem.setIcon(mService.getResources()
              .getIdentifier(item.mmIcon, "drawable", "android"));
        }
        menuItem.setOnMenuItemClickListener(item.mmListener);
      }
      return true;
    }
    return true;
  }

  /**
   * See <a href=http://code.google.com/p/android-scripting/wiki/FullScreenUI>wiki page</a> for more
   * detail.
   */
  @Rpc(description = "Show Full Screen.")
  public List<String> fullShow(
      @RpcParameter(name = "layout", description = "String containing View layout") String layout)
      throws InterruptedException {
    if (mFullScreenTask != null) {
      fullDismiss();
    }
    mFullScreenTask = new FullScreenTask(layout);
    mFullScreenTask.setEventFacade(mEventFacade);
    mFullScreenTask.setUiFacade(this);
    mTaskQueue.execute(mFullScreenTask);
    mFullScreenTask.getShowLatch().await();
    return mFullScreenTask.mInflater.getErrors();
  }

  @Rpc(description = "Dismiss Full Screen.")
  public void fullDismiss() {
    if (mFullScreenTask != null) {
      mFullScreenTask.finish();
      mFullScreenTask = null;
    }
  }

  @Rpc(description = "Get Fullscreen Properties")
  public Map<String, Map<String, String>> fullQuery() {
    if (mFullScreenTask == null) {
      throw new RuntimeException("No screen displayed.");
    }
    return mFullScreenTask.getViewAsMap();
  }

  @Rpc(description = "Get fullscreen properties for a specific widget")
  public Map<String, String> fullQueryDetail(
      @RpcParameter(name = "id", description = "id of layout widget") String id) {
    if (mFullScreenTask == null) {
      throw new RuntimeException("No screen displayed.");
    }
    return mFullScreenTask.getViewDetail(id);
  }

  @Rpc(description = "Set fullscreen widget property")
  public String fullSetProperty(
      @RpcParameter(name = "id", description = "id of layout widget") String id,
      @RpcParameter(name = "property", description = "name of property to set") String property,
      @RpcParameter(name = "value", description = "value to set property to") String value) {
    if (mFullScreenTask == null) {
      throw new RuntimeException("No screen displayed.");
    }
    return mFullScreenTask.setViewProperty(id, property, value);
  }

  @Rpc(description = "Attach a list to a fullscreen widget")
  public String fullSetList(
      @RpcParameter(name = "id", description = "id of layout widget") String id,
      @RpcParameter(name = "list", description = "List to set") JSONArray items) {
    if (mFullScreenTask == null) {
      throw new RuntimeException("No screen displayed.");
    }
    return mFullScreenTask.setList(id, items);
  }

  @Override
  public void shutdown() {
    fullDismiss();
    HtmlActivityTask.shutdown();
  }

  private class UiMenuItem {

    private final String mmTitle;
    private final String mmEvent;
    private final Object mmEventData;
    private final String mmIcon;
    private final MenuItem.OnMenuItemClickListener mmListener;

    public UiMenuItem(String title, String event, Object data, String icon) {
      mmTitle = title;
      mmEvent = event;
      mmEventData = data;
      mmIcon = icon;
      mmListener = new MenuItem.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
          // TODO(damonkohler): Does mmEventData need to be cloned somehow?
          mEventFacade.postEvent(mmEvent, mmEventData);
          return true;
        }
      };
    }
  }
}