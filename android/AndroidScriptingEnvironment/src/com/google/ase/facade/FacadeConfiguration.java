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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;

import com.google.ase.facade.ui.UiFacade;
import com.google.ase.jsonrpc.JsonRpcServer;
import com.google.ase.jsonrpc.RpcInfo;

/**
 * Encapsulates the list of supported facades and their construction.
 *
 * @author Damon Kohler <damonkohler@gmail.com>
 */
public class FacadeConfiguration {
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
    AlarmManagerFacade alarmManagerFacade = new AlarmManagerFacade(service, eventFacade);
    return new JsonRpcServer(androidFacade, settingsFacade, mediaFacade, ttsFacade, srFacade,
        uiFacade, eventFacade, sensorManagerFacade, locationManagerFacade, telephonyManagerFacade,
        alarmManagerFacade);
  }

  /**
   * Returns a list of {@link RpcInfo} objects for all facades.
   */
  public static List<RpcInfo> buildRpcInfoList() {
    List<RpcInfo> list = new ArrayList<RpcInfo>();
    list.addAll(JsonRpcServer.buildRpcInfoMap(AndroidFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(MediaFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(SpeechRecognitionFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(TextToSpeechFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(TelephonyManagerFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(AlarmManagerFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(SensorManagerFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(EventFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(LocationManagerFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(SettingsFacade.class).values());
    list.addAll(JsonRpcServer.buildRpcInfoMap(UiFacade.class).values());
    Collections.sort(list, new Comparator<RpcInfo>() {
      public int compare(RpcInfo info1, RpcInfo info2) {
        return info1.getName().compareTo(info2.getName());
      }
    });
    return list;
  }
}
