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
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.RpcError;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer {

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, RpcInfo> mKnownRpcs = new ConcurrentHashMap<String, RpcInfo>();

  /**
   * The list of RPC receiving objects.
   */
  private final List<RpcReceiver> mReceivers = new ArrayList<RpcReceiver>();

  private ServerSocket mServer;
  private Thread mServerThread;
  private final CopyOnWriteArraySet<Thread> mNetworkThreads;

  /**
   * Construct a {@link JsonRpcServer} connected to the provided {@link RpcReceiver}s.
   * 
   * @param receivers
   *          the {@link RpcReceiver}s to register with the server
   */
  public JsonRpcServer(List<RpcReceiver> receivers) {
    mNetworkThreads = new CopyOnWriteArraySet<Thread>();
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

  private int start(InetAddress address) {
    mServerThread = new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            Socket sock = mServer.accept();
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
        AseLog.v("Server thread " + getId() + " started.");
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
          PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
          process(in, out);
        } catch (Exception e) {
          AseLog.e("Server error.", e);
        } finally {
          mNetworkThreads.remove(this);
          AseLog.v("Server thread " + getId() + " died.");
        }
      }
    };

    mNetworkThreads.add(networkThread);
    networkThread.start();
  }

  private void process(BufferedReader in, PrintWriter out) throws JSONException, IOException {
    String data;
    while ((data = in.readLine()) != null) {
      AseLog.v("Received: " + data);
      JSONObject request = new JSONObject(data);
      JSONObject result = JsonRpcResult.empty();
      try {
        result = call(request);
      } catch (Exception e) {
        result = JsonRpcResult.error(e);
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
    // Notify all RPC receiving objects. They may have to clean up some of their state.
    for (RpcReceiver receiver : mReceivers) {
      receiver.shutdown();
    }
  }
}
