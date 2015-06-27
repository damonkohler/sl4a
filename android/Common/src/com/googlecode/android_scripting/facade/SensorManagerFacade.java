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
import android.os.Build;
import android.os.Bundle;
import android.util.SparseArray;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcParameter;
import com.googlecode.android_scripting.rpc.RpcStartEvent;
import com.googlecode.android_scripting.rpc.RpcStopEvent;

import java.util.Arrays;
import java.util.List;

/**
 * Exposes the SensorManager related functionality. <br>
 * <br>
 * <b>Guidance notes</b> <br>
 * For reasons of economy the sensors on smart phones are usually low cost and, therefore, low
 * accuracy (usually represented by 10 bit data). The floating point data values obtained from
 * sensor readings have up to 16 decimal places, the majority of which are noise. On many phones the
 * accelerometer is limited (by the phone manufacturer) to a maximum reading of 2g. The magnetometer
 * (which also provides orientation readings) is strongly affected by the presence of ferrous metals
 * and can give large errors in vehicles, on board ship etc.
 * 
 * Following a startSensingTimed(A,B) api call sensor events are entered into the Event Queue (see
 * EventFacade). For the A parameter: 1 = All Sensors, 2 = Accelerometer, 3 = Magnetometer and 4 =
 * Light. The B parameter is the minimum delay between recordings in milliseconds. To avoid
 * duplicate readings the minimum delay should be 20 milliseconds. The light sensor will probably be
 * much slower (taking about 1 second to register a change in light level). Note that if the light
 * level is constant no sensor events will be registered by the light sensor.
 * 
 * Following a startSensingThreshold(A,B,C) api call sensor events greater than a given threshold
 * are entered into the Event Queue. For the A parameter: 1 = Orientation, 2 = Accelerometer, 3 =
 * Magnetometer and 4 = Light. The B parameter is the integer value of the required threshold level.
 * For orientation sensing the integer threshold value is in milliradians. Since orientation events
 * can exceed the threshold value for long periods only crossing and return events are recorded. The
 * C parameter is the required axis (XYZ) of the sensor: 0 = No axis, 1 = X, 2 = Y, 3 = X+Y, 4 = Z,
 * 5= X+Z, 6 = Y+Z, 7 = X+Y+Z. For orientation X = azimuth, Y = pitch and Z = roll. <br>
 * 
 * <br>
 * <b>Example (python)</b>
 * 
 * <pre>
 * import android, time
 * droid = android.Android()
 * droid.startSensingTimed(1, 250)
 * time.sleep(1)
 * s1 = droid.readSensors().result
 * s2 = droid.sensorsGetAccuracy().result
 * s3 = droid.sensorsGetLight().result
 * s4 = droid.sensorsReadAccelerometer().result
 * s5 = droid.sensorsReadMagnetometer().result
 * s6 = droid.sensorsReadOrientation().result
 * droid.stopSensing()
 * </pre>
 * 
 * Returns:<br>
 * s1 = {u'accuracy': 3, u'pitch': -0.47323511242866517, u'xmag': 1.75, u'azimuth':
 * -0.26701245009899138, u'zforce': 8.4718560000000007, u'yforce': 4.2495484000000001, u'time':
 * 1297160391.2820001, u'ymag': -8.9375, u'zmag': -41.0625, u'roll': -0.031366908922791481,
 * u'xforce': 0.23154590999999999}<br>
 * s2 = 3 (Highest accuracy)<br>
 * s3 = None ---(not available on many phones)<br>
 * s4 = [0.23154590999999999, 4.2495484000000001, 8.4718560000000007] ----(x, y, z accelerations)<br>
 * s5 = [1.75, -8.9375, -41.0625] -----(x, y, z magnetic readings)<br>
 * s6 = [-0.26701245009899138, -0.47323511242866517, -0.031366908922791481] ---(azimuth, pitch, roll
 * in radians)<br>
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Felix Arends (felix.arends@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 * @author Robbie Mathews (rjmatthews62@gmail.com)
 * @author John Karwatzki (jokar49@gmail.com)
 */
