/*
 * Copyright (C) 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.future.FutureResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * AsyncTask for extracting ZIP files.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class ZipExtractorTask extends AsyncTask<Void, Integer, Long> {

  private static enum Replace {
    YES, NO, YESTOALL, SKIPALL
  }

  private final File mInput;
  private final File mOutput;
  private final ProgressDialog mDialog;
  private Throwable mException;
  private int mProgress = 0;
  private final Context mContext;
  private boolean mReplaceAll;

  private final class ProgressReportingOutputStream extends FileOutputStream {
    private ProgressReportingOutputStream(File f) throws FileNotFoundException {
      super(f);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
      super.write(buffer, offset, count);
      mProgress += count;
      publishProgress(mProgress);
    }
  }

  public ZipExtractorTask(String in, String out, Context context, boolean replaceAll)
      throws Sl4aException {
    super();
    mInput = new File(in);
    mOutput = new File(out);
    if (!mOutput.exists()) {
      if (!mOutput.mkdirs()) {
        throw new Sl4aException("Failed to make directories: " + mOutput.getAbsolutePath());
      }
    }
    if (context != null) {
      mDialog = new ProgressDialog(context);
    } else {
      mDialog = null;
    }

    mContext = context;
    mReplaceAll = replaceAll;

  }

  @Override
  protected void onPreExecute() {
    Log.v("Extracting " + mInput.getAbsolutePath() + " to " + mOutput.getAbsolutePath());
    if (mDialog != null) {
      mDialog.setTitle("Extracting");
      mDialog.setMessage(mInput.getName());
      mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
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
  protected Long doInBackground(Void... params) {
    try {
      return unzip();
    } catch (Exception e) {
      if (mInput.exists()) {
        // Clean up bad zip file.
        mInput.delete();
      }
      mException = e;
      return null;
    }
  }

  @Override
  protected void onProgressUpdate(Integer... progress) {
    if (mDialog == null) {
      return;
    }
    if (progress.length > 1) {
      int max = progress[1];
      mDialog.setMax(max);
    } else {
      mDialog.setProgress(progress[0].intValue());
    }
  }

  @Override
  protected void onPostExecute(Long result) {
    if (mDialog != null && mDialog.isShowing()) {
      mDialog.dismiss();
    }
    if (isCancelled()) {
      return;
    }
    if (mException != null) {
      Log.e("Zip extraction failed.", mException);
    }
  }

  @Override
  protected void onCancelled() {
    if (mDialog != null) {
      mDialog.setTitle("Extraction cancelled.");
    }
  }

  private long unzip() throws Exception {
    long extractedSize = 0l;
    Enumeration<? extends ZipEntry> entries;
    ZipFile zip = new ZipFile(mInput);
    long uncompressedSize = getOriginalSize(zip);

    publishProgress(0, (int) uncompressedSize);

    entries = zip.entries();

    try {
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (entry.isDirectory()) {
          // Not all zip files actually include separate directory entries.
          // We'll just ignore them
          // and create them as necessary for each actual entry.
          continue;
        }
        File destination = new File(mOutput, entry.getName());
        if (!destination.getParentFile().exists()) {
          destination.getParentFile().mkdirs();
        }
        if (destination.exists() && mContext != null && !mReplaceAll) {
          Replace answer = showDialog(entry.getName());
          switch (answer) {
          case YES:
            break;
          case NO:
            continue;
          case YESTOALL:
            mReplaceAll = true;
            break;
          default:
            return extractedSize;
          }
        }
        ProgressReportingOutputStream outStream = new ProgressReportingOutputStream(destination);
        extractedSize += IoUtils.copy(zip.getInputStream(entry), outStream);
        outStream.close();
      }
    } finally {
      try {
        zip.close();
      } catch (Exception e) {
        // swallow this exception, we are only interested in the original one
      }
    }
    Log.v("Extraction is complete.");
    return extractedSize;
  }

  private long getOriginalSize(ZipFile file) {
    Enumeration<? extends ZipEntry> entries = file.entries();
    long originalSize = 0l;
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.getSize() >= 0) {
        originalSize += entry.getSize();
      }
    }
    return originalSize;
  }

  private Replace showDialog(final String name) {
    final FutureResult<Replace> mResult = new FutureResult<Replace>();

    MainThread.run(mContext, new Runnable() {
      @Override
      public void run() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(String.format("Script \"%s\" already exist.", name));
        builder.setMessage(String.format("Do you want to replace script \"%s\" ?", name));

        DialogInterface.OnClickListener buttonListener = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            Replace result = Replace.SKIPALL;
            switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
              result = Replace.YES;
              break;
            case DialogInterface.BUTTON_NEGATIVE:
              result = Replace.NO;
              break;
            case DialogInterface.BUTTON_NEUTRAL:
              result = Replace.YESTOALL;
              break;
            }
            mResult.set(result);
            dialog.dismiss();
          }
        };
        builder.setNegativeButton("Skip", buttonListener);
        builder.setPositiveButton("Replace", buttonListener);
        builder.setNeutralButton("Replace All", buttonListener);

        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
          @Override
          public void onCancel(DialogInterface dialog) {
            mResult.set(Replace.SKIPALL);
            dialog.dismiss();
          }
        });
        builder.show();
      }
    });

    try {
      return mResult.get();
    } catch (InterruptedException e) {
      Log.e(e);
    }
    return null;
  }
}
