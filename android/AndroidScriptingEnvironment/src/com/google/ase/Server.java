/*
 * Copyright (C) 2010 Google Inc.
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

package com.google.ase;

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
import java.util.Collections;
import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArraySet;

public abstract class Server {

  private ServerSocket mServer;
  private Thread mServerThread;
  private final CopyOnWriteArraySet<Thread> mNetworkThreads;

  public Server() {
    mNetworkThreads = new CopyOnWriteArraySet<Thread>();
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
        } catch (IOException e) {
          AseLog.e("Unknown server error.", e);
        } finally {
          mNetworkThreads.remove(this);
          AseLog.v("Server thread " + getId() + " died.");
        }
      }
    };

    mNetworkThreads.add(networkThread);
    networkThread.start();
  }

  protected abstract void process(BufferedReader in, PrintWriter out) throws IOException;
}
