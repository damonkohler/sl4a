package com.googlecode.pythonforandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.InterpreterUninstaller;
import com.googlecode.android_scripting.activity.Main;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;
import com.googlecode.android_scripting.interpreter.InterpreterUtils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PythonMain extends Main {
  Button mBtnModules;
  TextView mEditText;
  File mDownloads;

  private Dialog mDialog;
  protected String mModule;
  private CharSequence[] mList;

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PythonDescriptor();
  }

  protected AsyncTask<Void, Integer, Long> extractExtras() {
    return null;
  }

  protected AsyncTask<Void, Integer, Long> extractSo() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PythonInstaller(descriptor, context, listener);
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new PythonUninstaller(descriptor, context, listener);
  }

  @Override
  protected void initializeViews() {
    super.initializeViews();
    mDownloads = new File(Environment.getExternalStorageDirectory(), "download");
    LinearLayout layout = getLayout();

    MarginLayoutParams marginParams =
        new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    final float scale = getResources().getDisplayMetrics().density;
    int marginPixels = (int) (MARGIN_DIP * scale + 0.5f);
    marginParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
    mBtnModules = new Button(this);
    mBtnModules.setLayoutParams(marginParams);
    mBtnModules.setText("Import Modules");
    layout.addView(mBtnModules);
    mBtnModules.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        doImportModule();
      }
    });
    mEditText = new EditText(this);
  }

  public void doImportModule() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        mDialog.dismiss();
        if (which == DialogInterface.BUTTON_NEUTRAL) {
          showMessage("Import Module",
              "This will take a previously downloaded (and appropriately formatted) "
                  + "python external module zip file.\nSee sl4a wiki for more defails.\n"
                  + "Looking for files in \n" + mDownloads);
        }
      }
    };

    List<String> flist = new Vector<String>();
    for (File f : mDownloads.listFiles()) {
      if (f.getName().endsWith(".zip")) {
        flist.add(f.getName());
      }
    }

    builder.setTitle("Import Module");

    mList = flist.toArray(new CharSequence[flist.size()]);
    builder.setItems(mList, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        mModule = (String) mList[which];
        performImport(mModule);
        mDialog.dismiss();
      }
    });
    // builder.setPositiveButton("OK", buttonListener);
    builder.setNegativeButton("Cancel", buttonListener);
    builder.setNeutralButton("Help", buttonListener);
    // builder.setMessage("Select module to import");
    mModule = null;
    mDialog = builder.show();
    if (mModule != null) {
    }
  }

  protected void performImport(String module) {
    File from = new File(mDownloads, module);
    File to = new File(InterpreterUtils.getInterpreterRoot(this), "python/lib/python2.6");
    if (extract("Shared Libraries", from, to, true)) {
      to = new File(mDescriptor.getEnvironmentVariables(this).get("PYTHONPATH"));
      if (extract("Python Modules", from, to, false)) {
        showMessage("Import Modules", mModule + " imported successfully.");
      }
    }
  }

  private boolean extract(String caption, File from, File to, boolean useshared) {
    byte[] buf = new byte[4096];
    ((AlertDialog) mDialog).setTitle("Extracting " + caption);
    ProgressDialog progress = null;
    try {
      progress = ProgressDialog.show(this, caption, "Extracting...");
      try {
        ZipInputStream z = new ZipInputStream(new FileInputStream(from));
        ZipEntry e;
        while ((e = z.getNextEntry()) != null) {
          if (e.isDirectory()) {
            continue;
          }
          File f = new File(to, e.getName());
          if (f.getName().endsWith(".so") == useshared) {
            File p = f.getParentFile();
            if (!p.exists()) {
              if (!p.mkdirs()) {
                showMessage("Import Log", "Unable to mkdirs\n" + p.getAbsolutePath());
              }
            }
            progress.incrementProgressBy(1);
            OutputStream o = new BufferedOutputStream(new FileOutputStream(f));
            int len;
            while ((len = z.read(buf)) > 0) {
              o.write(buf, 0, len);
            }
            o.flush();
            o.close();
            f.setLastModified(e.getTime());
            if (useshared) {
              FileUtils.chmod(f, 0755);
            }
          }
        }
        return true;
      } catch (Exception e) {
        showMessage(caption, "Error" + e);
        return false;
      }
    } finally {
      if (progress != null) {
        progress.dismiss();
      }
    }
  }

  protected ProgressDialog showProgress(String caption) {
    ProgressDialog b = new ProgressDialog(this);
    b.setTitle(caption);
    b.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    b.show();
    return b;
  }

  protected void showMessage(String title, String message) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(title);
    builder.setMessage(message);
    builder.setNeutralButton("OK", null);
    builder.show();
  }
}