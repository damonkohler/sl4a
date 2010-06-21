package com.google.ase.interpreter;

import android.content.Context;
import android.os.Bundle;

import com.google.ase.Constants;

import java.io.File;

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
    File interpreterExtrasDirectory = new File(Constants.INTERPRETER_EXTRAS_ROOT, name);
    return interpreterDirectory.exists() || interpreterExtrasDirectory.exists();
  }

  public static boolean isInstalled(Context context, InterpreterDescriptor descriptor) {
    File interpreterDirectory = getInterpreterRoot(context, descriptor.getName());
    File interpreterExtrasDirectory =
        new File(Constants.INTERPRETER_EXTRAS_ROOT, descriptor.getName());
    return interpreterDirectory.exists() || interpreterExtrasDirectory.exists();
  }

  public static Bundle bundle(InterpreterDescriptor descriptor) {
    Bundle bundle = new Bundle();
    bundle.putString(InterpreterStrings.NAME, descriptor.getName());
    bundle.putString(InterpreterStrings.NICE_NAME, descriptor.getNiceName());
    bundle.putString(InterpreterStrings.EXTENSION, descriptor.getExtension());
    bundle.putInt(InterpreterStrings.VERSION, descriptor.getVersion());
    bundle.putString(InterpreterStrings.BIN, descriptor.getBinary());

    if (descriptor.hasInterpreterArchive()) {
      bundle.putBoolean(InterpreterStrings.HAS_INTERPRETER_ARCHIVE, descriptor
          .hasInterpreterArchive());
      bundle.putString(InterpreterStrings.INTERPRETER_ARCHIVE_NAME, descriptor
          .getInterpreterArchiveName());
      bundle.putString(InterpreterStrings.INTERPRETER_ARCHIVE_URL, descriptor
          .getInterpreterArchiveUrl());
    }

    if (descriptor.hasExtrasArchive()) {
      bundle.putBoolean(InterpreterStrings.HAS_EXTRAS_ARCHIVE, descriptor.hasExtrasArchive());
      bundle.putString(InterpreterStrings.EXTRAS_ARCHIVE_NAME, descriptor.getExtrasArchiveName());
      bundle.putString(InterpreterStrings.EXTRAS_ARCHIVE_URL, descriptor.getExtrasArchiveUrl());
    }

    if (descriptor.hasScriptsArchive()) {
      bundle.putBoolean(InterpreterStrings.HAS_SCRIPTS_ARCHIVE, descriptor.hasScriptsArchive());
      bundle.putString(InterpreterStrings.SCRIPTS_ARCHIVE_NAME, descriptor.getScriptsArchiveName());
      bundle.putString(InterpreterStrings.SCRIPTS_ARCHIVE_URL, descriptor.getScriptsArchiveUrl());
    }

    return bundle;
  }

  public static InterpreterDescriptor unbundle(final Bundle bundle) {

    return new InterpreterDescriptor() {
      private final String mmName = bundle.getString(InterpreterStrings.NAME);
      private final String mmNiceName = bundle.getString(InterpreterStrings.NICE_NAME);
      private final String mmExtension = bundle.getString(InterpreterStrings.EXTENSION);
      private final String mmBin = bundle.getString(InterpreterStrings.BIN);
      private final int mmVersion = bundle.getInt(InterpreterStrings.VERSION);

      private final boolean mmHasInterpreterArch =
          bundle.getBoolean(InterpreterStrings.HAS_INTERPRETER_ARCHIVE);
      private final String mmInterpreterArchName =
          bundle.getString(InterpreterStrings.INTERPRETER_ARCHIVE_NAME);
      private final String mmInterpreterArchUrl =
          bundle.getString(InterpreterStrings.INTERPRETER_ARCHIVE_URL);

      private final boolean mmHasExtrasArch =
          bundle.getBoolean(InterpreterStrings.HAS_EXTRAS_ARCHIVE);
      private final String mmExtrasArchName =
          bundle.getString(InterpreterStrings.EXTRAS_ARCHIVE_NAME);
      private final String mmExtrasArchUrl =
          bundle.getString(InterpreterStrings.EXTRAS_ARCHIVE_URL);

      private final boolean mmHasScriptsArch =
          bundle.getBoolean(InterpreterStrings.HAS_SCRIPTS_ARCHIVE);
      private final String mmScriptsArchName =
          bundle.getString(InterpreterStrings.SCRIPTS_ARCHIVE_NAME);
      private final String mmScriptsArchUrl =
          bundle.getString(InterpreterStrings.SCRIPTS_ARCHIVE_URL);

      @Override
      public String getName() {
        return mmName;
      }

      @Override
      public String getNiceName() {
        return mmNiceName;
      }

      @Override
      public String getExtension() {
        return mmExtension;
      }

      @Override
      public String getBinary() {
        return mmBin;
      }

      @Override
      public int getVersion() {
        return mmVersion;
      }

      @Override
      public String getExtrasArchiveName() {
        return mmExtrasArchName;
      }

      @Override
      public String getExtrasArchiveUrl() {
        return mmExtrasArchUrl;
      }

      @Override
      public String getInterpreterArchiveName() {
        return mmInterpreterArchName;
      }

      @Override
      public String getInterpreterArchiveUrl() {
        return mmInterpreterArchUrl;
      }

      @Override
      public String getScriptsArchiveName() {
        return mmScriptsArchName;
      }

      @Override
      public String getScriptsArchiveUrl() {
        return mmScriptsArchUrl;
      }

      @Override
      public boolean hasExtrasArchive() {
        return mmHasExtrasArch;
      }

      @Override
      public boolean hasInterpreterArchive() {
        return mmHasInterpreterArch;
      }

      @Override
      public boolean hasScriptsArchive() {
        return mmHasScriptsArch;
      }

      @Override
      public String getEmptyCommand() {
        return null;
      }

      @Override
      public String getExecuteParams() {
        return null;
      }

    };
  }

}
