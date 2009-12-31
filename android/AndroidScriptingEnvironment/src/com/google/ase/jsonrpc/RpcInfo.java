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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Instances of this class describe specific RPCs on the server. An RPC on the server is described
 * by a triple consisting of: - a receiving object of the call - the method of the object to call -
 * an {@link RpcInvoker} object that knows to parse a {@link JSONArray} for the parameters
 *
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class RpcInfo {
  private final Object mReceiver;
  private final Method mMethod;
  private final RpcInvoker mInvoker;

  public RpcInfo(final Object receiver, final Method method, final RpcInvoker invoker) {
    mReceiver = receiver;
    mMethod = method;
    mInvoker = invoker;
  }

  /**
   * Invokes the call that belongs to this object with the given parameters. Wraps the response
   * (possibly an exception) in a JSONObject.
   *
   * @param parameters
   *          {@code JSONArray} containing the parameters
   * @return RPC response
   */
  public JSONObject invoke(final JSONArray parameters) {
    try {
      return mInvoker.invoke(mMethod, mReceiver, parameters);
    } catch (JSONException e) {
      return JsonRpcResult.error("Remote Exception", e);
    }
  }

  /**
   * Appends the name of the given type to the {@link StringBuilder}.
   *
   * @param builder
   *          string builder to append to
   * @param type
   *          type whose name to append
   */
  private void appendTypeName(final StringBuilder builder, final Type type) {
    if (type instanceof Class<?>) {
      builder.append(((Class<?>) type).getSimpleName());
    } else {
      ParameterizedType parametrizedType = (ParameterizedType) type;
      builder.append(((Class<?>) parametrizedType.getRawType()).getSimpleName());
      builder.append("<");

      Type[] arguments = parametrizedType.getActualTypeArguments();
      for (int i = 0; i < arguments.length; i++) {
        if (i > 0) {
          builder.append(", ");
        }
        appendTypeName(builder, arguments[i]);
      }
      builder.append(">");
    }
  }

  /**
   * Returns the help string for one particular parameter. This respects parameters of type {@code
   * OptionalParameter<T>}.
   *
   * @param parameterType
   *          (generic) type of the parameter
   * @param annotation
   *          {@link RpcParameter} annotation of the type, may be null
   * @return string describing the parameter based on source code annotations
   */
  private String getHelpForParameter(Type parameterType, Annotation[] annotations) {
    StringBuilder result = new StringBuilder();

    Object defaultValue = RpcAnnotationHelper.getDefaultValue(annotations);
    String description = RpcAnnotationHelper.getDescription(annotations);
    boolean isOptionalParameter = RpcAnnotationHelper.isOptionalParameter(annotations);

    appendTypeName(result, parameterType);
    if (isOptionalParameter) {
      result.append("[optional, default " + defaultValue + "]: ");
    } else {
      result.append(":");
    }

    result.append(description);
    return result.toString();
  }

  public String getName() {
    return mMethod.getName();
  }

  /**
   * Returns a human-readable help text for this RPC, based on annotations in the source code.
   *
   * @return derived help string
   */
  public String getHelp() {
    final StringBuilder helpBuilder = new StringBuilder();
    final Rpc rpcAnnotation = mMethod.getAnnotation(Rpc.class);

    helpBuilder.append(mMethod.getName());
    helpBuilder.append("(");
    final Class<?>[] parameterTypes = mMethod.getParameterTypes();
    final Type[] genericParameterTypes = mMethod.getGenericParameterTypes();
    final Annotation[][] annotations = mMethod.getParameterAnnotations();
    for (int i = 0; i < parameterTypes.length; i++) {
      if (i == 0) {
        helpBuilder.append("\n  ");
      } else {
        helpBuilder.append(",\n  ");
      }

      helpBuilder.append(getHelpForParameter(genericParameterTypes[i], annotations[i]));
    }
    helpBuilder.append(")\n\n");
    helpBuilder.append(rpcAnnotation.description());
    helpBuilder.append("\n");
    if (rpcAnnotation.returns() != "") {
      helpBuilder.append("\nReturns: ");
      helpBuilder.append(rpcAnnotation.returns());
      helpBuilder.append("\n");
    }

    return helpBuilder.toString();
  }
}