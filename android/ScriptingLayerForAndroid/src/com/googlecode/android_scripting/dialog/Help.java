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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.connectbot.HelpActivity;

public class Help {
  private Help() {
    // Utility class.
  }

  public static void show(final Activity activity) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    List<CharSequence> list = new Vector<CharSequence>();
    list.add("Wiki Documentation");
    list.add("YouTube Screencasts");
    list.add("Terminal Help");
    if ((new File(InterpreterConstants.SDCARD_SL4A_DOC, "index.html")).exists()) {
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
          Intent intent = new Intent();
          intent.setAction(Intent.ACTION_VIEW);
          intent.setDataAndType(Uri.fromFile((new File(InterpreterConstants.SDCARD_SL4A_DOC,
              "index.html"))), "text/html");
          activity.startActivity(intent);

        }
        }

      }
    });
    builder.show();
  }
}
