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

package com.googlecode.android_scripting;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;

import com.googlecode.android_scripting.activity.InterpreterManager;
import com.googlecode.android_scripting.activity.LogcatViewer;
import com.googlecode.android_scripting.activity.ScriptManager;
import com.googlecode.android_scripting.activity.TriggerManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ActivityFlinger {

  private static final int SWIPE_MIN_DISTANCE = 120;
  private static final int SWIPE_MAX_OFF_PATH = 100;
  private static final int SWIPE_THRESHOLD_VELOCITY = 200;

  private static class ActivityTransition {
    Class<? extends Activity> mLeft;
    Class<? extends Activity> mRight;

    public ActivityTransition(Class<? extends Activity> left, Class<? extends Activity> right) {
      mLeft = left;
      mRight = right;
    }
  }

  private static Map<Class<?>, ActivityTransition> mActivityTransitions =
      new HashMap<Class<?>, ActivityTransition>();

  private ActivityFlinger() {
    // Utility class.
  }

  static {
    List<Class<? extends Activity>> entries = new ArrayList<Class<? extends Activity>>();
    entries.add(ScriptManager.class);
    entries.add(InterpreterManager.class);
    entries.add(TriggerManager.class);
    entries.add(LogcatViewer.class);

    Class<? extends Activity> left = null;
    Class<? extends Activity> current = null;
    Class<? extends Activity> right = null;

    for (Iterator<Class<? extends Activity>> it = entries.iterator(); it.hasNext()
        || current != null;) {
      if (current == null) {
        current = it.next();
      }
      if (it.hasNext()) {
        right = it.next();
      } else {
        right = null;
      }
      mActivityTransitions.put(current, new ActivityTransition(left, right));
      left = current;
      current = right;
    }
  }

  public static void attachView(View view, Context context) {
    final LeftRightFlingListener mListener = new LeftRightFlingListener();
    final GestureDetector mGestureDetector = new GestureDetector(mListener);
    ActivityTransition transition = mActivityTransitions.get(context.getClass());
    if (transition.mLeft != null) {
      mListener.mLeftRunnable = new StartActivityRunnable(context, transition.mLeft);
    }
    if (transition.mRight != null) {
      mListener.mRightRunnable = new StartActivityRunnable(context, transition.mRight);
    }
    view.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
      }
    });
  }

  private static class StartActivityRunnable implements Runnable {

    private final Context mContext;
    private final Class<?> mActivityClass;

    private StartActivityRunnable(Context context, Class<?> activity) {
      mContext = context;
      mActivityClass = activity;
    }

    @Override
    public void run() {
      Intent intent = new Intent(mContext, mActivityClass);
      mContext.startActivity(intent);
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
        if (mRightRunnable != null) {
          mRightRunnable.run();
        }
      } else if (event2.getX() - event1.getX() > SWIPE_MIN_DISTANCE
          && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
        if (mLeftRunnable != null) {
          mLeftRunnable.run();
        }
      } else {
        return super.onFling(event1, event2, velocityX, velocityY);
      }
      return true;
    }
  }
}
