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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer {
  static final String TAG = "JsonRpcServer";
  private final Object receiver;
  private ServerSocket server;

  /**
   * Builds a JSON RPC server which forwards RPC calls to a receiver object.
   */
  public JsonRpcServer(Object receiver) {
    this.receiver = receiver;
  }

  /**
   * Starts the RPC server.
   *
   * @return the port that was allocated by the OS
   */
  public int start() {
    try {
      InetAddress localhost = InetAddress.getLocalHost();
      server = new ServerSocket(0 /* port */, 5 /* backlog */, localhost);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start server.", e);
      return 0;
    }

    new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            Socket sock = server.accept();
            Log.v(TAG, "Connected!");
            startConnectionThread(sock);
          } catch (IOException e) {
            Log.e(TAG, "Failed to accept connection.", e);
          }
        }
      }
    }.start();

    Log.v(TAG, "Listening on port: " + server.getLocalPort());
    return server.getLocalPort();
  }

  private void startConnectionThread(final Socket sock) {
    new Thread() {
      @Override
      public void run() {
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
          PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
          String data;
          while ((data = in.readLine()) != null) {
            Log.v(TAG, "Received: " + data.toString());
            JSONObject result = call(data);
            out.write(result.toString() + "\n");
            out.flush();
          }
        } catch (Exception e) {
          Log.e(TAG, "Communication with client failed.", e);
        }
      }
    }.start();
  }

  private JSONObject call(String json) throws JSONException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    JSONObject jsonRequest = new JSONObject(json);
    // The JSON RPC spec says that id can be any object. To make our lives a little easier, we'll
    // assume it's always a number.
    int id = jsonRequest.getInt("id");
    String methodName = jsonRequest.getString("method");
    JSONArray params = jsonRequest.getJSONArray("params");
    if (methodName.equals("_help")) {
      return help(id, params);
    }
    return dispatch(id, methodName, params);
  }

  private JSONObject help(int id, JSONArray params) throws JSONException {
    JSONObject result = JsonRpcResult.empty();
    result.put("id", id);
    JSONArray methods = new JSONArray();
    result.put("result", methods);
    try {
      String methodName = params.optString(0);
      if (!methodName.equals("")) {
        // Lookup help for a specific method.
        Method m = receiver.getClass().getMethod(methodName, new Class[] { JSONArray.class });
        Rpc annotation = m.getAnnotation(Rpc.class);
        if (annotation != null) {
          methods.put(m.getName() + "\n\t" + annotation.description() + "\n\targs: "
              + annotation.params() + "\n\treturns: " + annotation.returns());
        }
      } else {
        // Lookup help for all available RPC methods.
        for (Method m : receiver.getClass().getMethods()) {
          Rpc annotation = m.getAnnotation(Rpc.class);
          if (annotation != null) {
            methods.put(m.getName() + "\n\t" + annotation.description() + "\n\targs: "
                + annotation.params() + "\n\treturns: " + annotation.returns());
          }
        }
      }
    } catch (Exception e) {
      result = JsonRpcResult.error("RPC Error", e);
    }
    return result;
  }

  private JSONObject dispatch(int id, String methodName, JSONArray params) throws JSONException {
    JSONObject result;
    try {
      Method m = receiver.getClass().getMethod(methodName, new Class[] { JSONArray.class });
      if (m.isAnnotationPresent(Rpc.class)) {
        result = (JSONObject) m.invoke(receiver, new Object[] { params });
      } else {
        throw new Exception();
      }
    } catch (Exception e) {
      result = JsonRpcResult.error("RPC Error", e);
    }
    result.put("id", id);
    Log.v(TAG, "Sending reply " + result.toString());
    return result;
  }
}
