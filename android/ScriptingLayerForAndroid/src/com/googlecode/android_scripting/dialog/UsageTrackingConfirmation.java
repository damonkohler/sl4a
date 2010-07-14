/*
 * Copyright (C) 2009 Google Inc.
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

package com.googlecode.android_scripting.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class UsageTrackingConfirmation {
  private UsageTrackingConfirmation() {
    // Utility class.
  }

  public static void show(Activity activity) {
    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    if (prefs.getBoolean("present_usagetracking", true)) {
      final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
      builder.setTitle("Usage Tracking");
      builder.setCancelable(true);
      builder.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          prefs.edit().putBoolean("present_usagetracking", false).commit();
          prefs.edit().putBoolean("usagetracking", true).commit();
        }
      });
      builder.setNegativeButton("Refuse", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          prefs.edit().putBoolean("present_usagetracking", false).commit();
          prefs.edit().putBoolean("usagetracking", false).commit();
        }
      });
      builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
        public void onCancel(DialogInterface dialog) {
          prefs.edit().putBoolean("present_usagetracking", true).commit();
          prefs.edit().putBoolean("usagetracking", false).commit();
        }
      });
      builder.setMessage("Allow collection of anonymous usage information?\n\nThis can be "
          + "changed later under preferences.");
      builder.create().show();
    }
  }
}