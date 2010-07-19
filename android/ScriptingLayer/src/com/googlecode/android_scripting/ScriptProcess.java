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

package com.googlecode.android_scripting;

import java.net.InetSocketAddress;

import android.app.Service;

import com.googlecode.android_scripting.interpreter.InterpreterProcess;
import com.googlecode.android_scripting.trigger.Trigger;

public class ScriptProcess extends InterpreterProcess {

  private final Trigger mTrigger;
  private final AndroidProxy mProxy;

  public ScriptProcess(String scriptName, AndroidProxy proxy, Trigger trigger) {
    super(proxy.getAddress().getHostName(), proxy.getAddress().getPort(), proxy.getSecret());
    mProxy = proxy;
    mTrigger = trigger;
    mName = scriptName;
  }

  public void notifyTriggerOfShutDown(Service service) {
    if (mTrigger != null) {
      mTrigger.afterTrigger(service);
    }
  }

  public void notifyTriggerOfStart(Service service) {
    if (mTrigger != null) {
      mTrigger.beforeTrigger(service);
    }
  }

  @Override
  public void kill() {
    super.kill();
    if (mProxy != null) {
      mProxy.shutdown();
    }
  }

  @Override
  public String toString() {
    StringBuilder info = new StringBuilder();
    InetSocketAddress address = mProxy.getAddress();
    info.append(String.format("Running network service on: %s:%d\n", address.getHostName(), address
        .getPort()));
    info.append("Running script service: ");
    info.append(mName);
    return info.toString();
  }

  @Override
  protected void buildEnvironment() {
  }

  @Override
  protected String[] getInterpreterArguments() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected String getInterpreterCommand() {
    // TODO Auto-generated method stub
    return null;
  }
}
