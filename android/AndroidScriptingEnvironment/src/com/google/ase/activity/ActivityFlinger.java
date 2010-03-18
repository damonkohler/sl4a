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

import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.Toast;

public class ActivityFlinger {

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 100;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private ActivityFlinger() {
    // Use ActivityFlinger.Builder instead.
  }

  public static class Builder {

    private final GestureDetector mGestureDetector;
    private final LeftRightFlingListener mListener;

    public Builder() {
      mListener = new LeftRightFlingListener();
      mGestureDetector = new GestureDetector(mListener);
    }

    public void attachToView(View view) {
      view.setOnTouchListener(new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return mGestureDetector.onTouchEvent(event);
        }
      });
    }

    public Builder addLeftActivity(final Context context, final Class<?> activity,
        final String name) {
      mListener.mLeftRunnable = new StartActivityRunnable(name, context, activity);
      return this;
    }

    public Builder addRightActivity(final Context context, final Class<?> activity,
        final String name) {
      mListener.mRightRunnable = new StartActivityRunnable(name, context, activity);
      return this;
    }
  }

  private static class StartActivityRunnable implements Runnable {

    private final String message;
    private final Context context;
    private final Class<?> activity;

    private StartActivityRunnable(String message, Context context, Class<?> activity) {
      this.message = message;
      this.context = context;
      this.activity = activity;
    }

    @Override
    public void run() {
      Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
      Intent intent = new Intent(context, activity);
      context.startActivity(intent);
    }
  }

  private static class LeftRightFlingListener extends SimpleOnGestureListener {
    Runnable mLeftRunnable;
    Runnable mRightRunnable;

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
      if (Math.abs(event1.getY() - event2.getY()) > SWIPE_MAX_OFF_PATH) {
        return false;
      }
      if (event1.getX() - event2.getX() > SWIPE_MIN_DISTANCE
          && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        if (mLeftRunnable != null) {
          mLeftRunnable.run();
        }
      } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
          && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        if (mRightRunnable != null) {
          mRightRunnable.run();
        }
      } else {
        return super.onFling(event1, event2, velocityX, velocityY);
      }
      return true;
    }
  }
}
