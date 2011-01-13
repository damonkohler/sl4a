/*
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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.InterpreterUninstaller;
import com.googlecode.android_scripting.Log;
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

  protected final String mId = getClass().getPackage().getName();

  protected SharedPreferences mPreferences;
  protected InterpreterDescriptor mDescriptor;
  protected Button mButton;
  protected LinearLayout mLayout;

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
      getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS,
          Window.PROGRESS_VISIBILITY_OFF);
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
      Log.v(Main.this, message);
      mCurrentTask = null;
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDescriptor = getDescriptor();

    requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
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
    mLayout = new LinearLayout(this);
    mLayout.setOrientation(LinearLayout.VERTICAL);
    mLayout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    mLayout.setGravity(Gravity.CENTER_HORIZONTAL);

    mButton = new Button(this);
    MarginLayoutParams marginParams =
        new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    final float scale = getResources().getDisplayMetrics().density;
    int marginPixels = (int) (MARGIN_DIP * scale + 0.5f);
    marginParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
    mButton.setLayoutParams(marginParams);
    mLayout.addView(mButton);
    setContentView(mLayout);
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
    getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
    mCurrentTask = RunningTask.INSTALL;
    InterpreterInstaller installTask;
    try {
      installTask = getInterpreterInstaller(mDescriptor, Main.this, mTaskListener);
    } catch (Sl4aException e) {
      Log.e(this, e.getMessage(), e);
      return;
    }
    installTask.execute();
  }

  protected synchronized void uninstall() {
    if (mCurrentTask != null) {
      return;
    }
    getWindow().setFeatureInt(Window.FEATURE_INDETERMINATE_PROGRESS, Window.PROGRESS_VISIBILITY_ON);
    mCurrentTask = RunningTask.UNINSTALL;
    InterpreterUninstaller uninstallTask;
    try {
      uninstallTask = getInterpreterUninstaller(mDescriptor, Main.this, mTaskListener);
    } catch (Sl4aException e) {
      Log.e(this, e.getMessage(), e);
      return;
    }
    uninstallTask.execute();
  }

  protected void setInstalled(boolean isInstalled) {
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putBoolean(InterpreterConstants.INSTALLED_PREFERENCE_KEY, isInstalled);
    editor.commit();
    broadcastInstallationStateChange(isInstalled);
  }

  protected boolean checkInstalled() {
    boolean isInstalled =
        mPreferences.getBoolean(InterpreterConstants.INSTALLED_PREFERENCE_KEY, false);
    broadcastInstallationStateChange(isInstalled);
    return isInstalled;
  }

  public LinearLayout getLayout() {
    return mLayout;
  }

}
