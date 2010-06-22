package com.google.ase.interpreter;

import android.os.Environment;

public interface InterpreterConstants {

  public static final String EXTRA_INPUT_PATH = "com.google.ase.extra.INPUT_PATH";
  public static final String EXTRA_OUTPUT_PATH = "com.google.ase.extra.OUTPUT_PATH";

  public static final String EXTRA_URL = "com.google.ase.extra.URL";

  public static final String SDCARD_ROOT =
      Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
  public static final String DOWNLOAD_ROOT = SDCARD_ROOT;

  public static final String SDCARD_ASE_ROOT = SDCARD_ROOT + "ase/";

  public static final String ASE_DALVIK_CACHE_ROOT = SDCARD_ASE_ROOT + "dalvik-cache/";

  public static final String INTERPRETER_EXTRAS_ROOT = SDCARD_ASE_ROOT + "extras/";

  public static final String SCRIPTS_ROOT = SDCARD_ASE_ROOT + "scripts/";

  public static final String EXTRA_INTERPRETER_DESCRIPTION =
      "com.google.ase.extra.INTERPRETER_DESCRIPTION";

  // Interpreters discovery mechanism
  public static final String ACTION_DISCOVER_INTERPRETERS = "com.google.ase.DISCOVER_INTERPRETERS";
  // Interpreters broadcasts
  public static final String ACTION_INTERPRETER_ADDED = "com.google.ase.INTERPRETER_ADDED";
  public static final String ACTION_INTERPRETER_REMOVED = "com.google.ase.INTERPRETER_REMOVED";
  // Interpreter content provider
  public static final String PROVIDER_BASE = "com.google.ase.base";
  public static final String PROVIDER_ENV = "com.google.ase.env";

}
