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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.ase.AsyncTaskListener;
import com.google.ase.InterpreterInstaller;
import com.google.ase.InterpreterUninstaller;

import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

/**
 * Base activity for distributing interpreters as APK's.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public abstract class Main extends Activity {
  protected final static float MARGIN_DIP = 3.0f;
  protected final static float SPINNER_DIP = 10.0f;

  protected final String mId = getClass().getPackage().getName();

  protected SharedPreferences mPreferences;
  protected InterpreterDescriptor mDescriptor;
  protected Button mButton;
  private LinearLayout mProgressLayout;

  protected abstract InterpreterDescriptor getDescriptor();

  protected abstract InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException;

  protected abstract InterpreterUninstaller getInterpreterUninstaller(
      InterpreterDescriptor descriptor, Context context, AsyncTaskListener<Boolean> listener)
      throws Sl4aException;

  protected enum RunningTask {
    INSTALL, UNINSTALL
  }

  protected volatile RunningTask mCurrentTask = null;

  protected final AsyncTaskListener<Boolean> mTaskListener = new AsyncTaskListener<Boolean>() {
    @Override
    public void onTaskFinished(Boolean result, String message) {

      mProgressLayout.setVisibility(View.INVISIBLE);

      if (result) {
        switch (mCurrentTask) {
        case INSTALL:
          setInstalled(true);
          prepareUninstallButton();
          break;
        case UNINSTALL:
          setInstalled(false);
          prepareInstallButton();
          break;
        }
      }
      Sl4aLog.v(Main.this, message);
      mCurrentTask = null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDescriptor = getDescriptor();
    initializeViews();
    if (checkInstalled()) {
      prepareUninstallButton();
    } else {
      prepareInstallButton();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    finish();
  }

  // TODO(alexey): Pull out to a layout XML?
  protected void initializeViews() {
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    layout.setGravity(Gravity.CENTER_HORIZONTAL);

    mButton = new Button(this);
    MarginLayoutParams marginParams =
        new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    final float scale = getResources().getDisplayMetrics().density;
    int marginPixels = (int) (MARGIN_DIP * scale + 0.5f);
    marginParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
    mButton.setLayoutParams(marginParams);
    layout.addView(mButton);

    mProgressLayout = new LinearLayout(this);
    mProgressLayout.setOrientation(LinearLayout.HORIZONTAL);
    mProgressLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
        LayoutParams.FILL_PARENT));
    mProgressLayout.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

    LinearLayout bottom = new LinearLayout(this);
    bottom.setOrientation(LinearLayout.HORIZONTAL);
    bottom.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    bottom.setGravity(Gravity.CENTER_VERTICAL);
    mProgressLayout.addView(bottom);

    TextView message = new TextView(this);
    message.setText("   In Progress...");
    message.setTextSize(20);
    message.setTypeface(Typeface.DEFAULT_BOLD);
    message.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    ProgressBar bar = new ProgressBar(this);
    bar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    bottom.addView(bar);
    bottom.addView(message);
    mProgressLayout.setVisibility(View.INVISIBLE);

    layout.addView(mProgressLayout);
    setContentView(layout);
  }

  protected void prepareInstallButton() {
    mButton.setText("Install");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        install();
      }
    });
  }

  protected void prepareUninstallButton() {
    mButton.setText("Uninstall");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        uninstall();
      }
    });
  }

  protected void broadcastInstallationStateChange(boolean isInterpreterInstalled) {
    Intent intent = new Intent();
    intent.setData(Uri.parse("package:" + mId));
    if (isInterpreterInstalled) {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_ADDED);
    } else {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_REMOVED);
    }
    sendBroadcast(intent);
  }

  protected synchronized void install() {
    if (mCurrentTask != null) {
      return;
    }

    mProgressLayout.setVisibility(View.VISIBLE);

    mCurrentTask = RunningTask.INSTALL;
    InterpreterInstaller installTask;
    try {
      installTask = getInterpreterInstaller(mDescriptor, Main.this, mTaskListener);
    } catch (Sl4aException e) {
      Sl4aLog.e(this, e.getMessage(), e);
      return;
    }
    installTask.execute();
  }

  protected synchronized void uninstall() {
    if (mCurrentTask != null) {
      return;
    }

    mProgressLayout.setVisibility(View.VISIBLE);

    mCurrentTask = RunningTask.UNINSTALL;
    InterpreterUninstaller uninstallTask;
    try {
      uninstallTask = getInterpreterUninstaller(mDescriptor, Main.this, mTaskListener);
    } catch (Sl4aException e) {
      Sl4aLog.e(this, e.getMessage(), e);
      return;
    }
    uninstallTask.execute();
  }

  protected void setInstalled(boolean isInstalled) {
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putBoolean(InterpreterConstants.INSTALL_PREF, isInstalled);
    editor.commit();
    broadcastInstallationStateChange(isInstalled);
  }

  protected boolean checkInstalled() {
    boolean isInstalled = mPreferences.getBoolean(InterpreterConstants.INSTALL_PREF, false);
    broadcastInstallationStateChange(isInstalled);
    return isInstalled;
  }
}
