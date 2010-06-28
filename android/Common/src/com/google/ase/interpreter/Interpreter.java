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

package com.google.ase.interpreter;

import android.content.Context;

import com.google.ase.exception.AseException;
import com.google.ase.language.Language;
import com.google.ase.language.SupportedLanguages;
import com.google.ase.rpc.MethodDescriptor;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Combines all the execution-related specs of a particular interpreter installed in the system.
 * This class is instantiated through a map received from a concrete InterpreterProfider.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class Interpreter implements InterpreterExecutionDescriptor, InterpreterStrings {

  private static String[] mapKeys =
      { NAME, NICE_NAME, EXTENSION, BIN, PATH, EXECUTE, EMPTY_PARAMS, EXECUTE_PARAMS };

  private final String mExtension;
  private final String mName;
  private final String mNiceName;
  private final String mPath;
  private final String mBinary;
  private final String mEmptyParams;
  private final String mExecuteParams;
  private final String mExecute;
  private final Map<String, String> mEnvironmentVariables;

  private final Language mLanguage;

  public Interpreter(Map<String, String> data, Map<String, String> variables) throws AseException {

    for (String key : mapKeys) {
      if (data.get(key).equals(null)) {
        throw new AseException("Cannot create interpreter. Required parameter not specified: "
            + key);
      }
    }

    mName = data.get(NAME);
    mNiceName = data.get(NICE_NAME);
    mExtension = data.get(EXTENSION);
    mBinary = data.get(BIN);
    mPath = new File(data.get(PATH)).getAbsolutePath() + "/";
    mEmptyParams = data.get(EMPTY_PARAMS);
    mExecuteParams = data.get(EXECUTE_PARAMS);
    mExecute = data.get(EXECUTE);

    mLanguage = SupportedLanguages.getLanguageByExtention(mExtension);

    mEnvironmentVariables = new HashMap<String, String>(variables);
  }

  public InterpreterProcess buildProcess(String launchScript, int port) {
    return new ProcessWrapper(launchScript, port);
  }

  public String getBinary() {
    return mBinary;
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
    boolean pathCheck = mPath != null && new File(mPath, mBinary).exists();
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
      mEnvironment.putAll(mEnvironmentVariables);
    }

    @Override
    protected String getInterpreterCommand() {
      String action = null;
      if (mLaunchScript == null) {
        action = mEmptyParams;
      } else {
        action = String.format(mExecuteParams, mLaunchScript);
      }
      return String.format(mExecute, mPath, mBinary, action);
    }
  }

}