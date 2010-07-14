/*
 * Copyright (C) 2010 Irontec SL
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
 * Represents the PHP programming language.
 * 
 * @author ivan@irontec.com (Ivan Mosquera Paulo)
 */
public class PhpLanguage extends Language {

  @Override
  protected String getImportStatement() {
    return "<?php\n\nrequire_once(\"Android.php\");";

  }

  @Override
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    return rpcReceiver + " = new Android();\n";
  }

  @Override
  protected String getDefaultRpcReceiver() {
    return "$droid";
  }

  @Override
  protected String getApplyOperatorText() {
    return "->";
  }

  @Override
  protected String getQuote() {
    return "'";
  }

}