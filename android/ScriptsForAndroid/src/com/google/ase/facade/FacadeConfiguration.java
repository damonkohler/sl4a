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

import android.app.Service;
import android.content.Intent;

import com.google.ase.AseLog;
import com.google.ase.facade.ui.UiFacade;
import com.google.ase.jsonrpc.JsonRpcServer;
import com.google.ase.jsonrpc.RpcReceiver;
import com.google.ase.rpc.MethodDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Encapsulates the list of supported facades and their construction.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Igor Karp (igor.v.karp@gmail.com)
 */
public class FacadeConfiguration {

  private final static SortedMap<String, MethodDescriptor> sRpcs =
      new TreeMap<String, MethodDescriptor>();
  
  private final static List<Class<? extends RpcReceiverFacade>> mFacadeClassList;


  static {

    int sdkVersion = 0;

    try {
      sdkVersion = Integer.parseInt(android.os.Build.VERSION.SDK);
    } catch (NumberFormatException e) {
      AseLog.e(e);
    }

    mFacadeClassList = new ArrayList<Class<? extends RpcReceiverFacade>>();
    mFacadeClassList.add(AndroidFacade.class);
    mFacadeClassList.add(RecorderFacade.class);
    mFacadeClassList.add(SpeechRecognitionFacade.class);
    mFacadeClassList.add(PhoneFacade.class);
    mFacadeClassList.add(AlarmManagerFacade.class);
    mFacadeClassList.add(SensorManagerFacade.class);
    mFacadeClassList.add(EventFacade.class);
    mFacadeClassList.add(LocationFacade.class);
    mFacadeClassList.add(SettingsFacade.class);
    mFacadeClassList.add(UiFacade.class);
    mFacadeClassList.add(SmsFacade.class);
    mFacadeClassList.add(ContactsFacade.class);
    mFacadeClassList.add(CameraFacade.class);
    mFacadeClassList.add(WakeLockFacade.class);
    mFacadeClassList.add(WifiFacade.class);
    mFacadeClassList.add(ApplicationManagerFacade.class);
    mFacadeClassList.add(ToneGeneratorFacade.class);
    mFacadeClassList.add(CommonIntentsFacade.class);
    mFacadeClassList.add(PhoneFacade.class);
    mFacadeClassList.add(ConditionManagerFacade.class);

    if (sdkVersion >= 4) {
      mFacadeClassList.add(TextToSpeechFacade.class);
    } else {
      mFacadeClassList.add(EyesFreeFacade.class);
    }

    if (sdkVersion >= 5) {
      mFacadeClassList.add(BluetoothFacade.class);
    }

    if (sdkVersion >= 7) {
      mFacadeClassList.add(SignalStrengthFacade.class);
    }

    for (Class<? extends RpcReceiverFacade> recieverClass : mFacadeClassList) {
      for (MethodDescriptor rpcMethod : MethodDescriptor.collectFrom(recieverClass)) {
        sRpcs.put(rpcMethod.getName(), rpcMethod);
      }
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
  public static JsonRpcServer buildJsonRpcServer(final Service service, Intent intent) {

    FacadeManager facadeManager = new FacadeManager(service, intent, mFacadeClassList);

    return new JsonRpcServer(new ArrayList<Class<? extends RpcReceiver>>(mFacadeClassList),
        facadeManager);
  }
}
