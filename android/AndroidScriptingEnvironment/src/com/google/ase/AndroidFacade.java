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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class AndroidFacade {

  private static final String TAG = "AndroidFacade";

  private static final int REQUEST_CODE = 0;

  private final Context mContext;
  private final Handler mHandler;
  private final Intent mIntent; // The intent that started the activity.
  private final Intent mActivityResult; // The result of this activity.

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
  private Intent mStartActivityResult; // The result from a call to startActivityForResult().

  private TextToSpeech mTts;

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

  public AndroidFacade(Context context, Handler handler, Intent intent) {
    mContext = context;
    mHandler = handler;
    mIntent = intent;
    mActivityResult = new Intent();
    mSms = SmsManager.getDefault();
    mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    mWifi = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    mAudio = (AudioManager) mContext.getSystemService(Activity.AUDIO_SERVICE);
    mVibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
    mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
    mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
    mNotificationManager =
        (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    mGeocoder = new Geocoder(mContext);
    mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    mEventBuffer = new CircularBuffer<Bundle>(EVENT_BUFFER_LIMIT);
  }

  /**
   * Speaks the provided message via TTS.
   *
   * @param message
   *          message to speak
   * @throws AseException
   */
  public void speak(String message) throws AseException {
    if (mTts == null) {
      final CountDownLatch lock = new CountDownLatch(1);
      TextToSpeech.OnInitListener ttsInitListener = new TextToSpeech.OnInitListener() {
        public void onInit(int version) {
          lock.countDown();
        }
      };
      mTts = new TextToSpeech(mContext, ttsInitListener);
      try {
        if (!lock.await(10, TimeUnit.SECONDS)) {
          throw new AseException("TTS initialization timed out.");
        }
      } catch (InterruptedException e) {
        throw new AseException("TTS initialization interrupted.");
      }
    }
    mTts.speak(message, 1, null);
  }

  public void startTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
  }

  /**
   * Returns the current phone state and incoming number.
   */
  public Bundle readPhoneState() {
    return mPhoneState;
  }

  public void stopTrackingPhoneState() {
    mTelephonyManager.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
  }

  public void startSensing() {
    for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
      mSensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
  }

  public Bundle readSensors() {
    return mSensorReadings;
  }

  public void stopSensing() {
    mSensorManager.unregisterListener(mSensorListener);
    mSensorReadings = null;
  }

  public void startLocating(String accuracy, int minUpdateTime, int minUpdateDistance) {
    Criteria criteria = new Criteria();
    if (accuracy == "coarse") {
      criteria.setAccuracy(Criteria.ACCURACY_COARSE);
    } else if (accuracy == "fine") {
      criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }
    mLocationManager.requestLocationUpdates(mLocationManager.getBestProvider(criteria, true),
        minUpdateTime, minUpdateDistance, mLocationListener, mContext.getMainLooper());
  }

  // TODO(damonkohler): It might be nice to have a version of this method that
  // automatically starts locating and keeps locating until no more requests are
  // received for before some time out.
  public Bundle readLocation() {
    return mLocation;
  }

  public void stopLocating() {
    mLocationManager.removeUpdates(mLocationListener);
    mLocation = null;
  }

  public Bundle getLastKnownLocation() {
    Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if (location == null) {
      location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    return buildLocationBundle(location);
  }

  public List<Address> geocode(double latitude, double longitude, int maxResults)
      throws IOException {
    return mGeocoder.getFromLocation(latitude, longitude, maxResults);
  }

  public int getRingerVolume() {
    // TODO(damonkohler): We may want to pass in the stream type and rename
    // the method to getVolume().
    return mAudio.getStreamVolume(AudioManager.STREAM_RING);
  }

  public void setRingerVolume(int volume) {
    mAudio.setStreamVolume(AudioManager.STREAM_RING, volume, 0);
  }

  public Intent startActivityForResult(final Intent intent) {
    if (!(mContext instanceof Activity)) {
      AseLog.e("Invalid context. Activity required.");
      // TODO(damonkohler): Exception instead?
      return null;
    }

    // TODO(damonkohler): Make it possible for either ASE or user scripts to save and restore state.
    // This prevents ASE from being closed when the new activity is launched.
    ((Activity) mContext).setPersistent(true);

    mLatch = new CountDownLatch(1);
    mHandler.post(new Runnable() {
      public void run() {
        try {
          ((Activity) mContext).startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
          AseLog.e("Failed to launch intent.", e);
        }
      }
    });

    try {
      mLatch.await();
    } catch (InterruptedException e) {
      AseLog.e("Interrupted while waiting for handler to complete.", e);
    }

    // Restore the default behavior of ASE being closed when additional resources are required.
    ((Activity) mContext).setPersistent(false);
    return mStartActivityResult;
  }

  public Intent startActivityForResult(final String action, final String uri) {
    Intent intent = new Intent(action);
    if (uri != null) {
      intent.setData(Uri.parse(uri));
    }
    return startActivityForResult(intent);
  }

  public Intent pick(String uri) {
    return startActivityForResult(Intent.ACTION_PICK, uri);
  }

  public void startActivity(final Intent intent) {
    if (!(mContext instanceof Activity)) {
      AseLog.e("Invalid context. Activity required.");
      // TODO(damonkohler): Exception instead?
      return;
    }
    mLatch = new CountDownLatch(1);
    mHandler.post(new Runnable() {
      public void run() {
        try {
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
        } catch (Exception e) {
          Log.e(TAG, "Failed to launch intent.", e);
        }
        mLatch.countDown();
      }
    });
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while waiting for handler to complete.", e);
    }
  }

  public void startActivity(final String action, final String uri) {
    Intent intent = new Intent(action);
    if (uri != null) {
      intent.setData(Uri.parse(uri));
    }
    startActivity(intent);
  }

  public void view(String uri) {
    startActivity(Intent.ACTION_VIEW, uri);
  }

  public void launch(String className) {
    Intent intent = new Intent(Intent.ACTION_MAIN);
    String packageName = className.substring(0, className.lastIndexOf("."));
    intent.setClassName(packageName, className);
    startActivity(intent);
  }

  public void sendTextMessage(String destinationAddress, String text) {
    mSms.sendTextMessage(destinationAddress, null, text, null, null);
  }

  public void vibrate(long duration) {
    mVibrator.vibrate(duration);
  }

  public void setRingerSilent(boolean enabled) {
    if (enabled) {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_SILENT);
    } else {
      mAudio.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
    }
  }

  public void setWifiEnabled(boolean enabled) {
    mWifi.setWifiEnabled(enabled);
  }

  public void makeToast(final String message) {
    mLatch = new CountDownLatch(1);
    mHandler.post(new Runnable() {
      public void run() {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
        mLatch.countDown();
      }
    });
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while waiting for handler to complete.", e);
    }
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

  public String getInput(final String title, final String message) {
    mLatch = new CountDownLatch(1);
    final EditText input = new EditText(mContext);
    mHandler.post(new Runnable() {
      public void run() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(title);
        alert.setMessage(message);
        alert.setView(input);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
            mLatch.countDown();
          }
        });
        alert.show();
      }
    });
    try {
      mLatch.await();
    } catch (InterruptedException e) {
      Log.e(TAG, "Interrupted while waiting for handler to complete.", e);
    }
    return input.getText().toString();
  }

  public void notify(String ticker, String title, String message) {
    Notification notification =
        new Notification(R.drawable.ase_logo_48, ticker, System.currentTimeMillis());
    // This is pretty dumb. You _have_ to specify a PendingIntent to be triggered when the
    // notification is clicked on. You cannot specify null.
    Intent notificationIntent = new Intent();
    PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);
    notification.setLatestEventInfo(mContext, title, message, contentIntent);
    notification.flags = Notification.FLAG_AUTO_CANCEL;
    mNotificationManager.notify(0, notification);
  }

  public void dial(String uri) {
    startActivity(Intent.ACTION_DIAL, uri);
  }

  public void dialNumber(String number) {
    dial("tel:" + number);
  }

  public void call(String uri) {
    startActivity(Intent.ACTION_CALL, uri);
  }

  public void callNumber(String number) {
    call("tel:" + number);
  }

  public void map(String query) {
    view("geo:0,0?q=" + query);
  }

  public void showContacts() {
    view("content://contacts/people");
  }

  public void email() {
    view("mailto://");
  }

  public void pickContact() {
    pick("content://contacts/people");
  }

  public void pickPhone() {
    pick("content://contacts/phones");
  }

  public Intent scanBarcode() {
    return startActivityForResult("com.google.zxing.client.android.SCAN", null);
  }

  public Intent captureImage() {
    return startActivityForResult("android.media.action.IMAGE_CAPTURE", null);
  }

  public void webSearch(String query) {
    view("http://www.google.com/search?q=" + query);
  }

  public void exit() {
    if (mContext instanceof Activity) {
      ((Activity) mContext).finish();
    } else if (mContext instanceof Service) {
      ((Service) mContext).stopSelf();
    }
  }

  public void exitWithResultOk() {
    if (mContext instanceof Activity) {
      ((Activity) mContext).setResult(Activity.RESULT_OK, mActivityResult);
    }
    exit();
  }

  public void exitWithResultCanceled() {
    if (mContext instanceof Activity) {
      ((Activity) mContext).setResult(Activity.RESULT_CANCELED, mActivityResult);
    }
    exit();
  }

  public void putResultExtra(String name, String value) {
    mActivityResult.putExtra(name, value);
  }

  public void putResultExtra(String name, int value) {
    mActivityResult.putExtra(name, value);
  }

  public void putResultExtra(String name, double value) {
    mActivityResult.putExtra(name, value);
  }

  public void putResultExtra(String name, boolean value) {
    mActivityResult.putExtra(name, value);
  }

  public String getStringExtra(String name) {
    return mIntent.getStringExtra(name);
  }

  public int getIntExtra(String name, int defaultValue) {
    return mIntent.getIntExtra(name, defaultValue);
  }

  public double getDoubleExtra(String name, double defaultValue) {
    return mIntent.getDoubleExtra(name, defaultValue);
  }

  public boolean getBooleanExtra(String name, boolean defaultValue) {
    return mIntent.getBooleanExtra(name, defaultValue);
  }

  private void postEvent(String name, Bundle bundle) {
    Bundle event = new Bundle(bundle);
    event.putString("name", name);
    mEventBuffer.add(event);
  }

  public Bundle receiveEvent() {
    return mEventBuffer.get();
  }

  public void onDestroy() {
    stopSensing();
    stopLocating();
    stopTrackingPhoneState();
    // TTS is lazily initialized so it could still be null.
    if (mTts != null) {
      mTts.shutdown();
    }
  }

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

  public void forceStopPackage(String packageName) {
    mActivityManager.restartPackage(packageName);
  }
}
