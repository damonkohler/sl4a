package com.googlecode.pythonforandroid;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.activity.InterpreterInstaller;
import com.google.ase.activity.InterpreterUninstaller;
import com.google.ase.interpreter.python.PythonInterpreter;

public class Main extends Activity {
  private PythonInterpreter mInterpreter;
  private Button mButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mInterpreter = new PythonInterpreter();
    Intent intent = getIntent();
    if (intent.getAction().equals(Constants.ACTION_DISCOVER_INTERPRETERS)) {
      if (mInterpreter.isInstalled()) {
        setResult(RESULT_OK, buildResultIntent(mInterpreter));
      } else {
        setResult(RESULT_CANCELED);
      }
      finish();
      return;
    }
    setContentView(R.layout.main);
    mButton = (Button) findViewById(R.id.button);
    if (mInterpreter.isInstalled()) {
      prepareUninstallButton();
    } else {
      prepareInstallButton();
    }
  }

  private void prepareInstallButton() {
    mButton.setText("Install");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        install();
      }
    });
  }

  private void prepareUninstallButton() {
    mButton.setText("Uninstall");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        uninstall();
      }
    });
  }

  private void install() {
    Intent intent = new Intent(this, InterpreterInstaller.class);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, mInterpreter.getName());
    startActivityForResult(intent, 0);
  }

  private void uninstall() {
    Intent intent = new Intent(this, InterpreterUninstaller.class);
    intent.putExtra(Constants.EXTRA_INTERPRETER_NAME, mInterpreter.getName());
    startActivityForResult(intent, 1);
  }

  private Intent buildResultIntent(PythonInterpreter interpreter) {
    Intent result = new Intent();
    Bundle description = buildDescriptionBundle(interpreter);
    result.putExtra("description", description);
    Bundle environment = new Bundle();
    buildEnvironmentBundle(environment);
    result.putExtra("environment", environment);
    return result;
  }

  private File getPythonHome() {
    return new File(getFilesDir(), "python");
  }

  private File getPythonExtras() {
    return new File(Environment.getExternalStorageDirectory(), "ase/extras/python");
  }

  private File getScriptsRoot() {
    return new File(Environment.getExternalStorageDirectory(), "ase/scripts");
  }

  private File getPythonTemp() {
    return new File(Environment.getExternalStorageDirectory(), "ase/extras/python/tmp");
  }

  private void buildEnvironmentBundle(Bundle environment) {
    environment.putString("PYTHONHOME", getPythonHome().getAbsolutePath());
    environment.putString("PYTHONPATH", getPythonExtras().getAbsolutePath() + ":"
        + getScriptsRoot());
    File tmp = getPythonTemp();
    if (!tmp.isDirectory()) {
      tmp.mkdir();
    }
    environment.putString("TEMP", tmp.getAbsolutePath());
  }

  private Bundle buildDescriptionBundle(PythonInterpreter interpreter) {
    Bundle description = new Bundle();
    description.putString("name", interpreter.getName());
    description.putString("niceName", interpreter.getNiceName());
    description.putString("extension", interpreter.getExtension());
    description.putString("binaryAbsolutePath", interpreter.getBinary().getAbsolutePath());
    description.putBoolean("hasInterpreterArchive", interpreter.hasInterpreterArchive());
    description
        .putBoolean("hasInterpreterExtrasArchive", interpreter.hasInterpreterExtrasArchive());
    description.putBoolean("hasScriptsArchive", interpreter.hasScriptsArchive());
    return description;
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      // Interpreter installed.
      if (resultCode != RESULT_OK) {
        AseLog.v(this, "Installation failed.");
      } else {
        prepareUninstallButton();
      }
    } else {
      // Interpreter uninstalled.
      if (resultCode != RESULT_OK) {
        AseLog.v(this, "Uninstallation failed.");
      } else {
        prepareInstallButton();
      }
    }
  }
}