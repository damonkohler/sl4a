package com.googlecode.script;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import com.googlecode.android_scripting.AndroidProxy;
import com.googlecode.android_scripting.ScriptLauncher;
import com.googlecode.android_scripting.ScriptStorageAdapter;
import com.googlecode.android_scripting.Sl4aLog;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterAgent;
import com.googlecode.android_scripting.interpreter.InterpreterConfiguration;

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
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    ScriptApplication app = (ScriptApplication) getApplication();
    InterpreterConfiguration config = app.getInterpreterConfiguration();
    Resources resources = getResources();
    int id = R.raw.script;
    String name = resources.getText(id).toString();
    String fileName = name.substring(name.lastIndexOf('/') + 1, name.length());

    InterpreterAgent interpreter = config.getInterpreterForScript(fileName);

    if (interpreter == null || !interpreter.isInstalled(this)) {
      Sl4aLog.e("Cannot find an interpreter for script " + fileName);
      stopSelf(startId);
    }

    // Copies script to disk(sdcard).
    File script = ScriptStorageAdapter.copyFromStream(fileName, resources.openRawResource(id));

    AndroidProxy proxy = new AndroidProxy(this, null, true);
    proxy.startLocal();
    ScriptLauncher launcher = new ScriptLauncher(proxy, script, config);
    try {
      launcher.launch();
    } catch (Sl4aException e) {
      Sl4aLog.e(e);
      proxy.shutdown();
      stopSelf(startId);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

}
