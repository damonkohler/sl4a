package com.dummy.fooforandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.Log;

import java.net.URL;

public class DialogActivity extends Activity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    String scriptName = getIntent().getStringExtra(Constants.EXTRA_SCRIPT_NAME);
    String interpreter = FeaturedInterpreters.getInterpreterNameForScript(scriptName);
    if (interpreter == null) {
      Log.e("Cannot find interpreter for script " + scriptName);
      finish();
    }
    final URL url = FeaturedInterpreters.getUrlForName(interpreter);

    AlertDialog.Builder dialog = new AlertDialog.Builder(this);
    dialog.setTitle(String.format("%s is not installed.", interpreter));
    dialog.setMessage(String.format("Do you want to download APK for %s ?", interpreter));

    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
          Intent viewIntent = new Intent("android.intent.action.VIEW", Uri.parse(url.toString()));
          startActivity(viewIntent);
        }
        dialog.dismiss();
        finish();
      }
    };
    dialog.setNegativeButton("No", buttonListener);
    dialog.setPositiveButton("Yes", buttonListener);
    dialog.show();
  }

}
