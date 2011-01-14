package com.googlecode.pythonforandroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Python Installer. Incorporates module installer.
 * 
 * The module installer reads a zip file previously downloaded into "download", and unpacks it. If
 * the module contains shared libraries (*.so) then the module is unpacked into data, other installs
 * in extras.
 * 
 * Typically, these will be /data/data/com.googlecode.pythonforandroid/files/python/lib/python2.6
 * and /sdcard/com.googlecode.pythonforandroid/extras respectively.
 * 
 * @author Damon
 * @author Robbie Matthews (rjmatthews62@gmail.com)
 * @author Manuel Narango
 */

public class PythonMain extends Main {
  Button mBtnModules;
  File mDownloads;

  private Dialog mDialog;
  protected String mModule;
  private CharSequence[] mList;
  private ProgressDialog mProgress;
  private AlertDialog mPrompt;
  private boolean mPromptResult;

  final Handler mModuleHandler = new Handler() {

    @Override
    public void handleMessage(Message message) {
      Bundle bundle = message.getData();
      boolean running = bundle.getBoolean("running");
      if (running) {
        if (bundle.containsKey("max")) {
          mProgress.setProgress(0);
          mProgress.setMax(bundle.getInt("max"));
        } else if (bundle.containsKey("pos")) {
          mProgress.setProgress(bundle.getInt("pos"));
        } else if (bundle.containsKey("message")) {
          mProgress.setMessage(bundle.getString("message"));
        } else {
          mProgress.incrementProgressBy(1);
        }
      } else {
        mProgress.dismiss();
        String info = message.getData().getString("info");
        if (info != null) {
          showMessage("Import Module", info);
        }
      }
    }
  };

  private Button mBtnBrowse;
  private File mFrom;
  private File mSoPath;
  private File mPythonPath;

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new PythonDescriptor();
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

    for (File f : new File(Environment.getExternalStorageDirectory().getAbsolutePath()).listFiles()) {
      if (f.isDirectory()) {
        if (f.getName().toLowerCase().startsWith("download")) {
          mDownloads = f;
          break;
        }
      }
    }

    if (mDownloads == null) {
      mDownloads =
          new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "download");
    }

    LinearLayout mlayout = getLayout();

    MarginLayoutParams marginParams =
        new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    final float scale = getResources().getDisplayMetrics().density;
    int marginPixels = (int) (MARGIN_DIP * scale + 0.5f);
    marginParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
    mBtnModules = new Button(this);
    mBtnModules.setLayoutParams(marginParams);
    mBtnModules.setText("Import Modules");
    mlayout.addView(mBtnModules);
    mBtnModules.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        doImportModule();
      }
    });
    mBtnBrowse = new Button(this);
    mBtnBrowse.setLayoutParams(marginParams);
    mBtnBrowse.setText("Browse Modules");
    mlayout.addView(mBtnBrowse);
    mBtnBrowse.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        doBrowseModule();
      }
    });
  }

  protected void doBrowseModule() {
    Intent intent =
        new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mithril.com.au/android/modules"));
    startActivity(intent);
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
    builder.setNegativeButton("Cancel", buttonListener);
    builder.setNeutralButton("Help", buttonListener);
    mModule = null;
    mDialog = builder.show();
    if (mModule != null) {
    }
  }

  protected void performImport(String module) {
    mFrom = new File(mDownloads, mModule);
    mSoPath = new File(InterpreterUtils.getInterpreterRoot(this), "python/lib/python2.6");
    mPythonPath = new File(mDescriptor.getEnvironmentVariables(this).get("PYTHONPATH"));

    prompt("Install module " + module, new DialogInterface.OnClickListener() {

      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which == AlertDialog.BUTTON_POSITIVE) {
          extract("Extracting " + mModule, mFrom, mPythonPath, mSoPath);
        }
      }
    });
  }

  protected void extract(String caption, File from, File pypath, File sopath) {
    mProgress = showProgress(caption);
    Thread t = new RunExtract(caption, from, pypath, sopath, mModuleHandler);
    t.start();
  }

  protected ProgressDialog showProgress(String caption) {
    ProgressDialog b = new ProgressDialog(this);
    b.setTitle(caption);
    b.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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

  protected boolean prompt(String message, DialogInterface.OnClickListener btnlisten) {
    mPromptResult = false;
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Python Installer");
    builder.setMessage(message);
    builder.setNegativeButton("Cancel", btnlisten);
    builder.setPositiveButton("OK", btnlisten);
    mPrompt = builder.show();
    return mPromptResult;
  }

  class RunExtract extends Thread {
    String caption;
    File from;
    File sopath;
    File pypath;
    Handler mHandler;

    RunExtract(String caption, File from, File pypath, File sopath, Handler h) {
      this.caption = caption;
      this.from = from;
      this.pypath = pypath;
      this.sopath = sopath;
      mHandler = h;
    }

    @Override
    public void run() {
      byte[] buf = new byte[4096];
      boolean useshared;
      boolean hasSo = false;
      List<ZipEntry> list = new ArrayList<ZipEntry>();
      try {
        ZipFile zipfile = new ZipFile(from);
        int cnt = 0;
        sendmsg(true, "max", zipfile.size());
        Enumeration<? extends ZipEntry> entries = zipfile.entries();
        while (entries.hasMoreElements()) {
          ZipEntry ex = entries.nextElement();
          if (ex.getName().endsWith(".so")) {
            hasSo = true;
          }
          list.add(ex);
        }
        for (ZipEntry entry : list) {
          cnt += 1;
          if (entry.isDirectory()) {
            continue;
          }
          useshared = hasSo;
          File dpath = useshared ? sopath : pypath;
          File f = new File(dpath, entry.getName());
          File p = f.getParentFile();
          if (!p.exists()) {
            p.mkdirs();
            while (p != null && !p.equals(dpath)) {
              FileUtils.chmod(p, 0775);
              p = p.getParentFile();
            }
          }
          sendmsg(true, "pos", cnt);
          OutputStream output = new BufferedOutputStream(new FileOutputStream(f));
          InputStream input = zipfile.getInputStream(entry);
          int len;
          while ((len = input.read(buf)) > 0) {
            output.write(buf, 0, len);
          }
          input.close();
          output.flush();
          output.close();

          f.setLastModified(entry.getTime());
          FileUtils.chmod(f, entry.getName().endsWith(".so") ? 0755 : 0644);
        }
        sendmsg(false, "Success");
      } catch (Exception entry) {
        sendmsg(false, "Error" + entry);
      }
    }

    private void sendmsg(boolean running, String info) {
      Message message = mHandler.obtainMessage();
      Bundle bundle = new Bundle();
      bundle.putBoolean("running", running);
      if (info != null) {
        bundle.putString("info", info);
      }
      message.setData(bundle);
      mHandler.sendMessage(message);
    }

    private void sendmsg(boolean running, String key, int value) {
      Message message = mHandler.obtainMessage();
      Bundle bundle = new Bundle();
      bundle.putBoolean("running", running);
      bundle.putInt(key, value);
      message.setData(bundle);
      mHandler.sendMessage(message);
    }

    private void sendmsg(boolean running, String key, String value) {
      Message message = mHandler.obtainMessage();
      Bundle bundle = new Bundle();
      bundle.putBoolean("running", running);
      bundle.putString(key, value);
      message.setData(bundle);
      mHandler.sendMessage(message);
    }
  }
}