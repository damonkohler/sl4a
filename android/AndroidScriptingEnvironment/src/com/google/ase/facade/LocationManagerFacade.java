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
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefaultInteger;
import com.google.ase.rpc.RpcDefaultString;

/**
 * This facade exposes the LocationManager related functionality. 
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 *         Felix Arends (felix.arends@gmail.com)
 */
public class LocationManagerFacade implements RpcReceiver {
  EventFacade mEventFacade;
  Service mService;
  
  private Bundle mLocation;
  private final LocationManager mLocationManager;
  private final LocationListener mLocationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      mLocation = buildLocationBundle(location);
      mEventFacade.postEvent("location", mLocation);
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

  public LocationManagerFacade(Service service, EventFacade eventFacade) {
    this.mEventFacade = eventFacade;
    this.mService = service;
    mLocationManager = (LocationManager)service.getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void shutdown() {
    stopLocating();
  }

  @Rpc(description = "Starts collecting location data.")
  public void startLocating(
      @RpcDefaultString(name = "accuracy", description = "String accuracy (\"fine\", \"coarse\")", defaultValue = "coarse") String accuracy,
      @RpcDefaultInteger(name = "minDistance", description = "minimum time between updates (milli-seconds)", defaultValue = 60000) Integer minUpdateTimeMs,
      @RpcDefaultInteger(name = "minUpdateDistance", description = "minimum distance between updates (meters)", defaultValue = 30) Integer minUpdateDistanceM) {
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
