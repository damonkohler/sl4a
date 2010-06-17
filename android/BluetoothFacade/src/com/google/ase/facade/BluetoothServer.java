/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.ase.facade;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import com.google.ase.AseLog;

/**
 * This class does all the work for setting up and managing Bluetooth connections with other
 * devices. It has a thread that listens for incoming connections, a thread for connecting with a
 * device, and a thread for performing data transmissions when connected.
 */
public class BluetoothServer {
  private static final String SDP_NAME = "ASE";

  private final BluetoothAdapter mAdapter;
  private final EventFacade mEventFacade;

  private AcceptThread mAcceptThread;
  private ConnectThread mConnectThread;
  private ConnectedThread mConnectedThread;
  private State mState;
  private String mDeviceName;

  private OutputStream mOutputStream;
  private InputStream mInputStream;
  private BufferedReader mReader;

  enum State {
    IDLE, LISTENING, CONNECTING, CONNECTED
  }

  public BluetoothServer(EventFacade eventFacade) {
    mEventFacade = eventFacade;
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mState = State.IDLE;
  }

  public OutputStream getOutputStream() throws IOException {
    if (mOutputStream != null) {
      return mOutputStream;
    }
    throw new IOException("Bluetooth not ready.");
  }

  public BufferedReader getReader() throws IOException {
    if (mReader != null) {
      return mReader;
    }
    throw new IOException("Bluetooth not ready.");
  }

  public String getDeviceName() {
    return mDeviceName;
  }

  /**
   * Set the current state of the chat connection
   * 
   * @param state
   *          An integer defining the current connection state
   */
  private synchronized void setState(State state) {
    AseLog.v("Bluetooth state changed from " + mState + " to " + state);
    switch (state) {
    case CONNECTED:
      mEventFacade.postEvent("bluetooth", "connected");
      break;
    case CONNECTING:
      mEventFacade.postEvent("bluetooth", "connecting");
      break;
    case LISTENING:
      mEventFacade.postEvent("bluetooth", "listening");
      break;
    case IDLE:
      mEventFacade.postEvent("bluetooth", "idle");
      break;
    }
    mState = state;
  }

  /**
   * Start the Bluetooth service. Specifically start AcceptThread to begin a session in listening
   * (server) mode.
   */
  public synchronized void start(UUID uuid) {
    // Cancel any thread attempting to make a connection.
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    // Cancel any thread currently running a connection.
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Start the thread to listen on a BluetoothServerSocket.
    if (mAcceptThread == null) {
      mAcceptThread = new AcceptThread(uuid);
      mAcceptThread.start();
    }

    setState(State.LISTENING);
  }

  /**
   * Start the ConnectThread to initiate a connection to a remote device.
   * 
   * @param device
   *          The BluetoothDevice to connect
   */
  public synchronized void connect(BluetoothDevice device, UUID uuid) {
    // Cancel any thread attempting to make a connection
    if (mState == State.CONNECTING) {
      if (mConnectThread != null) {
        mConnectThread.cancel();
        mConnectThread = null;
      }
    }

    // Cancel any thread currently running a connection
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Start the thread to connect with the given device
    mConnectThread = new ConnectThread(device, uuid);
    mConnectThread.start();
    setState(State.CONNECTING);
  }

