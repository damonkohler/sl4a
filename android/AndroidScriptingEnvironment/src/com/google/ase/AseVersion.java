package com.google.ase;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AseVersion {

  private AseVersion() {
    // Utility class.
  }

  public static String getVersion(Activity activity) {
    try {
      PackageInfo info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
      return info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      AseLog.e("Package name not found", e);
    }
    return "?";
  }

}
