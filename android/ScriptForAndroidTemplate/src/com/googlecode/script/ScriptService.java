package com.googlecode.script;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterAgent;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

import java.io.File;

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

    InterpreterAgent interpreter = config.getInterpreterForScript(fileName);

    if (interpreter == null || !interpreter.isInstalled()) {
      Log.e(this, "Cannot find an interpreter for script " + fileName);
      stopSelf(startId);
      return;
    }

    // Copies script to memory.
    fileName = InterpreterUtils.getInterpreterRoot(this).getAbsolutePath() + "/" + fileName;

    File script = FileUtils.copyFromStream(fileName, resources.openRawResource(id));

    final AndroidProxy proxy = new AndroidProxy(this, null, true);
    proxy.startLocal();
    ScriptLauncher launcher = new ScriptLauncher(proxy, script, config);
    try {
      launcher.launch(new Runnable() {
        @Override
        public void run() {
          proxy.shutdown();
          stopSelf(startId);
        }
      });
    } catch (Sl4aException e) {
      Log.e(e);
      proxy.shutdown();
      stopSelf(startId);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

}
