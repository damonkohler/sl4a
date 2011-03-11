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

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Tests {@link MethodDescriptor}.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class MethodDescriptorTest extends TestCase {

  @SuppressWarnings("unused")
  public void testCollectFromClass() {
    class A extends RpcReceiver {
      public A() {
        super(null);
      }

      public void notRpc() {
      }

      @Rpc(description = "rpc", returns = "nothing")
      public void rpc() {
      }

      @Override
      public void shutdown() {
      }
    }

    Collection<MethodDescriptor> methods = MethodDescriptor.collectFrom(A.class);

    assertEquals(1, methods.size());
    assertEquals("rpc", methods.toArray(new MethodDescriptor[0])[0].getName());
  }

  @SuppressWarnings("unused")
  public void testGetDefaultValue() throws Exception {
    class A {
      @Rpc(description = "rpc", returns = "nothing")
      public void rpcString(
          @RpcParameter(name = "name", description = "Description") String string,
          @RpcParameter(name = "name", description = "Description") Integer integer,
          @RpcParameter(name = "name", description = "Description") List<String> list,
          @RpcParameter(name = "name", description = "Description") @RpcOptional String stringOptonal,
          @RpcParameter(name = "name", description = "Description") @RpcOptional Integer integerOptional,
          @RpcParameter(name = "name", description = "Description") @RpcOptional List<String> listOptional,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("value") String stringDefault,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("value") Integer integerDefault,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("value") Boolean booleanDefault,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("value") List<String> listDefault,
          @RpcParameter(name = "name", description = "Description") @RpcDefault(value = "value", converter = ToUpper.class) String stringCustom,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("123") Integer integerCorrect,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("true") Boolean booleanTrue,
          @RpcParameter(name = "name", description = "Description") @RpcDefault("false") Boolean booleanFalse,
          @RpcParameter(name = "name", description = "Description") @RpcDefault(value = "a,b,c", converter = CSVString.class) List<String> listString,
          @RpcParameter(name = "name", description = "Description") @RpcDefault(value = "1,2,a", converter = CSVInteger.class) List<String> listIntegerInvalid,
          @RpcParameter(name = "name", description = "Description") @RpcDefault(value = "1,2,3", converter = CSVInteger.class) List<String> listIntegerValid) {
      }
    }
    Method m =
        A.class.getMethod("rpcString", String.class, Integer.class, List.class, String.class,
            Integer.class, List.class, String.class, Integer.class, Boolean.class, List.class,
            String.class, Integer.class, Boolean.class, Boolean.class, List.class, List.class,
            List.class);

    assertDefaultThrows(m, 0);
    assertDefaultThrows(m, 1);
    assertDefaultThrows(m, 2);
    assertDefaultThrows(m, 3);
    assertDefaultThrows(m, 4);
    assertDefaultThrows(m, 5);
    assertDefault("value", m, 6);
    assertDefaultThrows(m, 7);
    assertDefaultThrows(m, 8);
    assertDefaultThrows(m, 9);
    assertDefault("VALUE", m, 10);
    assertDefault(Integer.valueOf(123), m, 11);
    assertDefault(Boolean.TRUE, m, 12);
    assertDefault(Boolean.FALSE, m, 13);
    assertDefault(Arrays.asList("a", "b", "c"), m, 14);
    assertDefaultThrows(m, 15);
    assertDefault(Arrays.asList(Integer.valueOf(1), Integer.valueOf(2), Integer.valueOf(3)), m, 16);
  }

  private void assertDefault(Object expected, Method m, int param) {
    assertEquals(expected, MethodDescriptor.getDefaultValue(m.getParameterTypes()[param],
        m.getParameterAnnotations()[param]));
  }

  private void assertDefaultThrows(Method m, int param) {
    try {
      MethodDescriptor.getDefaultValue(m.getParameterTypes()[param],
          m.getParameterAnnotations()[param]);
    } catch (Exception expected) {
    }
  }

  public static final class ToUpper implements Converter<String> {
    @Override
    public String convert(String value) {
      return value.toUpperCase();
    }
  }

  public static final class CSVString implements Converter<List<String>> {
    @Override
    public List<String> convert(String value) {
      return Arrays.asList(value.split(","));
    }
  }

  public static final class CSVInteger implements Converter<List<Integer>> {
    @Override
    public List<Integer> convert(String value) {
      List<Integer> result = new ArrayList<Integer>();
      for (String v : value.split(",")) {
        try {
          result.add(Integer.decode(v));
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException("'" + value
              + "' is not an comma separated list of integers");
        }
      }
      return result;
    }
  }

  public void testConvertParameter() throws JSONException, RpcError {
    Class<?>[] types =
        new Class[] { Object.class, Boolean.class, Boolean.class, Boolean.class, Integer.class,
          Double.class, String.class };
    JSONArray parameters = new JSONArray();
    parameters.put(null);
    parameters.put(true);
    parameters.put(0);
    parameters.put(1);
    parameters.put(42);
    parameters.put(3.14159);
    parameters.put("Hello, world!");
    // TODO(damonkohler): Test converting JSONArrays into Lists.
    Object[] args = new Object[parameters.length()];
    for (int i = 0; i < parameters.length(); i++) {
      args[i] = MethodDescriptor.convertParameter(parameters, i, types[i]);
    }
    assertNull(args[0]);
    assertEquals(Boolean.TRUE, args[1]);
    assertEquals(Boolean.FALSE, args[2]);
    assertEquals(Boolean.TRUE, args[3]);
    assertEquals(42, args[4]);
    assertEquals(3.14159, args[5]);
    assertEquals("Hello, world!", args[6]);
  }
}
