package com.googlecode.android_scripting.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.net.Uri;
import android.os.Bundle;
import android.provider.LiveFolders;

import com.googlecode.android_scripting.Analytics;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.provider.ScriptProvider;

public class ScriptsLiveFolder extends Activity {

  public static final Uri CONTENT_URI =
      Uri.parse("content://" + ScriptProvider.AUTHORITY + "/" + ScriptProvider.LIVEFOLDER);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final Intent intent = getIntent();
    final String action = intent.getAction();
    if (LiveFolders.ACTION_CREATE_LIVE_FOLDER.equals(action)) {
      setResult(RESULT_OK, createLiveFolder(this, CONTENT_URI, "Scripts", R.drawable.live_folder));
    } else {
      setResult(RESULT_CANCELED);
    }
    Analytics.trackActivity(this);
    finish();
  }

  private Intent createLiveFolder(Context context, Uri uri, String name, int icon) {
    final Intent intent = new Intent();
    intent.setData(uri);
    intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_NAME, name);
    intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_ICON, ShortcutIconResource
        .fromContext(this, icon));
    intent.putExtra(LiveFolders.EXTRA_LIVE_FOLDER_DISPLAY_MODE, LiveFolders.DISPLAY_MODE_LIST);
    return intent;
  }
}