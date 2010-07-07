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

package com.google.ase;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * AsyncTask for installing interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class InterpreterInstaller extends AsyncTask<Void, Void, Boolean> {
  protected final InterpreterDescriptor mDescriptor;
  protected final AsyncTaskListener<Boolean> mListener;
  protected final Queue<RequestCode> taskQueue;
  protected final Context mContext;

  protected final Handler mainThreadHandler;
  protected Handler mBackgroundHandler;

  protected volatile AsyncTask<Void, Integer, Long> taskHolder;

  protected static enum RequestCode {
    DOWNLOAD_INTERPRETER, DOWNLOAD_INTERPRETER_EXTRAS, DOWNLOAD_SCRIPTS, EXTRACT_INTERPRETER,
    EXTRACT_INTERPRETER_EXTRAS, EXTRACT_SCRIPTS
  }

  // Executed in the UI thread.
  private final Runnable taskStarter = new Runnable() {
    @Override
    public void run() {
      RequestCode task = taskQueue.peek();
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
        taskHolder = newTask.execute();
      } catch (AseException e) {
        AseLog.v(e.getMessage(), e);
      }

      if (mBackgroundHandler != null) {
        mBackgroundHandler.post(taskWorker);
      }
    }
  };

  // Executed in the background.
  private final Runnable taskWorker = new Runnable() {
    @Override
    public void run() {
      RequestCode request = taskQueue.remove();
      try {
        if (taskHolder != null && taskHolder.get() != null) {
          taskHolder = null;
          // Postprocessing.
          if (request == RequestCode.EXTRACT_INTERPRETER && !chmodIntepreter()) {
            // Chmod returned false.
            Looper.myLooper().quit();
          } else if (taskQueue.size() == 0) {
            // We're done here.
            Looper.myLooper().quit();
            return;
          } else if (mainThreadHandler != null) {
            // There's still some work to do.
            mainThreadHandler.post(taskStarter);
            return;
          }
        }
      } catch (Exception e) {
        AseLog.e(e);
      }
      // Something went wrong...
      switch (request) {
      case DOWNLOAD_INTERPRETER:
        AseLog.e("Downloading interpreter failed.");
        break;
      case DOWNLOAD_INTERPRETER_EXTRAS:
        AseLog.e("Downloading interpreter extras failed.");
        break;
      case DOWNLOAD_SCRIPTS:
        AseLog.e("Downloading scripts failed.");
        break;
      case EXTRACT_INTERPRETER:
        AseLog.e("Extracting interpreter failed.");
        break;
      case EXTRACT_INTERPRETER_EXTRAS:
        AseLog.e("Extracting interpreter extras failed.");
        break;
      case EXTRACT_SCRIPTS:
        AseLog.e("Extracting scripts failed.");
        break;
      }
      Looper.myLooper().quit();
    }
  };

  // TODO(Alexey): Add Javadoc.
  public InterpreterInstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {

    super();

    mDescriptor = descriptor;
    mContext = context;
    mListener = listener;

    mainThreadHandler = new Handler();

    taskQueue = new LinkedList<RequestCode>();

    if (mDescriptor == null) {
      throw new AseException("Interpreter description not provided.");
    }
    if (mDescriptor.getName() == null) {
      throw new AseException("Interpreter not specified.");
    }
    if (isInstalled()) {
      throw new AseException("Interpreter is installed.");
    }

    if (mDescriptor.hasInterpreterArchive()) {
      taskQueue.offer(RequestCode.DOWNLOAD_INTERPRETER);
      taskQueue.offer(RequestCode.EXTRACT_INTERPRETER);
    }
    if (mDescriptor.hasExtrasArchive()) {
      taskQueue.offer(RequestCode.DOWNLOAD_INTERPRETER_EXTRAS);
      taskQueue.offer(RequestCode.EXTRACT_INTERPRETER_EXTRAS);
    }
    if (mDescriptor.hasScriptsArchive()) {
      taskQueue.offer(RequestCode.DOWNLOAD_SCRIPTS);
      taskQueue.offer(RequestCode.EXTRACT_SCRIPTS);
    }
  }

  @Override
  protected Boolean doInBackground(Void... params) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        executeInBackground();
        final boolean result = (taskQueue.size() == 0);
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
    mainThreadHandler.post(taskStarter);
    Looper.loop();
    // Have we executed all the tasks?
    return (taskQueue.size() == 0);
  }

  protected void finish(boolean result) {
    if (result && setup()) {
      mListener.onTaskFinished(true, "Installation successful.");
    } else {
      if (taskHolder != null) {
        taskHolder.cancel(true);
      }
      cleanup();
      mListener.onTaskFinished(false, "Installation failed.");
    }
  }

  protected AsyncTask<Void, Integer, Long> download(String in, String out) throws AseException {
    return new UrlDownloaderTask(in, out, mContext);
  }

  protected AsyncTask<Void, Integer, Long> downloadInterpreter() throws AseException {
    String in = mDescriptor.getInterpreterArchiveUrl();
    String out = InterpreterConstants.DOWNLOAD_ROOT;
    return download(in, out);
  }

  protected AsyncTask<Void, Integer, Long> downloadInterpreterExtras() throws AseException {
    String in = mDescriptor.getExtrasArchiveUrl();
    String out = InterpreterConstants.DOWNLOAD_ROOT;
    return download(in, out);
  }

  protected AsyncTask<Void, Integer, Long> downloadScripts() throws AseException {
    String in = mDescriptor.getScriptsArchiveUrl();
    String out = InterpreterConstants.DOWNLOAD_ROOT;
    return download(in, out);
  }

  protected AsyncTask<Void, Integer, Long> extract(String in, String out) throws AseException {
    return new ZipExtractorTask(in, out, mContext);
  }

  protected AsyncTask<Void, Integer, Long> extractInterpreter() throws AseException {
    String in =
        new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getInterpreterArchiveName())
            .getAbsolutePath();
    String out = InterpreterUtils.getInterpreterRoot(mContext).getAbsolutePath();
    return extract(in, out);
  }

  protected AsyncTask<Void, Integer, Long> extractInterpreterExtras() throws AseException {
    String in =
        new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getExtrasArchiveName())
            .getAbsolutePath();
    String out = InterpreterConstants.INTERPRETER_EXTRAS_ROOT;
    return extract(in, out);
  }

  protected AsyncTask<Void, Integer, Long> extractScripts() throws AseException {
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
      AseLog.e(e);
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
      if (!taskQueue.contains(RequestCode.DOWNLOAD_INTERPRETER)) {
        directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
            .getInterpreterArchiveName()));
      }
      if (!taskQueue.contains(RequestCode.EXTRACT_INTERPRETER)) {
        directories.add(InterpreterUtils.getInterpreterRoot(mContext, mDescriptor.getName()));
      }
    }

    if (mDescriptor.hasExtrasArchive()) {
      if (!taskQueue.contains(RequestCode.DOWNLOAD_INTERPRETER_EXTRAS)) {
        directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
            .getExtrasArchiveName()));
      }
      if (!taskQueue.contains(RequestCode.EXTRACT_INTERPRETER_EXTRAS)) {
        directories.add(new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor
            .getName()));
      }
    }

    if (mDescriptor.hasScriptsArchive() && !taskQueue.contains(RequestCode.DOWNLOAD_SCRIPTS)) {
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
