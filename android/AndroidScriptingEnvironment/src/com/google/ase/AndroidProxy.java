/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase;

import java.net.InetSocketAddress;
import java.util.Map;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;

import com.google.ase.facade.AndroidFacade;
import com.google.ase.facade.EventFacade;
import com.google.ase.facade.MediaFacade;
import com.google.ase.facade.SpeechRecognitionFacade;
import com.google.ase.facade.TextToSpeechFacade;
import com.google.ase.facade.ui.UiFacade;
import com.google.ase.jsonrpc.JsonRpcServer;
import com.google.ase.jsonrpc.RpcInfo;

public class AndroidProxy {

  private InetSocketAddress mAddress;
  private final JsonRpcServer mJsonRpcServer;
  private final AndroidFacade mAndroidFacade;

  public AndroidProxy(Service service, Intent intent) {
    mAndroidFacade = new AndroidFacade(service, new Handler(), intent);
    final MediaFacade mediaFacade = new MediaFacade();
    final TextToSpeechFacade ttsFacade = new TextToSpeechFacade(service);
    final SpeechRecognitionFacade srFacade = new SpeechRecognitionFacade(mAndroidFacade);
    final UiFacade uiFacade = new UiFacade(service);
    final EventFacade eventFacade = new EventFacade(service);
    mJsonRpcServer = new JsonRpcServer(mAndroidFacade, mediaFacade, ttsFacade, srFacade, uiFacade,
        eventFacade);
  }

  public InetSocketAddress getAddress() {
    return mAddress;
  }

  public InetSocketAddress startLocal() {
    mAddress = mJsonRpcServer.startLocal();
    return mAddress;
  }

  public InetSocketAddress startPublic() {
    mAddress = mJsonRpcServer.startPublic();
    return mAddress;
  }

  public Map<String, RpcInfo> getKnownRpcs() {
    return mJsonRpcServer.getKnownRpcs();
  }

  public void shutdown() {
    mJsonRpcServer.shutdown();
  }
}
