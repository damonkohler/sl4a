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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.ase.facade.AndroidFacade;
import com.google.ase.facade.MediaFacade;
import com.google.ase.facade.TextToSpeechFacade;
import com.google.ase.jsonrpc.JsonRpcServer;

public class AndroidProxy {

  private final JsonRpcServer mJsonRpcServer;
  private final AndroidFacade mAndroidFacade;

  public AndroidProxy(Context context, Intent intent) {
    mAndroidFacade = new AndroidFacade(context, new Handler(), intent);
    MediaFacade mediaFacade = new MediaFacade();
    TextToSpeechFacade ttsFacade = new TextToSpeechFacade(context);
    mJsonRpcServer = JsonRpcServer.create(mAndroidFacade, mediaFacade, ttsFacade);
  }

  public InetSocketAddress startLocal() {
    return mJsonRpcServer.startLocal();
  }

  public InetSocketAddress startPublic() {
    return mJsonRpcServer.startPublic();
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mAndroidFacade.onActivityResult(requestCode, resultCode, data);
  }

  public void shutdown() {
    mJsonRpcServer.shutdown();
  }
}
