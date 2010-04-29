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

import android.content.Context;

import com.google.ase.trigger.TriggerRepository.TriggerInfo;

public class AlarmTrigger extends Trigger {
  private static final long serialVersionUID = 3175281973854075190L;
  private final double mExecutionTime;

  public AlarmTrigger(double executionTime, String scriptName) {
    super(scriptName);
    mExecutionTime = executionTime;
  }
  
  @Override
  public void beforeTrigger(Context context, TriggerInfo info) {
    super.beforeTrigger(context, info);
    // This trigger will only fire once: remove it from the repository.
    info.remove();
  }

  /**
   * Returns the execution time in seconds since epoch.
   */
  public double getExecutionTime() {
    return mExecutionTime;
  }
}
