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
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.RpcError;

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

  /**
   * Construct a {@link JsonRpcServer} connected to the provided {@link RpcReceiver}s.
   * 
   * @param receivers
   *          the {@link RpcReceiver}s to register with the server
   */
  public JsonRpcServer(List<RpcReceiver> receivers) {
    for (RpcReceiver receiver : receivers) {
      registerRpcReceiver(receiver);
    }
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
        // We already know an RPC of the same name. We don't catch this anywhere because this is a
        // programming error.
        throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
      }
      mKnownRpcs.put(m.getName(), new RpcInfo(receiver, m));
    }
    mReceivers.add(receiver);
  }

  @Override
  public void shutdown() {
    super.shutdown();
    // Notify all RPC receiving objects. They may have to clean up some of their state.
    for (RpcReceiver receiver : mReceivers) {
      receiver.shutdown();
    }
  }

  @Override
  protected void process(BufferedReader in, PrintWriter out) throws JSONException, IOException {
    String data;
    while ((data = in.readLine()) != null) {
      AseLog.v("Received: " + data);
      JSONObject request = new JSONObject(data);
      JSONObject result = JsonRpcResult.empty();
      try {
        result = call(request);
      } catch (Exception e) {
        result = JsonRpcResult.error(e.getMessage());
      } finally {
        result.put("id", request.getInt("id"));
        out.write(result + "\n");
        out.flush();
        AseLog.v("Sent: " + result);
      }
    }
  }

  private JSONObject call(JSONObject request) throws JSONException, RpcError {
    // The JSON RPC spec says that id can be any object. To make our lives a
    // little easier, we'll assume it's always a number.
    String methodName = request.getString("method");
    JSONArray params = request.getJSONArray("params");
    JSONObject result;
    RpcInfo rpcInfo = mKnownRpcs.get(methodName);
    if (rpcInfo == null) {
      throw new RpcError("Unknown RPC.");
    } else {
      result = rpcInfo.invoke(params);
    }
    return result;
  }
}
