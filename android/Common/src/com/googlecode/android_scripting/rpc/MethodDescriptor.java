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

package com.googlecode.android_scripting.rpc;

import android.content.Intent;
import android.net.Uri;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.facade.AndroidFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.util.VisibleForTesting;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An adapter that wraps {@code Method}.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public final class MethodDescriptor {
  private static final Map<Class<?>, Converter<?>> sConverters = populateConverters();

  private final Method mMethod;
  private final Class<? extends RpcReceiver> mClass;

  public MethodDescriptor(Class<? extends RpcReceiver> clazz, Method method) {
    mClass = clazz;
    mMethod = method;
  }

  @Override
  public String toString() {
    return mMethod.getDeclaringClass().getCanonicalName() + "." + mMethod.getName();
  }

  /** Collects all methods with {@code RPC} annotation from given class. */
  public static Collection<MethodDescriptor> collectFrom(Class<? extends RpcReceiver> clazz) {
    List<MethodDescriptor> descriptors = new ArrayList<MethodDescriptor>();
    for (Method method : clazz.getMethods()) {
      if (method.isAnnotationPresent(Rpc.class)) {
        descriptors.add(new MethodDescriptor(clazz, method));
      }
    }
    return descriptors;
  }

  /**
   * Invokes the call that belongs to this object with the given parameters. Wraps the response
   * (possibly an exception) in a JSONObject.
   * 
   * @param parameters
   *          {@code JSONArray} containing the parameters
   * @return result
   * @throws Throwable
   */
  public Object invoke(RpcReceiverManager manager, final JSONArray parameters) throws Throwable {
    // Issue track call first in case of failure.
    Analytics.track("api", getName());

    final Type[] parameterTypes = getGenericParameterTypes();
    final Object[] args = new Object[parameterTypes.length];
    final Annotation annotations[][] = getParameterAnnotations();

    if (parameters.length() > args.length) {
      throw new RpcError("Too many parameters specified.");
    }

    for (int i = 0; i < args.length; i++) {
      final Type parameterType = parameterTypes[i];
      if (i < parameters.length()) {
        args[i] = convertParameter(parameters, i, parameterType);
      } else if (MethodDescriptor.hasDefaultValue(annotations[i])) {
        args[i] = MethodDescriptor.getDefaultValue(parameterType, annotations[i]);
      } else {
        throw new RpcError("Argument " + (i + 1) + " is not present");
      }
    }

    Object result = null;
    try {
      result = manager.invoke(mClass, mMethod, args);
    } catch (Throwable t) {
      throw t.getCause();
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
      } else if (type == Intent.class) {
        return buildIntent(parameters.getJSONObject(index));
      } else {
        // Magically cast the parameter to the right Java type.
        return ((Class<?>) type).cast(parameters.get(index));
      }
    } catch (ClassCastException e) {
      throw new RpcError("Argument " + (index + 1) + " should be of type "
          + ((Class<?>) type).getSimpleName() + ".");
    }
  }

  public static Object buildIntent(JSONObject jsonObject) throws JSONException {
    Intent intent = new Intent();
    if (jsonObject.has("action")) {
      intent.setAction(jsonObject.getString("action"));
    }
    if (jsonObject.has("data") && jsonObject.has("type")) {
      intent.setDataAndType(Uri.parse(jsonObject.optString("data", null)),
          jsonObject.optString("type", null));
    } else if (jsonObject.has("data")) {
      intent.setData(Uri.parse(jsonObject.optString("data", null)));
    } else if (jsonObject.has("type")) {
      intent.setType(jsonObject.optString("type", null));
    }
    if (jsonObject.has("packagename") && jsonObject.has("classname")) {
      intent.setClassName(jsonObject.getString("packagename"), jsonObject.getString("classname"));
    }
    if (jsonObject.has("flags")) {
      intent.setFlags(jsonObject.getInt("flags"));
    }
    if (!jsonObject.isNull("extras")) {
      AndroidFacade.putExtrasFromJsonObject(jsonObject.getJSONObject("extras"), intent);
    }
    if (!jsonObject.isNull("categories")) {
      JSONArray categories = jsonObject.getJSONArray("categories");
      for (int i = 0; i < categories.length(); i++) {
        intent.addCategory(categories.getString(i));
      }
    }
    return intent;
  }

  public Method getMethod() {
    return mMethod;
  }

  public Class<? extends RpcReceiver> getDeclaringClass() {
    return mClass;
  }

  public String getName() {
    if (mMethod.isAnnotationPresent(RpcName.class)) {
      return mMethod.getAnnotation(RpcName.class).name();
    }
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
    StringBuilder helpBuilder = new StringBuilder();
    Rpc rpcAnnotation = mMethod.getAnnotation(Rpc.class);

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
    if (!rpcAnnotation.returns().equals("")) {
      helpBuilder.append("\n");
      helpBuilder.append("\nReturns:\n  ");
      helpBuilder.append(rpcAnnotation.returns());
    }

    if (mMethod.isAnnotationPresent(RpcStartEvent.class)) {
      String eventName = mMethod.getAnnotation(RpcStartEvent.class).value();
      helpBuilder.append(String.format("\n\nGenerates \"%s\" events.", eventName));
    }

    if (mMethod.isAnnotationPresent(RpcDeprecated.class)) {
      String replacedBy = mMethod.getAnnotation(RpcDeprecated.class).value();
      String release = mMethod.getAnnotation(RpcDeprecated.class).release();
      helpBuilder.append(String.format("\n\nDeprecated in %s! Please use %s instead.", release,
          replacedBy));
    }

    return helpBuilder.toString();
  }

  /**
   * Returns the help string for one particular parameter. This respects optional parameters.
   * 
   * @param parameterType
   *          (generic) type of the parameter
   * @param annotations
   *          annotations of the parameter, may be null
   * @return string describing the parameter based on source code annotations
   */
  private static String getHelpForParameter(Type parameterType, Annotation[] annotations) {
    StringBuilder result = new StringBuilder();

    appendTypeName(result, parameterType);
    result.append(" ");
    result.append(getName(annotations));
    if (hasDefaultValue(annotations)) {
      result.append("[optional");
      if (hasExplicitDefaultValue(annotations)) {
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
   * <p>
   * Uses parameter value, default value or name, whatever is available first.
   * 
   * @return an array of parameter descriptors
   */
  public ParameterDescriptor[] getParameterValues(String[] values) {
    Type[] parameterTypes = mMethod.getGenericParameterTypes();
    Annotation[][] parametersAnnotations = mMethod.getParameterAnnotations();
    ParameterDescriptor[] parameters = new ParameterDescriptor[parametersAnnotations.length];
    for (int index = 0; index < parameters.length; index++) {
      String value;
      if (index < values.length) {
        value = values[index];
      } else if (hasDefaultValue(parametersAnnotations[index])) {
        Object defaultValue = getDefaultValue(parameterTypes[index], parametersAnnotations[index]);
        if (defaultValue == null) {
          value = null;
        } else {
          value = String.valueOf(defaultValue);
        }
      } else {
        value = getName(parametersAnnotations[index]);
      }
      parameters[index] = new ParameterDescriptor(value, parameterTypes[index]);
    }
    return parameters;
  }

  /**
   * Returns parameter hints.
   * 
   * @return an array of parameter hints
   */
  public String[] getParameterHints() {
    Annotation[][] parametersAnnotations = mMethod.getParameterAnnotations();
    String[] hints = new String[parametersAnnotations.length];
    for (int index = 0; index < hints.length; index++) {
      String name = getName(parametersAnnotations[index]);
      String description = getDescription(parametersAnnotations[index]);
      String hint = "No paramenter description.";
      if (!name.equals("") && !description.equals("")) {
        hint = name + ": " + description;
      } else if (!name.equals("")) {
        hint = name;
      } else if (!description.equals("")) {
        hint = description;
      }
      hints[index] = hint;
    }
    return hints;
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
    throw new IllegalStateException("No parameter name");
  }

  /**
   * Extracts the parameter description from its annotations.
   * 
   * @param annotations
   *          the annotations of the parameter
   * @return the description of the parameter
   */
  private static String getDescription(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcParameter) {
        return ((RpcParameter) a).description();
      }
    }
    throw new IllegalStateException("No parameter description");
  }

  /**
   * Returns the default value for a specific parameter.
   * 
   * @param parameterType
   *          parameterType
   * @param annotations
   *          annotations of the parameter
   */
  public static Object getDefaultValue(Type parameterType, Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcDefault) {
        RpcDefault defaultAnnotation = (RpcDefault) a;
        Converter<?> converter = converterFor(parameterType, defaultAnnotation.converter());
        return converter.convert(defaultAnnotation.value());
      } else if (a instanceof RpcOptional) {
        return null;
      }
    }
    throw new IllegalStateException("No default value for " + parameterType);
  }

  @SuppressWarnings("rawtypes")
  private static Converter<?> converterFor(Type parameterType,
      Class<? extends Converter> converterClass) {
    if (converterClass == Converter.class) {
      Converter<?> converter = sConverters.get(parameterType);
      if (converter == null) {
        throw new IllegalArgumentException("No predefined converter found for " + parameterType);
      }
      return converter;
    }
    try {
      Constructor<?> constructor = converterClass.getConstructor(new Class<?>[0]);
      return (Converter<?>) constructor.newInstance(new Object[0]);
    } catch (Exception e) {
      throw new IllegalArgumentException("Cannot create converter from "
          + converterClass.getCanonicalName());
    }
  }

  /**
   * Determines whether or not this parameter has default value.
   * 
   * @param annotations
   *          annotations of the parameter
   */
  public static boolean hasDefaultValue(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcDefault || a instanceof RpcOptional) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns whether the default value is specified for a specific parameter.
   * 
   * @param annotations
   *          annotations of the parameter
   */
  @VisibleForTesting
  static boolean hasExplicitDefaultValue(Annotation[] annotations) {
    for (Annotation a : annotations) {
      if (a instanceof RpcDefault) {
        return true;
      }
    }
    return false;
  }

  /** Returns the converters for {@code String}, {@code Integer} and {@code Boolean}. */
  private static Map<Class<?>, Converter<?>> populateConverters() {
    Map<Class<?>, Converter<?>> converters = new HashMap<Class<?>, Converter<?>>();
    converters.put(String.class, new Converter<String>() {
      @Override
      public String convert(String value) {
        return value;
      }
    });
    converters.put(Integer.class, new Converter<Integer>() {
      @Override
      public Integer convert(String input) {
        try {
          return Integer.decode(input);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("'" + input + "' is not an integer");
        }
      }
    });
    converters.put(Boolean.class, new Converter<Boolean>() {
      @Override
      public Boolean convert(String input) {
        if (input == null) {
          return null;
        }
        input = input.toLowerCase();
        if (input.equals("true")) {
          return Boolean.TRUE;
        }
        if (input.equals("false")) {
          return Boolean.FALSE;
        }
        throw new IllegalArgumentException("'" + input + "' is not a boolean");
      }
    });
    return converters;
  }
}
