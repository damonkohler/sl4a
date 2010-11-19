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

package com.googlecode.android_scripting.facade;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.googlecode.android_scripting.rpc.RpcStartEvent;
import com.googlecode.android_scripting.rpc.RpcStopEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Exposes the SensorManager related functionality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class SensorManagerFacade extends RpcReceiver {
  private final EventFacade mEventFacade;
  private final SensorManager mSensorManager;

  private volatile Bundle mSensorReadings;

  private volatile Integer mAccuracy;
  private volatile Float mXForce;
  private volatile Float mYForce;
  private volatile Float mZForce;

  private volatile Float mXMag;
  private volatile Float mYMag;
  private volatile Float mZMag;

  private volatile Double mAzimuth;
  private volatile Double mPitch;
  private volatile Double mRoll;

  private volatile Float mLight;

  private SensorEventListener mSensorListener;

  public SensorManagerFacade(FacadeManager manager) {
    super(manager);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mSensorManager = (SensorManager) manager.getService().getSystemService(Context.SENSOR_SERVICE);
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  @RpcStartEvent("sensors")
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

  @Rpc(description = "Returns the most recently recorded sensor data.")
  public Bundle readSensors() {
    if (mSensorReadings == null) {
      return null;
    }
    synchronized (mSensorReadings) {
      return new Bundle(mSensorReadings);
    }
  }

  @Rpc(description = "Stops collecting sensor data.")
  @RpcStopEvent("sensors")
  public void stopSensing() {
    mSensorManager.unregisterListener(mSensorListener);
    mSensorListener = null;
    mSensorReadings = null;
  }

  @Rpc(description = "Returns the most recently received accuracy value.")
  public Integer sensorsGetAccuracy() {
    return mAccuracy;
  }

  @Rpc(description = "Returns the most recently received light value.")
  public Float sensorsGetLight() {
    return mLight;
  }

  @Rpc(description = "Returns the most recently received accelerometer values.", returns = "a List of Floats [(acceleration on the) X axis, Y axis, Z axis].")
  public List<Float> sensorsReadAccelerometer() {
    synchronized (mSensorReadings) {
      return Arrays.asList(mXForce, mYForce, mZForce);
    }
  }

  @Rpc(description = "Returns the most recently received magnetic field values.", returns = "a List of Floats [(magnetic field value for) X axis, Y axis, Z axis].")
  public List<Float> sensorsReadMagnetometer() {
    synchronized (mSensorReadings) {
      return Arrays.asList(mXMag, mYMag, mZMag);
    }
  }

  @Rpc(description = "Returns the most recently received orientation values.", returns = "a List of Doubles [azimuth, pitch, roll].")
  public List<Double> sensorsReadOrientation() {
    synchronized (mSensorReadings) {
      return Arrays.asList(mAzimuth, mPitch, mRoll);
    }
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

    private void postEvent() {
      mSensorReadings.putDouble("time", System.currentTimeMillis() / 1000.0);
      mEventFacade.postEvent("sensors", mSensorReadings.clone());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
      if (mSensorReadings == null) {
        return;
      }
      synchronized (mSensorReadings) {
        mSensorReadings.putInt("accuracy", accuracy);
        mAccuracy = accuracy;
        postEvent();
      }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (mSensorReadings == null) {
        return;
      }
      synchronized (mSensorReadings) {
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          mXForce = event.values[0];
          mYForce = event.values[1];
          mZForce = event.values[2];
          mSensorReadings.putFloat("xforce", mXForce);
          mSensorReadings.putFloat("yforce", mYForce);
          mSensorReadings.putFloat("zforce", mZForce);

          mmGravityValues = event.values.clone();
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          mXMag = event.values[0];
          mYMag = event.values[1];
          mZMag = event.values[2];

          mSensorReadings.putFloat("xmag", mXMag);
          mSensorReadings.putFloat("ymag", mYMag);
          mSensorReadings.putFloat("zmag", mZMag);

          mmGeomagneticValues = event.values.clone();
          break;
        case Sensor.TYPE_LIGHT:
          mLight = event.values[0];
          mSensorReadings.putFloat("light", mLight);
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

            mAzimuth = mmAzimuth.get();
            mPitch = mmPitch.get();
            mRoll = mmRoll.get();

            mSensorReadings.putDouble("azimuth", mAzimuth);
            mSensorReadings.putDouble("pitch", mPitch);
            mSensorReadings.putDouble("roll", mRoll);
          }
        }
        postEvent();
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
