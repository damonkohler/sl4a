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

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;


import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

/**
 * AsyncTask for installing interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class InterpreterInstaller extends AsyncTask<Void, Void, Boolean> {

  protected final InterpreterDescriptor mDescriptor;
  protected final AsyncTaskListener<Boolean> mTaskListener;
  protected final Queue<RequestCode> mTaskQueue;
  protected final Context mContext;

  protected final Handler mainThreadHandler;
  protected Handler mBackgroundHandler;

  protected volatile AsyncTask<Void, Integer, Long> mTaskHolder;

  protected static enum RequestCode {
    DOWNLOAD_INTERPRETER, DOWNLOAD_INTERPRETER_EXTRAS, DOWNLOAD_SCRIPTS, EXTRACT_INTERPRETER,
    EXTRACT_INTERPRETER_EXTRAS, EXTRACT_SCRIPTS
  }

  // Executed in the UI thread.
  private final Runnable mTaskStarter = new Runnable() {
    @Override
    public void run() {
      RequestCode task = mTaskQueue.peek();
      try {
        AsyncTask<Void, Integer, Long> newTask = null;
        switch (task) {
        case DOWNLOAD_INTERPRETER:
          newTask = downloadInterpreter();
          break;
        case DOWNLOAD_INTERPRETER_EXTRAS:
          newTask = downloadInterpreterExtras();
          break;
        case DOWNLOAD_SCRIPTS:
          newTask = downloadScripts();
          break;
        case EXTRACT_INTERPRETER:
          newTask = extractInterpreter();
          break;
        case EXTRACT_INTERPRETER_EXTRAS:
          newTask = extractInterpreterExtras();
          break;
        case EXTRACT_SCRIPTS:
          newTask = extractScripts();
          break;
        }
        mTaskHolder = newTask.execute();
      } catch (Exception e) {
        Sl4aLog.v(e.getMessage(), e);
      }

      if (mBackgroundHandler != null) {
        mBackgroundHandler.post(mTaskWorker);
      }
    }
  };

  // Executed in the background.
  private final Runnable mTaskWorker = new Runnable() {
    @Override
    public void run() {
      RequestCode request = mTaskQueue.remove();
      try {
        if (mTaskHolder != null && mTaskHolder.get() != null) {
          mTaskHolder = null;
          // Post processing.
          if (request == RequestCode.EXTRACT_INTERPRETER && !chmodIntepreter()) {
            // Chmod returned false.
            Looper.myLooper().quit();
          } else if (mTaskQueue.size() == 0) {
            // We're done here.
            Looper.myLooper().quit();
            return;
          } else if (mainThreadHandler != null) {
            // There's still some work to do.
            mainThreadHandler.post(mTaskStarter);
            return;
          }
        }
      } catch (Exception e) {
        Sl4aLog.e(e);
      }
      // Something went wrong...
      switch (request) {
      case DOWNLOAD_INTERPRETER:
        Sl4aLog.e("Downloading interpreter failed.");
        break;
      case DOWNLOAD_INTERPRETER_EXTRAS:
        Sl4aLog.e("Downloading interpreter extras failed.");
        break;
      case DOWNLOAD_SCRIPTS:
        Sl4aLog.e("Downloading scripts failed.");
        break;
      case EXTRACT_INTERPRETER:
        Sl4aLog.e("Extracting interpreter failed.");
        break;
      case EXTRACT_INTERPRETER_EXTRAS:
        Sl4aLog.e("Extracting interpreter extras failed.");
        break;
      case EXTRACT_SCRIPTS:
        Sl4aLog.e("Extracting scripts failed.");
        break;
      }
      Looper.myLooper().quit();
    }
  };

  // TODO(Alexey): Add Javadoc.
  public InterpreterInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> taskListener) throws Sl4aException {
    super();
    mDescriptor = descriptor;
    mContext = context;
    mTaskListener = taskListener;
    mainThreadHandler = new Handler();
    mTaskQueue = new LinkedList<RequestCode>();

    if (mDescriptor == null) {
      throw new Sl4aException("Interpreter description not provided.");
    }
    if (mDescriptor.getName() == null) {
      throw new Sl4aException("Interpreter not specified.");
    }
    if (isInstalled()) {
      throw new Sl4aException("Interpreter is installed.");
    }

    if (mDescriptor.hasInterpreterArchive()) {
      mTaskQueue.offer(RequestCode.DOWNLOAD_INTERPRETER);
      mTaskQueue.offer(RequestCode.EXTRACT_INTERPRETER);
    }
    if (mDescriptor.hasExtrasArchive()) {
      mTaskQueue.offer(RequestCode.DOWNLOAD_INTERPRETER_EXTRAS);
      mTaskQueue.offer(RequestCode.EXTRACT_INTERPRETER_EXTRAS);
    }
    if (mDescriptor.hasScriptsArchive()) {
      mTaskQueue.offer(RequestCode.DOWNLOAD_SCRIPTS);
      mTaskQueue.offer(RequestCode.EXTRACT_SCRIPTS);
    }
  }

  @Override
  protected Boolean doInBackground(Void... params) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        executeInBackground();
        final boolean result = (mTaskQueue.size() == 0);
        mainThreadHandler.post(new Runnable() {
          @Override
          public void run() {
            finish(result);
          }
        });
      }
    }).start();
    return true;
  }

  private boolean executeInBackground() {
    if (Looper.myLooper() == null) {
      Looper.prepare();
    }
    mBackgroundHandler = new Handler(Looper.myLooper());
    mainThreadHandler.post(mTaskStarter);
    Looper.loop();
    // Have we executed all the tasks?
    return (mTaskQueue.size() == 0);
  }

  protected void finish(boolean result) {
    if (result && setup()) {
      mTaskListener.onTaskFinished(true, "Installation successful.");
    } else {
      if (mTaskHolder != null) {
        mTaskHolder.cancel(true);
      }
      cleanup();
      mTaskListener.onTaskFinished(false, "Installation failed.");
    }
  }

  protected AsyncTask<Void, Integer, Long> download(String in) throws MalformedURLException {
    String out = InterpreterConstants.DOWNLOAD_ROOT;
    return new UrlDownloaderTask(in, out, mContext);
  }

  protected AsyncTask<Void, Integer, Long> downloadInterpreter() throws MalformedURLException {
    return download(mDescriptor.getInterpreterArchiveUrl());
  }

  protected AsyncTask<Void, Integer, Long> downloadInterpreterExtras() throws MalformedURLException {
    return download(mDescriptor.getExtrasArchiveUrl());
  }

  protected AsyncTask<Void, Integer, Long> downloadScripts() throws MalformedURLException {
    return download(mDescriptor.getScriptsArchiveUrl());
  }

  protected AsyncTask<Void, Integer, Long> extract(String in, String out) throws Sl4aException {
    return new ZipExtractorTask(in, out, mContext);
  }

  protected AsyncTask<Void, Integer, Long> extractInterpreter() throws Sl4aException {
    String in =
        new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getInterpreterArchiveName())
            .getAbsolutePath();
    String out = InterpreterUtils.getInterpreterRoot(mContext).getAbsolutePath();
    return extract(in, out);
  }

  protected AsyncTask<Void, Integer, Long> extractInterpreterExtras() throws Sl4aException {
    String in =
        new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getExtrasArchiveName())
            .getAbsolutePath();
    String out = InterpreterConstants.INTERPRETER_EXTRAS_ROOT;
    return extract(in, out);
  }

  protected AsyncTask<Void, Integer, Long> extractScripts() throws Sl4aException {
    String in =
        new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getScriptsArchiveName())
            .getAbsolutePath();
    String out = InterpreterConstants.SCRIPTS_ROOT;
    return extract(in, out);
  }

  protected boolean chmodIntepreter() {
    int dataChmodErrno;
    boolean interpreterChmodSuccess;
    try {
      dataChmodErrno = FileUtils.chmod(InterpreterUtils.getInterpreterRoot(mContext), 0755);
      interpreterChmodSuccess =
          FileUtils.recursiveChmod(InterpreterUtils.getInterpreterRoot(mContext, mDescriptor
              .getName()), 0755);
    } catch (Exception e) {
      Sl4aLog.e(e);
      return false;
    }
    return dataChmodErrno == 0 && interpreterChmodSuccess;
  }

  protected boolean isInstalled() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    return preferences.getBoolean(InterpreterConstants.INSTALL_PREF, false);
  }

  private void cleanup() {
    List<File> directories = new ArrayList<File>();

    if (mDescriptor.hasInterpreterArchive()) {
      if (!mTaskQueue.contains(RequestCode.DOWNLOAD_INTERPRETER)) {
        directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
            .getInterpreterArchiveName()));
      }
      if (!mTaskQueue.contains(RequestCode.EXTRACT_INTERPRETER)) {
        directories.add(InterpreterUtils.getInterpreterRoot(mContext, mDescriptor.getName()));
      }
    }

    if (mDescriptor.hasExtrasArchive()) {
      if (!mTaskQueue.contains(RequestCode.DOWNLOAD_INTERPRETER_EXTRAS)) {
        directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
            .getExtrasArchiveName()));
      }
      if (!mTaskQueue.contains(RequestCode.EXTRACT_INTERPRETER_EXTRAS)) {
        directories.add(new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor
            .getName()));
      }
    }

    if (mDescriptor.hasScriptsArchive() && !mTaskQueue.contains(RequestCode.DOWNLOAD_SCRIPTS)) {
      directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
          .getScriptsArchiveName()));
    }

    for (File directory : directories) {
      delete(directory);
    }
  }

  protected void delete(File path) {
    if (path.isDirectory()) {
      for (File child : path.listFiles()) {
        delete(child);
      }
      path.delete(); // Delete empty directory.
    }
    if (path.isFile()) {
      path.delete();
    }
  }

  protected abstract boolean setup();
}
