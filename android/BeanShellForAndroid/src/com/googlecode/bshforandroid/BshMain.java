package com.googlecode.bshforandroid;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;

import com.googlecode.android_scripting.AsyncTaskListener;
import com.googlecode.android_scripting.InterpreterInstaller;
import com.googlecode.android_scripting.InterpreterUninstaller;
import com.googlecode.android_scripting.activity.Main;
import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.interpreter.InterpreterDescriptor;

public class BshMain extends Main {
  private static enum MenuId {
    PREFENCES;
    public int getId() {
      return ordinal() + Menu.FIRST;
    }
  }

  @Override
  protected InterpreterDescriptor getDescriptor() {
    return new BshDescriptor();
  }

  @Override
  protected InterpreterInstaller getInterpreterInstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new BshInstaller(descriptor, context, listener);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    menu.add(Menu.NONE, MenuId.PREFENCES.getId(), Menu.NONE, "Preferences").setIcon(
        android.R.drawable.ic_menu_preferences);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (MenuId.PREFENCES.getId() == id) {
      startActivity(new Intent(this, BshPreferences.class));
    }
    return true;
  }

  @Override
  protected InterpreterUninstaller getInterpreterUninstaller(InterpreterDescriptor descriptor,
      Context context, AsyncTaskListener<Boolean> listener) throws Sl4aException {
    return new BshUninstaller(descriptor, context, listener);
  }

}