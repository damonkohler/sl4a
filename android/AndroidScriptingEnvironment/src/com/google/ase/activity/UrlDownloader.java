package com.google.ase.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;

public class UrlDownloader extends Activity {

  private String mFileName;
  private File mOutput;
  private URL mUrl;
  private URLConnection mUrlConnection;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    String url = getIntent().getStringExtra(Constants.EXTRA_URL);
    try {
      mUrl = new URL(url);
    } catch (MalformedURLException e) {
      AseLog.e("Cannot download malformed URL: " + url, e);
      finish();
      return;
    }
    try {
      mUrlConnection = mUrl.openConnection();
    } catch (IOException e) {
      AseLog.e("Cannot open URL connection: " + url, e);
      finish();
      return;
    }
    mFileName = new File(mUrl.getFile()).getName();
    mOutput = new File(getIntent().getStringExtra(Constants.EXTRA_OUTPUT_PATH), mFileName);
    download();
  }

  private void download() {
    if (mOutput.exists()) {
      AseLog.v("Output file already exists. Skipping download.");
      setResult(RESULT_OK);
      return;
    }
    AseLog.v("Downloading " + mUrl);

    final int size = mUrlConnection.getContentLength();

    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setTitle("Downloading");
    dialog.setMessage(mFileName);
    if (size == -1) {
      dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    } else {
      dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      dialog.setMax(size);
    }
    dialog.setCancelable(false);
    dialog.show();

    final OutputStream out;
    try {
      out = new FilterOutputStream(new FileOutputStream(mOutput)) {
        private int mSize = 0;

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
          super.write(buffer, offset, count);
          mSize += count;
          dialog.setProgress(mSize);
        }
      };
    } catch (FileNotFoundException e) {
      AseLog.e(e);
      setResult(RESULT_CANCELED);
      return;
    }

    new Thread() {
      @Override
      public void run() {
        try {
          int bytesCopied = IOUtils.copy(mUrlConnection.getInputStream(), out);
          if (bytesCopied != size && size != -1 /* -1 indicates no ContentLength */) {
            throw new IOException("Download incomplete: " + bytesCopied + " != " + size);
          }
          AseLog.v("Download completed successfully.");
          setResult(RESULT_OK);
        } catch (Exception e) {
          AseLog.e("Download failed.", e);
          if (mOutput.exists()) {
            // Clean up bad downloads.
            mOutput.delete();
          }
          setResult(RESULT_CANCELED);
        } finally {
          dialog.dismiss();
          finish();
        }
      }
    }.start();
  }
}
