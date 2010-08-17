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

import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.ParameterDescriptor;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the programming language supported by the SL4A.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public abstract class Language {

  private final static Map<Character, String> AUTO_CLOSE_MAP =
      buildAutoCloseMap('[', "[]", '{', "{}", '(', "()", '\'', "''", '"', "\"\"");

  /** Returns the initial template for newly created script. */
  public String getContentTemplate() {
    StringBuilder content = new StringBuilder(getImportStatement());
    if (content.length() != 0) {
      content.append('\n');
    }
    content.append(getRpcReceiverDeclaration(getDefaultRpcReceiver()));
    return content.toString();
  }

  /** Returns the Android package import statement. */
  protected String getImportStatement() {
    return "";
  }

  /** Returns the RPC receiver declaration. */
  protected String getRpcReceiverDeclaration(String rpcReceiver) {
    return "";
  }

  /** Returns the default RPC receiver name. */
  protected String getDefaultRpcReceiver() {
    return "droid";
  }

  /**
   * Returns the string containing opening and closing tokens if the input is an opening token.
   * Returns {@code null} otherwise.
   */
  public String autoClose(char token) {
    return AUTO_CLOSE_MAP.get(token);
  }

  /** Returns the RPC call text with given parameter values. */
  public final String getRpcText(String content, MethodDescriptor rpc, String[] values) {
    return getMethodCallText(getRpcReceiverName(content), rpc.getName(), rpc
        .getParameterValues(values));
  }

  /** Returns the RPC receiver found in the given script. */
  protected String getRpcReceiverName(String content) {
    return getDefaultRpcReceiver();
  }

  /** Returns the method call text in the language. */
  protected String getMethodCallText(String receiver, String method,
      ParameterDescriptor[] parameters) {
    StringBuilder result =
        new StringBuilder().append(getApplyReceiverText(receiver)).append(getApplyOperatorText())
            .append(method).append(getLeftParametersText());
    String separator = "";
    for (ParameterDescriptor parameter : parameters) {
      result.append(separator).append(getValueText(parameter));
      separator = getParameterSeparator();
    }
    result.append(getRightParametersText());

    return result.toString();
  }

  /** Returns the apply receiver text. */
  protected String getApplyReceiverText(String receiver) {
    return receiver;
  }

  /** Returns the apply operator text. */
  protected String getApplyOperatorText() {
    return ".";
  }

  /** Returns the text to the left of the parameters. */
  protected String getLeftParametersText() {
    return "(";
  }

  /** Returns the text to the right of the parameters. */
  protected String getRightParametersText() {
    return ")";
  }

  /** Returns the parameter separator text. */
  protected String getParameterSeparator() {
    return ", ";
  }

  /** Returns the text of the quotation. */
  protected String getQuote() {
    return "\"";
  }

  /** Returns the text of the {@code null} value. */
  protected String getNull() {
    return "null";
  }

  /** Returns the text of the {{@code true} value. */
  protected String getTrue() {
    return "true";
  }

  /** Returns the text of the false value. */
  protected String getFalse() {
    return "false";
  }

  /** Returns the parameter value suitable for code generation. */
  protected String getValueText(ParameterDescriptor parameter) {
    if (parameter.getValue() == null) {
      return getNullValueText();
    } else if (parameter.getType().equals(String.class)) {
      return getStringValueText(parameter.getValue());
    } else if (parameter.getType().equals(Boolean.class)) {
      return getBooleanValueText(parameter.getValue());
    } else {
      return parameter.getValue();
    }
  }

  /** Returns the null value suitable for code generation. */
  private String getNullValueText() {
    return getNull();
  }

  /** Returns the string parameter value suitable for code generation. */
  protected String getStringValueText(String value) {
    // TODO(igorkarp): do not quote expressions once they could be detected.
    return getQuote() + value + getQuote();
  }

  /** Returns the boolean parameter value suitable for code generation. */
  protected String getBooleanValueText(String value) {
    if (value.equals(Boolean.TRUE.toString())) {
      return getTrue();
    } else if (value.equals(Boolean.FALSE.toString())) {
      return getFalse();
    } else {
      // If it is neither true nor false it is must be an expression.
      return value;
    }
  }

  private static Map<Character, String> buildAutoCloseMap(char c1, String s1, char c2, String s2,
      char c3, String s3, char c4, String s4, char c5, String s5) {
    Map<Character, String> map = new HashMap<Character, String>(5);
    map.put(c1, s1);
    map.put(c2, s2);
    map.put(c3, s3);
    map.put(c4, s4);
    map.put(c5, s5);
    return map;
  }
}
