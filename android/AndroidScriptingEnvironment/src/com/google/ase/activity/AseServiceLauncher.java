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

package com.google.ase.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.ase.Constants;
import com.google.ase.IntentBuilders;

public class AseServiceLauncher extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String scriptName = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    String action = getIntent().getAction();
    Intent intent = null;
    if (action.equals(Constants.ACTION_LAUNCH_SCRIPT)) {
      intent = IntentBuilders.buildStartInBackgroundIntent(scriptName);
    }
    if (action.equals(Constants.ACTION_LAUNCH_TERMINAL)) {
      intent = IntentBuilders.buildStartInTerminalIntent(scriptName);
    }
    if (intent != null) {
      intent.putExtras(getIntent().getExtras());
      startService(intent);
    }
    finish();
  }
}
