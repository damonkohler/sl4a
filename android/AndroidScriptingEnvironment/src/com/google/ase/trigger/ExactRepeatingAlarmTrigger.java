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

package com.google.ase.trigger;


@SuppressWarnings("unused")
public class ExactRepeatingAlarmTrigger extends RepeatingAlarmTrigger {
  private static final long serialVersionUID = -9125118724160624255L;

  private final Double mFirstExecutionTimeS;

  public ExactRepeatingAlarmTrigger(Double intervalS, String scriptName,
      Double firstExecutionTimeS, boolean wakeUp) {
    super(scriptName, intervalS, wakeUp);
    mFirstExecutionTimeS = firstExecutionTimeS;
  }
}
