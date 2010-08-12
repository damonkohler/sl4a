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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import android.test.AndroidTestCase;


import com.googlecode.android_scripting.facade.FacadeConfiguration;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcDefault;
import com.googlecode.android_scripting.rpc.RpcOptional;
import com.googlecode.android_scripting.rpc.RpcParameter;

/**
 * Checks runtime characteristics of RPC annotations.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class RpcAnnotationsTest extends AndroidTestCase {

  public void testParameterAnnotationsCardinality() {
    for (MethodDescriptor rpc : FacadeConfiguration.collectMethodDescriptors()) {
      int param = -1;
      for (Annotation[] annotations : rpc.getParameterAnnotations()) {
        param++;
        int countRpcParameter = 0;
        int countRpcDefault = 0;
        int countRpcOptional = 0;
        for (Annotation annotation : annotations) {
          if (annotation instanceof RpcParameter) {
            countRpcParameter++;
          }
          if (annotation instanceof RpcDefault) {
            countRpcDefault++;
          }
          if (annotation instanceof RpcOptional) {
            countRpcOptional++;
          }
        }
        if (countRpcParameter == 0) {
          fail("No @RpcParameter annotation found on parameter #" + param + " of " + rpc);
        }
        if (countRpcDefault + countRpcOptional > 1) {
          fail("Both @RpcDefault and @RpcOptional annotation found on parameter #" + param + " of "
              + rpc);
        }
      }
    }
  }

  public void testDefaultValues() {
    for (MethodDescriptor rpc : FacadeConfiguration.collectMethodDescriptors()) {
      int param = -1;
      Type[] parameterTypes = rpc.getGenericParameterTypes();
      for (Annotation[] annotations : rpc.getParameterAnnotations()) {
        param++;
        if (MethodDescriptor.hasExplicitDefaultValue(annotations)) {
          Object value = MethodDescriptor.getDefaultValue(parameterTypes[param], annotations);
          assertTrue("Default value " + value + " of parameter #" + param + " of " + rpc
              + " is not of expected type " + parameterTypes[param],
              ((Class<?>) parameterTypes[param]).isInstance(value));
        } else if (MethodDescriptor.hasDefaultValue(annotations)) {
          Object value = MethodDescriptor.getDefaultValue(parameterTypes[param], annotations);
          assertNull("Default value " + value + " of parameter #" + param + " of " + rpc
              + " is expected to be null", value);
        } else {
          try {
            Object value = MethodDescriptor.getDefaultValue(parameterTypes[param], annotations);
            fail("Default value " + value + " of parameter #" + param + " of " + rpc
                + " is not expected");
          } catch (Exception expected) {
          }
        }
      }
    }
  }
}
