package com.googlecode.android_scripting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.content.Context;
import android.content.Intent;

import com.googlecode.android_scripting.activity.ScriptingLayerServiceHelper;
import com.googlecode.android_scripting.future.FutureActivityTask;

public class TaskQueue {

  private final Context mService;
  private final Queue<FutureActivityTask<?>> mTaskQueue =
      new ConcurrentLinkedQueue<FutureActivityTask<?>>();

  public TaskQueue(Context service) {
    mService = service;
  }

  public void offer(FutureActivityTask<?> task) {
    mTaskQueue.offer(task);
    launchHelper();
  }

  public FutureActivityTask<?> poll() {
    return mTaskQueue.poll();
  }

  private void launchHelper() {
    Intent helper = new Intent(mService, ScriptingLayerServiceHelper.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mService.startActivity(helper);
  }
}
