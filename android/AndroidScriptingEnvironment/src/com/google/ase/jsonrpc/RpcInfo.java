/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.jsonrpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.ParameterDescriptor;

/**
 * Instances of this class describe specific RPCs on the server. An RPC on the server is described
 * by a triple consisting of: - a receiving object of the call - the method of the object to call -
 * an {@link RpcInvoker} object that knows to parse a {@link JSONArray} for the parameters
 *
 * @author Felix Arends (felix.arends@gmail.com)
 *
 */
public class RpcInfo {
  private final Object mReceiver;
  private final MethodDescriptor mMethodDescriptor;
  private final RpcInvoker mInvoker;

  public RpcInfo(final Object receiver, final MethodDescriptor methodDescriptor, final RpcInvoker invoker) {
    mReceiver = receiver;
    mMethodDescriptor = methodDescriptor;
    mInvoker = invoker;
  }

  /**
   * Invokes the call that belongs to this object with the given parameters. Wraps the response
   * (possibly an exception) in a JSONObject.
   *
   * @param parameters
   *          {@code JSONArray} containing the parameters
   * @return RPC response
   */
  public JSONObject invoke(final JSONArray parameters) {
    try {
      return mInvoker.invoke(mMethodDescriptor.getMethod(), mReceiver, parameters);
    } catch (JSONException e) {
      return JsonRpcResult.error("Remote Exception", e);
    }
  }

  public String getName() {
    return mMethodDescriptor.getName();
  }

  /**
   * Returns a human-readable help text for this RPC, based on annotations in the source code.
   *
   * @return derived help string
   */
  public String getHelp() {
    return mMethodDescriptor.getHelp();
  }

  /**
   * Returns parameter descriptors suitable for the RPC call text representation.
   * 
   * <p>Uses parameter name or default value if it is more meaningful as value.
   * 
   * @return an array of parameter descriptors
   */
  public ParameterDescriptor[] getDefaultParameterValues() {
    return mMethodDescriptor.getDefaultParameterValues();
  }
}