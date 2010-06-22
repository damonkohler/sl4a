package com.google.ase.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.ase.AseLog;
import com.google.ase.interpreter.InterpreterConstants;
import com.google.ase.interpreter.InterpreterDescriptor;
import com.google.ase.interpreter.InterpreterUtils;



public abstract class Main extends Activity {
  protected final static float MARGIN_DIP = 3.0f;

  protected SharedPreferences mPreferences;
  protected InterpreterDescriptor mDescriptor;
  protected Button mButton;
  
  protected final String ID = getID();

  protected String getID() {
    return getClass().getPackage().getName();
  }

  protected abstract InterpreterDescriptor getDescriptor();

  protected abstract Class<? extends InterpreterInstaller> getInstallerClass();

  protected abstract Class<? extends InterpreterUninstaller> getUnstallerClass();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);    
    mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    mDescriptor = getDescriptor();
    setUI();
    if (checkInstalled()) {
      prepareUninstallButton();
    } else {
      prepareInstallButton();
    }
  }

  protected void setUI() {
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);
    layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
    
    mButton = new Button(this);
    MarginLayoutParams marginParams = new MarginLayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    final float scale = getResources().getDisplayMetrics().density;
    int marginPixels = (int) (MARGIN_DIP * scale + 0.5f);
    marginParams.setMargins(marginPixels, marginPixels, marginPixels, marginPixels);
    mButton.setLayoutParams(marginParams);
    layout.addView(mButton);
    
    setContentView(layout);
  }

  protected void prepareInstallButton() {
    mButton.setText("Install");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        install();
      }
    });
  }

  protected void prepareUninstallButton() {
    mButton.setText("Uninstall");
    mButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        uninstall();
      }
    });
  }

  protected void sendBroadcast(boolean isInterpreterInstalled) {
    Intent intent = new Intent();
    intent.setData(Uri.parse("package:" + ID));
    if (isInterpreterInstalled) {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_ADDED);
    } else {
      intent.setAction(InterpreterConstants.ACTION_INTERPRETER_REMOVED);
    }
    this.sendBroadcast(intent);
  }

  protected void install() {
    Intent intent = new Intent(this, getInstallerClass());
    intent.putExtra(InterpreterConstants.EXTRA_INTERPRETER_DESCRIPTION, InterpreterUtils
        .bundle(mDescriptor));
    startActivityForResult(intent, 0);
  }

  protected void uninstall() {
    Intent intent = new Intent(this, getUnstallerClass());
    intent.putExtra(InterpreterConstants.EXTRA_INTERPRETER_DESCRIPTION, InterpreterUtils
        .bundle(mDescriptor));
    startActivityForResult(intent, 1);
  }
  
  protected void updatePreferences(boolean isInstalled) {
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