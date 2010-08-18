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

package com.googlecode.android_scripting.facade.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.util.AndroidRuntimeException;
import android.widget.DatePicker;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wrapper class for date picker dialog running in separate thread.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class DatePickerDialogTask extends DialogTask {
  public static int mYear;
  public static int mMonth;
  public static int mDay;

  public DatePickerDialogTask(int year, int month, int day) {
    mYear = year;
    mMonth = month - 1;
    mDay = day;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mDialog = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
      @Override
      public void onDateSet(DatePicker view, int year, int month, int day) {
        JSONObject result = new JSONObject();
        try {
          result.put("which", "positive");
          result.put("year", year);
          result.put("month", month + 1);
          result.put("day", day);
          setResult(result);
        } catch (JSONException e) {
          throw new AndroidRuntimeException(e);
        }
      }
    }, mYear, mMonth, mDay);
    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface view) {
        JSONObject result = new JSONObject();
        try {
          result.put("which", "neutral");
          result.put("year", mYear);
          result.put("month", mMonth + 1);
          result.put("day", mDay);
          setResult(result);
        } catch (JSONException e) {
          throw new AndroidRuntimeException(e);
        }
      }
    });
    mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
      @Override
      public void onDismiss(DialogInterface dialog) {
        JSONObject result = new JSONObject();
        try {
          result.put("which", "negative");
          result.put("year", mYear);
          result.put("month", mMonth + 1);
          result.put("day", mDay);
          setResult(result);
        } catch (JSONException e) {
          throw new AndroidRuntimeException(e);
        }
      }
    });
    mDialog.show();
    mShowLatch.countDown();
  }
}
