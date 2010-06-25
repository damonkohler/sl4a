package com.google.ase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.google.ase.exception.AseException;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class InterpreterUninstaller extends AsyncTask<Void, Void, Boolean> {

  protected final InterpreterDescriptor mDescriptor;
  protected final Context mContext;
  protected final ProgressDialog mDialog;
  protected final AsyncTaskListener<Boolean> mListener;

  public InterpreterUninstaller(InterpreterDescriptor descriptor, Context context,
      AsyncTaskListener<Boolean> listener) throws AseException {

    super();

    mDescriptor = descriptor;
    mContext = context;
    mListener = listener;

    if (mDescriptor == null) {
      throw new AseException("Interpreter description not provided.");
    }
    if (mDescriptor.getName() == null) {
      throw new AseException("Interpreter not specified.");
    }
    if (!isInstalled()) {
      throw new AseException("Interpreter not installed.");
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

    if (mDescriptor.hasInterpreterArchive()) {
      directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
          .getInterpreterArchiveName()));

      directories.add(InterpreterUtils.getInterpreterRoot(mContext, mDescriptor.getName()));
    }
    if (mDescriptor.hasExtrasArchive()) {
      directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
          .getExtrasArchiveName()));

      directories
          .add(new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, mDescriptor.getName()));
    }
    if (mDescriptor.hasScriptsArchive()) {
      directories.add(new File(InterpreterConstants.DOWNLOAD_ROOT, mDescriptor
          .getScriptsArchiveName()));
    }

    for (File directory : directories) {
      delete(directory);
    }

    return cleanup();
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

  protected boolean isInstalled() {
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    return preferences.getBoolean(InterpreterConstants.INSTALL_PREF, false);
  }

  protected abstract boolean cleanup();
}
