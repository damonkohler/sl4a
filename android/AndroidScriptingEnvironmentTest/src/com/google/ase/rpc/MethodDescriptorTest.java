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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.ase.rpc.Converter;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.Rpc;
import com.google.ase.rpc.RpcDefault;
import com.google.ase.rpc.RpcOptional;
import com.google.ase.rpc.RpcParameter;

import junit.framework.TestCase;

/**
 * Tests {@link MethodDescriptor}.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class MethodDescriptorTest extends TestCase {
  
  public void testCollectFromClass() {
    @SuppressWarnings("unused")
    class A {
      public void notRpc() {}
      @Rpc(description = "rpc", returns = "nothing") public void rpc() {}
    }
    
    Collection<MethodDescriptor> methods = MethodDescriptor.collectFrom(A.class);
    
    assertEquals(1, methods.size());
    assertEquals("rpc", methods.toArray(new MethodDescriptor[0])[0].getName());
  }
  
  public void testGetDefaultValue() throws Exception {
    @SuppressWarnings("unused")
    class A {
      @Rpc(description = "rpc", returns = "nothing") public void rpcString(
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
          @RpcParameter(name = "name", description = "Description") @RpcDefault(value = "1,2,3", converter = CSVInteger.class) List<String> listIntegerValid
          ) {}
    }
    Method m = A.class.getMethod("rpcString", String.class, Integer.class, List.class,
        String.class, Integer.class, List.class,
        String.class, Integer.class, Boolean.class, List.class,
        String.class, Integer.class, Boolean.class, Boolean.class,
        List.class, List.class, List.class);
    
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
}
