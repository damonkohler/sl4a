/*
 * Copyright (C) 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.googlecode.android_scripting.interpreter;

import java.io.File;
import java.util.Map;

import com.googlecode.android_scripting.exception.Sl4aException;
import com.googlecode.android_scripting.language.Language;
import com.googlecode.android_scripting.language.SupportedLanguages;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

/**
 * Combines all the execution-related specs of a particular interpreter installed in the system.
 * This class is instantiated through a map received from a concrete InterpreterProfider.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class Interpreter implements InterpreterStrings {

  private static String[] mRequiredKeys =
      { NAME, NICE_NAME, EXTENSION, BINARY, PATH, EXECUTE, EXECUTE_PARAMS };

  private String mExtension;
  private String mName;
  private String mNiceName;
  private String mPath;
  private String mBinary;
  private String mEmptyParameters;
  private String mExecuteParameters;
  private String mExecute;
  private String[] mArguments;
  private Map<String, String> mEnvironmentVariables;

  private Language mLanguage;

  public static Interpreter buildFromMaps(Map<String, String> data, Map<String, String> variables,
      Map<String, String> args) throws Sl4aException {
    Interpreter interpreter = new Interpreter();
    for (String key : mRequiredKeys) {
      if (data.get(key) == null) {
        throw new Sl4aException("Cannot create interpreter. Required parameter not specified: "
            + key);
      }
    }

    String extension = data.get(EXTENSION);

    interpreter.setName(data.get(NAME));
    interpreter.setNiceName(data.get(NICE_NAME));
    interpreter.setExtension(extension);
    interpreter.setBinary(data.get(BINARY));
    interpreter.setPath(new File(data.get(PATH)).getAbsolutePath() + "/");
    interpreter.setEmptyParameters(data.get(EMPTY_PARAMS));
    interpreter.setExecuteParameters(data.get(EXECUTE_PARAMS));
    interpreter.setExecute(data.get(EXECUTE));
    interpreter.setLanguage(SupportedLanguages.getLanguageByExtension(extension));
    interpreter.setEnvironmentVariables(variables);

    if (args != null) {
      String[] arguments = new String[args.size()];
      int i = 0;
      for (String key : args.keySet()) {
        arguments[i++] = args.get(key);
      }
      interpreter.setArguments(arguments);
    }

    return interpreter;
  }

  private void setArguments(String[] arguments) {
    mArguments = arguments;
  }

  private void setEnvironmentVariables(Map<String, String> environmentVariables) {
    mEnvironmentVariables = environmentVariables;
  }

  protected void setExecute(String execute) {
    mExecute = execute;
  }

  private void setExecuteParameters(String exeucteParameters) {
    mExecuteParameters = exeucteParameters;
  }

  public void setEmptyParameters(String emptyParameters) {
    mEmptyParameters = emptyParameters;
  }

  public void setBinary(String binary) {
    mBinary = binary;
  }

  public String getBinary() {
    return mBinary;
  }

  public void setPath(String path) {
    mPath = path;
  }

  public String getPath() {
    return mPath;
  }

  public void setExtension(String extension) {
    mExtension = extension;
  }

  public String getExtension() {
    return mExtension;
  }

  public void setName(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }

  public void setNiceName(String niceName) {
    mNiceName = niceName;
  }

  public String getNiceName() {
    return mNiceName;
  }

  public String getContentTemplate() {
    return mLanguage.getContentTemplate();
  }

  public void setLanguage(Language language) {
    mLanguage = language;
  }

  public Language getLanguage() {
    return mLanguage;
  }

  public String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return mLanguage.getRpcText(content, rpc, values);
  }

  public boolean isInstalled() {
    return mPath != null && new File(mPath, mBinary).exists();
  }

  public boolean isUninstallable() {
    return true;
  }

  public InterpreterProcess buildProcess(String launchScript, String host, int port,
      String handshake) {
    return new ProcessWrapper(launchScript, host, port, handshake);
  }

  private class ProcessWrapper extends InterpreterProcess {

    private String mLaunchScript;

    public ProcessWrapper(String launchScript, String host, int port, String handshake) {
      super(host, port, handshake);
      mLaunchScript = launchScript;
    }

    @Override
    protected void buildEnvironment() {
      mEnvironment.putAll(mEnvironmentVariables);
    }

    @Override
    protected String getInterpreterCommand() {
      return mExecute;
    }

    @Override
    protected String[] getInterpreterArguments() {
      String action = null;

      if (mLaunchScript == null) {
        action = mEmptyParameters;
      } else {
        action = String.format(mExecuteParameters, mLaunchScript);
      }

      if (mArguments == null) {
        if (action == null) {
          return null;
        }
        return new String[] { action };
      }

      String[] args = new String[mArguments.length + ((action != null) ? 1 : 0)];
      System.arraycopy(mArguments, 0, args, 0, mArguments.length);
      if (action != null) {
        args[mArguments.length] = action;
      }
      return args;
    }
  }

}