/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.IoUtils;

/**
 * Activity for extracting ZIP files.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class ZipExtractor extends Activity {

  private File mInput;
  private File mOutput;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mInput = new File(getIntent().getStringExtra(Constants.EXTRA_INPUT_PATH));
    mOutput = new File(getIntent().getStringExtra(Constants.EXTRA_OUTPUT_PATH));
    if (!mOutput.exists()) {
      if (!mOutput.mkdirs()) {
        AseLog.e(this, "Failed to make directories.");
        setResult(RESULT_CANCELED);
        finish();
        return;
      }
    }
    startUnzipThread();
  }

  private void startUnzipThread() {
    AseLog.v("Extracting " + mInput.getAbsolutePath() + " to " + mOutput.getAbsolutePath());

    final ProgressDialog dialog = new ProgressDialog(this);
    dialog.setTitle("Extracting");
    dialog.setMessage(mInput.getName());
    dialog.setIndeterminate(true);

    final Thread unzipThread = new Thread() {
      @Override
      public void run() {
        try {
          unzip();
          setResult(RESULT_OK);
        } catch (Exception e) {
          AseLog.e("Zip extraction failed.", e);
          if (mInput.exists()) {
            // Clean up bad zip file.
            mInput.delete();
          }
          setResult(RESULT_CANCELED);
        } finally {
          dialog.dismiss();
          finish();
        }
      }
    };

    dialog.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        unzipThread.interrupt();
      }
    });

    unzipThread.start();
    dialog.show();
  }

  private void unzip() throws ZipException, IOException, InterruptedException {
    Enumeration<? extends ZipEntry> entries;
    ZipFile zip = new ZipFile(mInput);
    entries = zip.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      if (entry.isDirectory()) {
        // Not all zip files actually include separate directory entries. We'll just ignore them
        // and create them as necessary for each actual entry.
        continue;
      }
      File destination = new File(mOutput, entry.getName());
      if (!destination.getParentFile().exists()) {
        destination.getParentFile().mkdirs();
      }
      IoUtils.copy(zip.getInputStream(entry), new FileOutputStream(destination));
    }
    zip.close();
  }
}
