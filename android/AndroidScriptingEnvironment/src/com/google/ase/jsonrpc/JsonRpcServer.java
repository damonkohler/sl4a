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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.AseLog;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 *
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer {

  private ServerSocket mServer;

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, RpcInfo> mKnownRpcs = new ConcurrentHashMap<String, RpcInfo>();

  /**
   * The list of RPC receiving objects.
   */
  private final List<RpcReceiver> mReceivers = new ArrayList<RpcReceiver>();

  /**
   * The network thread that receives RPCs.
   */
  private Thread mServerThread;

  /**
   * The set of active threads spawned for each client connection.
   */
  private final CopyOnWriteArraySet<Thread> mNetworkThreads = new CopyOnWriteArraySet<Thread>();

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
    for (Method m : clazz.getMethods()) {
      if (m.getAnnotation(Rpc.class) != null) {
        if (mKnownRpcs.containsKey(m.getName())) {
          // We already know an RPC of the same name.
          throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
        }
        mKnownRpcs.put(m.getName(), new RpcInfo(receiver, m, RpcInvokerFactory.createInvoker(m
            .getGenericParameterTypes())));
      }
    }
    mReceivers.add(receiver);
  }

  /**
   * Builds a map of method names to {@link RpcInfo} objects.
   *
   * @param receiver
   *          the {@link RpcReceiver} class to inspect
   */
  public static Map<String, RpcInfo> buildRpcInfoMap(final Class<? extends RpcReceiver> receiver) {
    Map<String, RpcInfo> rpcs = new ConcurrentHashMap<String, RpcInfo>();
    for (Method m : receiver.getMethods()) {
      if (m.getAnnotation(Rpc.class) != null) {
        // TODO(damonkohler): This doesn't build valid RpcInfo objects since receiver is a class not
        // an instance.
        rpcs.put(m.getName(), new RpcInfo(receiver, m, RpcInvokerFactory.createInvoker(m
            .getGenericParameterTypes())));
      }
    }
    return rpcs;
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
      AseLog.e("Failed to start server.", e);
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
      AseLog.e("Failed to start server.", e);
      return null;
    }
    int port = start(address);
    return new InetSocketAddress(address, port);
  }

  /**
   * Shuts down the RPC server.
   */
  public void shutdown() {
    // Interrupt the server thread to ensure that beyond this point there are
    // no incoming requests.
    mServerThread.interrupt();
    // Since the server thread is not running, the mNetworkThreads set can only
    // shrink from this point onward. We can just cancel all of the running
    // threads. In the worst case, one of the running threads will already have
    // shut down. Since this is a CopyOnWriteSet, we don't have to worry about
    // concurrency issues while iterating over the set of threads.
    for (Thread networkThread : mNetworkThreads) {
      networkThread.interrupt();
    }
    // Notify all RPC receiving objects. They may have to clean up some of
    // their state.
    for (RpcReceiver receiver : mReceivers) {
      receiver.shutdown();
    }
    AseLog.v("RPC server shutdown.");
  }

  private int start(InetAddress address) {
    mServerThread = new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            Socket sock = mServer.accept();
            AseLog.v("Connected!");
            startConnectionThread(sock);
          } catch (IOException e) {
            AseLog.e("Failed to accept connection.", e);
          }
        }
      }
    };
    mServerThread.start();
    AseLog.v("Bound to " + address.getHostAddress() + ":" + mServer.getLocalPort());
    return mServer.getLocalPort();
  }

  private void startConnectionThread(final Socket sock) {
    final Thread networkThread = new Thread() {
      @Override
      public void run() {
        AseLog.v("RPC thread " + getId() + " started.");
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
          PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
          String data;
          while ((data = in.readLine()) != null) {
            AseLog.v("Received: " + data.toString());
            JSONObject result = JsonRpcResult.empty();
            try {
              result = call(data);
            } catch (Exception e) {
              result = JsonRpcResult.error(e.getMessage());
              throw e;
            } finally {
              out.write(result.toString() + "\n");
              out.flush();
            }
          }
        } catch (Exception e) {
          AseLog.e("Server error.", e);
        } finally {
          mNetworkThreads.remove(this);
          AseLog.v("RPC thread " + getId() + " died.");
        }
      }
    };

    mNetworkThreads.add(networkThread);
    networkThread.start();
  }

  private JSONObject call(String json) throws JSONException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    JSONObject jsonRequest = new JSONObject(json);
    // The JSON RPC spec says that id can be any object. To make our lives a
    // little easier, we'll assume it's always a number.
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
        // Lookup help for one specific method.
        final RpcInfo rpcInfo = mKnownRpcs.get(methodName);
        if (rpcInfo == null) { // Method not found.
          methods.put("Unknown Function.");
        } else {
          methods.put(rpcInfo.getHelp());
        }
      } else {
        // Lookup help for all available RPC methods.
        for (RpcInfo rpcInfo : mKnownRpcs.values()) {
          methods.put(rpcInfo.getHelp() + "\n");
        }
      }
    } catch (Exception e) {
      result = JsonRpcResult.error("RPC Error", e);
    }

    return result;
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
