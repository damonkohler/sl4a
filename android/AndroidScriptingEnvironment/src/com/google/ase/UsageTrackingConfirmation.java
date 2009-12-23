package com.google.ase;

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