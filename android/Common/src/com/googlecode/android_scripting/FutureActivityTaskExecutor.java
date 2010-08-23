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

import android.content.Context;
import android.content.Intent;

import com.googlecode.android_scripting.activity.FutureActivity;
import com.googlecode.android_scripting.future.FutureActivityTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureActivityTaskExecutor {

  private final Context mContext;
  private final Map<Integer, FutureActivityTask<?>> mTaskMap =
      new ConcurrentHashMap<Integer, FutureActivityTask<?>>();
  private final AtomicInteger mIdGenerator = new AtomicInteger(0);

  public FutureActivityTaskExecutor(Context context) {
    mContext = context;
  }

  public void execute(FutureActivityTask<?> task) {
    int id = mIdGenerator.incrementAndGet();
    mTaskMap.put(id, task);
    launchHelper(id);
  }

  public FutureActivityTask<?> getTask(int id) {
    return mTaskMap.remove(id);
  }

  private void launchHelper(int id) {
    Intent helper = new Intent(mContext, FutureActivity.class);
    helper.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
    helper.putExtra(Constants.EXTRA_TASK_ID, id);
    mContext.startActivity(helper);
  }
}
