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

import android.app.Service;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcReceiver;

public class SensorManagerFacade implements RpcReceiver {
  
  private final EventFacade mEventFacade;
  private final SensorManager mSensorManager;
  private Bundle mSensorReadings;
  private final SensorEventListener mSensorListener = new SensorEventListener() {
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      if (mSensorReadings == null) {
        mSensorReadings = new Bundle();
      }
      mSensorReadings.putInt("accuracy", accuracy);
      mEventFacade.postEvent("sensors", mSensorReadings);
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
      mEventFacade.postEvent("sensors", mSensorReadings);
    }
  };
  
  public SensorManagerFacade(Service service, EventFacade eventFacade) {
    this.mEventFacade = eventFacade;    
    this.mSensorManager = (SensorManager)service.getSystemService(Context.SENSOR_SERVICE);
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

  @Override
  public void shutdown() {
    stopSensing();
  }
}
