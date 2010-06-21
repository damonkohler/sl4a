package com.google.ase.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class InterpreterUninstaller extends Activity {

  protected InterpreterDescriptor mDescriptor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Bundle descriptionBundle = getIntent().getBundleExtra(Constants.EXTRA_INTERPRETER_DESCRIPTION);
    
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
    
    if (!InterpreterUtils.isInstalled(this, mDescriptor)) {
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
    dialog.setMessage("Uninstalling " + mDescriptor.getNiceName());
    dialog.setIndeterminate(true);
    dialog.setCancelable(false);
    dialog.show();

    new Thread() {
      @Override
      public void run() {
        File extras = new File(Constants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName());
        File root =
            InterpreterUtils.getInterpreterRoot(InterpreterUninstaller.this, mDescriptor.getName());
        File scriptsArchive =
            new File(Constants.DOWNLOAD_ROOT, mDescriptor.getScriptsArchiveName());
        File archive = new File(Constants.DOWNLOAD_ROOT, mDescriptor.getInterpreterArchiveName());
        File extrasArchive =
            new File(Constants.DOWNLOAD_ROOT, mDescriptor.getExtrasArchiveName());
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