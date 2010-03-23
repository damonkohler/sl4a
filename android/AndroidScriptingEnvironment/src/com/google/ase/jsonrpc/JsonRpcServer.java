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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.AseLog;
import com.google.ase.Server;
import com.google.ase.exception.AseRuntimeException;
import com.google.ase.rpc.MethodDescriptor;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer extends Server {

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, RpcInfo> mKnownRpcs = new ConcurrentHashMap<String, RpcInfo>();

  /**
   * The list of RPC receiving objects.
   */
  private final List<RpcReceiver> mReceivers = new ArrayList<RpcReceiver>();

  public JsonRpcServer(final RpcReceiver... receivers) {
    for (RpcReceiver receiver : receivers) {
      registerRpcReceiver(receiver);
    }
  }

  public Map<String, RpcInfo> getKnownRpcs() {
    return mKnownRpcs;
  }

  /**
   * Registers an RPC receiving object with this {@link JsonRpcServer} object.
   * 
   * @param receiver
   *          the receiving object
   */
  private void registerRpcReceiver(final RpcReceiver receiver) {
    final Class<?> clazz = receiver.getClass();
    for (MethodDescriptor m : MethodDescriptor.collectFrom(clazz)) {
      if (mKnownRpcs.containsKey(m.getName())) {
          // We already know an RPC of the same name.
          throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
        }
        mKnownRpcs.put(m.getName(), new RpcInfo(receiver, m));
    }
    mReceivers.add(receiver);
  }

  /**
   * Shuts down the RPC server.
   */
  @Override
  public void shutdown() {
    super.shutdown();
    // Notify all RPC receiving objects. They may have to clean up some of
    // their state.
    for (RpcReceiver receiver : mReceivers) {
      receiver.shutdown();
    }
  }

  @Override
  protected void process(BufferedReader in, PrintWriter out) throws IOException,
      AseRuntimeException {
    String data;
    while ((data = in.readLine()) != null) {
      AseLog.v("Received: " + data.toString());
      JSONObject result = JsonRpcResult.empty();
      try {
        result = call(data);
      } catch (AseRuntimeException e) {
        result = JsonRpcResult.error(e.getMessage());
        throw e;
      } finally {
        out.write(result.toString() + "\n");
        out.flush();
      }
    }
  }

  private JSONObject call(String json) throws AseRuntimeException {
    try {
      JSONObject jsonRequest = new JSONObject(json);
      // The JSON RPC spec says that id can be any object. To make our lives a
      // little easier, we'll assume it's always a number.
      int id = jsonRequest.getInt("id");
      String methodName = jsonRequest.getString("method");
      JSONArray params = jsonRequest.getJSONArray("params");
      return dispatch(id, methodName, params);
    } catch (Exception e) {
      throw new AseRuntimeException(e);
    }
  }

  private JSONObject dispatch(final int id, final String methodName, final JSONArray params)
      throws JSONException {
    JSONObject result = null;
    final RpcInfo rpcInfo = mKnownRpcs.get(methodName);
    if (rpcInfo == null) {
      result = JsonRpcResult.error("Unknown RPC.");
    } else {
      result = rpcInfo.invoke(params);
    }
    result.put("id", id);
    AseLog.v("Sending reply " + result.toString());
    return result;
  }
}