  /**
   * Start the ConnectedThread to begin managing a Bluetooth connection.
   * 
   * @param socket
   *          The BluetoothSocket on which the connection was made
   * @param device
   *          The BluetoothDevice that has been connected
   */
  public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
    // Cancel the thread that completed the connection
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }

    // Cancel any thread currently running a connection
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }

    // Cancel the accept thread because we only want to connect to one device
    if (mAcceptThread != null) {
      mAcceptThread.cancel();
      mAcceptThread = null;
    }

    // Start the thread to manage the connection and perform transmissions
    mConnectedThread = new ConnectedThread(socket);
    mConnectedThread.start();

    // Send the name of the connected device back to the UI Activity
    mDeviceName = device.getName();

    setState(State.CONNECTED);
  }

  /**
   * Stop all threads
   */
  public synchronized void stop() {
    if (mConnectThread != null) {
      mConnectThread.cancel();
      mConnectThread = null;
    }
    if (mConnectedThread != null) {
      mConnectedThread.cancel();
      mConnectedThread = null;
    }
    if (mAcceptThread != null) {
      mAcceptThread.cancel();
      mAcceptThread = null;
    }
    setState(State.IDLE);
  }

  /**
   * This thread runs while listening for incoming connections. It behaves like a server-side
   * client. It runs until a connection is accepted (or until cancelled).
   */
  private class AcceptThread extends Thread {
    // The local server socket
    private final BluetoothServerSocket mmServerSocket;

    public AcceptThread(UUID uuid) {
      BluetoothServerSocket tmp = null;
      // Create a new listening server socket.
      try {
        tmp = mAdapter.listenUsingRfcommWithServiceRecord(SDP_NAME, uuid);
      } catch (IOException e) {
        AseLog.e("Bluetooth listen failed.", e);
      }
      mmServerSocket = tmp;
    }

    @Override
    public void run() {
      setName("AcceptThread");
      BluetoothSocket socket = null;
      // Listen to the server socket if we're not connected.
      while (mState != BluetoothServer.State.CONNECTED) {
        try {
          // This is a blocking call and will only return on a successful
          // connection or an exception.
          socket = mmServerSocket.accept();
        } catch (IOException e) {
          AseLog.e("Bluetooth accept failed.", e);
          break;
        }

        // If a connection was accepted.
        if (socket != null) {
          synchronized (BluetoothServer.this) {
            switch (mState) {
            case LISTENING:
            case CONNECTING:
              // Situation normal. Start the connected thread.
              connected(socket, socket.getRemoteDevice());
              break;
            case IDLE:
            case CONNECTED:
              // Either not ready or already connected. Terminate new socket.
              try {
                socket.close();
              } catch (IOException e) {
                AseLog.e("Blueooth could not close unwanted socket.", e);
              }
              break;
            }
          }
        }
      }
    }

    public void cancel() {
      try {
        mmServerSocket.close();
      } catch (IOException e) {
        AseLog.e("Bluetooth server failed to close.", e);
      }
    }
  }

  /**
   * This thread runs while attempting to make an outgoing connection with a device. It runs
   * straight through; the connection either succeeds or fails.
   */
  private class ConnectThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final BluetoothDevice mmDevice;

    public ConnectThread(BluetoothDevice device, UUID uuid) {
      mmDevice = device;
      BluetoothSocket tmp = null;

      // Get a BluetoothSocket for a connection with the given BluetoothDevice.
      try {
        tmp = device.createRfcommSocketToServiceRecord(uuid);
      } catch (IOException e) {
        AseLog.e("Bluetooth create failed.", e);
      }
      mmSocket = tmp;
    }

    @Override
    public void run() {
      setName("ConnectThread");
      // Always cancel discovery because it will slow down a connection.
      mAdapter.cancelDiscovery();

      // Make a connection to the BluetoothSocket.
      try {
        // This is a blocking call and will only return on a successful connection or an exception.
        mmSocket.connect();
      } catch (IOException e) {
        setState(BluetoothServer.State.IDLE);
        try {
          mmSocket.close();
        } catch (IOException e2) {
          AseLog.e("Bluetooth unable to close socket during connection failure.", e2);
        }
        BluetoothServer.this.stop();
        return;
      }

      // Reset the ConnectThread because we're done.
      synchronized (BluetoothServer.this) {
        mConnectThread = null;
      }

      // Start the connected thread.
      connected(mmSocket, mmDevice);
    }

    public void cancel() {
      try {
        mmSocket.close();
      } catch (IOException e) {
        AseLog.e("Bluetooth connect thread failed to close.", e);
      }
    }
  }

  /**
   * This thread runs during a connection with a remote device. It handles all incoming and outgoing
   * transmissions.
   */
  private class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;

    public ConnectedThread(BluetoothSocket socket) {
      mmSocket = socket;
      InputStream tmpIn = null;
      OutputStream tmpOut = null;

      // Get the BluetoothSocket input and output streams.
      try {
        tmpIn = socket.getInputStream();
        tmpOut = socket.getOutputStream();
      } catch (IOException e) {
        AseLog.e("Bluetooth temp sockets not created.", e);
      }

      mOutputStream = tmpOut;
      mInputStream = tmpIn;
      try {
        mReader = new BufferedReader(new InputStreamReader(tmpIn, "ASCII"));
      } catch (IOException e) {
        AseLog.e("Bluetooth sockets not created.", e);
      }
    }

    @Override
    public void run() {
      while (true) {
        try {
          // Detect a disconnected socket.
          mInputStream.available();
          Thread.sleep(100);
        } catch (IOException e) {
          AseLog.e("Bluetooth disconnected.", e);
          setState(BluetoothServer.State.IDLE);
        } catch (InterruptedException e) {
          AseLog.e("Bluetooth connection interrupted.", e);
          setState(BluetoothServer.State.IDLE);
        }
      }
    }

    public void cancel() {
      try {
        mmSocket.close();
      } catch (IOException e) {
        AseLog.e("Bluetooth close failed.", e);
      }
    }
  }
}
