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

package com.google.ase.language;

import com.google.ase.jsonrpc.ParameterDescriptor;
import com.google.ase.jsonrpc.RpcInfo;

/**
 * Represents the programming language supported by the ASE.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public abstract class Language {
  
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

  /** Returns the RPC call text with default parameter values. */
  public final String getRpcText(String content, RpcInfo rpc) {
    return getRpcText(content, rpc, rpc.getDefaultParameterValues());
  }
  
  /** Returns the RPC call text with given parameter values. */
  public final String getRpcText(String content, RpcInfo rpc, ParameterDescriptor[] parameters) {
    return getMethodCallText(getRpcReceiverName(content), rpc.getName(), parameters);
  }
  
  /** Returns the RPC receiver found in the given script. */
  protected String getRpcReceiverName(String content) {
    return getDefaultRpcReceiver();
  }
  
  /** Returns the method call text in the language.*/
  protected String getMethodCallText(String receiver, String method,
      ParameterDescriptor[] parameters) {
    StringBuilder result = new StringBuilder().append(getApplyReceiverText(receiver))
        .append(getApplyOperatorText()).append(method).append(getLeftParametersText());
    String separator = "";
    for (ParameterDescriptor parameter : parameters) {
      result.append(separator).append(maybeQuote(parameter));
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

  /** Returns the parameter value, quoted if needed. */
  private String maybeQuote(ParameterDescriptor parameter) {
    return parameter.getType().equals(String.class) && parameter.getValue() != null
        ? quote(parameter.getValue())
        : parameter.getValue();
  }

  /** Returns the quoted value. */
  private String quote(String value) {
    return getQuote() + value + getQuote();
  }

}
