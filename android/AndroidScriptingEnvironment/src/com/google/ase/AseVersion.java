package com.google.ase;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class AseVersion {

  private AseVersion() {
    // Utility class.
  }

  public static String getVersion(Context context) {
    try {
      PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
      return info.versionName;
    } catch (PackageManager.NameNotFoundException e) {
      AseLog.e("Package name not found", e);
    }
    return "?";
  }

}
