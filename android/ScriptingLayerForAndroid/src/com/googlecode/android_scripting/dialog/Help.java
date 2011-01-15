/*
 * Copyright (C) 2010 Google Inc.
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
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.connectbot.HelpActivity;

public class Help {
  private Help() {
    // Utility class.
  }

  private static int helpChecked = 0;

  public static boolean checkApiHelp(Context context) {
    byte[] buf = new byte[1024];
    if (helpChecked == 0) {
      try {
        File dest = new File(InterpreterConstants.SDCARD_SL4A_DOC);
        if (!dest.exists()) {
          dest.mkdirs();
        }
        new File(InterpreterConstants.SDCARD_SL4A_DOC, "index.html");
        AssetManager assetManager = context.getAssets();
        ZipInputStream zip = new ZipInputStream(assetManager.open("sl4adoc.zip"));
        ZipEntry entry;
        while ((entry = zip.getNextEntry()) != null) {
          File file = new File(InterpreterConstants.SDCARD_SL4A_DOC, entry.getName());
          if (!file.exists() || file.lastModified() < entry.getTime()) {
            if (!file.exists() && !file.getParentFile().exists()) {
              file.getParentFile().mkdirs();
            }
            OutputStream output = new BufferedOutputStream(new FileOutputStream(file));
            int len;
            while ((len = zip.read(buf)) > 0) {
              output.write(buf, 0, len);
            }
            output.flush();
            output.close();
            file.setLastModified(entry.getTime());
          }
        }
        helpChecked = 1;
      } catch (IOException e) {
        Log.e("Help not found ", e);
        helpChecked = -1;
        return false;
      }
    }
    return helpChecked > 0;
  }

  public static void showApiHelp(Context context, String help) {
    Intent intent = new Intent();
    intent.setAction(Intent.ACTION_VIEW);
    Uri uri = Uri.parse("file://" + InterpreterConstants.SDCARD_SL4A_DOC + help);
    intent.setDataAndType(uri, "text/html");
    SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
    if (p.getBoolean(Constants.FORCE_BROWSER, true)) {
      intent.setComponent(new ComponentName("com.android.browser",
          "com.android.browser.BrowserActivity"));
    }
    context.startActivity(intent);
  }

  public static void show(final Activity activity) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    List<CharSequence> list = new ArrayList<CharSequence>();
    list.add("Wiki Documentation");
    list.add("YouTube Screencasts");
    list.add("Terminal Help");
    if (checkApiHelp(activity)) {
      list.add("API Help");
    }
    CharSequence[] mylist = list.toArray(new CharSequence[list.size()]);
    builder.setItems(mylist, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        switch (which) {
        case 0: {
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(activity.getString(R.string.wiki_url)));
          activity.startActivity(intent);
          break;
        }
        case 1: {
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(activity.getString(R.string.youtube_url)));
          activity.startActivity(intent);
          break;
        }
        case 2: {
          Intent intent = new Intent(activity, HelpActivity.class);
          activity.startActivity(intent);
          break;
        }
        case 3: {
          showApiHelp(activity, "index.html");
        }
        }
      }
    });
    builder.show();
  }
}
