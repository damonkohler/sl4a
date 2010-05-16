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

import com.google.ase.AseApplication;
import com.google.ase.AseLog;
import com.google.ase.R;
import com.google.ase.facade.ui.UiFacade;
import com.google.ase.jsonrpc.JsonRpcServer;
import com.google.ase.jsonrpc.RpcReceiver;
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
    list.addAll(MethodDescriptor.collectFrom(PhoneFacade.class));
    list.addAll(MethodDescriptor.collectFrom(AlarmManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SensorManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(EventFacade.class));
    list.addAll(MethodDescriptor.collectFrom(LocationFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SettingsFacade.class));
    list.addAll(MethodDescriptor.collectFrom(UiFacade.class));
    list.addAll(MethodDescriptor.collectFrom(SmsFacade.class));
    list.addAll(MethodDescriptor.collectFrom(ContactsFacade.class));
    list.addAll(MethodDescriptor.collectFrom(CameraFacade.class));
    list.addAll(MethodDescriptor.collectFrom(WakeLockFacade.class));
    list.addAll(MethodDescriptor.collectFrom(WifiFacade.class));
    list.addAll(MethodDescriptor.collectFrom(ApplicationManagerFacade.class));
    list.addAll(MethodDescriptor.collectFrom(ToneGeneratorFacade.class));
    list.addAll(MethodDescriptor.collectFrom(CommonIntentsFacade.class));
    list.addAll(MethodDescriptor.collectFrom(PhoneFacade.class));

    // Bluetooth is not available before API level 5.
    try {
      list.addAll(MethodDescriptor.collectFrom(BluetoothFacade.class));
    } catch (Throwable t) {
      AseLog.e("Bluetooth not available.", t);
    }

    // TTS is not available before API level 4. For earlier platforms, we rely on Eyes-Free.
    try {
      list.addAll(MethodDescriptor.collectFrom(TextToSpeechFacade.class));
    } catch (Throwable t) {
      AseLog.e("TTS not available. Falling back to Eyes-Free project for TTS support.", t);
      list.addAll(MethodDescriptor.collectFrom(EyesFreeFacade.class));
    }

    for (MethodDescriptor rpc : list) {
      sRpcs.put(rpc.getName(), rpc);
    }
  }

  private FacadeConfiguration() {
    // Utility class.
  }

  /** Returns a list of {@link MethodDescriptor} objects for all facades. */
  public static List<MethodDescriptor> collectRpcDescriptors() {
    return new ArrayList<MethodDescriptor>(sRpcs.values());
  }

  /** Returns a method by name. */
  public static MethodDescriptor getMethodDescriptor(String name) {
    return sRpcs.get(name);
  }

  /**
   * Returns a {@link JsonRpcServer} with all facades configured.
   * 
   * @param service
   *          service to configure facades with
   * @param intent
   *          intent to configure facades with
   * @return a new {@link JsonRpcServer} configured with all facades
   */
  public static JsonRpcServer buildJsonRpcServer(Service service, Intent intent) {
    List<RpcReceiver> receivers = new ArrayList<RpcReceiver>();
    TriggerRepository triggerRepository =
        ((AseApplication) service.getApplication()).getTriggerRepository();

    AndroidFacade androidFacade = new AndroidFacade(service, intent, new AndroidFacade.Resources() {
      @Override
      public int getAseLogo48() {
        return R.drawable.ase_logo_48;
      }
    });

    EventFacade eventFacade = new EventFacade(service);
    CommonIntentsFacade commonIntentsFacade = new CommonIntentsFacade(androidFacade);

    receivers.add(androidFacade);
    receivers.add(eventFacade);
    receivers.add(commonIntentsFacade);
    receivers.add(new SettingsFacade(service));
    receivers.add(new UiFacade(service));
    receivers.add(new MediaFacade());
    receivers.add(new SpeechRecognitionFacade(androidFacade));
    receivers.add(new SensorManagerFacade(service, eventFacade));
    receivers.add(new LocationFacade(service, eventFacade));
    receivers.add(new PhoneFacade(service, androidFacade, eventFacade));
    receivers.add(new AlarmManagerFacade(service, eventFacade, triggerRepository));
    receivers.add(new SmsFacade(service));
    receivers.add(new ContactsFacade(service, commonIntentsFacade));
    receivers.add(new CameraFacade(androidFacade));
    receivers.add(new WakeLockFacade(service));
    receivers.add(new WifiFacade(service));
    receivers.add(new ApplicationManagerFacade(service, androidFacade));
    receivers.add(new ToneGeneratorFacade());

    // Bluetooth is not available before Android 2.0.
    try {
      receivers.add(new BluetoothFacade(androidFacade, eventFacade));
    } catch (Throwable t) {
      AseLog.e("Bluetooth not available.", t);
    }

    // TTS is not available before API level 4. For earlier platforms, we rely on Eyes-Free.
    try {
      receivers.add(new TextToSpeechFacade(service));
    } catch (Throwable t) {
      AseLog.e("TTS not available. Falling back to Eyes-Free project for TTS support.", t);
      receivers.add(new EyesFreeFacade(service));
    }

    return new JsonRpcServer(receivers);
  }
}