public class SensorManagerFacade extends RpcReceiver {
  private final EventFacade mEventFacade;
  private final SensorManager mSensorManager;
    private final int nMaxSensorType;

  private volatile Bundle mSensorReadings;

  private volatile Integer mAccuracy;
  private volatile Integer mSensorNumber;
  private volatile Integer mXAxis = 0;
  private volatile Integer mYAxis = 0;
  private volatile Integer mZAxis = 0;
  private volatile Integer mThreshing = 0;
  private volatile Integer mThreshOrientation = 0;
  private volatile Integer mXCrossed = 0;
  private volatile Integer mYCrossed = 0;
  private volatile Integer mZCrossed = 0;

  private volatile Float mThreshold;
  private volatile Float mXForce;
  private volatile Float mYForce;
  private volatile Float mZForce;

  private volatile Float mXMag;
  private volatile Float mYMag;
  private volatile Float mZMag;

  private volatile Float mLight;

  private volatile Double mAzimuth;
  private volatile Double mPitch;
  private volatile Double mRoll;

  private volatile Long mLastTime;
  private volatile Long mDelayTime;

  private SensorValuesCollector mSensorListener;
    private SparseArray<SensorInfo> sensors;

  public SensorManagerFacade(FacadeManager manager) {
    super(manager);
    mEventFacade = manager.getReceiver(EventFacade.class);
    mSensorManager = (SensorManager) manager.getService().getSystemService(Context.SENSOR_SERVICE);

        // List
        final int ever = Integer.MAX_VALUE;
        sensors = new SparseArray<SensorInfo>() {
            // 1-4 is in below SL4A 6x04: -1, 1, 2, 5
            {put(1, new SensorInfo(Sensor.TYPE_ALL, ever, ""));}
            {put(2, new SensorInfo(Sensor.TYPE_ACCELEROMETER, ever,
                                   "accelerometer"));}
            {put(3, new SensorInfo(Sensor.TYPE_MAGNETIC_FIELD, ever,
                                   "magnetic_field"));}
            {put(4, new SensorInfo(Sensor.TYPE_LIGHT, ever, "light"));}
            // 5- is at SL4A 6.1.0: 4, 6, 7, 8
            {put(5, new SensorInfo(Sensor.TYPE_GYROSCOPE, ever, "gyroscope"));}
            {put(6, new SensorInfo(Sensor.TYPE_PRESSURE, ever, "pressure"));}
            {put(7, new SensorInfo(Sensor.TYPE_TEMPERATURE,
                    Build.VERSION_CODES.ICE_CREAM_SANDWICH,
                    "temperature"));}
            {put(8, new SensorInfo(Sensor.TYPE_PROXIMITY, ever, "proximity"));}

            /* 3: {put(?, new SensorInfo(Sensor.TYPE_ORIENTATION,
                    Build.VERSION_CODES.FROYO,
                    "orientation"));} */
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            // API9: 9(combo?), 10(combo), 11(combo)
            sensors.put(9, new SensorInfo(Sensor.TYPE_GRAVITY, ever,
                                          "gravity"));
            sensors.put(10, new SensorInfo(Sensor.TYPE_LINEAR_ACCELERATION,
                                           ever, "linear_acceleration"));
            sensors.put(11, new SensorInfo(Sensor.TYPE_ROTATION_VECTOR, ever,
                                           "rotation_vector"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // API14: 12, 13
            sensors.put(12, new SensorInfo(Sensor.TYPE_RELATIVE_HUMIDITY, ever,
                                           "relative_humidity"));
            sensors.put(13, new SensorInfo(Sensor.TYPE_AMBIENT_TEMPERATURE,
                                           ever, "ambient_temperature"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // API18: 14, 15(combo), 16, 17(combo)
            sensors.put(14, new SensorInfo(
                Sensor.TYPE_MAGNETIC_FIELD_UNCALIBRATED, ever,
                "magnetic_field_uncalibrated"));
            sensors.put(15, new SensorInfo(Sensor.TYPE_GAME_ROTATION_VECTOR,
                                           ever, "game_rotation_vector"));
            sensors.put(16, new SensorInfo(Sensor.TYPE_GYROSCOPE_UNCALIBRATED,
                                           ever, "gyroscope_uncalibrated"));
            sensors.put(17, new SensorInfo(Sensor.TYPE_SIGNIFICANT_MOTION,
                                           ever, "significant_motion"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // API19: 18(combo), 19(combo), 20(combo)
            sensors.put(18, new SensorInfo(Sensor.TYPE_STEP_DETECTOR, ever,
                                           "step_detector"));
            sensors.put(19, new SensorInfo(Sensor.TYPE_STEP_COUNTER, ever,
                                           "step_counter"));
            sensors.put(20, new SensorInfo(
                Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR, ever,
                "geomagnetic_rotation_vector"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            // API20: 21
            sensors.put(21, new SensorInfo(Sensor.TYPE_HEART_RATE, ever,
                                           "heart_rate"));
        }
        int max = 0;
        for (int i = 0; i < sensors.size(); i++) {
            int n = sensors.keyAt(i);
            SensorInfo info = this.sensors.valueAt(i);
            info.nSL4A = n;
            max = (n > max) ? n: max;
        }
        this.nMaxSensorType = max;
  }

        public SensorInfo getInfo(int nAndroid) {
            for (int i = 0; i < this.sensors.size(); i++) {
                SensorInfo info = this.sensors.valueAt(i);
                if (info.nSL4A == nAndroid) { return info; }
            }
            return null;
        }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  @RpcStartEvent("sensors")
  public void startSensingTimed(
      @RpcParameter(name="sensorNumber",
                    description="1 = All, 2 = Accelerometer, " +
                                "3 = Magnetometer, 4 = Light," +
                                "5 = Temperature, 6 = GYRO, 7 = PRESSURE, " +
                                "8 = Proximity, 9 = Gravity, " +
                                "10 = Linear Accelero, 11 = Rotation, " +
                                "12 = Humidity, 13 = Temperature(Ambient), " +
                                "14 = Magnetometer(uncalibrated, " +
                                "15 = Game Rotation, 16 = GYRO(uncalib), " +
                                "17 = Sig. Motion, 18 = Step detect, " +
                                "19 = Step count, 20 = GeoMag rotate, " +
                                "21 = Hart rate"
                    ) Integer sensorNumber,
      @RpcParameter(name = "delayTime", description = "Minimum time between readings in milliseconds") Integer delayTime) {
    mSensorNumber = sensorNumber;
    if (delayTime < 20) {
      delayTime = 20;
    }
    mDelayTime = (long) (delayTime);
    mLastTime = System.currentTimeMillis();
        if (mSensorListener != null) {
            throw new RuntimeException("SensorListener was already launched," +
                                       "close it first.");
        }
      mSensorListener = new SensorValuesCollector();
      mSensorReadings = new Bundle();

        if (sensorNumber > this.nMaxSensorType) {
            throw new RuntimeException("requested sensors is not supported " +
                                       "in your OS version...");
        }

        boolean fExist = false;
        for (int i = 0; i < this.sensors.size(); i++) {
            SensorInfo info = this.sensors.valueAt(i);
            if (sensorNumber != info.nSL4A) { continue; }
            if (Build.VERSION.SDK_INT >= info.verDepricated) {
                String msg;
                msg = String.format("%d>=%d", Build.VERSION.SDK_INT,
                        info.verDepricated);
                throw new RuntimeException("requested sensors is no longer " +
                                           "supported your OS version " + msg);
            }
            for (Sensor sensor: mSensorManager.getSensorList(info.nAndroid)) {
                mSensorManager.registerListener(mSensorListener, sensor,
                        SensorManager.SENSOR_DELAY_FASTEST);
                fExist = true;
            }
        }
        if (!fExist) {
            throw new RuntimeException("requested sensor is not " +
                                       "support in your device...");
        }
    }

  @Rpc(description = "Records to the Event Queue sensor data exceeding a chosen threshold.")
  @RpcStartEvent("threshold")
  public void startSensingThreshold(

      @RpcParameter(name = "sensorNumber", description = "1 = Orientation, 2 = Accelerometer, 3 = Magnetometer and 4 = Light") Integer sensorNumber,
      @RpcParameter(name = "threshold", description = "Threshold level for chosen sensor (integer)") Integer threshold,
      @RpcParameter(name = "axis", description = "0 = No axis, 1 = X, 2 = Y, 3 = X+Y, 4 = Z, 5= X+Z, 6 = Y+Z, 7 = X+Y+Z") Integer axis) {
    mSensorNumber = sensorNumber;
    mXAxis = axis & 1;
    mYAxis = axis & 2;
    mZAxis = axis & 4;
    if (mSensorNumber == 1) {
      mThreshing = 0;
      mThreshOrientation = 1;
      mThreshold = ((float) threshold) / ((float) 1000);
    } else {
      mThreshing = 1;
      mThreshold = (float) threshold;
    }
    startSensingTimed(mSensorNumber, 20);
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
        if (mSensorListener != null) {
            // mSensorReading = null
            // cause NullPointerException in 2nd invoke.
            mSensorListener.waitUpdating();
            mSensorListener.fUpdating = true;
        }
    mSensorListener = null;
    mSensorReadings = null;
    mThreshing = 0;
    mThreshOrientation = 0;
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

  /** not work with Android 5.1 <!-- {{{1 -->
   * with missing Intent.prepareToLeaveProcess, do not use it. \
   *
   * @param sampleSize
   */
  @Rpc(description = "Starts recording sensor data to be available for polling.")
  @RpcDeprecated(value = "startSensingTimed or startSensingThreshhold", release = "4")
  public void startSensing(
      @RpcParameter(name = "sampleSize", description = "number of samples for calculating average readings") @RpcDefault("5") Integer sampleSize) {
    if (mSensorListener == null) {
      startSensingTimed(1, 220);
    }
  }

  @Override
  public void shutdown() {
    stopSensing();
  }

  private class SensorValuesCollector implements SensorEventListener {
    private final static int MATRIX_SIZE = 9;
          private volatile boolean fUpdating = false;
          private final int N_TIMEOUT_UPDATE = 100;

    private final RollingAverage mmAzimuth;
    private final RollingAverage mmPitch;
    private final RollingAverage mmRoll;

    private float[] mmGeomagneticValues;
    private float[] mmGravityValues;
    private float[] mmR;
    private float[] mmOrientation;

    public SensorValuesCollector() {
      mmAzimuth = new RollingAverage();
      mmPitch = new RollingAverage();
      mmRoll = new RollingAverage();
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

      }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
      if (mSensorReadings == null) {
        return;
      }

        if (this.waitUpdating()) {
            return;
        }
        this.fUpdating = true;

      synchronized (mSensorReadings) {
        switch (event.sensor.getType()) {
        case Sensor.TYPE_ACCELEROMETER:
          mXForce = event.values[0];
          mYForce = event.values[1];
          mZForce = event.values[2];
          if (mThreshing == 0) {
            mSensorReadings.putFloat("xforce", mXForce);
            mSensorReadings.putFloat("yforce", mYForce);
            mSensorReadings.putFloat("zforce", mZForce);
            if ((mSensorNumber == 2) && (System.currentTimeMillis() > (mDelayTime + mLastTime))) {
              mLastTime = System.currentTimeMillis();
              postEvent();
            }
          }
          if ((mThreshing == 1) && (mSensorNumber == 2)) {
            if ((Math.abs(mXForce) > mThreshold) && (mXAxis == 1)) {
              mSensorReadings.putFloat("xforce", mXForce);
              postEvent();
            }

            if ((Math.abs(mYForce) > mThreshold) && (mYAxis == 2)) {
              mSensorReadings.putFloat("yforce", mYForce);
              postEvent();
            }

            if ((Math.abs(mZForce) > mThreshold) && (mZAxis == 4)) {
              mSensorReadings.putFloat("zforce", mZForce);
              postEvent();
            }
          }

          mmGravityValues = event.values.clone();
          break;
        case Sensor.TYPE_MAGNETIC_FIELD:
          mXMag = event.values[0];
          mYMag = event.values[1];
          mZMag = event.values[2];
          if (mThreshing == 0) {
            mSensorReadings.putFloat("xMag", mXMag);
            mSensorReadings.putFloat("yMag", mYMag);
            mSensorReadings.putFloat("zMag", mZMag);
            if ((mSensorNumber == 3) && (System.currentTimeMillis() > (mDelayTime + mLastTime))) {
              mLastTime = System.currentTimeMillis();
              postEvent();
            }
          }
          if ((mThreshing == 1) && (mSensorNumber == 3)) {
            if ((Math.abs(mXMag) > mThreshold) && (mXAxis == 1)) {
              mSensorReadings.putFloat("xforce", mXMag);
              postEvent();
            }
            if ((Math.abs(mYMag) > mThreshold) && (mYAxis == 2)) {
              mSensorReadings.putFloat("yforce", mYMag);
              postEvent();
            }
            if ((Math.abs(mZMag) > mThreshold) && (mZAxis == 4)) {
              mSensorReadings.putFloat("zforce", mZMag);
              postEvent();
            }
          }
          mmGeomagneticValues = event.values.clone();
          break;
        case Sensor.TYPE_LIGHT:
          mLight = event.values[0];
          if (mThreshing == 0) {
            mSensorReadings.putFloat("light", mLight);
            if ((mSensorNumber == 4) && (System.currentTimeMillis() > (mDelayTime + mLastTime))) {
              mLastTime = System.currentTimeMillis();
              postEvent();
            }
          }
          if ((mThreshing == 1) && (mSensorNumber == 4)) {
            if (mLight > mThreshold) {
              mSensorReadings.putFloat("light", mLight);
              postEvent();
            }
          }
          break;
        case Sensor.TYPE_ALL:
          break;
        default:
                // normal sensors (not detailed approach in SL4A 6.1>)
                if (this.updateOneSensor(event)) {
                    this.fUpdating = false;
                    return;
                }
        }

        if (mSensorNumber == 1) {
                if (this.dumpSensors()) {
                    postEvent();
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
              if (mThreshOrientation == 0) {
                mSensorReadings.putDouble("azimuth", mAzimuth);
                mSensorReadings.putDouble("pitch", mPitch);
                mSensorReadings.putDouble("roll", mRoll);
                if ((mSensorNumber == 1) && (System.currentTimeMillis() > (mDelayTime + mLastTime))) {
                  mLastTime = System.currentTimeMillis();
                  postEvent();
                }
              }
              if ((mThreshOrientation == 1) && (mSensorNumber == 1)) {
                if ((mXAxis == 1) && (mXCrossed == 0)) {
                  if (Math.abs(mAzimuth) > ((double) mThreshold)) {
                    mSensorReadings.putDouble("azimuth", mAzimuth);
                    postEvent();
                    mXCrossed = 1;
                  }
                }
                if ((mXAxis == 1) && (mXCrossed == 1)) {
                  if (Math.abs(mAzimuth) < ((double) mThreshold)) {
                    mSensorReadings.putDouble("azimuth", mAzimuth);
                    postEvent();
                    mXCrossed = 0;
                  }
                }
                if ((mYAxis == 2) && (mYCrossed == 0)) {
                  if (Math.abs(mPitch) > ((double) mThreshold)) {
                    mSensorReadings.putDouble("pitch", mPitch);
                    postEvent();
                    mYCrossed = 1;
                  }
                }
                if ((mYAxis == 2) && (mYCrossed == 1)) {
                  if (Math.abs(mPitch) < ((double) mThreshold)) {
                    mSensorReadings.putDouble("pitch", mPitch);
                    postEvent();
                    mYCrossed = 0;
                  }
                }
                if ((mZAxis == 4) && (mZCrossed == 0)) {
                  if (Math.abs(mRoll) > ((double) mThreshold)) {
                    mSensorReadings.putDouble("roll", mRoll);
                    postEvent();
                    mZCrossed = 1;
                  }
                }
                if ((mZAxis == 4) && (mZCrossed == 1)) {
                  if (Math.abs(mRoll) < ((double) mThreshold)) {
                    mSensorReadings.putDouble("roll", mRoll);
                    postEvent();
                    mZCrossed = 0;
                  }
                }
              }
            }
          }
        }
      }
            this.fUpdating = false;
    }

        private boolean waitUpdating() {
            int n = 0;
            while (this.fUpdating) {
                try {
                    Thread.sleep(1, 0);
                } catch (InterruptedException e) {

                }
                if (n++ > N_TIMEOUT_UPDATE) {
                    return true;
                }
            }
            return this.fUpdating;
        }

        /** <!-- {{{1 -->
         */
        private boolean updateOneSensor(SensorEvent ev) {
            // synchronized (mSensorReadings) {
                // TODO: only 1 sensor in 1 system. make it to N in 1 system.
                SensorInfo info = getInfo(ev.sensor.getType());
                if (info == null) {
                    throw new RuntimeException("sensor" + ev.sensor.getName() +
                                               "was not registered anymore.");
                }
                if (mSensorNumber == 1) {
                    info.putValues(null, ev.values);    // update values only.
                    return false;
                }
                info.putValues(mSensorReadings, ev.values); // set return value.

                Sensor s = ev.sensor;
                info.nameAndroid = s.getName();
                info.strAndroid = s.toString();
                mSensorReadings.putString("name", info.nameAndroid);
                mSensorReadings.putString("toString", info.strAndroid);
                postEvent();
                return true;
        }

        /** <!-- {{{1 -->
         */
        private boolean dumpSensors() {
            boolean f = false;
            for (int i = 0; i < sensors.size(); i++) {
                SensorInfo info = sensors.valueAt(i);
                f = f | info.putValues(mSensorReadings, null);
            }
            return f;
        }
  }

  static class RollingAverage {
    private final int mmSampleSize;
    private final double mmData[];
    private int mmIndex = 0;
    private boolean mmFilled = false;
    private double mmSum = 0.0;

    public RollingAverage() {
      mmSampleSize = 5;
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

    /** Sensor Info <!-- {{{1 -->
     */
    public static class SensorInfo {
        String name;
        int nSL4A;
        int nAndroid;
        int verDepricated;
        float[] value_cached;
        String nameAndroid, strAndroid;

        public SensorInfo(int n, int ver, String name) {
            this.nAndroid = n;
            this.verDepricated = ver;
            this.name = name;
            this.value_cached = null;
        }

        public boolean putValues(Bundle store, float[] values) {
            boolean f = values != null;
            if (f) {
                this.value_cached = new float[values.length];
            } else if (this.value_cached == null) {
                return false;
            } else {
                values = this.value_cached;
            }
            for (int i = 0; i < values.length; i++) {
                String key = this.name + String.format("-%d", i);
                float v = values[i];
                if (store != null) {
                    store.putDouble(key, v);
                }
                if (f) {
                    this.value_cached[i] = v;
                }
            }
            return f;
        }
    }
}
