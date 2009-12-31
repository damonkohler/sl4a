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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.google.ase.facade.AndroidFacade;
import com.google.ase.facade.MediaFacade;
import com.google.ase.facade.TextToSpeechFacade;
import com.google.ase.jsonrpc.JsonRpcServer;

public class AndroidProxyFactory {

  private AndroidProxyFactory() {
    // Utitlity class.
  }

  public static JsonRpcServer create(Context context, Intent intent) {
    AndroidFacade androidFacade = new AndroidFacade(context, new Handler(), intent);
    MediaFacade mediaFacade = new MediaFacade();
    TextToSpeechFacade ttsFacade = new TextToSpeechFacade(context);
    return JsonRpcServer.create(androidFacade, mediaFacade, ttsFacade);
  }
}
