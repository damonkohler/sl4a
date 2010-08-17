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

import com.googlecode.android_scripting.rpc.ParameterDescriptor;

/**
 * Represents the BeanShell programming language.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class BeanShellLanguage extends Language {

  @Override
  protected String getImportStatement() {
    // FIXME(igor.v.karp): this is interpreter specific
    return "source(\"/sdcard/com.googlecode.bshforandroid/extras/bsh/android.bsh\");\n";
  }

  @Override
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    return rpcReceiver + " = Android();\n";
  }

  @Override
  protected String getMethodCallText(String receiver, String method,
      ParameterDescriptor[] parameters) {
    StringBuilder result =
        new StringBuilder().append(getApplyReceiverText(receiver)).append(getApplyOperatorText())
            .append(method);
    if (parameters.length > 0) {
      result.append(getLeftParametersText());
    } else {
      result.append(getQuote());
    }
    String separator = "";
    for (ParameterDescriptor parameter : parameters) {
      result.append(separator).append(getValueText(parameter));
      separator = getParameterSeparator();
    }
    result.append(getRightParametersText());

    return result.toString();
  }

  @Override
  protected String getApplyOperatorText() {
    return ".call(\"";
  }

  @Override
  protected String getLeftParametersText() {
    return "\", ";
  }

  @Override
  protected String getRightParametersText() {
    return ")";
  }
}
