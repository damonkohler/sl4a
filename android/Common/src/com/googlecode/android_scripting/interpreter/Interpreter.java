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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
  private final List<String> mArguments;
  private final Map<String, String> mEnvironment;
  private Language mLanguage;

  public Interpreter() {
    mArguments = new ArrayList<String>();
    mEnvironment = new HashMap<String, String>();
  }

  public static Interpreter buildFromMaps(Map<String, String> data, Map<String, String> variables,
      Map<String, String> arguments) throws Sl4aException {
    Interpreter interpreter = new Interpreter();
    for (String key : mRequiredKeys) {
      if (data.get(key) == null) {
        throw new Sl4aException("Cannot create interpreter. Required parameter not specified: "
            + key);
      }
    }

    String extension = data.get(EXTENSION);
    String name = data.get(NAME);
    String niceName = data.get(NICE_NAME);
    String binary = data.get(BINARY);
    String emptyParameters = data.get(EMPTY_PARAMS);
    String exeucteParameters = data.get(EXECUTE_PARAMS);
    String execute = data.get(EXECUTE);

    interpreter.setName(name);
    interpreter.setNiceName(niceName);
    interpreter.setExtension(extension);
    interpreter.setBinary(binary);
    interpreter.setPath(new File(data.get(PATH)).getAbsolutePath() + "/");
    interpreter.setEmptyParameters(emptyParameters);
    interpreter.setExecuteParameters(exeucteParameters);
    interpreter.setExecute(execute);
    interpreter.setLanguage(SupportedLanguages.getLanguageByExtension(extension));
    interpreter.addEvironmentVariables(variables);
    interpreter.addArguments(arguments.values());
    return interpreter;
  }

  private void addArguments(Collection<String> arguments) {
    mArguments.addAll(arguments);
  }

  private void addEvironmentVariables(Map<String, String> environmentVariables) {
    mEnvironment.putAll(environmentVariables);
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

  public String getEmptyParameters() {
    return mEmptyParameters;
  }
}