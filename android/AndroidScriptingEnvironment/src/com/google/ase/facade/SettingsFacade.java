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

package com.google.ase.facade;

import android.app.Service;
import android.content.Context;
import android.provider.Settings.SettingNotFoundException;

import com.google.ase.jsonrpc.Rpc;
import com.google.ase.jsonrpc.RpcParameter;
import com.google.ase.jsonrpc.RpcReceiver;

public class SettingsFacade implements RpcReceiver {

  private final Service mService;

  /**
   * Creates a new AndroidFacade that simplifies the interface to settings.
   *
   * @param service
   *          is the {@link Context} the APIs will run under
   */
  public SettingsFacade(Service service) {
    mService = service;
  }

  @Rpc(description = "Set screen timeout to this number of seconds). Returns the old value.")
  public Integer setScreenTimeout(@RpcParameter("value") Integer value) {
    Integer old_value = getScreenTimeout();
    android.provider.Settings.System.putInt(mService.getContentResolver(),
        android.provider.Settings.System.SCREEN_OFF_TIMEOUT, value * 1000);
    return old_value;
  }
  
  @Rpc(description = "Get current screen timeout in seconds.")
  public Integer getScreenTimeout() {
    try {
      return android.provider.Settings.System.getInt(mService.getContentResolver(),
          android.provider.Settings.System.SCREEN_OFF_TIMEOUT) / 1000;
    } catch (SettingNotFoundException e) {
      return 0;
    }
  }

  @Override
  public void shutdown() {
    // Nothing to do yet.
  }
}
