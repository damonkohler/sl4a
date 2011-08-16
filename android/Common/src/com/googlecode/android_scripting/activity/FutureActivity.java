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
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;

import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FutureActivityTaskExecutor;
import com.googlecode.android_scripting.Log;
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
    Log.v("FutureActivity created.");
    int id = getIntent().getIntExtra(Constants.EXTRA_TASK_ID, 0);
    if (id == 0) {
      throw new RuntimeException("FutureActivityTask ID is not specified.");
    }
    FutureActivityTaskExecutor taskQueue = ((BaseApplication) getApplication()).getTaskExecutor();
    mTask = taskQueue.getTask(id);
    if (mTask == null) { // TODO: (Robbie) This is now less of a kludge. Would still like to know
                         // what is happening.
      Log.w("FutureActivity has no task!");
      try {
        Intent intent = new Intent(Intent.ACTION_MAIN); // Should default to main of current app.
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        String packageName = getPackageName();
        for (ResolveInfo resolve : getPackageManager().queryIntentActivities(intent, 0)) {
          if (resolve.activityInfo.packageName.equals(packageName)) {
            intent.setClassName(packageName, resolve.activityInfo.name);
            break;
          }
        }
        startActivity(intent);
      } catch (Exception e) {
        Log.e("Can't find main activity.");
      }
    } else {
      mTask.setActivity(this);
      mTask.onCreate();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (mTask != null) {
      mTask.onStart();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mTask != null) {
      mTask.onResume();
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mTask != null) {
      mTask.onPause();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mTask != null) {
      mTask.onStop();
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mTask != null) {
      mTask.onDestroy();
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    if (mTask != null) {
      mTask.onCreateContextMenu(menu, v, menuInfo);
    }
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    if (mTask == null) {
      return false;
    } else {
      return mTask.onPrepareOptionsMenu(menu);
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (mTask != null) {
      mTask.onActivityResult(requestCode, resultCode, data);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (mTask != null) {
      return mTask.onKeyDown(keyCode, event);
    }
    return false;
  }
}
