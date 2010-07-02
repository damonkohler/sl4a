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

import android.app.Service;
import android.content.Intent;

import com.google.ase.facade.FacadeConfiguration;
import com.google.ase.facade.FacadeManager;
import com.google.ase.jsonrpc.JsonRpcServer;

public class AndroidProxy {

  private InetSocketAddress mAddress;
  private final JsonRpcServer mJsonRpcServer;

  public AndroidProxy(Service service, Intent intent) {
    FacadeManager facadeManager =
        new FacadeManager(service, intent, FacadeConfiguration.getFacadeClasses());
    mJsonRpcServer = new JsonRpcServer(facadeManager);
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

  public void shutdown() {
    mJsonRpcServer.shutdown();
  }
}
