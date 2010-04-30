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

import java.io.IOException;
import java.util.List;

import android.app.Service;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcParameter;

/**
 * This facade exposes the LocationManager related functionality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class LocationFacade implements RpcReceiver {
  EventFacade mEventFacade;
  Service mService;

  private Location mLocation;
  private final LocationManager mLocationManager;
  private final Geocoder mGeocoder;

  private final LocationListener mLocationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      mLocation = location;
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

  public LocationFacade(Service service, EventFacade eventFacade) {
    mService = service;
    mEventFacade = eventFacade;
    mGeocoder = new Geocoder(mService);
    mLocationManager = (LocationManager) service.getSystemService(Context.LOCATION_SERVICE);
  }

  @Override
  public void shutdown() {
    stopLocating();
  }

  @Rpc(description = "Starts collecting location data.")
  public void startLocating(
      @RpcParameter(name = "accuracy", description = "String accuracy (\"fine\", \"coarse\")") @RpcDefault("coarse") String accuracy,
      @RpcParameter(name = "minDistance", description = "minimum time between updates (milli-seconds)") @RpcDefault("60000") Integer minUpdateTimeMs,
      @RpcParameter(name = "minUpdateDistance", description = "minimum distance between updates (meters)") @RpcDefault("30") Integer minUpdateDistanceM) {
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
  public Location readLocation() {
    return mLocation;
  }

  @Rpc(description = "Stops collecting location data.")
  public void stopLocating() {
    mLocationManager.removeUpdates(mLocationListener);
    mLocation = null;
  }

  @Rpc(description = "Returns the last known location of the device.", returns = "A map of location information.")
  public Location getLastKnownLocation() {
    Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if (location == null) {
      location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }
    return location;
  }

  @Rpc(description = "Returns a list of addresses for the given latitude and longitude.", returns = "A list of addresses.")
  public List<Address> geocode(
      @RpcParameter(name = "latitude") Double latitude,
      @RpcParameter(name = "longitude") Double longitude,
      @RpcParameter(name = "maxResults", description = "max. no. of results") @RpcDefault("1") Integer maxResults)
      throws IOException {
    return mGeocoder.getFromLocation(latitude, longitude, maxResults);
  }
}
