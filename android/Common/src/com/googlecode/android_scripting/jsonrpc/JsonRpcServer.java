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

package com.googlecode.android_scripting.jsonrpc;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.SimpleServer;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcError;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer extends SimpleServer {

  private final RpcReceiverManagerFactory mRpcReceiverManagerFactory;
  private final String mHandshake;

  /**
   * Construct a {@link JsonRpcServer} connected to the provided {@link RpcReceiverManager}.
   * 
   * @param managerFactory
   *          the {@link RpcReceiverManager} to register with the server
   * @param handshake
   *          the secret handshake required for authorization to use this server
   */
  public JsonRpcServer(RpcReceiverManagerFactory managerFactory, String handshake) {
    mHandshake = handshake;
    mRpcReceiverManagerFactory = managerFactory;
  }

  @Override
  public void shutdown() {
    super.shutdown();
    // Notify all RPC receiving objects. They may have to clean up some of their state.
    for (RpcReceiverManager manager : mRpcReceiverManagerFactory.getRpcReceiverManagers()) {
      manager.shutdown();
    }
  }

  @Override
  protected void handleConnection(Socket socket) throws Exception {
    RpcReceiverManager receiverManager = mRpcReceiverManagerFactory.create();
    BufferedReader reader =
        new BufferedReader(new InputStreamReader(socket.getInputStream()), 8192);
    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
    boolean passedAuthentication = false;
    String data;
    while ((data = reader.readLine()) != null) {
      Log.v("Received: " + data);
      JSONObject request = new JSONObject(data);
      int id = request.getInt("id");
      String method = request.getString("method");
      JSONArray params = request.getJSONArray("params");

      // First RPC must be _authenticate if a handshake was specified.
      if (!passedAuthentication && mHandshake != null) {
        if (!checkHandshake(method, params)) {
          SecurityException exception = new SecurityException("Authentication failed!");
          send(writer, JsonRpcResult.error(id, exception));
          shutdown();
          throw exception;
        }
        passedAuthentication = true;
        send(writer, JsonRpcResult.result(id, true));
        continue;
      }

      MethodDescriptor rpc = receiverManager.getMethodDescriptor(method);
      if (rpc == null) {
        send(writer, JsonRpcResult.error(id, new RpcError("Unknown RPC.")));
        continue;
      }
      try {
        send(writer, JsonRpcResult.result(id, rpc.invoke(receiverManager, params)));
      } catch (Throwable t) {
        Log.e("Invocation error.", t);
        send(writer, JsonRpcResult.error(id, t));
      }
    }
  }

  private boolean checkHandshake(String method, JSONArray params) throws JSONException {
    if (!method.equals("_authenticate") || !mHandshake.equals(params.getString(0))) {
      return false;
    }
    return true;
  }

  private void send(PrintWriter writer, JSONObject result) {
    writer.write(result + "\n");
    writer.flush();
    Log.v("Sent: " + result);
  }
}
