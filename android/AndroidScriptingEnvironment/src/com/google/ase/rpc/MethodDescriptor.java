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

package com.google.ase.rpc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * An adapter that wraps {@code Method}.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class MethodDescriptor {
  private final Method mMethod;
  
  public MethodDescriptor(Method method) {
    mMethod = method;
  }

  public Method getMethod() {
    return mMethod;
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
  private static String getHelpForParameter(Type parameterType, Annotation[] annotations) {
    StringBuilder result = new StringBuilder();

    final Object defaultValue = RpcAnnotationHelper.getDefaultValue(annotations);
    final String name = RpcAnnotationHelper.getName(annotations);
    final String description = RpcAnnotationHelper.getDescription(annotations);
    boolean isOptionalParameter = RpcAnnotationHelper.isOptionalParameter(annotations);
    boolean hasDefaultValue = RpcAnnotationHelper.hasDefaultValue(annotations);

    appendTypeName(result, parameterType);
    result.append(" ");
    result.append(name);
    if (isOptionalParameter) {
      result.append("[optional");
      if (hasDefaultValue) {
        result.append(", default " + defaultValue);         
      }
      result.append("]");
    }
    
    if (description.length() > 0) {
      result.append(": ");
      result.append(description);
    }

    return result.toString();
  }

  /**
   * Appends the name of the given type to the {@link StringBuilder}.
   *
   * @param builder
   *          string builder to append to
   * @param type
   *          type whose name to append
   */
  private static void appendTypeName(final StringBuilder builder, final Type type) {
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
   * Returns parameter descriptors suitable for the RPC call text representation.
   * 
   * <p>Uses parameter name or default value if it is more meaningful as value.
   * 
   * @return an array of parameter descriptors
   */
  public ParameterDescriptor[] getDefaultParameterValues() {
    final Type[] parameterTypes = mMethod.getGenericParameterTypes();
    final Annotation[][] parametersAnnotations = mMethod.getParameterAnnotations();
    final ParameterDescriptor[] parameters = new ParameterDescriptor[parametersAnnotations.length];
    for (int index = 0; index < parameters.length; index ++) {
      parameters[index] = new ParameterDescriptor( 
          RpcAnnotationHelper.hasDefaultValue(parametersAnnotations[index]) ?
              String.valueOf(RpcAnnotationHelper.getDefaultValue(parametersAnnotations[index])) :
              RpcAnnotationHelper.getDescription(parametersAnnotations[index]),
          parameterTypes[index]);
    }
    return parameters;
  }
}
