package com.google.ase.interpreter;

import android.content.Context;

import com.google.ase.Constants;
import com.google.ase.language.Language;
import com.google.ase.language.LanguageWrapper;
import com.google.ase.rpc.MethodDescriptor;

import java.io.File;
import java.util.Map;

public class InterpreterWrapper implements Interpreter, InterpreterStrings {

  private final String mExtension;
  private final String mName;
  private final String mNiceName;
  private final String mPath;
  private final String mBin;
  private final String mEmptyCommand;
  private final String mExecuteCommand;
  private final Map<String, String> mEnvvars;

  private final Language mLanguage;

  private InterpreterWrapper(Map<String, Map<String, String>> data) {
    mLanguage = LanguageWrapper.extractFromMap(data.get(Constants.PROVIDER_LANG));

    Map<String, String> interpreterMap = data.get(Constants.PROVIDER_BASE);
    mEnvvars = data.get(Constants.PROVIDER_ENV);
    mName = interpreterMap.get(NAME);
    mNiceName = interpreterMap.get(NICE_NAME);
    mExtension = interpreterMap.get(EXTENSION);
    mBin = interpreterMap.get(BIN);
    mPath = interpreterMap.get(PATH);
    mEmptyCommand = interpreterMap.get(EMPTY);
    mExecuteCommand = interpreterMap.get(EXECUTE);
  }

  public static InterpreterWrapper extractFromMap(Map<String, Map<String, String>> data) {
    if (data == null || data.get(Constants.PROVIDER_BASE) == null) {
      return null;
    }
    return new InterpreterWrapper(data);
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
