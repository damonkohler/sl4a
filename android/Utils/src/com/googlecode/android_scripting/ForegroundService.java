package com.googlecode.android_scripting;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;

import java.lang.reflect.Method;

public abstract class ForegroundService extends Service {
  private static final Class<?>[] mStartForegroundSignature =
      new Class[] { int.class, Notification.class };
  private static final Class<?>[] mStopForegroundSignature = new Class[] { boolean.class };

  private final int mNotificationId;

  private NotificationManager mNotificationManager;
  private Method mStartForeground;
  private Method mStopForeground;
  private Object[] mStartForegroundArgs = new Object[2];
  private Object[] mStopForegroundArgs = new Object[1];

  public ForegroundService(int id) {
    mNotificationId = id;
  }

  protected abstract Notification createNotification();

  /**
   * This is a wrapper around the new startForeground method, using the older APIs if it is not
   * available.
   */
  private void startForegroundCompat(Notification notification) {
    // If we have the new startForeground API, then use it.
    if (mStartForeground != null) {
      mStartForegroundArgs[0] = Integer.valueOf(mNotificationId);
      mStartForegroundArgs[1] = notification;
      try {
        mStartForeground.invoke(this, mStartForegroundArgs);
      } catch (Exception e) {
        Log.e(e);
      }
      return;
    }

    // Fall back on the old API.
    setForeground(true);
    if (notification != null) {
      mNotificationManager.notify(mNotificationId, notification);
    }
  }

  /**
   * This is a wrapper around the new stopForeground method, using the older APIs if it is not
   * available.
   */
  private void stopForegroundCompat() {
    // If we have the new stopForeground API, then use it.
    if (mStopForeground != null) {
      mStopForegroundArgs[0] = Boolean.TRUE;
      try {
        mStopForeground.invoke(this, mStopForegroundArgs);
      } catch (Exception e) {
        Log.e(e);
      }
      return;
    }

    // Fall back on the old API. Note to cancel BEFORE changing the
    // foreground state, since we could be killed at that point.
    mNotificationManager.cancel(mNotificationId);
    setForeground(false);
  }

  @Override
  public void onCreate() {
    mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    try {
      mStartForeground = getClass().getMethod("startForeground", mStartForegroundSignature);
      mStopForeground = getClass().getMethod("stopForeground", mStopForegroundSignature);
    } catch (NoSuchMethodException e) {
      // Running on an older platform.
      mStartForeground = mStopForeground = null;
    }
    startForegroundCompat(createNotification());
  }

  @Override
  public void onDestroy() {
    // Make sure our notification is gone.
    stopForegroundCompat();
  }
}
