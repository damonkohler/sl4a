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
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcParameter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This facade exposes the LocationManager related functionality.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class LocationFacade extends RpcReceiver {
  private final EventFacade mEventFacade;
  private final Service mService;
  private final Map<String, Location> mLocationUpdates;
  private final LocationManager mLocationManager;
  private final Geocoder mGeocoder;

  private final LocationListener mLocationListener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      mLocationUpdates.put(location.getProvider(), location);
      mEventFacade.postEvent("location", mLocationUpdates);
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

  public LocationFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mEventFacade = manager.getReceiver(EventFacade.class);
    mGeocoder = new Geocoder(mService);
    mLocationManager = (LocationManager) mService.getSystemService(Context.LOCATION_SERVICE);
    mLocationUpdates = new HashMap<String, Location>();
  }

  @Override
  public void shutdown() {
    stopLocating();
  }

  @Rpc(description = "Starts collecting location data.")
  public void startLocating(
      @RpcParameter(name = "minDistance", description = "minimum time between updates in milliseconds") @RpcDefault("60000") Integer minUpdateTime,
      @RpcParameter(name = "minUpdateDistance", description = "minimum distance between updates in meters") @RpcDefault("30") Integer minUpdateDistance) {
    for (String provider : mLocationManager.getAllProviders()) {
      mLocationManager.requestLocationUpdates(provider, minUpdateTime, minUpdateDistance,
          mLocationListener, mService.getMainLooper());
    }
  }

  @Rpc(description = "Returns the current location as indicated by all available providers.", returns = "A map of location information by provider.")
  public Map<String, Location> readLocation() {
    return mLocationUpdates;
  }

  @Rpc(description = "Stops collecting location data.")
  public void stopLocating() {
    mLocationManager.removeUpdates(mLocationListener);
    mLocationUpdates.clear();
  }

  @Rpc(description = "Returns the last known location of the device.", returns = "A map of location information by provider.")
  public Map<String, Location> getLastKnownLocation() {
    Map<String, Location> location = new HashMap<String, Location>();
    for (String provider : mLocationManager.getAllProviders()) {
      location.put(provider, mLocationManager.getLastKnownLocation(provider));
    }
    return location;
  }

  @Rpc(description = "Returns a list of addresses for the given latitude and longitude.", returns = "A list of addresses.")
  public List<Address> geocode(
      @RpcParameter(name = "latitude") Double latitude,
      @RpcParameter(name = "longitude") Double longitude,
      @RpcParameter(name = "maxResults", description = "maximum number of results") @RpcDefault("1") Integer maxResults)
      throws IOException {
    return mGeocoder.getFromLocation(latitude, longitude, maxResults);
  }
}
