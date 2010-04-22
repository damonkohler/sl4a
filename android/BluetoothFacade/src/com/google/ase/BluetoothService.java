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

package com.google.ase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

/**
 * This class does all the work for setting up and managing Bluetooth connections with other
 * devices. It has a thread that listens for incoming connections, a thread for connecting with a
 * device, and a thread for performing data transmissions when connected.
 */
public class BluetoothService {
  // Name for the SDP record when creating server socket
  private static final String NAME = "ASE";

  private final BluetoothAdapter mAdapter;
  private final Handler mHandler;
  private AcceptThread mAcceptThread;
  private ConnectThread mConnectThread;
  private ConnectedThread mConnectedThread;
  private int mState;

  // Constants that indicate the current connection state.
  public static final int STATE_IDLE = 0; // We're doing nothing.
  public static final int STATE_LISTEN = 1; // Now listening for incoming connections.
  public static final int STATE_CONNECTING = 2; // Now initiating an outgoing connection.
  public static final int STATE_CONNECTED = 3; // Now connected to a remote device.

  // Message types sent.
  public static final int MESSAGE_STATE_CHANGE = 1;
  public static final int MESSAGE_READ = 2;
  public static final int MESSAGE_WRITE = 3;
  public static final int MESSAGE_DEVICE_NAME = 4;
  public static final int MESSAGE_TOAST = 5;

  // Key names received.
  public static final String DEVICE_NAME = "device_name";
  public static final String TOAST = "toast";

  /**
   * Constructor. Prepares a new Bluetooth session.
   * 
   * @param handler
   *          A Handler to send messages back to the calling {@link Activity}.
   */
  public BluetoothService(Handler handler) {
    mHandler = handler;
    mAdapter = BluetoothAdapter.getDefaultAdapter();
    mState = STATE_IDLE;
  }

  /**
   * Set the current state of the chat connection
   * 
   * @param state
   *          An integer defining the current connection state
   */
  private synchronized void setState(int state) {
    AseLog.v("Bluetooth set changed from " + mState + " to " + state);
    mState = state;
    mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
  }

  /**
   * Return the current connection state.
   */
  public synchronized int getState() {
    return mState;
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

    setState(STATE_LISTEN);
  }

  /**
   * Start the ConnectThread to initiate a connection to a remote device.
   * 
   * @param device
   *          The BluetoothDevice to connect
   */
  public synchronized void connect(BluetoothDevice device, UUID uuid) {
    // Cancel any thread attempting to make a connection
    if (mState == STATE_CONNECTING) {
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
    setState(STATE_CONNECTING);
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
    Message msg = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
    Bundle bundle = new Bundle();
    bundle.putString(DEVICE_NAME, device.getName());
    msg.setData(bundle);
    mHandler.sendMessage(msg);

    setState(STATE_CONNECTED);
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
    setState(STATE_IDLE);
  }

  /**
   * Write to the ConnectedThread in an unsynchronized manner
   * 
   * @param out
   *          The bytes to write
   * @see ConnectedThread#write(byte[])
   */
  public void write(byte[] out) {
    // Create temporary object.
    ConnectedThread r;
    // Synchronize a copy of the ConnectedThread.
    synchronized (this) {
      if (mState != STATE_CONNECTED)
        return;
      r = mConnectedThread;
    }
    // Perform the write unsynchronized.
    r.write(out);
  }

  /**
   * Indicate that the connection attempt failed and notify the calling {@link Activity}.
   */
  private void connectionFailed() {
    setState(STATE_LISTEN);
    // Send a failure message back to the Activity.
    Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
    Bundle bundle = new Bundle();
    bundle.putString(TOAST, "Unable to connect device");
    msg.setData(bundle);
    mHandler.sendMessage(msg);
  }

  /**
   * Indicate that the connection was lost and notify the calling {@link Activity}.
   */
  private void connectionLost() {
    setState(STATE_LISTEN);
    // Send a failure message back to the Activity
    Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
    Bundle bundle = new Bundle();
    bundle.putString(TOAST, "Device connection was lost");
    msg.setData(bundle);
    mHandler.sendMessage(msg);
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
        tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, uuid);
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
      while (mState != STATE_CONNECTED) {
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
          synchronized (BluetoothService.this) {
            switch (mState) {
            case STATE_LISTEN:
            case STATE_CONNECTING:
              // Situation normal. Start the connected thread.
              connected(socket, socket.getRemoteDevice());
              break;
            case STATE_IDLE:
            case STATE_CONNECTED:
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
        connectionFailed();
        try {
          mmSocket.close();
        } catch (IOException e2) {
          AseLog.e("Bluetooth unable to close socket during connection failure.", e2);
        }
        return;
      }

      // Reset the ConnectThread because we're done.
      synchronized (BluetoothService.this) {
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
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

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

      mmInStream = tmpIn;
      mmOutStream = tmpOut;
    }

    @Override
    public void run() {
      byte[] buffer = new byte[1024];
      int bytes;
      // Keep listening to the InputStream while connected.
      while (true) {
        try {
          // Read from the InputStream.
          bytes = mmInStream.read(buffer);
          // Send the obtained bytes to the calling Activity.
          mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer).sendToTarget();
        } catch (IOException e) {
          AseLog.e("Bluetooth disconnected.", e);
          connectionLost();
          break;
        }
      }
    }

    /**
     * Write to the connected OutStream.
     * 
     * @param buffer
     *          The bytes to write
     */
    public void write(byte[] buffer) {
      try {
        mmOutStream.write(buffer);

        // Share the sent message back to the UI Activity
        mHandler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
      } catch (IOException e) {
        AseLog.e("Bluetooth exception during write.", e);
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
