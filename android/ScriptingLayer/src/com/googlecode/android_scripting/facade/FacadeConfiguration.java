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

package com.googlecode.android_scripting.facade;

import com.google.common.collect.Maps;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.facade.ui.UiFacade;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcDeprecated;
import com.googlecode.android_scripting.rpc.RpcMinSdk;
import com.googlecode.android_scripting.rpc.RpcStartEvent;
import com.googlecode.android_scripting.rpc.RpcStopEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Encapsulates the list of supported facades and their construction.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Igor Karp (igor.v.karp@gmail.com)
 */
public class FacadeConfiguration {
  private final static Set<Class<? extends RpcReceiver>> sFacadeClassList;
  private final static SortedMap<String, MethodDescriptor> sRpcs =
      new TreeMap<String, MethodDescriptor>();

  private static int sSdkLevel;

  static {

    if (android.os.Build.VERSION.SDK == null) {
      sSdkLevel = 8; // For documentation purposes.
    } else {
      try {
        sSdkLevel = Integer.parseInt(android.os.Build.VERSION.SDK);
      } catch (NumberFormatException e) {
        Log.e(e);
      }
    }

    sFacadeClassList = new HashSet<Class<? extends RpcReceiver>>();
    sFacadeClassList.add(AndroidFacade.class);
    sFacadeClassList.add(ApplicationManagerFacade.class);
    sFacadeClassList.add(CameraFacade.class);
    sFacadeClassList.add(CommonIntentsFacade.class);
    sFacadeClassList.add(ContactsFacade.class);
    sFacadeClassList.add(EventFacade.class);
    sFacadeClassList.add(LocationFacade.class);
    sFacadeClassList.add(PhoneFacade.class);
    sFacadeClassList.add(MediaRecorderFacade.class);
    sFacadeClassList.add(SensorManagerFacade.class);
    sFacadeClassList.add(SettingsFacade.class);
    sFacadeClassList.add(SmsFacade.class);
    sFacadeClassList.add(SpeechRecognitionFacade.class);
    sFacadeClassList.add(ToneGeneratorFacade.class);
    sFacadeClassList.add(WakeLockFacade.class);
    sFacadeClassList.add(WifiFacade.class);
    sFacadeClassList.add(UiFacade.class);
    sFacadeClassList.add(BatteryManagerFacade.class);
    sFacadeClassList.add(ActivityResultFacade.class);
    sFacadeClassList.add(MediaPlayerFacade.class);
    sFacadeClassList.add(PreferencesFacade.class);

    if (sSdkLevel >= 4) {
      sFacadeClassList.add(TextToSpeechFacade.class);
    } else {
      sFacadeClassList.add(EyesFreeFacade.class);
    }

    if (sSdkLevel >= 5) {
      sFacadeClassList.add(BluetoothFacade.class);
    }

    if (sSdkLevel >= 7) {
      sFacadeClassList.add(SignalStrengthFacade.class);
    }

    if (sSdkLevel >= 8) {
      sFacadeClassList.add(WebCamFacade.class);
    }

    for (Class<? extends RpcReceiver> recieverClass : sFacadeClassList) {
      for (MethodDescriptor rpcMethod : MethodDescriptor.collectFrom(recieverClass)) {
        sRpcs.put(rpcMethod.getName(), rpcMethod);
      }
    }
  }

  private FacadeConfiguration() {
    // Utility class.
  }

  public static int getSdkLevel() {
    return sSdkLevel;
  }

  /** Returns a list of {@link MethodDescriptor} objects for all facades. */
  public static List<MethodDescriptor> collectMethodDescriptors() {
    return new ArrayList<MethodDescriptor>(sRpcs.values());
  }

  /**
   * Returns a list of not deprecated {@link MethodDescriptor} objects for facades supported by the
   * current SDK version.
   */
  public static List<MethodDescriptor> collectSupportedMethodDescriptors() {
    List<MethodDescriptor> list = new ArrayList<MethodDescriptor>();
    for (MethodDescriptor descriptor : sRpcs.values()) {
      Method method = descriptor.getMethod();
      if (method.isAnnotationPresent(RpcDeprecated.class)) {
        continue;
      } else if (method.isAnnotationPresent(RpcMinSdk.class)) {
        int requiredSdkLevel = method.getAnnotation(RpcMinSdk.class).value();
        if (sSdkLevel < requiredSdkLevel) {
          continue;
        }
      }
      list.add(descriptor);
    }
    return list;
  }

  public static Map<String, MethodDescriptor> collectStartEventMethodDescriptors() {
    Map<String, MethodDescriptor> map = Maps.newHashMap();
    for (MethodDescriptor descriptor : sRpcs.values()) {
      Method method = descriptor.getMethod();
      if (method.isAnnotationPresent(RpcStartEvent.class)) {
        String eventName = method.getAnnotation(RpcStartEvent.class).value();
        if (map.containsKey(eventName)) {
          throw new RuntimeException("Duplicate start event method descriptor found.");
        }
        map.put(eventName, descriptor);
      }
    }
    return map;
  }

  public static Map<String, MethodDescriptor> collectStopEventMethodDescriptors() {
    Map<String, MethodDescriptor> map = Maps.newHashMap();
    for (MethodDescriptor descriptor : sRpcs.values()) {
      Method method = descriptor.getMethod();
      if (method.isAnnotationPresent(RpcStopEvent.class)) {
        String eventName = method.getAnnotation(RpcStopEvent.class).value();
        if (map.containsKey(eventName)) {
          throw new RuntimeException("Duplicate stop event method descriptor found.");
        }
        map.put(eventName, descriptor);
      }
    }
    return map;
  }

  /** Returns a method by name. */
  public static MethodDescriptor getMethodDescriptor(String name) {
    return sRpcs.get(name);
  }

  public static Collection<Class<? extends RpcReceiver>> getFacadeClasses() {
    return sFacadeClassList;
  }
}
