package com.dummy.fooforandroid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ScriptActivity extends Activity {
  public static final String ACTION_QUIT = "script_for_android_template.ACTION_QUIT";
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ScriptApplication application = (ScriptApplication) getApplication();
    if (application.readyToStart()) {
      startService(new Intent(this, ScriptService.class));
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    if (ACTION_QUIT.equals(intent.getAction())) {
      finish();
    }
  }
}
