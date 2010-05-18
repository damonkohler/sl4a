package com.googlecode.pythonforandroid;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.google.ase.Constants;
import com.google.ase.interpreter.python.PythonInterpreter;
import com.google.ase.interpreter.python.PythonInterpreterProcess;

public class Main extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    if (intent.getAction().equals(Constants.ACTION_DISCOVER_INTERPRETERS)) {
      PythonInterpreter interpreter = new PythonInterpreter();
      setResult(RESULT_OK, buildResultIntent(interpreter));
      finish();
      return;
    }
    setContentView(R.layout.main);
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

  private void buildEnvironmentBundle(Bundle environment) {
    environment.putString("PYTHONHOME", PythonInterpreterProcess.PYTHON_HOME);
    environment.putString("PYTHONPATH", PythonInterpreterProcess.PYTHON_EXTRAS + ":"
        + Constants.SCRIPTS_ROOT);
    File tmp = new File(PythonInterpreterProcess.PYTHON_EXTRAS + "tmp/");
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
    description.putBoolean("hasInterpreterExtrasArchive", interpreter
        .hasInterpreterExtrasArchive());
    description.putBoolean("hasScriptsArchive", interpreter.hasScriptsArchive());
    return description;
  }
}