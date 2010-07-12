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
public class Interpreter implements InterpreterAgent, InterpreterStrings {

  private static String[] mapKeys =
      { NAME, NICE_NAME, EXTENSION, BIN, PATH, EXECUTE, EMPTY_PARAMS, EXECUTE_PARAMS, ARGS };

  private final String mExtension;
  private final String mName;
  private final String mNiceName;
  private final String mPath;
  private final String mBinary;
  private final String mEmptyParams;
  private final String mExecuteParams;
  private final String mExecute;
  private final String[] mArguments;
  private final Map<String, String> mEnvironmentVariables;

  private final Language mLanguage;

  public Interpreter(Map<String, String> data, Map<String, String> variables,
      Map<String, String> args) throws AseException {

    for (String key : mapKeys) {
      if (data.get(key) == null && !(key.equals(EMPTY_PARAMS) || key.equals(ARGS))) {
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
    if (args == null) {
      mArguments = null;
    } else {
      mArguments = new String[args.size()];
      int i = 0;
      for (String key : args.keySet()) {
        mArguments[i++] = args.get(key);
      }
    }

    mLanguage = SupportedLanguages.getLanguageByExtension(mExtension);

    mEnvironmentVariables = new HashMap<String, String>();
    if (variables != null) {
      mEnvironmentVariables.putAll(variables);
    }
  }

  public InterpreterProcess buildProcess(String launchScript, String host, int port,
      String handshake) {
    return new ProcessWrapper(launchScript, host, port, handshake);
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

    public ProcessWrapper(String launchScript, String host, int port, String handshake) {
      super(launchScript, host, port, handshake);
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
        action = mEmptyParams;
      } else {
        action = String.format(mExecuteParams, mLaunchScript);
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