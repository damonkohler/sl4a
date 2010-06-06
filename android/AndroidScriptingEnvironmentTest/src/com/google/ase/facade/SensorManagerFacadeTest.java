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

package com.google.ase.facade;

import junit.framework.TestCase;

import com.google.ase.facade.SensorManagerFacade.Average;

public class SensorManagerFacadeTest extends TestCase {
  public void testAverage() {
	Average average = new Average(5);
	try {
	  average.get();
	  fail();
	} catch (IllegalStateException ise) {
	  // This is the expected behavior.
	}
	average.add(1);

	assertEquals(1.0d, average.get());

	average.add(2);
	average.add(3);
	average.add(4);
	average.add(5);

	assertEquals(3.0d, average.get());

	average.add(1);
	average.add(2);

	assertEquals(3.0d, average.get());

	average.add(3);
	average.add(4);
	average.add(5);
	average.add(6);
	average.add(7);
	
	assertEquals(5.0d, average.get());
  }
}
