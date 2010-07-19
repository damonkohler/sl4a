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

  private static enum State {
    ALIVE("Alive"), DEAD("Dead");

    private final String mName;

    State(String name) {
      mName = name;
    }

    @Override
    public String toString() {
      return mName;
    }
  }

  private final int mServerPort;
  private final Trigger mTrigger;
  private final AndroidProxy mProxy;
  private final long mStartTime;
  private final String mScriptName;

  private volatile State mState;

  public ScriptProcess(String scriptName, AndroidProxy proxy, Trigger trigger) {
    super(proxy.getAddress().getHostName(), proxy.getAddress().getPort(), proxy.getSecret());
    mProxy = proxy;
    mTrigger = trigger;
    mState = State.ALIVE;
    mStartTime = System.currentTimeMillis();
    mServerPort = proxy.getAddress().getPort();
    mScriptName = scriptName;
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

  public int getPort() {
    return mServerPort;
  }

  public long getStartTime() {
    return mStartTime;
  }

  public String getScriptName() {
    return mScriptName;
  }

  public String getServerName() {
    if (mProxy == null) {
      return null;
    }
    InetSocketAddress address = mProxy.getAddress();
    return address.getHostName();
  }

  public boolean isAlive() {
    return mState.equals(State.ALIVE);
  }

  public String getState() {
    return mState.toString();
  }

  @Override
  public void kill() {
    kill();
    if (mProxy != null) {
      mProxy.shutdown();
    }
    mState = State.DEAD;
  }

  @Override
  public String toString() {
    StringBuilder info = new StringBuilder();
    InetSocketAddress address = mProxy.getAddress();
    info.append(String.format("Running network service on: %s:%d\n", address.getHostName(), address
        .getPort()));
    info.append("Running script service: ");
    info.append(mScriptName);
    return info.toString();
  }

  public String getUptime() {
    long ms = System.currentTimeMillis() - mStartTime;
    StringBuffer buffer = new StringBuffer();
    int days = (int) (ms / (1000 * 60 * 60 * 24));
    int hours = (int) (ms % (1000 * 60 * 60 * 24)) / 3600000;
    int minutes = (int) (ms % 3600000) / 60000;
    int seconds = (int) (ms % 60000) / 1000;
    if (days != 0) {
      buffer.append(String.format("%02d:%02d:", days, hours));
    } else if (hours != 0) {
      buffer.append(String.format("%02d:", hours));
    }
    buffer.append(String.format("%02d:%02d", minutes, seconds));
    return buffer.toString();
  }

  @Override
  protected void buildEnvironment() {
    // TODO Auto-generated method stub

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
