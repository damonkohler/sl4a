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

import java.util.HashMap;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.TaskQueue;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.future.FutureResult;

/**
 * This {@link Activity} is launched by the {@link Sl4aService} in order to perform operations that
 * a {@link Service} is unable to do. For example: start another activity for result, show dialogs,
 * etc.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ScriptingLayerServiceHelper extends Activity {

  private TaskQueue mTaskQueue;
  private Handler mHandler;
  private HashMap<Integer, FutureResult<?>> mResultMap;
  private volatile boolean mFinished = false;
  private Integer mBlockOnId = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mHandler = new Handler();
    mTaskQueue = ((BaseApplication) getApplication()).getTaskQueue();
    mResultMap = new HashMap<Integer, FutureResult<?>>();
    mFinished = false;
    setPersistent(true);
  }

  @Override
  protected void onResume() {
    mFinished = false;
    super.onResume();
    if (mBlockOnId != null) {
      FutureResult<?> result = mResultMap.get(mBlockOnId);
      result.set(null);
      mFinished = true;
      taskDone(mBlockOnId);
      mBlockOnId = null;
    } else {
      process();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mBlockOnId != null) {
      FutureResult<?> result = mResultMap.get(mBlockOnId);
      result.set(null);
      mResultMap.remove(mBlockOnId);
      mBlockOnId = null;
      throw new RuntimeException();
    }
  }

  private void process() {
    while (true) {
      FutureActivityTask<?> task = mTaskQueue.poll();
      if (task == null) {
        break;
      }
      if (task.isBlocking()) {
        mBlockOnId = task.getTaskId();
      }
      mHandler.post(task.getRunnable(this));
      FutureResult<?> result = task.getFutureResult();
      mResultMap.put(task.getTaskId(), result);
    }
    mFinished = true;
  }

  public void taskDone(int taskId) {
    FutureResult<?> futureResult = mResultMap.remove(taskId);
    if (mFinished && futureResult != null && mResultMap.isEmpty()) {
      finish();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    FutureResult<?> result = mResultMap.get(requestCode);
    if (result != null) {
      result.set(data);
      taskDone(requestCode);
    }
  }
}
