package com.google.ase.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.google.ase.AseLog;
import com.google.ase.IoUtils;
import com.google.ase.interpreter.InterpreterConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlDownloader extends Activity {

  private File mFile;
  private FileOutputStream mFileOutputStream;
  private URL mUrl;
  private ProgressDialog mConnectingDialog;
  private ProgressDialog mDialog;

  private OutputStream mProgressReportingOutputStream;
  private final Thread mDownloader = new Thread(new Downloader());

  private static final int MSG_REPORT_CONTENT_LENGTH = 0;
  private static final int MSG_REPORT_PROGRESS = 1;

  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
      case MSG_REPORT_CONTENT_LENGTH:
        mConnectingDialog.dismiss();
        showDownloadingDialog(msg.arg1);
        break;
      case MSG_REPORT_PROGRESS:
        mDialog.setProgress(msg.arg1);
        break;
      default:
        throw new IllegalArgumentException("Unknown message id " + msg.what);
      }
    }
  };

  private void showDownloadingDialog(int contentLength) {
    mDialog = new ProgressDialog(this);
    mDialog.setTitle("Downloading");
    mDialog.setMessage(mFile.getName());
    mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        mDownloader.interrupt();
      }
    });
    if (contentLength == -1) {
      mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    } else {
      mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mDialog.setMax(contentLength);
    }
    mDialog.show();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    showConnectingDialog();
    String url = getIntent().getStringExtra(InterpreterConstants.EXTRA_URL);
    try {
      mUrl = new URL(url);
    } catch (MalformedURLException e) {
      AseLog.e("Cannot download malformed URL: " + url, e);
      finish();
      return;
    }
    String name = new File(mUrl.getFile()).getName();
    mFile = new File(getIntent().getStringExtra(InterpreterConstants.EXTRA_OUTPUT_PATH), name);
    if (mFile.exists()) {
      AseLog.v("Output file already exists. Skipping download.");
      setResult(RESULT_OK);
      finish();
      return;
    }
    try {
      mFileOutputStream = new FileOutputStream(mFile);
    } catch (FileNotFoundException e) {
      AseLog.e(e);
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    mProgressReportingOutputStream = new ProgressReportingOuputStream();
    startDownload();
  }

  private void startDownload() {
    AseLog.v("Downloading " + mUrl);
    mDownloader.setPriority(Thread.NORM_PRIORITY - 1);
    mDownloader.start();
  }

  private final class ProgressReportingOuputStream extends FilterOutputStream {
    private int mProgress = 0;

    private ProgressReportingOuputStream() {
      super(mFileOutputStream);
    }

    @Override
    public void write(byte[] buffer, int offset, int count) throws IOException {
      mProgress += count;
      mHandler.sendMessage(Message.obtain(mHandler, MSG_REPORT_PROGRESS, mProgress, 0));
      out.write(buffer, offset, count);
    }
  }

  private class Downloader implements Runnable {
    private URLConnection mUrlConnection;

    @Override
    public void run() {
      if (!buildConnection()) {
        return;
      }

      int contentLength = mUrlConnection.getContentLength();
      mHandler.sendMessage(Message.obtain(mHandler, MSG_REPORT_CONTENT_LENGTH, contentLength, 0));
      try {
        int bytesCopied =
            IoUtils.copy(mUrlConnection.getInputStream(), mProgressReportingOutputStream);
        if (bytesCopied != contentLength && contentLength != -1 /* -1 indicates no ContentLength */) {
          throw new IOException("Download incomplete: " + bytesCopied + " != " + contentLength);
        }
        AseLog.v("Download completed successfully.");
        setResult(RESULT_OK);
      } catch (Exception e) {
        AseLog.e("Download failed.", e);
        if (mFile.exists()) {
          // Clean up bad downloads.
          mFile.delete();
        }
        setResult(RESULT_CANCELED);
      } finally {
        mDialog.dismiss();
        finish();
      }
    }

    private boolean buildConnection() {
      try {
        mUrlConnection = mUrl.openConnection();
      } catch (IOException e) {
        AseLog.e("Cannot open URL: " + mUrl, e);
        setResult(RESULT_CANCELED);
        finish();
        return false;
      }
      return true;
    }
  }

  private void showConnectingDialog() {
    mConnectingDialog = new ProgressDialog(this);
    mConnectingDialog.setMessage("Connecting...");
    mConnectingDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    mConnectingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        mDownloader.interrupt();
      }
    });
    mConnectingDialog.show();
  }
}
