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

import com.googlecode.android_scripting.language.Language;
import com.googlecode.android_scripting.language.SupportedLanguages;
import com.googlecode.android_scripting.rpc.MethodDescriptor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combines all the execution-related specs of a particular interpreter installed in the system.
 * This class is instantiated through a map received from a concrete InterpreterProfider.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public class Interpreter implements InterpreterPropertyNames {

  private String mExtension;
  private String mName;
  private String mNiceName;
  private String mInteractiveCommand;
  private String mScriptExecutionCommand;
  private File mBinary;
  private boolean mHasInteractiveMode;
  private final List<String> mArguments;
  private final Map<String, String> mEnvironment;
  private Language mLanguage;

  public Interpreter() {
    mArguments = new ArrayList<String>();
    mEnvironment = new HashMap<String, String>();
  }

  public static Interpreter buildFromMaps(Map<String, String> data,
      Map<String, String> environment_variables, Map<String, String> arguments) {
    String extension = data.get(EXTENSION);
    String name = data.get(NAME);
    String niceName = data.get(NICE_NAME);
    String binary = data.get(BINARY);
    String interactiveCommand = data.get(INTERACTIVE_COMMAND);
    String scriptCommand = data.get(SCRIPT_COMMAND);
    Boolean hasInteractiveMode;
    if (data.containsKey(HAS_INTERACTIVE_MODE)) {
      hasInteractiveMode = Boolean.parseBoolean(data.get(HAS_INTERACTIVE_MODE));
    } else {
      // Default to true so that older interpreter APKs that don't have this value define still
      // work.
      hasInteractiveMode = true;
    }
    Interpreter interpreter = new Interpreter();
    interpreter.setName(name);
    interpreter.setNiceName(niceName);
    interpreter.setExtension(extension);
    interpreter.setBinary(new File(binary));
    interpreter.setInteractiveCommand(interactiveCommand);
    interpreter.setScriptCommand(scriptCommand);
    interpreter.setHasInteractiveMode(hasInteractiveMode);
    interpreter.setLanguage(SupportedLanguages.getLanguageByExtension(extension));
    interpreter.putAllEnvironmentVariables(environment_variables);
    interpreter.addAllArguments(arguments.values());
    return interpreter;
  }

  // TODO(damonkohler): This should take a List<String> since order is important.
  private void addAllArguments(Collection<String> arguments) {
    mArguments.addAll(arguments);
  }

  List<String> getArguments() {
    return mArguments;
  }

  private void putAllEnvironmentVariables(Map<String, String> environmentVariables) {
    mEnvironment.putAll(environmentVariables);
  }

  public Map<String, String> getEnvironmentVariables() {
    return mEnvironment;
  }

  protected void setScriptCommand(String executeParameters) {
    mScriptExecutionCommand = executeParameters;
  }

  public String getScriptCommand() {
    return mScriptExecutionCommand;
  }

  protected void setInteractiveCommand(String interactiveCommand) {
    mInteractiveCommand = interactiveCommand;
  }

  public String getInteractiveCommand() {
    return mInteractiveCommand;
  }

  protected void setBinary(File binary) {
    if (!binary.exists()) {
      throw new RuntimeException("Binary " + binary + " does not exist!");
    }
    mBinary = binary;
  }

  public File getBinary() {
    return mBinary;
  }

  protected void setExtension(String extension) {
    mExtension = extension;
  }

  protected void setHasInteractiveMode(boolean hasInteractiveMode) {
    mHasInteractiveMode = hasInteractiveMode;
  }

  public boolean hasInteractiveMode() {
    return mHasInteractiveMode;
  }

  public String getExtension() {
    return mExtension;
  }

  protected void setName(String name) {
    mName = name;
  }

  public String getName() {
    return mName;
  }

  protected void setNiceName(String niceName) {
    mNiceName = niceName;
  }

  public String getNiceName() {
    return mNiceName;
  }

  public String getContentTemplate() {
    return mLanguage.getContentTemplate();
  }

  protected void setLanguage(Language language) {
    mLanguage = language;
  }

  public Language getLanguage() {
    return mLanguage;
  }

  public String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return mLanguage.getRpcText(content, rpc, values);
  }

  public boolean isInstalled() {
    return mBinary.exists();
  }

  public boolean isUninstallable() {
    return true;
  }
}