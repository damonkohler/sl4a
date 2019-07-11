/*
 * Copyright (C) 2016 Google Inc.
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

package com.googlecode.android_scripting.locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.IntentBuilders;

import java.io.File;

public class LocaleReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    final File script =
        new File(intent.getBundleExtra(com.twofortyfouram.locale.platform.Intent.EXTRA_BUNDLE)
            .getString(Constants.EXTRA_SCRIPT_PATH));
    Log.v("LocaleReceiver", "Locale initiated launch of " + script);
    Intent launchIntent;
    if (intent.getBooleanExtra(Constants.EXTRA_LAUNCH_IN_BACKGROUND, false)) {
      launchIntent = IntentBuilders.buildStartInBackgroundIntent(script);
    } else {
      launchIntent = IntentBuilders.buildStartInTerminalIntent(script);
    }
    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(launchIntent);
  }
}
