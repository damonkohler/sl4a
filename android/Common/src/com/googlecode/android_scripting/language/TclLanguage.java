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

package com.googlecode.android_scripting.language;

/**
 * Represents the Tcl programming language.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class TclLanguage extends Language {

  @Override
  protected String getImportStatement() {
    return "package require android\n";
  }

  @Override
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    return "set " + rpcReceiver + " [android new]\n";
  }

  @Override
  protected String getApplyReceiverText(String receiver) {
    return "$" + receiver;
  }

  @Override
  protected String getApplyOperatorText() {
    return " ";
  }

  @Override
  protected String getLeftParametersText() {
    return " ";
  }

  @Override
  protected String getRightParametersText() {
    return "";
  }

  @Override
  protected String getParameterSeparator() {
    return " ";
  }
}
