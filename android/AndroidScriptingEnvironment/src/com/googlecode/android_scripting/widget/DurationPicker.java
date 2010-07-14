/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.android_scripting.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.googlecode.android_scripting.R;

/**
 * A view for selecting the a duration using days, hours, minutes, and seconds.
 */
public class DurationPicker extends FrameLayout {

  private int mCurrentDay = 0; // 0-99
  private int mCurrentHour = 0; // 0-23
  private int mCurrentMinute = 0; // 0-59
  private int mCurrentSecond = 0; // 0-59

  private final NumberPicker mDayPicker;
  private final NumberPicker mHourPicker;
  private final NumberPicker mMinutePicker;
  private final NumberPicker mSecondPicker;

  public DurationPicker(Context context) {
    this(context, null);
  }

  public DurationPicker(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public DurationPicker(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    LayoutInflater inflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    inflater.inflate(R.layout.duration_picker, this, true);

    mDayPicker = (NumberPicker) findViewById(R.id.day);
    mDayPicker.setRange(0, 99);
    mDayPicker.setSpeed(100);
    // mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
    mDayPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
      public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
        mCurrentDay = newVal;
      }
    });

    mHourPicker = (NumberPicker) findViewById(R.id.hour);
    mHourPicker.setRange(0, 23);
    mHourPicker.setSpeed(100);
    // mHourPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
    mHourPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
      public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
        mCurrentHour = newVal;
      }
    });

    mMinutePicker = (NumberPicker) findViewById(R.id.minute);
    mMinutePicker.setRange(0, 59);
    mMinutePicker.setSpeed(100);
    mMinutePicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
    mMinutePicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
      public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
        mCurrentMinute = newVal;
      }
    });

    mSecondPicker = (NumberPicker) findViewById(R.id.second);
    mSecondPicker.setRange(0, 59);
    mSecondPicker.setSpeed(100);
    mSecondPicker.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
    mSecondPicker.setOnChangeListener(new NumberPicker.OnChangedListener() {
      public void onChanged(NumberPicker spinner, int oldVal, int newVal) {
        mCurrentSecond = newVal;
      }
    });
  }

  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    mDayPicker.setEnabled(enabled);
    mHourPicker.setEnabled(enabled);
    mMinutePicker.setEnabled(enabled);
    mSecondPicker.setEnabled(enabled);
  }

  /**
   * Returns the current day.
   */
  public Integer getCurrentDay() {
    return mCurrentDay;
  }

  /**
   * Set the current hour.
   */
  public void setCurrentDay(Integer currentDay) {
    mCurrentDay = currentDay;
    updateDayDisplay();
  }

  /**
   * Returns the current hour.
   */
  public Integer getCurrentHour() {
    return mCurrentHour;
  }

  /**
   * Set the current hour.
   */
  public void setCurrentHour(Integer currentHour) {
    mCurrentHour = currentHour;
    updateHourDisplay();
  }

  /**
   * Returns the current minute.
   */
  public Integer getCurrentMinute() {
    return mCurrentMinute;
  }

  /**
   * Set the current minute.
   */
  public void setCurrentMinute(Integer currentMinute) {
    mCurrentMinute = currentMinute;
    updateMinuteDisplay();
  }

  /**
   * Returns the current second.
   */
  public Integer getCurrentSecond() {
    return mCurrentSecond;
  }

  /**
   * Set the current minute.
   */
  public void setCurrentSecond(Integer currentSecond) {
    mCurrentSecond = currentSecond;
    updateSecondDisplay();
  }

  /**
   * Set the state of the spinners appropriate to the current day.
   */
  private void updateDayDisplay() {
    int currentDay = mCurrentDay;
    mDayPicker.setCurrent(currentDay);
  }

  /**
   * Set the state of the spinners appropriate to the current hour.
   */
  private void updateHourDisplay() {
    int currentHour = mCurrentHour;
    mHourPicker.setCurrent(currentHour);
  }

  /**
   * Set the state of the spinners appropriate to the current minute.
   */
  private void updateMinuteDisplay() {
    mMinutePicker.setCurrent(mCurrentMinute);
  }

  /**
   * Set the state of the spinners appropriate to the current minute.
   */
  private void updateSecondDisplay() {
    mSecondPicker.setCurrent(mCurrentSecond);
  }

  /**
   * Returns the duration in seconds.
   */
  public double getDuration() {
    // The text views may still have focus so clear theirs focus which will trigger the on focus
    // changed and any typed values to be pulled.
    mDayPicker.clearFocus();
    mHourPicker.clearFocus();
    mMinutePicker.clearFocus();
    mSecondPicker.clearFocus();
    return (((((mCurrentDay * 24l + mCurrentHour) * 60) + mCurrentMinute) * 60) + mCurrentSecond);
  }

  /**
   * Sets the duration in milliseconds.
   * 
   * @return
   */
  public void setDuration(long duration) {
    double seconds = duration / 1000;
    double minutes = seconds / 60;
    seconds = seconds % 60;
    double hours = minutes / 60;
    minutes = minutes % 60;
    double days = hours / 24;
    hours = hours % 24;

    setCurrentDay((int) days);
    setCurrentHour((int) hours);
    setCurrentMinute((int) minutes);
    setCurrentSecond((int) seconds);
  }
}
