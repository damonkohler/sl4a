package com.googlecode.android_scripting;

import android.content.Context;
import android.content.Intent;

import com.googlecode.android_scripting.activity.ScriptingLayerServiceHelper;
import com.googlecode.android_scripting.future.FutureActivityTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskQueue {

  private final Context mService;
  private final Map<Integer, FutureActivityTask<?>> mTaskQueue =
      new ConcurrentHashMap<Integer, FutureActivityTask<?>>();

  private final AtomicInteger mIdGenerator = new AtomicInteger(0);

  public TaskQueue(Context service) {
    mService = service;
  }

  public void offer(FutureActivityTask<?> task) {
    int id = mIdGenerator.incrementAndGet();
    mTaskQueue.put(id, task);
    launchHelper(id);
  }

  public FutureActivityTask<?> poll(int id) {
    return mTaskQueue.remove(id);
  }

  private void launchHelper(int id) {    
    Intent helper = new Intent(mService, ScriptingLayerServiceHelper.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    helper.putExtra(Constants.EXTRA_TASK_ID, id);
    mService.startActivity(helper);
  }
}
