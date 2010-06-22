package com.google.ase.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.ase.AseLog;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public abstract class InterpreterUninstaller extends Activity {

  protected InterpreterDescriptor mDescriptor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Bundle descriptionBundle =
        getIntent().getBundleExtra(InterpreterConstants.EXTRA_INTERPRETER_DESCRIPTION);

    if (descriptionBundle == null) {
      AseLog.e("Interpreter description not provided.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    mDescriptor = InterpreterUtils.unbundle(descriptionBundle);

    if (mDescriptor.getName() == null) {
      AseLog.e("Interpreter not specified.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    if (!isInstalled()) {
      AseLog.e("Interpreter not installed.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    uninstall();
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

  protected void uninstall() {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage("Uninstalling " + mDescriptor.getNiceName());
    dialog.setIndeterminate(true);
    dialog.setCancelable(false);
    dialog.show();

    new Thread() {
      @Override
      public void run() {
        File extras = new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName());
        File root =
            InterpreterUtils.getInterpreterRoot(InterpreterUninstaller.this, mDescriptor.getName());
        File scriptsArchive =
            new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getScriptsArchiveName());
        File archive =
            new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getInterpreterArchiveName());
        File extrasArchive =
            new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor.getExtrasArchiveName());
        List<File> directories =
            Arrays.asList(extras, root, scriptsArchive, archive, extrasArchive);
        for (File directory : directories) {
          delete(directory);
        }
        dialog.dismiss();
        if (cleanup()) {
          setResult(RESULT_OK);
        } else {
          setResult(RESULT_CANCELED);
        }
        finish();
      }
    }.start();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  protected boolean isInstalled() {
    String packageName = getClass().getPackage().getName();
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    return preferences.getBoolean(packageName, false);
  }

  protected abstract boolean cleanup();
}
