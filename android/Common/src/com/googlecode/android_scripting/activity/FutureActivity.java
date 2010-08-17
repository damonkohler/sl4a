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

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;

/**
 * This {@link Activity} is launched by {@link RpcReceiver}s in order to perform operations that a
 * {@link Service} is unable to do. For example: start another activity for result, show dialogs,
 * etc.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class FutureActivity extends Activity {
  private FutureActivityTask<?> mTask;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    int id = getIntent().getIntExtra(Constants.EXTRA_TASK_ID, 0);
    FutureActivityTaskExecutor taskQueue = ((BaseApplication) getApplication()).getTaskQueue();
    mTask = taskQueue.getTask(id);
    mTask.setActivity(this);
    mTask.onCreate();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mTask.onStart();
  }

  @Override
  protected void onResume() {
    super.onResume();
    mTask.onResume();
  }

  @Override
  protected void onPause() {
    super.onPause();
    mTask.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mTask.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mTask.onDestroy();
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    mTask.onActivityResult(requestCode, resultCode, data);
  }
}
