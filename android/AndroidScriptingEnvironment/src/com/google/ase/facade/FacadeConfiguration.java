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

package com.google.ase.facade;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;

import com.google.ase.AseApplication;
import com.google.ase.facade.ui.UiFacade;
import com.google.ase.jsonrpc.JsonRpcServer;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.trigger.TriggerRepository;

/**
 * Encapsulates the list of supported facades and their construction.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Igor Karp (igor.v.karp@gmail.com)
 */
public class FacadeConfiguration {
  private final static SortedMap<String, MethodDescriptor> sRpcs =
      new TreeMap<String, MethodDescriptor>();
  
  static {
    List<MethodDescriptor> list = new ArrayList<MethodDescriptor>();
    list.addAll(MethodDescriptor.collectFrom(AndroidFacade.class));
    list.addAll(MethodDescriptor.collectFrom(MediaFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SpeechRecognitionFacade.class));
    list.addAll(MethodDescriptor.collectFrom(TextToSpeechFacade.class));
    list.addAll(MethodDescriptor.collectFrom(TelephonyManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(AlarmManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SensorManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(EventFacade.class));
    list.addAll(MethodDescriptor.collectFrom(LocationManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SettingsFacade.class));
    list.addAll(MethodDescriptor.collectFrom(UiFacade.class));
    for (MethodDescriptor rpc : list) {
      sRpcs.put(rpc.getName(), rpc);
    }
  }
  
  private FacadeConfiguration() {
    // Utility class.
  }

  /**
   * Returns a {@link JsonRpcServer} with all facades configured.
   * 
   * @param service
   *          service to configure facades with
   * @param intent
   *          intent to configure facades with
   * @param handler
   *          handler to configure facades with
   * @return a new {@link JsonRpcServer} configured with all facades
   */
  public static JsonRpcServer buildJsonRpcServer(Service service, Intent intent, Handler handler) {
    final TriggerRepository triggerRepository =
        ((AseApplication) service.getApplication()).getTriggerRepository();

    AndroidFacade androidFacade = new AndroidFacade(service, handler, intent);
    SettingsFacade settingsFacade = new SettingsFacade(service);
    UiFacade uiFacade = new UiFacade(service);
    MediaFacade mediaFacade = new MediaFacade();
    TextToSpeechFacade ttsFacade = new TextToSpeechFacade(service);
    SpeechRecognitionFacade srFacade = new SpeechRecognitionFacade(androidFacade);
    EventFacade eventFacade = new EventFacade(service);
    SensorManagerFacade sensorManagerFacade = new SensorManagerFacade(service, eventFacade);
    LocationManagerFacade locationManagerFacade = new LocationManagerFacade(service, eventFacade);
    TelephonyManagerFacade telephonyManagerFacade =
        new TelephonyManagerFacade(service, eventFacade);
    AlarmManagerFacade alarmManagerFacade =
        new AlarmManagerFacade(service, eventFacade, triggerRepository);
    return new JsonRpcServer(androidFacade, settingsFacade, mediaFacade, ttsFacade, srFacade,
        uiFacade, eventFacade, sensorManagerFacade, locationManagerFacade, telephonyManagerFacade,
        alarmManagerFacade);
  }

  /** Returns a list of {@link MethodDescriptor} objects for all facades. */
  public static List<MethodDescriptor> collectRpcDescriptors() {
    return new ArrayList<MethodDescriptor>(sRpcs.values());
  }
  
  /** Returns a method by name. */
  public static MethodDescriptor getMethodDescriptor(String name) {
    return sRpcs.get(name);
  }
}
