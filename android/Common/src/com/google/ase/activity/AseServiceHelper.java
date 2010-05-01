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

import java.util.Queue;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.ase.AseApplication;
import com.google.ase.future.FutureActivityTask;
import com.google.ase.future.FutureIntent;

/**
 * This {@link Activity} is launched by the {@link AseService} in order to perform operations that a
 * {@link Service} is unable to do. For example: start another activity for result, show dialogs,
 * etc.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class AseServiceHelper extends Activity {
  FutureIntent mResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setPersistent(true);
    Queue<FutureActivityTask> taskQueue = ((AseApplication) getApplication()).getTaskQueue();
    FutureActivityTask task = taskQueue.poll();
    mResult = task.getResult();
    Handler handler = new Handler();
    handler.post(task.getRunnable(this));
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      // TODO(damonkohler): Also return resultCode if necessary.
      mResult.set(data);
      finish();
    }
  }
}
