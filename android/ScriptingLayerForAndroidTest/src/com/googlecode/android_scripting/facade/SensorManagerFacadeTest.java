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

package com.googlecode.android_scripting.facade;

import com.googlecode.android_scripting.facade.SensorManagerFacade.RollingAverage;

import junit.framework.TestCase;

public class SensorManagerFacadeTest extends TestCase {
  public void testAverage() {
    RollingAverage average = new RollingAverage();
    try {
      average.get();
      fail();
    } catch (IllegalStateException ise) {
      // This is the expected behavior.
    }
    average.add(1);
    assertEquals(1.0d, average.get());
    addSeveral(average, 2, 3, 4, 5);
    assertEquals(3.0d, average.get());
    addSeveral(average, 1, 2);
    assertEquals(3.0d, average.get());
    addSeveral(average, 3, 4, 5, 6, 7);
    assertEquals(5.0d, average.get());
  }

  private void addSeveral(RollingAverage avg, double... data) {
    for (double value : data) {
      avg.add(value);
    }
  }

}
