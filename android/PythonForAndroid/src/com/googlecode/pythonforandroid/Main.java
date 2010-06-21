package com.googlecode.pythonforandroid;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.ase.AseLog;
import com.google.ase.Constants;
import com.google.ase.activity.InterpreterUninstaller;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;


public class Main extends Activity {
  private SharedPreferences mPreferences;
  private InterpreterDescriptor mDescriptor;
  private Button mButton;
  
  static final String ID = Main.class.getPackage().getName();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);    
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDescriptor = new PythonDescriptor();
    Intent intent = getIntent();
    setContentView(R.layout.main);
    mButton = (Button) findViewById(R.id.button);
    if (checkInstalled()) {
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

  private void sendBroadcast(boolean isInterpreterInstalled) {
    Intent intent = new Intent();
    intent.setData(Uri.parse("package:" + ID));
    if (isInterpreterInstalled) {
      intent.setAction(Constants.ACTION_INTERPRETER_ADDED);
    } else {
      intent.setAction(Constants.ACTION_INTERPRETER_REMOVED);
    }
    this.sendBroadcast(intent);
  }

  private void install() {
    Intent intent = new Intent(this, PythonInstaller.class);
    intent.putExtra(Constants.EXTRA_INTERPRETER_DESCRIPTION, InterpreterUtils.bundle(mDescriptor));
    startActivityForResult(intent, 0);
  }

  private void uninstall() {
    Intent intent = new Intent(this, InterpreterUninstaller.class);
    intent.putExtra(Constants.EXTRA_INTERPRETER_DESCRIPTION, InterpreterUtils.bundle(mDescriptor));
    startActivityForResult(intent, 1);
  }
  
  private void updatePreferences(boolean isInstalled){
    SharedPreferences.Editor editor = mPreferences.edit();
    editor.putBoolean(ID, isInstalled);
    editor.commit();
    sendBroadcast(isInstalled);
  }
  
  protected boolean checkInstalled() {
    boolean isInstalled = mPreferences.getBoolean(ID, false);
    sendBroadcast(isInstalled);
    return isInstalled;
  }
    
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == 0) {
      // Interpreter installed.
      if (resultCode != RESULT_OK) {
        AseLog.v(this, "Installation failed.");
      } else {
        updatePreferences(true);
        prepareUninstallButton();
      }
    } else {
      // Interpreter uninstalled.
      if (resultCode != RESULT_OK) {
        AseLog.v(this, "Uninstallation failed.");
      } else {
        updatePreferences(false);
        prepareInstallButton();
      }
    }
  }
}