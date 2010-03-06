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

package com.google.ase.jsonrpc;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;

import com.google.ase.AseLog;

public class JsonResultBuilders {
  private JsonResultBuilders() {
    // This is a utility class.
  }

  public static JSONObject buildJsonAddress(Address address) {
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
      AseLog.e("Failed to build JSON for address: " + address, e);
      return null;
    }
    return result;
  }

  public static JSONObject buildJsonBundle(Bundle bundle) throws JSONException {
    if (bundle == null) {
      return null;
    }
    JSONObject result = new JSONObject();
    for (String key : bundle.keySet()) {
      result.put(key, bundle.get(key));
    }
    return result;
  }

  public static JSONObject buildJsonIntent(Intent data) throws JSONException {
    JSONObject result = new JSONObject();
    Bundle extras = data.getExtras();
    if (extras != null) {
      for (String key : extras.keySet()) {
        Object value = extras.get(key);
        if (value instanceof Intent) {
          result.put(key, buildJsonIntent((Intent) value));
        } else {
          result.put(key, value);
        }
      }
    }
    return result;
  }

  public static <T> JSONObject buildJsonList(final List<T> list) {
    JSONArray result = new JSONArray();
    for (T item : list) {
      if (item instanceof Address) {
        result.put(buildJsonAddress((Address) item));
      } else {
        result.put(item);
      }
    }
    return JsonRpcResult.result(result);
  }

}
