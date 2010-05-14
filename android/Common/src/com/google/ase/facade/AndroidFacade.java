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

package com.google.ase.facade;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Queue;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Contacts.People;
import android.text.ClipboardManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.activity.AseServiceHelper;
import com.google.ase.exception.AseRuntimeException;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureResult;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

public class AndroidFacade implements RpcReceiver {
  /**
   * An instance of this interface is passed to the facade. From this object, the resource IDs can
   * be obtained.
   */
  public interface Resources {
    int getAseLogo48();
  }

  private final Service mService;
  private final Handler mHandler = new Handler();
  private final Intent mIntent;
  private final Queue<FutureActivityTask> mTaskQueue;

  private final Vibrator mVibrator;
  private final NotificationManager mNotificationManager;

  private final Resources mResources;

  @Override
  public void shutdown() {
  }

  /**
   * Creates a new AndroidFacade that simplifies the interface to various Android APIs.
   * 
   * @param service
   *          is the {@link Context} the APIs will run under
   */
  public AndroidFacade(Service service, Intent intent, Resources resources) {
    mService = service;
    mIntent = intent;
    mTaskQueue = ((AseApplication) mService.getApplication()).getTaskQueue();
    mVibrator = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
    mNotificationManager =
        (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    mResources = resources;
  }

  @Rpc(description = "Put text in the clipboard.")
  public void setClipboard(@RpcParameter(name = "text") String text) {
    ClipboardManager clipboard =
        (ClipboardManager) mService.getSystemService(Context.CLIPBOARD_SERVICE);
    clipboard.setText(text);
  }

  @Rpc(description = "Read text from the clipboard.", returns = "The text in the clipboard.")
  public String getClipboard() {
    ClipboardManager clipboard =
        (ClipboardManager) mService.getSystemService(Context.CLIPBOARD_SERVICE);
    return clipboard.getText().toString();
  }

  Intent startActivityForResult(final Intent intent) {
    FutureActivityTask task = new FutureActivityTask() {
      @Override
      public void run(Activity activity, FutureResult result) {
        // TODO(damonkohler): Throwing an exception here (e.g. specifying a non-existent activity)
        // causes a force close. There needs to be a way to pass back an error condition from the
        // helper.
        activity.startActivityForResult(intent, 0);
      }
    };
    mTaskQueue.offer(task);
    try {
      launchHelper();
    } catch (Exception e) {
      AseLog.e("Failed to launch intent.", e);
    }
    FutureResult result = task.getResult();
    try {
      return (Intent) result.get();
    } catch (Exception e) {
      throw new AseRuntimeException(e);
    }
  }

  private void launchHelper() {
    Intent helper = new Intent(mService, AseServiceHelper.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mService.startActivity(helper);
  }

  @Rpc(description = "Starts an activity for result and returns the result.", returns = "A Map representation of the result Intent.")
  public Intent startActivityForResult(
      @RpcParameter(name = "action") String action,
      @RpcParameter(name = "uri") @RpcOptional String uri,
      @RpcParameter(name = "type", description = "MIME type/subtype of the URI") @RpcOptional String type) {
    Intent intent = new Intent(action);
    intent.setDataAndType(Uri.parse(uri), type);
    return startActivityForResult(intent);
  }

  @Rpc(description = "Display content to be picked by URI (e.g. contacts)", returns = "A map of result values.")
  public Intent pick(@RpcParameter(name = "uri") String uri) {
    return startActivityForResult(Intent.ACTION_PICK, uri, null);
  }

  void startActivity(final Intent intent) {
    try {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mService.startActivity(intent);
    } catch (Exception e) {
      AseLog.e("Failed to launch intent.", e);
    }
  }

  @Rpc(description = "Starts an activity.")
  public void startActivity(
      @RpcParameter(name = "action") String action,
      @RpcParameter(name = "uri") @RpcOptional String uri,
      @RpcParameter(name = "type", description = "MIME type/subtype of the URI") @RpcOptional String type) {
    Intent intent = new Intent(action);
    intent.setDataAndType(Uri.parse(uri), type);
    startActivity(intent);
  }

  @Rpc(description = "Start activity with view action by URI (i.e. browser, contacts, etc.).")
  public void view(
      @RpcParameter(name = "uri") String uri,
      @RpcParameter(name = "type", description = "MIME type/subtype of the URI") @RpcOptional String type) {
    startActivity(Intent.ACTION_VIEW, uri, type);
  }

  @Rpc(description = "Opens the browser to display a local HTML file.")
  public void viewHtml(
      @RpcParameter(name = "path", description = "the path to the HTML file") String path) {
    File html = new File(path);
    view(html.toURI().toString(), "text/html");
  }

  @Rpc(description = "Vibrates the phone or a specified duration in milliseconds.")
  public void vibrate(
      @RpcParameter(name = "duration", description = "duration in milliseconds") @RpcDefault("300") Integer duration) {
    mVibrator.vibrate(duration);
  }

  @Rpc(description = "Displays a short-duration Toast notification.")
  public void makeToast(@RpcParameter(name = "message") final String message) {
    mHandler.post(new Runnable() {
      public void run() {
        Toast.makeText(mService, message, Toast.LENGTH_SHORT).show();
      }
    });
  }

  private String getInputFromAlertDialog(final String title, final String message,
      final boolean password) {
    FutureActivityTask task = new FutureActivityTask() {
      @Override
      public void run(final Activity activity, final FutureResult result) {
        final EditText input = new EditText(activity);
        if (password) {
          input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
          input.setTransformationMethod(new PasswordTransformationMethod());
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            result.set(input.getText().toString());
            activity.finish();
          }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface arg0) {
            result.set(null);
            activity.finish();
          }
        });
        alert.show();
      }
    };
    mTaskQueue.offer(task);

