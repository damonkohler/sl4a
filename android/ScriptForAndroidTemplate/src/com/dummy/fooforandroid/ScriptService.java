package com.dummy.fooforandroid;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

public class ScriptService extends Service {
  private final IBinder mBinder;

  public class LocalBinder extends Binder {
    public ScriptService getService() {
      return ScriptService.this;
    }
  }

  public ScriptService() {
    mBinder = new LocalBinder();
  }

  @Override
  public void onStart(Intent intent, final int startId) {
    super.onStart(intent, startId);
    ScriptApplication app = (ScriptApplication) getApplication();
    InterpreterConfiguration config = app.getInterpreterConfiguration();
    Resources resources = getResources();
    int id = R.raw.script;
    String name = resources.getText(id).toString();
    String fileName = name.substring(name.lastIndexOf('/') + 1, name.length());
    Interpreter interpreter = config.getInterpreterForScript(fileName);

    if (interpreter == null || !interpreter.isInstalled()) {
      Log.e(this, "Cannot find an interpreter for script " + fileName);
      stopSelf(startId);
      return;
    }

    // Copies script to internal memory.
    fileName = InterpreterUtils.getInterpreterRoot(this).getAbsolutePath() + "/" + fileName;
    File script = FileUtils.copyFromStream(fileName, resources.openRawResource(id));

    final AndroidProxy proxy = new AndroidProxy(this, null, true);
    proxy.startLocal();
    ScriptLauncher.launchScript(proxy, script, null, new Runnable() {
      @Override
      public void run() {
        proxy.shutdown();
        stopSelf(startId);
      }
    });
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }
}
