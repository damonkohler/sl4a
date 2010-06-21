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

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.FileUtils;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;

import java.io.File;

/**
 * Activity for installing interpreters.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
//TODO() make abstract
public abstract class InterpreterInstaller extends Activity {
  protected InterpreterDescriptor mDescriptor;

  private static enum RequestCode {
    DOWNLOAD_INTERPRETER, DOWNLOAD_INTERPRETER_EXTRAS, DOWNLOAD_SCRIPTS, EXTRACT_INTERPRETER,
    EXTRACT_INTERPRETER_EXTRAS, EXTRACT_SCRIPTS
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    Bundle descriptionBundle = getIntent().getBundleExtra(Constants.EXTRA_INTERPRETER_DESCRIPTION);
    
    if (descriptionBundle == null) {
      AseLog.e("Interpreter description not provided.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    
    mDescriptor = InterpreterUtils.unbundle(descriptionBundle);
    
    if (mDescriptor.getName() == null) {
      AseLog.e("Interpreter not specified.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }

    if (isInstalled()) {
      AseLog.e("Interpreter already installed.");
      setResult(RESULT_CANCELED);
      finish();
      return;
    }
    if (mDescriptor.hasInterpreterArchive()) {
      downloadInterpreter();
    } else {
      downloadInterpreterExtras();
    }
  }

  private void downloadInterpreter() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mDescriptor.getInterpreterArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_INTERPRETER.ordinal());
  }

  private void downloadInterpreterExtras() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mDescriptor.getExtrasArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_INTERPRETER_EXTRAS.ordinal());
  }

  private void downloadScripts() {
    Intent intent = new Intent(this, UrlDownloader.class);
    intent.putExtra(Constants.EXTRA_URL, mDescriptor.getScriptsArchiveUrl());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.DOWNLOAD_ROOT);
    startActivityForResult(intent, RequestCode.DOWNLOAD_SCRIPTS.ordinal());
  }

  private void extractInterpreter() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mDescriptor
        .getInterpreterArchiveName()).getAbsolutePath());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, InterpreterUtils.getInterpreterRoot(this)
        .getAbsolutePath());
    startActivityForResult(intent, RequestCode.EXTRACT_INTERPRETER.ordinal());
  }

  private void extractInterpreterExtras() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mDescriptor
        .getExtrasArchiveName()).getAbsolutePath());
    intent.putExtra(Constants.EXTRA_OUTPUT_PATH, Constants.INTERPRETER_EXTRAS_ROOT);
    startActivityForResult(intent, RequestCode.EXTRACT_INTERPRETER_EXTRAS.ordinal());
  }

  private void extractScripts() {
    Intent intent = new Intent(this, ZipExtractor.class);
    intent.putExtra(Constants.EXTRA_INPUT_PATH, new File(Constants.DOWNLOAD_ROOT, mDescriptor
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
        if (mDescriptor.hasInterpreterArchive()) {
          AseLog.e("Downloading interpreter failed.");
          abort();
          return;
        }
        break;
      case DOWNLOAD_INTERPRETER_EXTRAS:
        if (mDescriptor.hasExtrasArchive()) {
          AseLog.e("Downloading interpreter extras failed.");
          abort();
          return;
        }
        break;
      case DOWNLOAD_SCRIPTS:
        if (mDescriptor.hasScriptsArchive()) {
          AseLog.e("Downloading scripts failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_INTERPRETER:
        if (mDescriptor.hasInterpreterArchive()) {
          AseLog.e("Extracting interpreter failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_INTERPRETER_EXTRAS:
        if (mDescriptor.hasExtrasArchive()) {
          AseLog.e("Extracting interpreter extras failed.");
          abort();
          return;
        }
        break;
      case EXTRACT_SCRIPTS:
        if (mDescriptor.hasScriptsArchive()) {
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
      if(setup()){
        AseLog.v(this, "Installation successful.");
        setResult(RESULT_OK);
        finish();
        return; 
      }
        //$FALL-THROUGH$
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
      dataChmodErrno = FileUtils.chmod(InterpreterUtils.getInterpreterRoot(this), 0755);
      interpreterChmodSuccess =
          FileUtils.recursiveChmod(
              InterpreterUtils.getInterpreterRoot(this, mDescriptor
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
 
  protected abstract boolean setup();

  protected abstract boolean isInstalled();
}
