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

package com.googlecode.android_scripting.facade;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.rpc.Rpc;
import com.googlecode.android_scripting.rpc.RpcParameter;

import java.util.List;

/**
 * Provides Text To Speech services for API 3 or less.
 */

public class EyesFreeFacade extends RpcReceiver {

  private final Service mService;
  private final PackageManager mPackageManager;

  public EyesFreeFacade(FacadeManager manager) {
    super(manager);
    mService = manager.getService();
    mPackageManager = mService.getPackageManager();
  }

  @Rpc(description = "Speaks the provided message via TTS.")
  public void ttsSpeak(@RpcParameter(name = "message") String message) {
    Intent intent = new Intent("com.google.tts.makeBagel");
    intent.putExtra("message", message);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    List<ResolveInfo> infos = mPackageManager.queryIntentActivities(intent, 0);
    if (infos.size() > 0) {
      mService.startActivity(intent);
    } else {
      throw new RuntimeException("Eyes-Free is not installed.");
    }
  }

  @Override
  public void shutdown() {
  }
}
