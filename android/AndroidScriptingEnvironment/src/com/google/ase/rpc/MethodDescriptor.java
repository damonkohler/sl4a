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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.ase.AseLog;

/**
 * An adapter that wraps {@code Method}.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public final class MethodDescriptor {
  private static final Map<Class<?>, Converter<?>> sConverters = populateConverters();

  private final Method mMethod;
  
  public MethodDescriptor(Method method) {
    mMethod = method;
  }
  
  /** Collects all methods with {@code RPC} annotation from given class. */
  public static Collection<MethodDescriptor> collectFrom(Class<?> clazz) {
    List<MethodDescriptor> descriptors = new ArrayList<MethodDescriptor>();
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(Rpc.class)) {
        descriptors.add(new MethodDescriptor(method));
      }
    }

    return descriptors;
  }

  public Method getMethod() {
    return mMethod;
  }

  public String getName() {
    return mMethod.getName();
  }

  public Type[] getGenericParameterTypes() {
    return mMethod.getGenericParameterTypes();
  }

  public Annotation[][] getParameterAnnotations() {
    return mMethod.getParameterAnnotations();
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
   * Returns the help string for one particular parameter. This respects optional parameters.
   *
   * @param parameterType (generic) type of the parameter
   * @param annotations annotations of the parameter, may be null
   * @return string describing the parameter based on source code annotations
   */
  private static String getHelpForParameter(Type parameterType, Annotation[] annotations) {
    StringBuilder result = new StringBuilder();

    appendTypeName(result, parameterType);
    result.append(" ");
    result.append(getName(annotations));
    if (isOptionalParameter(annotations)) {
      result.append("[optional");
      if (hasDefaultValue(annotations)) {
        result.append(", default " + getDefaultValue(parameterType, annotations));         
      }
      result.append("]");
    }
    
    String description = getDescription(annotations);
    if (description.length() > 0) {
      result.append(": ");
      result.append(description);
    }

    return result.toString();
  }

  /**
   * Appends the name of the given type to the {@link StringBuilder}.
   *
   * @param builder string builder to append to
   * @param type type whose name to append
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
          hasDefaultValue(parametersAnnotations[index])
              ? String.valueOf(getDefaultValue(parameterTypes[index], parametersAnnotations[index]))
              : getDescription(parametersAnnotations[index]),
          parameterTypes[index]);
    }
    return parameters;
  }

  /**
   * Extracts the formal parameter name from an annotation.
   * 
   * @param annotations
   *          the annotations of the parameter
   * @return the formal name of the parameter
   */
  private static String getName(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return ((RpcParameter) a).name();
      }
    }
    return "(unknown)";
  }

  /**
   * Extracts the parameter description from its annotations.
   * 
   * @param annotations the annotations of the parameter
   * @return the description of the parameter
   */
  private static String getDescription(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return ((RpcParameter) a).description();
      }
    }
    return "(no description)";
  }

  /**
   * Returns the default value for a specific parameter.
   * @param parameterType parameterType
   * @param annotations annotations of the parameter
   */
  public static Object getDefaultValue(Type parameterType, Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcDefault) {
        RpcDefault defaultAnnotation = (RpcDefault) a;
        Converter<?> converter = converterFor(parameterType, defaultAnnotation.converter());
        return converter == null ? null : converter.convert(defaultAnnotation.value());
      }
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  private static Converter<?> converterFor(Type parameterType,
      Class<? extends Converter> converterClass) {
    if (converterClass == Converter.class) {
      Converter<?> converter = sConverters.get(parameterType);
      if (converter == null) {
        AseLog.e("No converter found for " + parameterType);
      }
      return converter;
    }
    try {
      Constructor<?> constructor = converterClass.getConstructor(new Class<?>[0]);
      return (Converter<?>) constructor.newInstance(new Object[0]);
    } catch (Exception e) {
      AseLog.e("Cannot create converter from " + converterClass.getCanonicalName());
    }
    return null;
  }

  /**
   * Returns whether the default value is specified for a specific parameter.
   * 
   * @param annotations annotations of the parameter
   */
  private static boolean hasDefaultValue(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcDefault) {
        return true;
      }
    }
    return false;
  }

  /**
   * Determines whether or not this parameter is optional.
   * 
   * @param annotations annotations of the parameter
   */
  private static boolean isOptionalParameter(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcOptional) {
        return true;
      }
    }
    return false;
  }

  /** Returns the converters for {@code String}, {@code Integer} and {@code Boolean}. */
  private static Map<Class<?>, Converter<?>> populateConverters() {
    Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();
    converters.put(String.class, new Converter<String>() {
      @Override public String convert(String value) {
        return value;
      }});
    converters.put(Integer.class, new Converter<Integer>() {
      @Override public Integer convert(String input) {
        AseLog.v("Converting '" + input + "' as integer");
        try {
          return Integer.decode(input);
        } catch (NumberFormatException e) {
          AseLog.e("'" + input + "' is not an integer");
          return null;
        }
      }});
    converters.put(Boolean.class, new Converter<Boolean>() {
      @Override public Boolean convert(String input) {
        AseLog.v("Converting '" + input + "' as boolean");
        if (input == null) {
          return null;
        }
        input = input.toLowerCase();
        if (input.equals("true")) {
          return true;
        }
        if (input.equals("false")) {
          return false;
        }
        AseLog.e("'" + input + "' is not a boolean");
        return null;
      }});
    return converters;
  }
}
