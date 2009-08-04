package com.google.ase;

import android.content.Intent;
import android.os.Parcelable;

public class IntentBuilders {

  private IntentBuilders() {
    // Utility class.
  }

  public static Intent buildLaunchIntent(String scriptName) {
    Intent i = new Intent();
    i.setAction(Constants.ACTION_LAUNCH_SCRIPT);
    i.putExtra(Constants.EXTRA_SCRIPT_NAME, scriptName);
    return i;
  }

  /**
   * Builds an intent that creates a shortcut to the provided script.
   *
   * @param scriptName
   *          the script to link to
   * @param iconResource
   *          the icon resource to associate with the shortcut
   * @return the intent that will create the shortcut
   */
  public static Intent buildShortcutIntent(String scriptName, Parcelable iconResource) {
    // Then, set up the container intent (the response to the caller)
    Intent intent = new Intent();
    intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, buildLaunchIntent(scriptName));
    intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, scriptName);
    intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);
    return intent;
  }

}
