package com.googlecode.script;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ScriptActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  protected void onResume() {
    super.onResume();
    ScriptApplication application = (ScriptApplication) getApplication();
    if (application.readyToStart()) {
      startService(new Intent(this, ScriptService.class));
    }
    finish();
  }

}
