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

import com.googlecode.android_scripting.R;
import com.googlecode.android_scripting.widget.DurationPicker;

public class DurationPickerDialog {

  private DurationPickerDialog() {
    // Utility class.
  }

  public interface DurationPickedListener {
    public void onSet(double duration);

    public void onCancel();
  }

  public static void getDurationFromDialog(Activity activity, String title,
      final DurationPickedListener done) {
    final DurationPicker picker = new DurationPicker(activity);
    AlertDialog.Builder alert = new AlertDialog.Builder(activity);
    alert.setIcon(R.drawable.ic_dialog_time);
    alert.setTitle(title);
    alert.setView(picker);
    alert.setPositiveButton("Set", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        done.onSet(picker.getDuration());
      }
    });
    alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface arg0) {
        done.onCancel();
      }
    });
    alert.show();
  }
}
