/*
 * Copyright (C) 2016 Google Inc.
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
 * Represents the Squirrel programming language, by Alberto Demichelis
 * this file adapted by Andy Tai, atai@atai.org
 * based on the Python version by
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class SquirrelLanguage extends Language {

  @Override
  protected String getImportStatement() {
    /* initialization code */
    return "";
  }

  @Override
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    return rpcReceiver + " <- Android();\n";
  }

  @Override
  protected String getQuote() {
    return "\"";
  }

  @Override
  protected String getNull() {
    return "null";
  }

  @Override
  protected String getTrue() {
    return "true";
  }

  @Override
  protected String getFalse() {
    return "false";
  }
}

