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
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

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
  private final Object mReceiver;
  private ServerSocket mServer;

  /**
   * Builds a JSON RPC server which forwards RPC calls to a receiver object.
   */
  public JsonRpcServer(Object receiver) {
    mReceiver = receiver;
  }

  private InetAddress getPublicInetAddress() throws UnknownHostException, SocketException {
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    for (NetworkInterface netint : Collections.list(nets)) {
      Enumeration<InetAddress> addresses = netint.getInetAddresses();
      for (InetAddress address : Collections.list(addresses)) {
        if (!address.getHostAddress().equals("127.0.0.1")) {
          return address;
        }
      }
    }
    return InetAddress.getLocalHost();
  }

  /**
   * Starts the RPC server bound to the localhost address.
   *
   * @return the port that was allocated by the OS
   */
  public InetSocketAddress startLocal() {
    InetAddress address;
    try {
      address = InetAddress.getLocalHost();
      mServer = new ServerSocket(0 /* port */, 5 /* backlog */, address);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start server.", e);
      return null;
    }
    int port = start(address);
    return new InetSocketAddress(address, port);
  }

  /**
   * Starts the RPC server bound to the public facing address.
   *
   * @return the port that was allocated by the OS
   */
  public InetSocketAddress startPublic() {
    InetAddress address;
    try {
      address = getPublicInetAddress();
      mServer = new ServerSocket(0 /* port */, 5 /* backlog */, address);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start server.", e);
      return null;
    }
    int port = start(address);
    return new InetSocketAddress(address, port);
  }

  private int start(InetAddress address) {
    new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            Socket sock = mServer.accept();
            Log.v(TAG, "Connected!");
            startConnectionThread(sock);
          } catch (IOException e) {
            Log.e(TAG, "Failed to accept connection.", e);
          }
        }
      }
    }.start();

    Log.v(TAG, "Bound to " + address.getHostAddress() + ":" + mServer.getLocalPort());
    return mServer.getLocalPort();
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
        Method m = mReceiver.getClass().getMethod(methodName, new Class[] { JSONArray.class });
        Rpc annotation = m.getAnnotation(Rpc.class);
        if (annotation != null) {
          methods.put(m.getName() + "\n\t" + annotation.description() + "\n\targs: "
              + annotation.params() + "\n\treturns: " + annotation.returns());
        }
      } else {
        // Lookup help for all available RPC methods.
        for (Method m : mReceiver.getClass().getMethods()) {
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
      Method m = mReceiver.getClass().getMethod(methodName, new Class[] { JSONArray.class });
      if (m.isAnnotationPresent(Rpc.class)) {
        result = (JSONObject) m.invoke(mReceiver, new Object[] { params });
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
