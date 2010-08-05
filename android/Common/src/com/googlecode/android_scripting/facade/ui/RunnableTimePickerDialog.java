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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.util.AndroidRuntimeException;
import android.widget.TimePicker;

import com.googlecode.android_scripting.activity.ScriptingLayerServiceHelper;
import com.googlecode.android_scripting.future.FutureResult;

/**
 * Wrapper class for time picker dialog running in separate thread.
 * 
 * @author MeanEYE.rcf (meaneye.rcf@gmail.com)
 */
public class RunnableTimePickerDialog extends RunnableDialog {
  private final int mHour;
  private final int mMinute;
  private final boolean mIs24Hour;

  public RunnableTimePickerDialog(int hour, int minute, boolean is24hour) {
    mHour = hour;
    mMinute = minute;
    mIs24Hour = is24hour;
  }

  @Override
  public void run(ScriptingLayerServiceHelper activity, FutureResult<Object> result) {
    mActivity = activity;
    mFutureResult = result;
    mDialog = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker view, int hour, int minute) {
        JSONObject result = new JSONObject();
        try {
          result.put("which", "positive");
          result.put("hour", hour);
          result.put("minute", minute);
          mFutureResult.set(result);
        } catch (JSONException e) {
          throw new AndroidRuntimeException(e);
        }
      }
    }, mHour, mMinute, mIs24Hour);
    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface view) {
        JSONObject result = new JSONObject();
        try {
          result.put("which", "neutral");
          result.put("hour", mHour);
          result.put("minute", mMinute);
          mFutureResult.set(result);
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
          result.put("hour", mHour);
          result.put("minute", mMinute);
          mFutureResult.set(result);
        } catch (JSONException e) {
          throw new AndroidRuntimeException(e);
        }
      }
    });
    mDialog.show();
    mShowLatch.countDown();
  }
}
