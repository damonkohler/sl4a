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

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;

import com.google.ase.jsonrpc.JsonRpcResult;
import com.google.ase.jsonrpc.Rpc;

/**
 * A proxy for JSON RPC calls to access an {@link AndroidFacade}.
 *
 * Each RPC method expects a JSONArray of parameters and returns a JSONObject as a result. The
 * JavaDoc below describes the params in terms of what is expected from JSON RPC clients.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class AndroidProxy {

  private static final String TAG = "AndroidProxy";
  private final AndroidFacade mAndroidFacade;

  public AndroidProxy(AndroidFacade facade) {
    mAndroidFacade = facade;
  }

  @Rpc(
      description = "Speaks the provided message using TTS.",
      params = "String message"
  )
  public JSONObject speak(JSONArray params) {
    String message;
    try {
      message = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Message must be specified.", e);
    }
    try {
      mAndroidFacade.speak(message);
    } catch (AseException e) {
      return JsonRpcResult.error(e.getMessage(), e);
    }
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Starts tracking phone state.")
  public JSONObject startTrackingPhoneState(JSONArray params) {
    mAndroidFacade.startTrackingPhoneState();
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Returns the current phone state.",
      returns = "A map of \"state\" and \"incomingNumber\""
  )
  public JSONObject readPhoneState(JSONArray params) {
    try {
      JSONObject result = buildJsonBundle(mAndroidFacade.readPhoneState());
      return JsonRpcResult.result(result);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build location.", e);
    }
  }

  @Rpc(description = "Stops tracking phone state.")
  public JSONObject stopTrackingPhoneState(JSONArray params) {
    mAndroidFacade.stopTrackingPhoneState();
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Sets whether or not the ringer should be silent.",
      params = "Boolean silent"
  )
  public JSONObject setRingerSilent(JSONArray params) {
    boolean enabled = params.optBoolean(0, true);
    mAndroidFacade.setRingerSilent(enabled);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Returns the current ringer volume.",
      returns = "The current volume as an Integer."
  )
  public JSONObject getRingerVolume(JSONArray params) {
    int volume = mAndroidFacade.getRingerVolume();
    return JsonRpcResult.result(volume);
  }

  @Rpc(
      description = "Sets the ringer volume.",
      params = "Integer volume"
  )
  public JSONObject setRingerVolume(JSONArray params) {
    int volume;
    try {
      volume = params.getInt(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to set valume.", e);
    }
    mAndroidFacade.setRingerVolume(volume);
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Starts recording sensor data to be available for polling.")
  public JSONObject startSensing(JSONArray params) {
    mAndroidFacade.startSensing();
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Returns the current value of all collected sensor data.")
  public JSONObject readSensors(JSONArray params) {
    try {
      JSONObject result = buildJsonBundle(mAndroidFacade.readSensors());
      return JsonRpcResult.result(result);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build location.", e);
    }
  }

  @Rpc(description = "Stops collecting sensor data.")
  public JSONObject stopSensing(JSONArray params) {
    mAndroidFacade.stopSensing();
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Starts collecting location data.",
      params = "String accuracy (\"fine\", \"coarse\")"
  )
  public JSONObject startLocating(JSONArray params) {
    String accuracy = params.optString(0, "coarse");
    mAndroidFacade.startLocating(accuracy);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Returns the current location.",
      returns = "A map of location information."
  )
  public JSONObject readLocation(JSONArray params) {
    try {
      JSONObject result = buildJsonBundle(mAndroidFacade.readLocation());
      return JsonRpcResult.result(result);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build location.", e);
    }
  }

  @Rpc(description = "Stops collecting location data.")
  public JSONObject stopLocating(JSONArray params) {
    mAndroidFacade.stopLocating();
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Returns the last known location of the device.",
      returns = "A map of location information."
  )
  public JSONObject getLastKnownLocation(JSONArray params) {
    Bundle location = mAndroidFacade.getLastKnownLocation();
    try {
      return JsonRpcResult.result(buildJsonBundle(location));
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to construct location result.", e);
    }
  }

  @Rpc(
      description = "Returns a list of addresses for the given latitude and longitude.",
      params = "Double latitude, Double longitude, [Int maxResults]",
      returns = "A list of addresses."
  )
  public JSONObject geocode(JSONArray params) {
    List<Address> addresses;
    try {
      double latitude = params.getDouble(0);
      double longitude = params.getDouble(1);
      int maxResults = params.optInt(2, 1);
      addresses = mAndroidFacade.geocode(latitude, longitude, maxResults);
    } catch (Exception e) {
      return JsonRpcResult.error("Failed to geocode location.");
    }
    JSONArray result = new JSONArray();
    for (Address address : addresses) {
      result.put(buildJsonAddress(address));
    }
    return JsonRpcResult.result(result);
  }

  private JSONObject buildJsonAddress(Address address) {
    JSONObject result = new JSONObject();
    try {
      result.put("admin_area", address.getAdminArea());
      result.put("country_code", address.getCountryCode());
      result.put("country_name", address.getCountryName());
      result.put("feature_name", address.getFeatureName());
      result.put("phone", address.getPhone());
      result.put("locality", address.getLocality());
      result.put("postal_code", address.getPostalCode());
      result.put("sub_admin_area", address.getSubAdminArea());
      result.put("thoroughfare", address.getThoroughfare());
      result.put("url", address.getUrl());
    } catch (JSONException e) {
      Log.e(TAG, "Failed to build JSON for address: " + address, e);
      return null;
    }
    return result;
  }

  @Rpc(
      description = "Sends a text message.",
      params = "String subject, String body"
  )
  public JSONObject sendTextMessage(JSONArray params) {
    String destinationAddress;
    String text;
    try {
      destinationAddress = params.getString(0);
      text = params.getString(1);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to construct SMS.", e);
    }
    mAndroidFacade.sendTextMessage(destinationAddress, text);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Enables or disables Wifi according to the supplied boolean.",
      params = "Boolean enabled"
  )
  public JSONObject setWifiEnabled(JSONArray params) {
    boolean enabled;
    enabled = params.optBoolean(0, true);
    mAndroidFacade.setWifiEnabled(enabled);
    return JsonRpcResult.result(enabled);
  }

  @Rpc(
      description = "Starts an activity for result and returns the result.",
      params = "String action, [String uri]",
      returns = "A map of result values."
  )
  public JSONObject startActivityForResult(JSONArray params) {
    String action;
    try {
      action = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("No action specified.", e);
    }
    String uri = params.optString(1, null);
    Intent data = mAndroidFacade.startActivityForResult(action, uri);
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.");
    }
    return JsonRpcResult.result(result);
  }

  private JSONObject buildJsonIntent(Intent data) throws JSONException {
    JSONObject result = new JSONObject();
    result.put("data", data.toURI()); // Add result data URI.
    Bundle extras = data.getExtras(); // Add any result data extras.
    if (extras != null) {
      for (String key : extras.keySet()) {
        // TODO(damonkohler): Extras may not be strings.
        result.put(key, data.getStringExtra(key));
      }
    }
    return result;
  }

  @Rpc(
      description = "Starts an activity.",
      params = "String action, String uri"
  )
  public JSONObject startActivity(JSONArray params) {
    String action;
    try {
      action = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Action must be specified.", e);
    }
    String uri = params.optString(1, null);
    mAndroidFacade.startActivity(action, uri);
    return JsonRpcResult.empty();
  }

  @Rpc(
     description = "Displays a short-duration Toast notification.",
     params = "String message"
  )
  public JSONObject makeToast(JSONArray params) {
    try {
      mAndroidFacade.makeToast(params.getString(0));
    } catch (JSONException e) {
      return JsonRpcResult.error("Message must be specified.", e);
    }
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Displays an input {@link AlertDialog} and returns the input string.",
      params = "String title, String message",
      returns = "The input string."
  )
  public JSONObject getInput(JSONArray params) {
    String title = params.optString(0, "ASE Input");
    String message = params.optString(1, "Please enter value.");
    String result = mAndroidFacade.getInput(title, message);
    return JsonRpcResult.result(result);
  }

  @Rpc(
      description = "Vibrates the phone for a specified duration in milliseconds.",
      params = "[Integer milliseconds]"
  )
  public JSONObject vibrate(JSONArray params) {
    Long duration = params.optLong(0, 300);
    mAndroidFacade.vibrate(duration);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Displays a notification that will be canceled when the user clicks on it.",
      params = "String message, [String title], [String ticker]"
  )
  public JSONObject notify(JSONArray params) {
    String message;
    try {
      message = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Message must be specified.", e);
    }
    String title = params.optString(1, "ASE Notification");
    String ticker = params.optString(2, "ASE Notification");
    mAndroidFacade.notify(ticker, title, message);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Dial a contact/phone number by URI.",
      params = "String uri"
  )
  public JSONObject dial(JSONArray params) {
    String uri;
    try {
      uri = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("URI must be specified.", e);
    }
    mAndroidFacade.dial(uri);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Dial a phone number.",
      params = "String phone_number"
  )
  public JSONObject dialNumber(JSONArray params) {
    String number;
    try {
      number = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Phone number must be specified.", e);
    }
    mAndroidFacade.dial("tel:" + number);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Call a contact/phone number by URI.",
      params = "String uri"
  )
  public JSONObject call(JSONArray params) {
    String uri;
    try {
      uri = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("URI must be specified.", e);
    }
    mAndroidFacade.call(uri);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Call a phone number.",
      params = "String phone_number"
  )
  public JSONObject callNumber(JSONArray params) {
    String number;
    try {
      number = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Phone number must be specified.", e);
    }
    mAndroidFacade.call("tel:" + number);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Open a view by URI (i.e. browser, contacts, etc.).",
      params = "String uri"
  )
  public JSONObject view(JSONArray params) {
    String uri;
    try {
      uri = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("URI must be specified.", e);
    }
    mAndroidFacade.view(uri);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Open a web search for query.",
      params = "String query"
  )
  public JSONObject webSearch(JSONArray params) {
    String query;
    try {
      query = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Query must be specified.", e);
    }
    mAndroidFacade.view("http://www.google.com/search?q=" + query);
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Open a map search for query (e.g. pizza, 123 My Street).",
      params = "String query"
  )
  public JSONObject map(JSONArray params) {
    String query;
    try {
      query = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Query must be specified.", e);
    }
    mAndroidFacade.view("geo:0,0?q=" + query);
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Display the contacts activity.")
  public JSONObject showContacts(JSONArray params) {
    mAndroidFacade.view("content://contacts/people");
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Display the email activity.")
  public JSONObject email(JSONArray params) {
    mAndroidFacade.view("mailto://");
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Display content to be picked by URI (e.g. contacts)",
      params = "String uri",
      returns = "A map of result values."
  )
  public JSONObject pick(JSONArray params) {
    String uri;
    try {
      uri = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("URI must be specified.", e);
    }
    Intent data = mAndroidFacade.pick(uri);
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.", e);
    }
    return JsonRpcResult.result(result);
  }

  @Rpc(
      description = "Display list of contacts to pick from.",
      returns = "A map of result values."
  )
  public JSONObject pickContact(JSONArray params) {
    Intent data = mAndroidFacade.pick("content://contacts/people");
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.", e);
    }
    return JsonRpcResult.result(result);
  }


  @Rpc(
      description = "Display list of phone numbers to pick from.",
      returns = "A map of result values."
  )
  public JSONObject pickPhone(JSONArray params) {
    Intent data = mAndroidFacade.pick("content://contacts/phones");
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.");
    }
    return JsonRpcResult.result(result);
  }

   @Rpc(description = "Start barcode scanner.", returns = "A map of result values.")
  public JSONObject scanBarcode(JSONArray params) {
    Intent data = mAndroidFacade.scanBarcode();
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.");
    }
    return JsonRpcResult.result(result);
  }

   @Rpc(
       description = "Start image capture.",
       returns = "A map of result values."
  )
  public JSONObject captureImage(JSONArray params) {
    Intent data = mAndroidFacade.captureImage();
    JSONObject result;
    try {
      result = buildJsonIntent(data);
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build JSON result.");
    }
    return JsonRpcResult.result(result);
  }

  @Rpc(
      description = "Add an extra value to the result of this script.",
      params = "String name, String/Integer/Double/Boolean value"
  )
  public JSONObject putResultExtra(JSONArray params) {
    String name;
    Object value;
    try {
      name = params.getString(0);
      value = params.get(1);
    } catch (JSONException e) {
      return JsonRpcResult.error("Name and value must be specified.", e);
    }
    if (value instanceof String) {
      mAndroidFacade.putResultExtra(name, (String) value);
    }
    if (value instanceof Integer) {
      mAndroidFacade.putResultExtra(name, (Integer) value);
    }
    if (value instanceof Double) {
      mAndroidFacade.putResultExtra(name, (Double) value);
    }
    if (value instanceof Boolean) {
      mAndroidFacade.putResultExtra(name, (Boolean) value);
    }
    return JsonRpcResult.empty();
  }

  @Rpc(
      description = "Returns an extra value that was specified in the launch intent.",
      params = "String name, [Integer/Double/Boolean default]",
      returns = "The extra value."
  )
  public JSONObject getExtra(JSONArray params) {
    String name;
    try {
      name = params.getString(0);
    } catch (JSONException e) {
      return JsonRpcResult.error("Extra value name must be specified.", e);
    }
    Object defaultValue = params.opt(1);
    if (defaultValue == null) {
      return JsonRpcResult.result(mAndroidFacade.getStringExtra(name));
    }
    if (defaultValue instanceof Integer) {
      return JsonRpcResult.result(mAndroidFacade.getIntExtra(name, (Integer) defaultValue));
    }
    if (defaultValue instanceof Double) {
      return JsonRpcResult.result(mAndroidFacade.getDoubleExtra(name, (Double) defaultValue));
    }
    if (defaultValue instanceof Boolean) {
      return JsonRpcResult.result(mAndroidFacade.getBooleanExtra(name, (Boolean) defaultValue));
    }
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Exits the activity or service running the script.")
  public JSONObject exit(JSONArray params) {
    mAndroidFacade.exit();
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Exits the activity or service running the script with RESULT_OK.")
  public JSONObject exitWithResultOk(JSONArray params) {
    mAndroidFacade.exitWithResultOk();
    return JsonRpcResult.empty();
  }

  @Rpc(description = "Exits the activity or service running the script with RESULT_CANCELED.")
  public JSONObject exitWithResultCanceled(JSONArray params) {
    mAndroidFacade.exitWithResultCanceled();
    return JsonRpcResult.empty();
  }

  private JSONObject buildJsonBundle(Bundle bundle) throws JSONException {
    if (bundle == null) {
      return null;
    }
    JSONObject result = new JSONObject();
    for (String key : bundle.keySet()) {
      result.put(key, bundle.get(key));
    }
    return result;
  }

  @Rpc(
      description = "Receives the most recent event (i.e. location or sensor update, etc.",
      returns = "Map of event properties."
  )
  public JSONObject receiveEvent(JSONArray params) {
    try {
      return JsonRpcResult.result(buildJsonBundle(mAndroidFacade.receiveEvent()));
    } catch (JSONException e) {
      return JsonRpcResult.error("Failed to build event object.", e);
    }
  }

  @Rpc(
     description = "Returns a list of packages running activities or services.",
     returns = "List of packages running activities."
  )
  public JSONObject getRunningPackages(JSONArray params) {
    List<String> packages = mAndroidFacade.getRunningPackages().getStringArrayList("packages");
    JSONArray result = new JSONArray(packages);
    return JsonRpcResult.result(result);
  }

  @Rpc(
     description = "Force stops a package.",
     params = "String package_name"
  )
  public JSONObject forceStopPackage(JSONArray params) {
    try {
      mAndroidFacade.forceStopPackage(params.getString(0));
    } catch (JSONException e) {
      return JsonRpcResult.error("Package name parameter must be specified.", e);
    }
    return JsonRpcResult.empty();
  }

  /**
   * This must be called in the {@link Context}'s onActivityResult.
   */
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mAndroidFacade.onActivityResult(requestCode, resultCode, data);
  }

  /**
   * This must be called in the {@link Context}'s onDestroy.
   */
  public void onDestroy() {
    mAndroidFacade.onDestroy();
  }
}
