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
import java.lang.reflect.Type;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

import com.google.ase.exception.AseRuntimeException;
import com.google.ase.rpc.MethodDescriptor;

/**
 * A factory for {@link RpcInvoker} objects.
 *
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class RpcInvokerFactory {
  /**
   * Produces an RpcInvoker implementation for a given list of parameter types.
   *
   * @param parameterTypes
   *          an array of the (possibly generic) types of the parameters
   * @return an {@link RpcInvoker} object that can invoke methods with the given parameter types
   */
  public static RpcInvoker createInvoker(final Type[] parameterTypes) {
    return new RpcInvoker() {
      @Override
      public JSONObject invoke(final MethodDescriptor m, final Object receiver, final JSONArray parameters)
          throws JSONException {
        final Type[] parameterTypes = m.getGenericParameterTypes();
        final Object[] args = new Object[parameterTypes.length];
        final Annotation annotations[][] = m.getParameterAnnotations();

        for (int i = 0; i < args.length; i++) {
          final Type parameterType = parameterTypes[i];
          Object defaultValue = MethodDescriptor.getDefaultValue(annotations[i]);
          if (i < parameters.length()) {
            // Parameter is specified.
            try {
              // We must handle numbers explicitly because we cannot magically cast between them.
              if (parameterType == Long.class) {
                args[i] = parameters.getLong(i);
              } else if (parameterType == Double.class) {
                args[i] = parameters.getDouble(i);
              } else if (parameterType == Integer.class) {
                args[i] = parameters.getInt(i);
              } else {
                // Magically cast the parameter to the right Java type.
                args[i] = ((Class<?>) parameterType).cast(parameters.get(i));
              }
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
          Object result = m.getMethod().invoke(receiver, args);
          if (result instanceof Bundle) {
            return JsonRpcResult.result(JsonResultBuilders.buildJsonBundle((Bundle) result));
          } else if (result instanceof Intent) {
            return JsonRpcResult.result(JsonResultBuilders.buildJsonIntent((Intent) result));
          } else if (result instanceof List<?>) {
            return JsonRpcResult.result(JsonResultBuilders.buildJsonList((List<?>) result));
          } else {
            return JsonRpcResult.result(result);
          }
        } catch (Exception e) {
          throw new AseRuntimeException("Failed to invoke: " + m.getName(), e.getCause());
        }
      }
    };
  }

  private RpcInvokerFactory() {
    // This static class is not to be instantiated.
  }
}
