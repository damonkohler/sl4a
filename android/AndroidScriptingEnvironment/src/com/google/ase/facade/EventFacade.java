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

package com.google.ase.facade;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.google.ase.IntentBuilders;
import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcDefaultInteger;
import com.google.ase.jsonrpc.RpcDefaultString;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class EventFacade implements RpcReceiver {
  private static final int EXECUTE_SCRIPT_REQUEST_CODE = 1;

  final Queue<Bundle> mEventQueue = new ConcurrentLinkedQueue<Bundle>();
  final Context mService;
  final AlarmManager mAlarmManager;
  
  public EventFacade(final Service service) {
    mService = service;
    mAlarmManager = (AlarmManager)service.getSystemService(Context.ALARM_SERVICE);
    mSensorManager = (SensorManager)service.getSystemService(Context.SENSOR_SERVICE);
    mLocationManager = (LocationManager)service.getSystemService(Context.LOCATION_SERVICE);
    mTelephonyManager = (TelephonyManager)service.getSystemService(Context.TELEPHONY_SERVICE);
  }
  
  @Rpc(description = "scheudles a script for regular execution")
  public void scheduleRepeating(
      @RpcParameter("interval") Long interval,
      @RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mService,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
        SystemClock.elapsedRealtime(), interval, pendingIntent);
  }
  
  @Rpc(description = "cancels the regular execution of a given script")
  public void cancelRepeating(@RpcParameter("script") String script) {
    final PendingIntent pendingIntent = PendingIntent.getService(mService,
        EXECUTE_SCRIPT_REQUEST_CODE, IntentBuilders.buildStartInBackgroundIntent(script), 0);

    mAlarmManager.cancel(pendingIntent);
  }

  @Rpc(description = "Receives the most recent event (i.e. location or sensor update, etc.", returns = "Map of event properties.")
  public Bundle receiveEvent() {
    return mEventQueue.poll();
  }

  private void postEvent(String name, Bundle bundle) {
    Bundle event = new Bundle(bundle);
    event.putString("name", name);
    mEventQueue.add(event);
  }
  
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

  @Override
  public void shutdown() {
    stopSensing();
    stopLocating();
    stopTrackingPhoneState();
  }

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
}
