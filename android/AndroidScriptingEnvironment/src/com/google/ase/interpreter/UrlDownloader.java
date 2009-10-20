package com.google.ase.interpreter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    AseLog.v("Downloading " + mUrl);

    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setMessage("Downloading " + mFileName);
    dialog.setIndeterminate(true);
    dialog.setCancelable(false);
    dialog.show();

    new Thread() {
      @Override
      public void run() {
        try {
          if (!mOutput.exists()) {
            int bytesCopied = IOUtils.copy(mUrlConnection.getInputStream(),
                new FileOutputStream(mOutput));
            int size = mUrlConnection.getContentLength();
            if (bytesCopied != size && size != -1 /* -1 indicates no ContentLength */) {
              throw new IOException("Download incomplete: " + bytesCopied + " != " + size);
            }
            AseLog.v("Download completed successfully.");
          } else {
            AseLog.v("Output file already exists.");
          }
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
