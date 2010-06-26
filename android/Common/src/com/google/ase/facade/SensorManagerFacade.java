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

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcParameter;

/**
 * Exposes the SensorManager related functionality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class SensorManagerFacade implements RpcReceiver {
  private final EventFacade mEventFacade;
  private final SensorManager mSensorManager;
  private Bundle mSensorReadings;

  private SensorEventListener mSensorListener;

  public SensorManagerFacade(Service service, EventFacade eventFacade) {
    mEventFacade = eventFacade;
    mSensorManager = (SensorManager) service.getSystemService(Context.SENSOR_SERVICE);
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  public void startSensing(
      @RpcParameter(name = "sampleSize", description = "number of samples for calculating average readings") @RpcDefault("5") Integer sampleSize) {
    if (mSensorListener == null) {
      mSensorListener = new SensorValuesCollector(sampleSize);
      mSensorReadings = new Bundle();
      for (Sensor sensor : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
        mSensorManager.registerListener(mSensorListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
      }
    }
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  public Bundle readSensors() {
    synchronized (mSensorReadings) {
      if (mSensorReadings == null) {
        return null;
      }
      return new Bundle(mSensorReadings);
    }
  }

  @Rpc(description = "Stops collecting sensor data.")
  public void stopSensing() {
    if (mSensorManager == null) {
      return;
    }
    mSensorManager.unregisterListener(mSensorListener);
    mSensorReadings = null;
    mSensorListener = null;
  }

  @Override
  public void shutdown() {
    stopSensing();
  }

  private class SensorValuesCollector implements SensorEventListener {
    private final static int MATRIX_SIZE = 9;

    private final RollingAverage mmAzimuth;
    private final RollingAverage mmPitch;
    private final RollingAverage mmRoll;

    private float[] mmGeomagneticValues;
    private float[] mmGravityValues;
    private float[] mmR;
    private float[] mmOrientation;

    public SensorValuesCollector(int avgSampleSize) {
      mmAzimuth = new RollingAverage(avgSampleSize);
      mmPitch = new RollingAverage(avgSampleSize);
      mmRoll = new RollingAverage(avgSampleSize);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      synchronized (mSensorReadings) {
        mSensorReadings.putInt("accuracy", accuracy);
        mEventFacade.postEvent("sensors", mSensorReadings);
      }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      synchronized (mSensorReadings) {
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          mSensorReadings.putFloat("xforce", event.values[0]);
          mSensorReadings.putFloat("yforce", event.values[1]);
          mSensorReadings.putFloat("zforce", event.values[2]);

          mmGravityValues = event.values.clone();
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          mSensorReadings.putFloat("xmag", event.values[0]);
          mSensorReadings.putFloat("ymag", event.values[1]);
          mSensorReadings.putFloat("zmag", event.values[2]);

          mmGeomagneticValues = event.values.clone();
          break;
        case Sensor.TYPE_LIGHT:
          mSensorReadings.putFloat("light", event.values[0]);
          break;
        }

        if (mmGeomagneticValues != null && mmGravityValues != null) {
          if (mmR == null) {
            mmR = new float[MATRIX_SIZE];
          }
          if (SensorManager.getRotationMatrix(mmR, null, mmGravityValues, mmGeomagneticValues)) {
            if (mmOrientation == null) {
              mmOrientation = new float[3];
            }
            SensorManager.getOrientation(mmR, mmOrientation);
            mmAzimuth.add(mmOrientation[0]);
            mmPitch.add(mmOrientation[1]);
            mmRoll.add(mmOrientation[2]);
            mSensorReadings.putDouble("azimuth", mmAzimuth.get());
            mSensorReadings.putDouble("pitch", mmPitch.get());
            mSensorReadings.putDouble("roll", mmRoll.get());
          }
        }
        mEventFacade.postEvent("sensors", mSensorReadings);
      }
    }
  }

  static class RollingAverage {
    private final int mmSampleSize;
    private final double mmData[];
    private int mmIndex = 0;
    private boolean mmFilled = false;
    private double mmSum = 0.0;

    public RollingAverage(int sampleSize) {
      mmSampleSize = sampleSize;
      mmData = new double[mmSampleSize];
    }

    public void add(double value) {
      mmSum -= mmData[mmIndex];
      mmData[mmIndex] = value;
      mmSum += mmData[mmIndex];
      ++mmIndex;
      mmIndex %= mmSampleSize;
      mmFilled = (!mmFilled) ? mmIndex == 0 : mmFilled;
    }

    public double get() throws IllegalStateException {
      if (!mmFilled && mmIndex == 0) {
        throw new IllegalStateException("No values to average.");
      }
      return (mmFilled) ? (mmSum / mmSampleSize) : (mmSum / mmIndex);
    }
  }
}
