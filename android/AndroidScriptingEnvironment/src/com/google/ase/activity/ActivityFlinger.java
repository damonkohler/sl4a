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

package com.google.ase.activity;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;

public abstract class ActivityFlinger {

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 100;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private final GestureDetector mGestureDetector;

  public ActivityFlinger(View view) {
    mGestureDetector = new GestureDetector(new SwitchActivity());
    view.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
      }
    });
  }

  protected abstract void left();

  protected abstract void right();

  private class SwitchActivity extends SimpleOnGestureListener {
    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
      if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
        return false;
      }
      if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE
          && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        left();
      } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
          && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        right();
      } else {
        return super.onFling(event1, event2, velocityX, velocityY);
      }
      return true;
    }
  }
}
