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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.FileUtils;
import com.google.ase.interpreter.Interpreter;
import com.google.ase.interpreter.InterpreterConfiguration;

/**
 * Activity for installing interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class InterpreterInstaller extends Activity {
  private String mName;
  private Interpreter mInterpreter;

  private static enum RequestCode {
    DOWNLOAD_INTERPRETER, DOWNLOAD_INTERPRETER_EXTRAS, DOWNLOAD_SCRIPTS, EXTRACT_INTERPRETER,
    EXTRACT_INTERPRETER_EXTRAS, EXTRACT_SCRIPTS
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mName = getIntent().getStringExtra(Constants.EXTRA_INTERPRETER_NAME);
    if (mName == null) {
      AseLog.e("Interpreter not specified.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    mInterpreter = InterpreterConfiguration.getInterpreterByName(mName);
    if (mInterpreter == null) {
      AseLog.e("No matching interpreter found for name: " + mName);
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if (mInterpreter.isInstalled(this)) {
      AseLog.e("Interpreter already installed.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if (mInterpreter.hasInterpreterArchive()) {
      downloadInterpreter();
    } else {
      downloadInterpreterExtras();
    }
  }

  private void downloadInterpreter() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mInterpreter.getInterpreterArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_INTERPRETER.ordinal());
  }

  private void downloadInterpreterExtras() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mInterpreter.getInterpreterExtrasArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_INTERPRETER_EXTRAS.ordinal());
  }

  private void downloadScripts() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mInterpreter.getScriptsArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_SCRIPTS.ordinal());
  }

  private void extractInterpreter() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mInterpreter
        .getInterpreterArchiveName()).getAbsolutePath());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, InterpreterConfiguration.getInterpreterRoot(this)
        .getAbsolutePath());
    startActivityForResult(intent, RequestCode.EXTRACT_INTERPRETER.ordinal());
  }

  private void extractInterpreterExtras() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mInterpreter
        .getInterpreterExtrasArchiveName()).getAbsolutePath());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.INTERPRETER_EXTRAS_ROOT);
    startActivityForResult(intent, RequestCode.EXTRACT_INTERPRETER_EXTRAS.ordinal());
  }

  private void extractScripts() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mInterpreter
        .getScriptsArchiveName()).getAbsolutePath());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.SCRIPTS_ROOT);
    startActivityForResult(intent, RequestCode.EXTRACT_SCRIPTS.ordinal());
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    RequestCode request = RequestCode.values()[requestCode];

    if (resultCode != RESULT_OK) {
      // This switch handles failure cases. If a step is expected to succeed, it will cause the
      // installation to abort.
      switch (request) {
      case DOWNLOAD_INTERPRETER:
        if (mInterpreter.hasInterpreterArchive()) {
          AseLog.e("Downloading interpreter failed.");
          abort();
          return;
        }
        break;
      case DOWNLOAD_INTERPRETER_EXTRAS:
        if (mInterpreter.hasInterpreterExtrasArchive()) {
          AseLog.e("Downloading interpreter extras failed.");
          abort();
          return;
        }
        break;
      case DOWNLOAD_SCRIPTS:
        if (mInterpreter.hasScriptsArchive()) {
          AseLog.e("Downloading scripts failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_INTERPRETER:
        if (mInterpreter.hasInterpreterArchive()) {
          AseLog.e("Extracting interpreter failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_INTERPRETER_EXTRAS:
        if (mInterpreter.hasInterpreterExtrasArchive()) {
          AseLog.e("Extracting interpreter extras failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_SCRIPTS:
        if (mInterpreter.hasScriptsArchive()) {
          AseLog.e("Extracting scripts failed.");
          abort();
          return;
        }
        break;
      default:
        AseLog.e("Unknown installation state.");
        abort();
        return;
      }
    }

    // This switch defines the progression of installation steps.
    switch (request) {
    case DOWNLOAD_INTERPRETER:
      downloadInterpreterExtras();
      break;
    case DOWNLOAD_INTERPRETER_EXTRAS:
      downloadScripts();
      break;
    case DOWNLOAD_SCRIPTS:
      extractInterpreter();
      break;
    case EXTRACT_INTERPRETER:
      if (!chmodIntepreter()) {
        abort();
        return;
      }
      extractInterpreterExtras();
      break;
    case EXTRACT_INTERPRETER_EXTRAS:
      extractScripts();
      break;
    case EXTRACT_SCRIPTS:
      AseLog.v(this, "Installation successful.");
      setResult(RESULT_OK);
      finish();
      return;
    default:
      AseLog.e(this, "Unknown installation state.");
      abort();
      return;
    }
  }

  private boolean chmodIntepreter() {
    int dataChmodErrno;
    boolean interpreterChmodSuccess;
    try {
      dataChmodErrno = FileUtils.chmod(InterpreterConfiguration.getInterpreterRoot(this), 0755);
      interpreterChmodSuccess =
          FileUtils.recursiveChmod(InterpreterConfiguration.getInterpreterRoot(this, mInterpreter
              .getName()), 0755);
    } catch (Exception e) {
      AseLog.e(e);
      return false;
    }
    return dataChmodErrno == 0 && interpreterChmodSuccess;
  }

  private void abort() {
    AseLog.v(this, "Installation failed.");
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }
}
