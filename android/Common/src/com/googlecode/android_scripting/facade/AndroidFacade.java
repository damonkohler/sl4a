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

package com.googlecode.android_scripting.facade;

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
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.text.ClipboardManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.NotificationIdFactory;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Some general purpose Android routines.
 * 
 */
public class AndroidFacade extends RpcReceiver {
  /**
   * An instance of this interface is passed to the facade. From this object, the resource IDs can
   * be obtained.
   */
  public interface Resources {
    int getLogo48();
  }

  private final Service mService;
  private final Handler mHandler;
  private final Intent mIntent;
  private final FutureActivityTaskExecutor mTaskQueue;

  private final Vibrator mVibrator;
  private final NotificationManager mNotificationManager;

  private final Resources mResources;

  @Override
  public void shutdown() {
  }

  public AndroidFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mIntent = manager.getIntent();
    BaseApplication application = ((BaseApplication) mService.getApplication());
    mTaskQueue = application.getTaskExecutor();
    mHandler = new Handler(mService.getMainLooper());
    mVibrator = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
    mNotificationManager =
        (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    mResources = manager.getAndroidFacadeResources();
  }

  /**
   * Creates a new AndroidFacade that simplifies the interface to various Android APIs.
   * 
   * @param service
   *          is the {@link Context} the APIs will run under
   */

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
    FutureActivityTask<Intent> task = new FutureActivityTask<Intent>() {
      @Override
      public void onCreate() {
        super.onCreate();
        startActivityForResult(intent, 0);
      }

      @Override
      public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setResult(data);
      }
    };
    mTaskQueue.execute(task);

    try {
      return task.getResult();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      task.finish();
    }
  }

  // TODO(damonkohler): It's unnecessary to add the complication of choosing between startActivity
  // and startActivityForResult. It's probably better to just always use the ForResult version.
  // However, this makes the call always blocking. We'd need to add an extra boolean parameter to
  // indicate if we should wait for a result.
  @Rpc(description = "Starts an activity and returns the result.", returns = "A Map representation of the result Intent.")
  public Intent startActivityForResult(
      @RpcParameter(name = "action") String action,
      @RpcParameter(name = "uri") @RpcOptional String uri,
      @RpcParameter(name = "type", description = "MIME type/subtype of the URI") @RpcOptional String type,
      @RpcParameter(name = "extras", description = "a Map of extras to add to the Intent") @RpcOptional JSONObject extras)
      throws JSONException {
    Intent intent = new Intent(action);
    intent.setDataAndType(uri != null ? Uri.parse(uri) : null, type);
    if (extras != null) {
      putExtrasFromJsonObject(extras, intent);
    }
    return startActivityForResult(intent);
  }

  // TODO(damonkohler): Pull this out into proper argument deserialization and support
  // complex/nested types being passed in.
  private void putExtrasFromJsonObject(JSONObject extras, Intent intent) throws JSONException {
    JSONArray names = extras.names();
    for (int i = 0; i < names.length(); i++) {
      String name = names.getString(i);
      Object data = extras.get(name);
      if (data == null) {
        continue;
      }
      if (data instanceof Integer) {
        intent.putExtra(name, (Integer) data);
      }
      if (data instanceof Float) {
        intent.putExtra(name, (Float) data);
      }
      if (data instanceof Double) {
        intent.putExtra(name, (Double) data);
      }
      if (data instanceof Long) {
        intent.putExtra(name, (Long) data);
      }
      if (data instanceof String) {
        intent.putExtra(name, (String) data);
      }
      if (data instanceof Boolean) {
        intent.putExtra(name, (Boolean) data);
      }
    }
  }

  void startActivity(final Intent intent) {
    try {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mService.startActivity(intent);
    } catch (Exception e) {
      Log.e("Failed to launch intent.", e);
    }
  }

  @Rpc(description = "Starts an activity.")
  public void startActivity(
      @RpcParameter(name = "action") String action,
      @RpcParameter(name = "uri") @RpcOptional String uri,
      @RpcParameter(name = "type", description = "MIME type/subtype of the URI") @RpcOptional String type,
      @RpcParameter(name = "extras", description = "a Map of extras to add to the Intent") @RpcOptional JSONObject extras,
      @RpcParameter(name = "wait", description = "block until the user exits the started activity") @RpcOptional Boolean wait)
      throws JSONException {
    final Intent intent = new Intent(action);
    intent.setDataAndType(uri != null ? Uri.parse(uri) : null, type);
    if (extras != null) {
      putExtrasFromJsonObject(extras, intent);
    }
    if (wait == null || wait == false) {
      startActivity(intent);
    } else {
      FutureActivityTask<Intent> task = new FutureActivityTask<Intent>() {
        private boolean mSecondResume = false;

        @Override
        public void onCreate() {
          super.onCreate();
          startActivity(intent);
        }

        @Override
        public void onResume() {
          if (mSecondResume) {
            finish();
          }
          mSecondResume = true;
        }

        @Override
        public void onDestroy() {
          setResult(null);
        }

      };
      mTaskQueue.execute(task);

      try {
        task.getResult();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
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
    final FutureActivityTask<String> task = new FutureActivityTask<String>() {
      @Override
      public void onCreate() {
        super.onCreate();
        final EditText input = new EditText(getActivity());
        if (password) {
          input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
          input.setTransformationMethod(new PasswordTransformationMethod());
        }
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            setResult(input.getText().toString());
            finish();
          }
        });
        alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
            setResult(null);
            finish();
          }
        });
        alert.show();
      }
    };
    mTaskQueue.execute(task);

    try {
      return task.getResult();
    } catch (Exception e) {
      Log.e("Failed to display dialog.", e);
      throw new RuntimeException(e);
    }
  }

  @Rpc(description = "Queries the user for a text input.")
  @RpcDeprecated("dialogGetInput")
  public String getInput(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("SL4A Input") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter value:") final String message) {
    return getInputFromAlertDialog(title, message, false);
  }

  @Rpc(description = "Queries the user for a password.")
  @RpcDeprecated("dialogGetPassword")
  public String getPassword(
      @RpcParameter(name = "title", description = "title of the input box") @RpcDefault("SL4A Password Input") final String title,
      @RpcParameter(name = "message", description = "message to display above the input box") @RpcDefault("Please enter password:") final String message) {
    return getInputFromAlertDialog(title, message, true);
  }

  @Rpc(description = "Displays a notification that will be canceled when the user clicks on it.")
  public void notify(@RpcParameter(name = "title", description = "title") String title,
      @RpcParameter(name = "message") String message) {
    Notification notification =
        new Notification(mResources.getLogo48(), message, System.currentTimeMillis());
    // This contentIntent is a noop.
    PendingIntent contentIntent = PendingIntent.getService(mService, 0, new Intent(), 0);
    notification.setLatestEventInfo(mService, title, message, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;

    // Get a unique notification id from the application.
    final int notificationId = NotificationIdFactory.create();
    mNotificationManager.notify(notificationId, notification);
  }

  @Rpc(description = "Returns the intent that launched the script.")
  public Object getIntent() {
    return mIntent;
  }

  @Rpc(description = "Launches an activity that sends an e-mail message to a given recipient.")
  public void sendEmail(
      @RpcParameter(name = "to", description = "A comma separated list of recipients.") final String to,
      @RpcParameter(name = "subject") final String subject,
      @RpcParameter(name = "body") final String body,
      @RpcParameter(name = "attachmentUri") @RpcOptional final String attachmentUri) {
    final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
    intent.setType("plain/text");
    intent.putExtra(android.content.Intent.EXTRA_EMAIL, to.split(","));
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    if (attachmentUri != null) {
      intent.putExtra(android.content.Intent.EXTRA_STREAM, Uri.parse(attachmentUri));
    }
    startActivity(intent);
  }

  @Rpc(description = "Returns package version code.")
  public int getPackageVersionCode(@RpcParameter(name = "packageName") final String packageName) {
    int result = -1;
    PackageInfo pInfo = null;
    try {
      pInfo =
          mService.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      pInfo = null;
    }
    if (pInfo != null) {
      result = pInfo.versionCode;
    }
    return result;
  }

  @Rpc(description = "Returns package version name.")
  public String getPackageVersion(@RpcParameter(name = "packageName") final String packageName) {
    PackageInfo packageInfo = null;
    try {
      packageInfo =
          mService.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
    } catch (NameNotFoundException e) {
      return null;
    }
    if (packageInfo != null) {
      return packageInfo.versionName;
    }
    return null;
  }

  @Rpc(description = "Checks if version of SL4A is greater than or equal to the specified version.")
  public boolean requiredVersion(@RpcParameter(name = "requiredVersion") final Integer version) {
    boolean result = false;
    int packageVersion = getPackageVersionCode("com.googlecode.android_scripting");
    if (version > -1) {
      result = (packageVersion >= version);
    }
    return result;
  }

  @Rpc(description = "Writes message to logcat.")
  public void log(@RpcParameter(name = "message") String message) {
    android.util.Log.v("SCRIPT", message);
  }

  @Rpc(description = "A map of various useful environment details")
  public Map<String, Object> environment() {
    Map<String, Object> result = new HashMap<String, Object>();
    Map<String, Object> zone = new HashMap<String, Object>();
    TimeZone tz = TimeZone.getDefault();
    zone.put("id", tz.getID());
    zone.put("display", tz.getDisplayName());
    zone.put("offset", tz.getOffset((new Date()).getTime()));
    result.put("TZ", zone);
    result.put("SDK", android.os.Build.VERSION.SDK);
    result.put("download", FileUtils.getExternalDownload().getAbsolutePath());
    result.put("appcache", mService.getCacheDir().getAbsolutePath());
    return result;
  }
}