    try {
      launchHelper();
    } catch (Exception e) {
      AseLog.e("Failed to launch intent.", e);
    }

    FutureResult result = task.getResult();
    try {
      if (result.get() == null) {
        return null;
      } else {
        return (String) result.get();
      }
    } catch (Exception e) {
      AseLog.e("Failed to display dialog.", e);
      throw new AseRuntimeException(e);
    }
  }

  @Rpc(description = "Queries the user for a text input.")
  public String getInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("ASE Input") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message) {
    return getInputFromAlertDialog(title, message, false);
  }

  @Rpc(description = "Queries the user for a password.")
  public String getPassword(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("ASE Password Input") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message) {
    return getInputFromAlertDialog(title, message, true);
  }

  @Rpc(description = "Displays a notification that will be canceled when the user clicks on it.")
  public void notify(
      @RpcParameter(name = "message") String message,
      @RpcParameter(name = "title", description = "title") @RpcDefault("ASE Notification") final String title,
      @RpcParameter(name = "ticker", description = "ticker") @RpcDefault("ASE Notification") final String ticker) {
    Notification notification =
        new Notification(mResources.getAseLogo48(), ticker, System.currentTimeMillis());
    // This contentIntent is a noop.
    PendingIntent contentIntent = PendingIntent.getService(mService, 0, new Intent(), 0);
    notification.setLatestEventInfo(mService, title, ticker, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    mNotificationManager.notify(1, notification);
  }

  @Rpc(description = "Dials a contact/phone number by URI.")
  public void dial(@RpcParameter(name = "uri") final String uri) {
    startActivity(Intent.ACTION_DIAL, uri, null);
  }

  @Rpc(description = "Dials a phone number.")
  public void dialNumber(@RpcParameter(name = "phone number") final String number) {
    dial("tel:" + number);
  }

  @Rpc(description = "Calls a contact/phone number by URI.")
  public void call(@RpcParameter(name = "uri") final String uri) {
    startActivity(Intent.ACTION_CALL, uri, null);
  }

  @Rpc(description = "Calls a phone number.")
  public void callNumber(@RpcParameter(name = "phone number") final String number)
      throws UnsupportedEncodingException {
    call("tel:" + URLEncoder.encode(number, "ASCII"));
  }

  @Rpc(description = "Opens a map search for query (e.g. pizza, 123 My Street).")
  public void map(@RpcParameter(name = "query, e.g. pizza, 123 My Street") String query) {
    view("geo:0,0?q=" + query, null);
  }

  @Rpc(description = "Displays the contacts activity.")
  public void showContacts() {
    view("content://contacts/people", null);
  }

  @Rpc(description = "Displays a list of contacts to pick from.", returns = "A map of result values.")
  public Intent pickContact() {
    return pick("content://contacts/people");
  }

  @Rpc(description = "Displays a list of phone numbers to pick from.", returns = "The selected phone number.")
  public String pickPhone() {
    Intent data = pick("content://contacts/phones");
    Uri phoneData = data.getData();
    Cursor c = mService.getContentResolver().query(phoneData, null, null, null, null);
    String result = "";
    if (c.moveToFirst()) {
      result = c.getString(c.getColumnIndexOrThrow(People.NUMBER));
    }
    c.close();
    return result;
  }

  @Rpc(description = "Starts the barcode scanner.", returns = "A Map representation of the result Intent.")
  public Intent scanBarcode() {
    return startActivityForResult("com.google.zxing.client.android.SCAN", null, null);
  }

  @Rpc(description = "Starts image capture.", returns = "A Map representation of the result Intent.")
  public Intent captureImage() {
    return startActivityForResult("android.media.action.IMAGE_CAPTURE", null, null);
  }

  @Rpc(description = "Opens a web search for the given query.")
  public void webSearch(@RpcParameter(name = "query") String query) {
    Uri.Builder builder = Uri.parse("http://www.google.com/search").buildUpon();
    builder.appendQueryParameter("q", query);
    view(builder.build().toString(), null);
  }

  @Rpc(description = "Exits the activity or service running the script.")
  public void exit() {
    mService.stopSelf();
  }

  @Rpc(description = "Returns an extra value that was specified in the launch intent.", returns = "The extra value.")
  public Object getExtra(@RpcParameter(name = "name") String name) {
    return mIntent.getExtras().get(name);
  }

  /**
   * Launches an activity that sends an e-mail message to a given recipient.
   * 
   * @param recipientAddress
   *          recipient's e-mail address
   * @param subject
   *          message subject
   * @param body
   *          message body
   */
  @Rpc(description = "Launches an activity that sends an e-mail message to a given recipient.")
  public void sendEmail(@RpcParameter(name = "recipientAddress") final String recipientAddress,
      @RpcParameter(name = "subject") final String subject,
      @RpcParameter(name = "body") final String body) {
    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipientAddress });
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    startActivity(emailIntent);
  }

  /**
   * Retrieve package version code
   * 
   * @param packageName
   * @return
   */
  @Rpc(description = "Retrieve package version code")
  public int getPackageVersionCode(@RpcParameter(name = "packageName") final String packageName) {
    int result = -1;
    PackageInfo pInfo = null;
    try {
      pInfo =
          mService.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      pInfo = null;
    }
    if (pInfo != null)
      result = pInfo.versionCode;
    return result;
  }

  /**
   * Retrieve package version string
   * 
   * @param packageName
   * @return
   */
  @Rpc(description = "Retrieve package version string")
  public String getPackageVersion(@RpcParameter(name = "packageName") final String packageName) {
    String result = "";
    PackageInfo pInfo = null;
    try {
      pInfo =
          mService.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      pInfo = null;
    }
    if (pInfo != null)
      result = "" + pInfo.versionName;
    return result;
  }

  /**
   * Check if ASE is higher or equal of specified version
   * 
   * @param version
   * @return
   */
  @Rpc(description = "Check if ASE is higher or equal of specified version")
  public boolean requiredVersion(@RpcParameter(name = "requiredVersion") final Integer version) {
    boolean result = false;
    int packageVersion = getPackageVersionCode("com.google.ase");
    if (version > -1)
      result = (packageVersion >= version);
    return result;
  }
}
