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

/**
 * Implementations of this interface are used to parse the JSONArray arriving
 * from the client and invoking a particular RPC.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public interface RpcInvoker {
  /**
   * Parses a {@code JSONArray} of parameters and invokes an RPC on the server
   * side.
   * 
   * @param m the method to invoke
   * @param receiver the object containing the method to invoke
   * @param parameters array of parameters as received by the client
   * @return the {@code JsonRpcResult} object with the appropriate result
   */
  public JSONObject invoke(MethodDescriptor m, Object receiver, JSONArray parameters) throws JSONException;
}
