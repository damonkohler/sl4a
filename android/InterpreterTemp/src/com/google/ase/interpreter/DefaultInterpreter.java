package com.google.ase.interpreter;

import android.content.Context;

import com.google.ase.language.LanguageDescriptor;
import com.google.ase.rpc.MethodDescriptor;

import java.io.File;

public abstract class DefaultInterpreter implements InterpreterExecutionDescriptor {

  private final LanguageDescriptor mLanguage;

  public DefaultInterpreter(LanguageDescriptor language) {
    mLanguage = language;
  }

  public final LanguageDescriptor getLanguage() {
    return mLanguage;
  }

  public final String getContentTemplate() {
    return getLanguage().getContentTemplate();
  }

  public final String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return getLanguage().getRpcText(content, rpc, values);
  }

  public String getPath() {
    return null;
  }

  public boolean isInstalled(Context context) {
    if (getName().equals("sh")) {
      // Shell is installed by the system.
      return true;
    }
    String path = getPath();
    boolean pathCheck = path != null && new File(path).exists();
    return pathCheck || InterpreterUtils.isInstalled(context, getName());
  }

  public boolean isUninstallable() {
    return true;
  }

  public abstract int getVersion();

}
