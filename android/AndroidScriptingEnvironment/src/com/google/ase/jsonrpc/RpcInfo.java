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

import com.google.ase.AseLog;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.RpcError;

/**
 * Instances of this class describe specific RPCs on the server. An RPC on the server is described
 * by a receiving object of the call and the descriptor of the method of the object to call.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public final class RpcInfo {
  private final Object mReceiver;
  private final MethodDescriptor mMethodDescriptor;

  public RpcInfo(final Object receiver, final MethodDescriptor methodDescriptor) {
    mReceiver = receiver;
    mMethodDescriptor = methodDescriptor;
  }

  /**
   * Invokes the call that belongs to this object with the given parameters. Wraps the response
   * (possibly an exception) in a JSONObject.
   * 
   * @param parameters {@code JSONArray} containing the parameters
   * @return RPC response
   * @throws RpcError
   * @throws JSONException
   */
  public JSONObject invoke(final JSONArray parameters) throws RpcError, JSONException {
    final Type[] parameterTypes = mMethodDescriptor.getGenericParameterTypes();
    final Object[] args = new Object[parameterTypes.length];
    final Annotation annotations[][] = mMethodDescriptor.getParameterAnnotations();

    for (int i = 0; i < args.length; i++) {
      final Type parameterType = parameterTypes[i];
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
          throw new RpcError("Argument " + (i + 1) + " should be of type "
              + ((Class<?>) parameterType).getSimpleName() + ".");
        }
      } else if (MethodDescriptor.hasDefaultValue(annotations[i])) {
        args[i] = MethodDescriptor.getDefaultValue(parameterType, annotations[i]);
      } else {
        throw new RpcError("Argument " + (i + 1) + " is not present");
      }
    }

    Object result = null;
    try {
      result = mMethodDescriptor.getMethod().invoke(mReceiver, args);
    } catch (Exception e) {
      AseLog.e("Invocation error.", e.getCause());
      throw new RpcError(e.getCause().getMessage());
    }
    if (result instanceof Bundle) {
      return JsonRpcResult.result(JsonResultBuilders.buildJsonBundle((Bundle) result));
    } else if (result instanceof Intent) {
      return JsonRpcResult.result(JsonResultBuilders.buildJsonIntent((Intent) result));
    } else if (result instanceof List<?>) {
      return JsonRpcResult.result(JsonResultBuilders.buildJsonList((List<?>) result));
    } else {
      return JsonRpcResult.result(result);
    }
  }
}