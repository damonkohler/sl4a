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

package com.googlecode.android_scripting.event;

import com.google.common.base.Preconditions;

public class Event {

  private String mName;
  private Object mData;
  private double mCreationTime;

  public Event(String name, Object data) {
    Preconditions.checkNotNull(name);
    setName(name);
    setData(data);
    mCreationTime = System.currentTimeMillis() * 1000;
  }

  public void setName(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }

  public void setData(Object data) {
    mData = data;
  }

  public Object getData() {
    return mData;
  }

  public double getCreationTime() {
    return mCreationTime;
  }

  public boolean nameEquals(String name) {
    return mName.equals(name);
  }
}
