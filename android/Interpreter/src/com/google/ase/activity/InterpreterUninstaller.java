package com.google.ase.activity;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterConfiguration;

public class InterpreterUninstaller extends Activity {
  private String mName;
  private Interpreter mInterpreter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mName = getIntent().getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
    if (mName == null) {
      AseLog.e("Interpreter not specified.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    mInterpreter = InterpreterConfiguration.getInterpreterByName(mName);
    if (mInterpreter == null) {
      AseLog.e("No matching interpreter found for name: " + mName);
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if (!mInterpreter.isInstalled(this)) {
      AseLog.e("Interpreter not installed.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    uninstall();
  }

  private void delete(File path) {
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

  private void uninstall() {
    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage("Uninstalling " + mInterpreter.getNiceName());
    dialog.setIndeterminate(true);
    dialog.setCancelable(false);
    dialog.show();

    new Thread() {
      @Override
      public void run() {
        File extras = new File(Constants.INTERPRETER_EXTRAS_ROOT, mName);
        File root = InterpreterConfiguration.getInterpreterRoot(InterpreterUninstaller.this, mName);
        File scriptsArchive =
            new File(Constants.DOWNLOAD_ROOT, mInterpreter.getScriptsArchiveName());
        File archive = new File(Constants.DOWNLOAD_ROOT, mInterpreter.getInterpreterArchiveName());
        File extrasArchive =
            new File(Constants.DOWNLOAD_ROOT, mInterpreter.getInterpreterExtrasArchiveName());
        List<File> directories =
            Arrays.asList(extras, root, scriptsArchive, archive, extrasArchive);
        for (File directory : directories) {
          delete(directory);
        }
        dialog.dismiss();
        setResult(RESULT_OK);
        finish();
      }
    }.start();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }
}
