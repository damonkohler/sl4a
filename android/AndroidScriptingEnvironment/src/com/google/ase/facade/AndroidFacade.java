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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.telephony.gsm.SmsManager;
import android.text.ClipboardManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;
import android.widget.Toast;

import com.google.ase.ActivityRunnable;
import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.AseRuntimeException;
import com.google.ase.AseServiceHelper;
import com.google.ase.CircularBuffer;
import com.google.ase.FutureIntent;
import com.google.ase.R;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultBoolean;
import com.google.ase.jsonrpc.RpcDefaultInteger;
import com.google.ase.jsonrpc.RpcDefaultString;
import com.google.ase.jsonrpc.RpcOptionalString;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class AndroidFacade implements RpcReceiver {

  private static final int REQUEST_CODE = 0;

  private final Service mService;
  private final Handler mHandler;
  private final AseApplication mApplication;

  private final CircularBuffer<Bundle> mEventBuffer;
  private static final int EVENT_BUFFER_LIMIT = 1024;

  private final ActivityManager mActivityManager;
  private final WifiManager mWifi;
  private final SmsManager mSms;
  private final AudioManager mAudio;
  private final Vibrator mVibrator;
  private final NotificationManager mNotificationManager;
  private final Geocoder mGeocoder;

  private CountDownLatch mLatch;
  // The result from a call to startActivityForResult().
  private Intent mStartActivityResult;

  private final TextToSpeechFacade mTts;

  private Bundle mSensorReadings;
  private final SensorManager mSensorManager;
  private final SensorEventListener mSensorListener = new SensorEventListener() {
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      if (mSensorReadings == null) {
        mSensorReadings = new Bundle();
      }
      mSensorReadings.putInt("accuracy", accuracy);
      postEvent("sensors", mSensorReadings);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (mSensorReadings == null) {
        mSensorReadings = new Bundle();
      }
      switch (event.sensor.getType()) {
        case Sensor.TYPE_ORIENTATION:
          mSensorReadings.putFloat("azimuth", event.values[0]);
          mSensorReadings.putFloat("pitch", event.values[1]);
          mSensorReadings.putFloat("roll", event.values[2]);
          break;
        case Sensor.TYPE_ACCELEROMETER:
          mSensorReadings.putFloat("xforce", event.values[0]);
          mSensorReadings.putFloat("yforce", event.values[1]);
          mSensorReadings.putFloat("zforce", event.values[2]);
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          mSensorReadings.putFloat("xmag", event.values[0]);
          mSensorReadings.putFloat("ymag", event.values[1]);
          mSensorReadings.putFloat("zmag", event.values[2]);
          break;
      }
      postEvent("sensors", mSensorReadings);
    }
  };

  @Override
  public void shutdown() {
    stopSensing();
    stopLocating();
    stopTrackingPhoneState();
    mTts.shutdown();
  }

  private Bundle buildLocationBundle(Location location) {
    Bundle bundle = new Bundle();
    bundle.putDouble("altitude", location.getAltitude());
    bundle.putDouble("latitude", location.getLatitude());
    bundle.putDouble("longitude", location.getLongitude());
    bundle.putLong("time", location.getTime());
    bundle.putFloat("accuracy", location.getAccuracy());
    bundle.putFloat("speed", location.getSpeed());
    bundle.putString("provider", location.getProvider());
    return bundle;
  }

  private Bundle mLocation;
  private final LocationManager mLocationManager;
  private final LocationListener mLocationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      mLocation = buildLocationBundle(location);
      postEvent("location", mLocation);
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  private Bundle mPhoneState;
  private final TelephonyManager mTelephonyManager;
  private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged(int state, String incomingNumber) {
      mPhoneState = new Bundle();
      mPhoneState.putString("incomingNumber", incomingNumber);
      switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
          mPhoneState.putString("state", "idle");
          break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
          mPhoneState.putString("state", "offhook");
          break;
        case TelephonyManager.CALL_STATE_RINGING:
          mPhoneState.putString("state", "ringing");
          break;
      }
      postEvent("phone_state", mPhoneState);
    }
  };

  /**
   * Creates a new AndroidFacade that simplifies the interface to various Android APIs.
   *
   * @param service
   *          is the {@link Context} the APIs will run under
   * @param handler
   *          is the {@link Handler} the APIs will use to communicate with the UI thread
   * @param intent
   *          is the {@link Intent} that was used to start the {@link Activity}
   */
  public AndroidFacade(Service service, Handler handler, Intent intent) {
    mService = service;
    mHandler = handler;
    mApplication = (AseApplication) mService.getApplication();
    mSms = SmsManager.getDefault();
    mActivityManager = (ActivityManager) mService.getSystemService(Context.ACTIVITY_SERVICE);
    mWifi = (WifiManager) mService.getSystemService(Context.WIFI_SERVICE);
    mAudio = (AudioManager) mService.getSystemService(Activity.AUDIO_SERVICE);
    mVibrator = (Vibrator) mService.getSystemService(Context.VIBRATOR_SERVICE);
    mSensorManager = (SensorManager) mService.getSystemService(Context.SENSOR_SERVICE);
    mLocationManager = (LocationManager) mService.getSystemService(Context.LOCATION_SERVICE);
    mNotificationManager =
        (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);
    mGeocoder = new Geocoder(mService);
    mTelephonyManager = (TelephonyManager) mService.getSystemService(Context.TELEPHONY_SERVICE);
    mEventBuffer = new CircularBuffer<Bundle>(EVENT_BUFFER_LIMIT);
    mTts = new TextToSpeechFacade(service);
  }

  @Rpc(description = "Put text in the clipboard.")
  public void setClipboard(@RpcParameter("text") String text) {
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

  @Rpc(description = "Starts tracking phone state.")
  public void startTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  @Rpc(description = "Returns the current phone state and incoming number.", returns = "A map of \"state\" and \"incomingNumber\"")
  public Bundle readPhoneState() {
    return mPhoneState;
  }

  @Rpc(description = "Stops tracking phone state.")
  public void stopTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  public void startSensing() {
    for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
      mSensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  public Bundle readSensors() {
    return mSensorReadings;
  }

  @Rpc(description = "Stops collecting sensor data.")
  public void stopSensing() {
    mSensorManager.unregisterListener(mSensorListener);
    mSensorReadings = null;
  }

  @Rpc(description = "Starts collecting location data.")
  public void startLocating(
      @RpcDefaultString(description = "String accuracy (\"fine\", \"coarse\")", defaultValue = "coarse") String accuracy,
      @RpcDefaultInteger(description = "minimum time between updates (milli-seconds)", defaultValue = 60000) Integer minUpdateTimeMs,
      @RpcDefaultInteger(description = "minimum distance between updates (meters)", defaultValue = 30) Integer minUpdateDistanceM) {
    Criteria criteria = new Criteria();
    if (accuracy == "coarse") {
      criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    } else if (accuracy == "fine") {
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }
    mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true),
        minUpdateTimeMs, minUpdateDistanceM, mLocationListener, mService.getMainLooper());
  }

  @Rpc(description = "Returns the current location.", returns = "A map of location information.")
  // TODO(damonkohler): It might be nice to have a version of this method that
  // automatically starts locating and keeps locating until no more requests are
  // received for before some time out.
  public Bundle readLocation() {
    return mLocation;
  }

  @Rpc(description = "Stops collecting location data.")
  public void stopLocating() {
    mLocationManager.removeUpdates(mLocationListener);
    mLocation = null;
  }

  @Rpc(description = "Returns the last known location of the device.", returns = "A map of location information.")
  public Bundle getLastKnownLocation() {
    Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if (location == null) {
      location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    return buildLocationBundle(location);
  }

  @Rpc(description = "Returns a list of addresses for the given latitude and longitude.", returns = "A list of addresses.")
  public List<Address> geocode(
      @RpcParameter("latitude") Double latitude,
      @RpcParameter("longitude") Double longitude,
      @RpcDefaultInteger(description = "max. no. of results (default 1)", defaultValue = 1) Integer maxResults)
      throws IOException {
    return mGeocoder.getFromLocation(latitude, longitude, maxResults);
  }

  @Rpc(description = "Returns the current ringer volume.", returns = "The current volume as an Integer.")
  public int getRingerVolume() {
    // TODO(damonkohler): We may want to pass in the stream type and rename
    // the method to getVolume().
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  @Rpc(description = "Sets the ringer volume.")
  public void setRingerVolume(@RpcParameter("volume") Integer volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  private synchronized void post(Runnable runnable, Handler handler) {
    mLatch = new CountDownLatch(1);
    handler.post(runnable);
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      String message = "Interrupted while waiting for handler to complete.";
      AseLog.e(message, e);
      throw new AseRuntimeException(message);
    }
  }

  public Intent startActivityForResult(final Intent intent) {
    // Help ensure the service isn't killed to free up memory.
    mService.setForeground(true);
    post(new Runnable() {
      public void run() {
        try {
          Intent helper = new Intent(mService, AseServiceHelper.class);
          helper.putExtra("launchIntent", intent);
          helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mService.startActivity(helper);
        } catch (Exception e) {
          AseLog.e("Failed to launch intent.", e);
        }
      }
    }, mHandler);
    mService.setForeground(false);
    return mStartActivityResult;
  }

  @Rpc(description = "Starts an activity for result and returns the result.", returns = "A map of result values.")
  public Intent startActivityForResult(@RpcParameter("action") final String action,
      @RpcOptionalString("uri") final String uri) {
    Intent intent = new Intent(action);
    if (uri != null) {
      intent.setData(Uri.parse(uri));
    }
    return startActivityForResult(intent);
  }

  @Rpc(description = "Display content to be picked by URI (e.g. contacts)", returns = "A map of result values.")
  public Intent pick(@RpcParameter("uri") String uri) {
    return startActivityForResult(Intent.ACTION_PICK, uri);
  }

  public void startActivity(final Intent intent) {
    post(new Runnable() {
      public void run() {
        try {
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mService.startActivity(intent);
        } catch (Exception e) {
          AseLog.e("Failed to launch intent.", e);
        }
        mLatch.countDown();
      }
    }, mHandler);
  }

  @Rpc(description = "Starts an activity for result and returns the result.", returns = "A map of result values.")
  public void startActivity(@RpcParameter("action") final String action,
      @RpcOptionalString("uri") final String uri) {
    Intent intent = new Intent(action);
    if (uri != null) {
      intent.setData(Uri.parse(uri));
    }
    startActivity(intent);
  }

  @Rpc(description = "Start activity with view action by URI (i.e. browser, contacts, etc.).")
  public void view(@RpcParameter("uri") String uri) {
    startActivity(Intent.ACTION_VIEW, uri);
  }

  @Rpc(description = "Start activity with the given class name (i.e. Browser, Maps, etc.).")
  public void launch(@RpcParameter("className") String className) {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    String packageName = className.substring(0, className.lastIndexOf("."));
    intent.setClassName(packageName, className);
    startActivity(intent);
  }

  @Rpc(description = "Sends a text message to the given recipient.")
  public void sendTextMessage(@RpcParameter("destinationAddress") String destinationAddress,
      @RpcParameter("text") String text) {
    mSms.sendTextMessage(destinationAddress, null, text, null, null);
  }

  @Rpc(description = "Vibrates the phone or a specified duration in milliseconds.")
  public void vibrate(
      @RpcDefaultInteger(description = "duration in milliseconds", defaultValue = 300) Integer duration) {
    mVibrator.vibrate(300);
  }

  @Rpc(description = "Sets whether or not the ringer should be silent.")
  public void setRingerSilent(
      @RpcDefaultBoolean(description = "Boolean silent", defaultValue = true) Boolean enabled) {
    if (enabled) {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    } else {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
  }

  @Rpc(description = "Enables or disables Wifi according to the supplied boolean.")
  public void setWifiEnabled(
      @RpcDefaultBoolean(description = "enabled", defaultValue = true) Boolean enabled) {
    mWifi.setWifiEnabled(enabled);
  }

  @Rpc(description = "Displays a short-duration Toast notification.")
  public void makeToast(@RpcParameter("message") final String message) {
    post(new Runnable() {
      public void run() {
        Toast.makeText(mService, message, Toast.LENGTH_SHORT).show();
        mLatch.countDown();
      }
    }, mHandler);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE) {
      if (resultCode == Activity.RESULT_OK) {
        AseLog.v("Request completed. Received intent: " + data);
        mStartActivityResult = data;
      } else if (requestCode == Activity.RESULT_CANCELED) {
        AseLog.v("Request canceled.");
      }
      if (mLatch != null) {
        mLatch.countDown();
      }
    }
  }

  private String getInputFromAlertDialog(final String title, final String message,
      final boolean password) {
    FutureIntent result = mApplication.offerTask(new ActivityRunnable() {
      @Override
      public void run(final Activity activity, final FutureIntent result) {
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
          public void onClick(DialogInterface dialog, int whichButton) {
            Intent intent = new Intent();
            intent.putExtra("result", input.getText().toString());
            result.set(intent);
            activity.finish();
          }
        });
        alert.show();
      }
    });
    try {
      Intent helper = new Intent(mService, AseServiceHelper.class);
      helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mService.startActivity(helper);
    } catch (Exception e) {
      AseLog.e("Failed to launch intent.", e);
    }
    try {
      return result.get().getStringExtra("result");
    } catch (Exception e) {
      AseLog.e("Failed to display dialog.", e);
      throw new AseRuntimeException(e);
    }
  }

  @Rpc(description = "Queries the user for a text input.")
  public String getInput(
      @RpcDefaultString(description = "title of the input box", defaultValue = "ASE Input") final String title,
      @RpcDefaultString(description = "message to display above the input box", defaultValue = "Please enter value:") final String message) {
    return getInputFromAlertDialog(title, message, false);
  }

  @Rpc(description = "Queries the user for a password.")
  public String getPassword(
      @RpcDefaultString(description = "title of the input box", defaultValue = "ASE Password Input") final String title,
      @RpcDefaultString(description = "message to display above the input box", defaultValue = "Please enter password:") final String message) {
    return getInputFromAlertDialog(title, message, true);
  }

  @Rpc(description = "Displays a notification that will be canceled when the user clicks on it.")
  public void notify(
      @RpcParameter("message") String message,
      @RpcDefaultString(description = "title", defaultValue = "ASE Notification") final String title,
      @RpcDefaultString(description = "ticker", defaultValue = "ASE Notification") final String ticker) {
    Notification notification =
        new Notification(R.drawable.ase_logo_48, ticker, System.currentTimeMillis());
    // This is pretty dumb. You _have_ to specify a PendingIntent to be
    // triggered when the notification is clicked on. You cannot specify null.
    Intent notificationIntent = new Intent();
    PendingIntent contentIntent = PendingIntent.getActivity(mService, 0, notificationIntent, 0);
    notification.setLatestEventInfo(mService, title, message, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    mNotificationManager.notify(0, notification);
  }

  @Rpc(description = "Dials a contact/phone number by URI.")
  public void dial(@RpcParameter("uri") final String uri) {
    startActivity(Intent.ACTION_DIAL, uri);
  }

  @Rpc(description = "Dials a phone number.")
  public void dialNumber(@RpcParameter("phone number") final String number) {
    dial("tel:" + number);
  }

  @Rpc(description = "Calls a contact/phone number by URI.")
  public void call(@RpcParameter("uri") final String uri) {
    startActivity(Intent.ACTION_CALL, uri);
  }

  @Rpc(description = "Calls a phone number.")
  public void callNumber(@RpcParameter("phone number") final String number) {
    call("tel:" + number);
  }

  @Rpc(description = "Opens a map search for query (e.g. pizza, 123 My Street).")
  public void map(@RpcParameter("query, e.g. pizza, 123 My Street") String query) {
    view("geo:0,0?q=" + query);
  }

  @Rpc(description = "Displays the contacts activity.")
  public void showContacts() {
    view("content://contacts/people");
  }

  @Rpc(description = "Displays a list of contacts to pick from.", returns = "A map of result values.")
  public Intent pickContact() {
    return pick("content://contacts/people");
  }

  @Rpc(description = "Displays a list of phone numbers to pick from.", returns = "A map of result values.")
  public Intent pickPhone() {
    return pick("content://contacts/phones");
  }

  @Rpc(description = "Starts the barcode scanner.", returns = "A map of result values.")
  public Intent scanBarcode() {
    return startActivityForResult("com.google.zxing.client.android.SCAN", null);
  }

  @Rpc(description = "Starts image capture.", returns = "A map of result values.")
  public Intent captureImage() {
    return startActivityForResult("android.media.action.IMAGE_CAPTURE", null);
  }

  @Rpc(description = "Opens a web search for the given query.")
  public void webSearch(@RpcParameter("query") String query) {
    view("http://www.google.com/search?q=" + query);
  }

  @Rpc(description = "Exits the activity or service running the script.")
  public void exit() {
    mService.stopSelf();
  }

  private void postEvent(String name, Bundle bundle) {
    Bundle event = new Bundle(bundle);
    event.putString("name", name);
    mEventBuffer.add(event);
  }

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.", returns = "Map of event properties.")
  public Bundle receiveEvent() {
    return mEventBuffer.get();
  }

  @Rpc(description = "Returns a list of packages running activities or services.", returns = "List of packages running activities.")
  public Bundle getRunningPackages() {
    Set<String> runningPackages = new HashSet<String>();
    List<ActivityManager.RunningAppProcessInfo> appProcesses =
        mActivityManager.getRunningAppProcesses();
    for (ActivityManager.RunningAppProcessInfo info : appProcesses) {
      runningPackages.addAll(Arrays.asList(info.pkgList));
    }
    List<ActivityManager.RunningServiceInfo> serviceProcesses =
        mActivityManager.getRunningServices(Integer.MAX_VALUE);
    for (ActivityManager.RunningServiceInfo info : serviceProcesses) {
      runningPackages.add(info.service.getPackageName());
    }
    Bundle result = new Bundle();
    result.putStringArrayList("packages", new ArrayList<String>(runningPackages));
    return result;
  }

  @Rpc(description = "Force stops a package.")
  public void forceStopPackage(@RpcParameter("package name") String packageName) {
    mActivityManager.restartPackage(packageName);
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
  public void sendEmail(
      @RpcParameter("the recipient's e-mail address") final String recipientAddress,
      @RpcParameter("subject of the e-mail") final String subject,
      @RpcParameter("message body") final String body) {
    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { recipientAddress });
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, body);
    mService.startActivity(emailIntent);
  }
}
