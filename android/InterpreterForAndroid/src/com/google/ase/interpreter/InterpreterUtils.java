package com.google.ase.interpreter;

import java.io.File;

import android.content.Context;

public class InterpreterUtils {

  private InterpreterUtils() {
    // Utility class
  }

  public static File getInterpreterRoot(Context context) {
    return context.getFilesDir().getParentFile();
  }

  public static File getInterpreterRoot(Context context, String interpreterName) {
    return new File(getInterpreterRoot(context), interpreterName);
  }

  public static boolean isInstalled(Context context, String name) {
    File interpreterDirectory = getInterpreterRoot(context, name);
    File interpreterExtrasDirectory = new File(InterpreterConstants.INTERPRETER_EXTRAS_ROOT, name);
    return interpreterDirectory.exists() || interpreterExtrasDirectory.exists();
  }

  public static boolean isInstalled(Context context, InterpreterDescriptor descriptor) {
    return isInstalled(context, descriptor.getName());
  }
}
