package com.googlecode.script;

import android.app.Service;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Binder;
import android.os.IBinder;

import com.google.ase.AndroidProxy;
import com.google.ase.AseLog;
import com.google.ase.ScriptLauncher;
import com.google.ase.ScriptStorageAdapter;
import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterAgent;
import com.google.ase.interpreter.InterpreterConfiguration;

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
      AseLog.e("Cannot find an interpreter for script " + fileName);
      stopSelf(startId);
    }

    // Copies script to disk(sdcard).
    File script = ScriptStorageAdapter.copyFromStream(fileName, resources.openRawResource(id));

    AndroidProxy proxy = new AndroidProxy(this, null, true);
    proxy.startLocal();
    ScriptLauncher launcher = new ScriptLauncher(script, proxy.getAddress(), config);
    try {
      launcher.launch();
    } catch (AseException e) {
      AseLog.e(e);
      proxy.shutdown();
      stopSelf(startId);
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return mBinder;
  }

}
