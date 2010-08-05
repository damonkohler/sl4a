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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * AsyncTask for uninstalling interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class InterpreterUninstaller extends AsyncTask<Void, Void, Boolean> {

  protected final InterpreterDescriptor mDescriptor;
  protected final Context mContext;
  protected final ProgressDialog mDialog;
  protected final AsyncTaskListener<Boolean> mListener;

  protected final String mInterpreterRoot;

  public InterpreterUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws Sl4aException {

    super();

    mDescriptor = descriptor;
    mContext = context;
    mListener = listener;

    String packageName = mDescriptor.getClass().getPackage().getName();

    if (packageName.length() == 0) {
      throw new Sl4aException("Interpreter package name is empty.");
    }

    mInterpreterRoot = InterpreterConstants.SDCARD_ROOT + packageName;

    if (mDescriptor == null) {
      throw new Sl4aException("Interpreter description not provided.");
    }
    if (mDescriptor.getName() == null) {
      throw new Sl4aException("Interpreter not specified.");
    }
    if (!isInstalled()) {
      throw new Sl4aException("Interpreter not installed.");
    }

    if (context != null) {
      mDialog = new ProgressDialog(context);
    } else {
      mDialog = null;
    }
  }

  public final void execute() {
    execute(null, null, null);
  }

  @Override
  protected void onPreExecute() {
    if (mDialog != null) {
      mDialog.setMessage("Uninstalling " + mDescriptor.getNiceName());
      mDialog.setIndeterminate(true);
      mDialog.setOnCancelListener(new OnCancelListener() {
        @Override
        public void onCancel(DialogInterface dialog) {
          cancel(true);
        }
      });
      mDialog.show();
    }
  }

  @Override
  protected void onPostExecute(Boolean result) {
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    if (result) {
      mListener.onTaskFinished(result, "Uninstallation successful.");
    } else {
      mListener.onTaskFinished(result, "Uninstallation failed.");
    }
  }

  @Override
  protected Boolean doInBackground(Void... params) {
    List<File> directories = new ArrayList<File>();

    directories.add(new File(mInterpreterRoot));

    if (mDescriptor.hasInterpreterArchive()) {
      directories.add(InterpreterUtils.getInterpreterRoot(mContext, mDescriptor.getName()));
    }

    for (File directory : directories) {
      FileUtils.delete(directory);
    }

    return cleanup();
  }

  protected boolean isInstalled() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    return preferences.getBoolean(InterpreterConstants.INSTALLED_PREFERENCE_KEY, false);
  }

  protected abstract boolean cleanup();
}
