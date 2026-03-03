/*
 * Copyright (C) 2017 Shimoda.
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

package com.googlecode.android_scripting.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.R;

public class Preferences extends PreferenceActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Load the preferences from an XML resource
    addPreferencesFromResource(R.xml.preferences);
  }

    public static int getPrefInt(SharedPreferences prefs,
                                 String key, int defaultValue) {
        int result = defaultValue;
        String value = prefs.getString(key, null);
        if (value != null) {
            try {
                result = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                result = defaultValue;
            }
        }
        return result;
    }

    public static void launch_setIntentExtras(SharedPreferences prefs,
                                              Intent intent,
                                              boolean usePublicIp
    ) {
        intent.putExtra(Constants.EXTRA_USE_EXTERNAL_IP, usePublicIp);
        intent.putExtra(Constants.EXTRA_USE_SERVICE_PORT,
                getPrefInt(prefs, "use_service_port", 0));
        intent.putExtra(Constants.EXTRA_USE_SERVICE_IPV,
                getPrefInt(prefs, "use_service_ipv", 0));
    }
}
