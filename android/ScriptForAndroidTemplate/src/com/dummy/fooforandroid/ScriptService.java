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

package com.dummy.fooforandroid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.BaseApplication;
import com.googlecode.android_scripting.Constants;
import com.googlecode.android_scripting.FeaturedInterpreters;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;
import com.googlecode.android_scripting.interpreter.html.HtmlActivityTask;
import com.googlecode.android_scripting.interpreter.html.HtmlInterpreter;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;

import java.io.File;
import java.util.concurrent.CountDownLatch;

/**
 * A service that allows scripts and the RPC server to run in the background.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class ScriptService extends Service {

  private final IBinder mBinder;
  private InterpreterConfiguration mInterpreterConfiguration;
  private RpcReceiverManager mManager;
  private final CountDownLatch mLatch = new CountDownLatch(1);

  public class LocalBinder extends Binder {
    public ScriptService getService() {
      return ScriptService.this;
    }
  }

  public ScriptService() {
    mBinder = new LocalBinder();
  }

  @Override
  public void onCreate() {
    mInterpreterConfiguration = ((BaseApplication) getApplication()).getInterpreterConfiguration();
  }

  @Override
  public void onStart(Intent intent, final int startId) {
    super.onStart(intent, startId);
    String fileName = Script.getFileName(this);
    Interpreter interpreter = mInterpreterConfiguration.getInterpreterForScript(fileName);
    if (interpreter == null || !interpreter.isInstalled()) {
      mLatch.countDown();
      if (FeaturedInterpreters.isSupported(fileName)) {
        Intent i = new Intent(this, DialogActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.putExtra(Constants.EXTRA_SCRIPT_PATH, fileName);
        startActivity(i);
      } else {
        Log.e(this, "Cannot find an interpreter for script " + fileName);
      }
      stopSelf(startId);
      return;
    }

    // Copies script to internal memory.
    fileName = InterpreterUtils.getInterpreterRoot(this).getAbsolutePath() + "/" + fileName;
    File script = new File(fileName);
    // TODO(raaar): Check size here!
    if (!script.exists()) {
      script = FileUtils.copyFromStream(fileName, getResources().openRawResource(Script.ID));
    }

    if (Script.getFileExtension(this).equals(HtmlInterpreter.HTML_EXTENSION)) {
      HtmlActivityTask htmlTask =
          ScriptLauncher.launchHtmlScript(script, this, intent, mInterpreterConfiguration);
      mManager = htmlTask.getRpcReceiverManager();
      mLatch.countDown();
      stopSelf(startId);
    } else {
      final AndroidProxy proxy = new AndroidProxy(this, null, true);
      proxy.startLocal();
      mManager = proxy.getRpcReceiverManager();
      mLatch.countDown();
      ScriptLauncher.launchScript(script, mInterpreterConfiguration, proxy, null, new Runnable() {
        @Override
        public void run() {
          proxy.shutdown();
          stopSelf(startId);
        }
      });
    }
  }

  RpcReceiverManager getRpcReceiverManager() throws InterruptedException {
    mLatch.await();
    return mManager;
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
