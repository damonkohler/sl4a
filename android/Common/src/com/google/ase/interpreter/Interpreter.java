package com.google.ase.interpreter;

import android.content.Context;

import com.google.ase.exception.AseException;
import com.google.ase.language.Language;
import com.google.ase.language.SupportedLanguages;
import com.google.ase.rpc.MethodDescriptor;

import java.io.File;
import java.util.Map;

public class Interpreter implements InterpreterExecutionDescriptor, InterpreterStrings {

  private static String[] mapKeys = { NAME, NICE_NAME, EXTENSION, BIN, PATH, EMPTY, EXECUTE };

  private final String mExtension;
  private final String mName;
  private final String mNiceName;
  private final String mPath;
  private final String mBin;
  private final String mEmptyCommand;
  private final String mExecuteCommand;
  private final Map<String, String> mEnvvars;

  private final Language mLanguage;

  public Interpreter(Map<String, String> data, Map<String, String> envvars) throws AseException {

    for (String key : mapKeys) {
      if (data.get(key).equals(null)) {
        throw new AseException("Cannot create interpreter. Required parameter not specified: "
            + key);
      }
    }

    mName = data.get(NAME);
    mNiceName = data.get(NICE_NAME);
    mExtension = data.get(EXTENSION);
    mBin = data.get(BIN);
    mPath = data.get(PATH);
    mEmptyCommand = data.get(EMPTY);
    mExecuteCommand = data.get(EXECUTE);

    mLanguage = SupportedLanguages.getLanguageByExtention(mExtension);

    mEnvvars = envvars;
  }

  public InterpreterProcess buildProcess(String launchScript, int port) {
    return new ProcessWrapper(launchScript, port);
  }

  public String getBinary() {
    return mBin;
  }

  public String getPath() {
    return mPath;
  }

  public String getExtension() {
    return mExtension;
  }

  public String getName() {
    return mName;
  }

  public String getNiceName() {
    return mNiceName;
  }

  public String getContentTemplate() {
    return mLanguage.getContentTemplate();
  }

  public Language getLanguage() {
    return mLanguage;
  }

  public String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return mLanguage.getRpcText(content, rpc, values);
  }

  public boolean isInstalled(Context context) {
    boolean pathCheck = mPath != null && new File(mPath).exists();
    return pathCheck || InterpreterUtils.isInstalled(context, getName());
  }

  public boolean isUninstallable() {
    return true;
  }

  private class ProcessWrapper extends InterpreterProcess {

    public ProcessWrapper(String launchScript, int port) {
      super(launchScript, port);
    }

    @Override
    protected void buildEnvironment() {
      if (mEnvvars != null) {
        mEnvironment.putAll(mEnvvars);
      }
    }

    @Override
    protected String getInterpreterCommand() {
      String cmd = getBinary() + "%s";
      return String.format(cmd, (mLaunchScript == null) ? mEmptyCommand : mExecuteCommand);
    }
  }

}