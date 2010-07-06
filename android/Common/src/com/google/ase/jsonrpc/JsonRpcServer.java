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

import com.google.ase.AseLog;
import com.google.ase.exception.AseException;
import com.google.ase.rpc.MethodDescriptor;
import com.google.ase.rpc.RpcError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer {

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, MethodDescriptor> mKnownRpcs = new HashMap<String, MethodDescriptor>();

  private final RpcReceiverManager mRpcReceiverManager;

  private ServerSocket mServer;
  private Thread mServerThread;
  private final CopyOnWriteArrayList<ConnectionThread> mNetworkThreads;
  private volatile boolean mStopServer = false;

  private final UUID mSecret;

  private final class ConnectionThread extends Thread {
    private final Socket mmSocket;
    private BufferedReader mmReader;
    private PrintWriter mmWriter;

    private ConnectionThread(Socket sock) {
      mmSocket = sock;
    }

    @Override
    public void run() {
      AseLog.v("Server thread " + getId() + " started.");
      try {
        mmReader = new BufferedReader(new InputStreamReader(mmSocket.getInputStream()), 8192);
        mmWriter = new PrintWriter(mmSocket.getOutputStream(), true);
        if (mSecret != null && !checkHandshake()) {
          AseLog.e("Authentication failed.");
          return;
        }
        process();
      } catch (Exception e) {
        if (!mStopServer) {
          AseLog.e("Server error.", e);
        }
      } finally {
        close();
        mNetworkThreads.remove(this);
        AseLog.v("Server thread " + getId() + " died.");
      }
    }

    private void process() throws JSONException, IOException {
      String data;
      while ((data = mmReader.readLine()) != null) {
        AseLog.v("Received: " + data);
        JSONObject request = new JSONObject(data);
        int id = request.getInt("id");
        String method = request.getString("method");
        JSONArray params = request.getJSONArray("params");
        MethodDescriptor rpc = mKnownRpcs.get(method);
        if (rpc == null) {
          send(JsonRpcResult.error(id, new RpcError("Unknown RPC.")));
          continue;
        }
        try {
          send(JsonRpcResult.result(id, rpc.invoke(mRpcReceiverManager, params)));
        } catch (Throwable t) {
          AseLog.e("Invocation error.", t);
          send(JsonRpcResult.error(id, t));
        }
      }
    }

    private void send(JSONObject result) {
      mmWriter.write(result + "\n");
      mmWriter.flush();
      AseLog.v("Sent: " + result);
    }

    private boolean checkHandshake() throws AseException {
      try {
        String data = mmReader.readLine();
        return mSecret.toString().equals(data);
      } catch (Exception e) {
        throw new AseException("Handshake failed!", e);
      }
    }

    private void close() {
      if (mmSocket != null) {
        try {
          mmSocket.close();
        } catch (IOException e) {
          AseLog.e(e.getMessage(), e);
        }
      }
      if (mmReader != null) {
        try {
          mmReader.close();
        } catch (IOException e) {
          AseLog.e(e.getMessage(), e);
        }
      }
      if (mmWriter != null) {
        mmWriter.close();
      }
    }
  }

  /**
   * Construct a {@link JsonRpcServer} connected to the provided {@link RpcReceiver}s.
   * 
   * @param receivers
   *          the {@link RpcReceiver}s to register with the server
   */
  public JsonRpcServer(RpcReceiverManager manager, UUID secret) {
    mRpcReceiverManager = manager;
    mSecret = secret;
    mNetworkThreads = new CopyOnWriteArrayList<ConnectionThread>();
    for (Class<? extends RpcReceiver> receiver : manager.getRpcReceiverClasses()) {
      registerRpcReceiver(receiver);
    }
  }

  /**
   * Registers an RPC receiving object with this {@link JsonRpcServer} object.
   * 
   * @param receiver
   *          the receiving object
   */
  private void registerRpcReceiver(final Class<? extends RpcReceiver> receiverClass) {
    Collection<MethodDescriptor> methodList = MethodDescriptor.collectFrom(receiverClass);
    for (MethodDescriptor m : methodList) {
      if (mKnownRpcs.containsKey(m.getName())) {
        // We already know an RPC of the same name. We don't catch this anywhere because this is a
        // programming error.
        throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
      }
      mKnownRpcs.put(m.getName(), m);
    }
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
   * data Starts the RPC server bound to the public facing address.
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
        while (!mStopServer) {
          try {
            Socket sock = mServer.accept();
            startConnectionThread(sock);
          } catch (IOException e) {
            if (!mStopServer) {
              AseLog.e("Failed to accept connection.", e);
            }
          }
        }
      }
    };
    mServerThread.start();
    AseLog.v("Bound to " + address.getHostAddress() + ":" + mServer.getLocalPort());
    return mServer.getLocalPort();
  }

  private void startConnectionThread(final Socket sock) {
    ConnectionThread networkThread = new ConnectionThread(sock);
    mNetworkThreads.add(networkThread);
    networkThread.start();
  }

  public void shutdown() {
    // Stop listening on the server socket to ensure that
    // beyond this point there are no incoming requests.
    mStopServer = true;
    try {
      mServer.close();
    } catch (IOException e) {
      AseLog.e("Failed to close server socket.", e);
    }
    // Since the server is not running, the mNetworkThreads set can only
    // shrink from this point onward. We can just stop all of the running helper
    // threads. In the worst case, one of the running threads will already have
    // shut down. Since this is a CopyOnWriteList, we don't have to worry about
    // concurrency issues while iterating over the set of threads.
    for (ConnectionThread networkThread : mNetworkThreads) {
      networkThread.close();
    }
    // Notify all RPC receiving objects. They may have to clean up some of their state.
    mRpcReceiverManager.shutdown();
  }
}
