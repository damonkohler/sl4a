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

import org.json.JSONArray;
import org.json.JSONException;

import com.google.ase.AseLog;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.RpcError;
import com.google.ase.util.VisibleForTesting;

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
   * @param parameters
   *          {@code JSONArray} containing the parameters
   * @return RPC response
   * @throws RpcError
   * @throws JSONException
   */
  public Object invoke(final JSONArray parameters) throws RpcError, JSONException {
    final Type[] parameterTypes = mMethodDescriptor.getGenericParameterTypes();
    final Object[] args = new Object[parameterTypes.length];
    final Annotation annotations[][] = mMethodDescriptor.getParameterAnnotations();

    for (int i = 0; i < args.length; i++) {
      final Type parameterType = parameterTypes[i];
      if (i < parameters.length()) {
        // Parameter is specified.
        args[i] = convertParameter(parameters, i, parameterType);
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
    return result;
  }

  /**
   * Converts a parameter from JSON into a Java Object.
   * 
   * @return TODO
   */
  // TODO(damonkohler): This signature is a bit weird (auto-refactored). The obvious alternative
  // would be to work on one supplied parameter and return the converted parameter. However, that's
  // problematic because you lose the ability to call the getXXX methods on the JSON array.
  @VisibleForTesting
  static Object convertParameter(final JSONArray parameters, int index, Type type)
      throws JSONException, RpcError {
    try {
      // We must handle null and numbers explicitly because we cannot magically cast them. We
      // also need to convert implicitly from numbers to bools.
      if (parameters.isNull(index)) {
        return null;
      } else if (type == Boolean.class) {
        try {
          return parameters.getBoolean(index);
        } catch (JSONException e) {
          return new Boolean(parameters.getInt(index) != 0);
        }
      } else if (type == Long.class) {
        return parameters.getLong(index);
      } else if (type == Double.class) {
        return parameters.getDouble(index);
      } else if (type == Integer.class) {
        return parameters.getInt(index);
      } else {
        // Magically cast the parameter to the right Java type.
        return ((Class<?>) type).cast(parameters.get(index));
      }
    } catch (ClassCastException e) {
      throw new RpcError("Argument " + (index + 1) + " should be of type "
          + ((Class<?>) type).getSimpleName() + ".");
    }
  }
}