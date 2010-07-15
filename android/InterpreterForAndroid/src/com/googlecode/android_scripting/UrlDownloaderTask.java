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

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;


import com.googlecode.android_scripting.IoUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.exception.Sl4aException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * AsyncTask for extracting ZIP files.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class UrlDownloaderTask extends AsyncTask<Void, Integer, Long> {

  private final URL mUrl;
  private final File mFile;
  private final ProgressDialog mDialog;

  private Throwable mException;
  private OutputStream mProgressReportingOutputStream;

  private final class ProgressReportingOutputStream extends FileOutputStream {
    private int mProgress = 0;

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

  public UrlDownloaderTask(String url, String out, Context context) throws MalformedURLException {
    super();
    if (context != null) {
      mDialog = new ProgressDialog(context);
    } else {
      mDialog = null;
    }
    mUrl = new URL(url);
    String fileName = new File(mUrl.getFile()).getName();
    mFile = new File(out, fileName);
  }

  @Override
  protected void onPreExecute() {
    Log.v("Downloading " + mUrl);
    if (mDialog != null) {
      mDialog.setTitle("Downloading");
      mDialog.setMessage(mFile.getName());
      // mDialog.setIndeterminate(true);
      mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
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
      return download();
    } catch (Exception e) {
      if (mFile.exists()) {
        // Clean up bad downloads.
        mFile.delete();
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
      int contentLength = progress[1];
      if (contentLength == -1) {
        mDialog.setIndeterminate(true);
      } else {
        mDialog.setMax(contentLength);
      }
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
      Log.e("Download failed.", mException);
    }
  }

  @Override
  protected void onCancelled() {
    if (mDialog != null) {
      mDialog.setTitle("Download cancelled.");
    }
  }

  private long download() throws Exception {
    URLConnection connection = null;
    try {
      connection = mUrl.openConnection();
    } catch (IOException e) {
      throw new Sl4aException("Cannot open URL: " + mUrl, e);
    }

    int contentLength = connection.getContentLength();

    if (mFile.exists() && contentLength == mFile.length()) {
      Log.v("Output file already exists. Skipping download.");
      return 0l;
    }

    try {
      mProgressReportingOutputStream = new ProgressReportingOutputStream(mFile);
    } catch (FileNotFoundException e) {
      throw new Sl4aException(e);
    }

    publishProgress(0, contentLength);

    int bytesCopied = IoUtils.copy(connection.getInputStream(), mProgressReportingOutputStream);
    if (bytesCopied != contentLength && contentLength != -1) {
      throw new IOException("Download incomplete: " + bytesCopied + " != " + contentLength);
    }
    mProgressReportingOutputStream.close();
    Log.v("Download completed successfully.");
    return bytesCopied;
  }
}
