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

package com.google.ase.jsonrpc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;

import com.google.ase.AseLog;

/**
 * A factory for {@link RpcInvoker} objects.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public class RpcInvokerFactory {
  private static JSONObject buildJsonBundle(Bundle bundle) throws JSONException {
    if (bundle == null) {
      return null;
    }
    JSONObject result = new JSONObject();
    for (String key : bundle.keySet()) {
      result.put(key, bundle.get(key));
    }
    return result;
  }


  private static JSONObject buildJsonIntent(Intent data) throws JSONException {
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

  private static JSONObject buildJsonAddress(Address address) {
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
  
  private static <T> JSONObject buildJsonList(final List<T> list) {
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

  /**
   * Produces an RpcInvoker implementation for a given list of parameter types.
   * 
   * @param parameterTypes an array of the (possibly generic) types of the
   *        parameters
   * @return an {@link RpcInvoker} object that can invoke methods with the given
   *         parameter types
   */
  public static RpcInvoker createInvoker(final Type[] parameterTypes) {
    return new RpcInvoker() {
      @Override
      public JSONObject invoke(final Method m, final Object receiver, final JSONArray parameters)
          throws JSONException {
        final Object[] args = new Object[parameterTypes.length];
        final Annotation annotations[][] = m.getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
          final Type parameterType = parameterTypes[i];

          Object defaultValue = RpcAnnotationHelper.getDefaultValue(annotations[i]);

          if (i < parameters.length()) {
            // Parameter is specified.
            // NOTE (Felix Arends): we have to add code to deal with generic types here.
            try {
              args[i] = ((Class <?>)parameterType).cast(parameters.get(i));
            } catch (ClassCastException e) {
              return JsonRpcResult.error("Argument " + (i + 1) + " should be of type " +
                  ((Class<?>)parameterType).getSimpleName() + ".");
            }
          } else {
            // Use default value of optional parameter.
            args[i] = defaultValue;
          }
        }

        try {
          final Object result = m.invoke(receiver, args);
          if (result instanceof Bundle) {
            return JsonRpcResult.result(buildJsonBundle((Bundle) result));
          } else if (result instanceof Intent) {
            return JsonRpcResult.result(buildJsonIntent((Intent) result));
          } else if (result instanceof List<?>) {
            return JsonRpcResult.result(buildJsonList((List <?>)result));
          } else {
            return JsonRpcResult.result(result);
          }
        } catch (Throwable t) {
          // All other exceptions are passed back to the client.
          AseLog.e("Server Exception", t);
          return JsonRpcResult.error("Exception", t);
        }
      }
    };
  }

  // This static class is not to be instantiated.
  private RpcInvokerFactory() {
  }
}
