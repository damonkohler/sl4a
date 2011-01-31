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

package com.googlecode.android_scripting.activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;

import com.googlecode.android_scripting.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A utility class supplying helper methods for {@link Service} objects.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 */
public class ServiceUtils {
  private ServiceUtils() {
  }

  /**
   * Marks the service as a foreground service. This uses reflection to figure out whether the new
   * APIs for marking a service as a foreground service are available. If not, it falls back to the
   * old {@link #setForeground(boolean)} call.
   * 
   * @param service
   *          the service to put in foreground mode
   * @param notificationId
   *          id of the notification to show
   * @param notification
   *          the notification to show
   */
  public static void setForeground(Service service, Integer notificationId,
      Notification notification) {
    final Class<?>[] startForegroundSignature = new Class[] { int.class, Notification.class };
    Method startForeground = null;
    try {
      startForeground = service.getClass().getMethod("startForeground", startForegroundSignature);

      try {
        startForeground.invoke(service, new Object[] { notificationId, notification });
      } catch (IllegalArgumentException e) {
        // Should not happen!
        Log.e("Could not set TriggerService to foreground mode.", e);
      } catch (IllegalAccessException e) {
        // Should not happen!
        Log.e("Could not set TriggerService to foreground mode.", e);
      } catch (InvocationTargetException e) {
        // Should not happen!
        Log.e("Could not set TriggerService to foreground mode.", e);
      }

    } catch (NoSuchMethodException e) {
      // Fall back on old API.
      service.setForeground(true);

      NotificationManager manager =
          (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);
      manager.notify(notificationId, notification);
    }
  }
}
