/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.connectbot.transport;

import org.connectbot.service.TerminalBridge;
import org.connectbot.service.TerminalManager;

import java.io.IOException;

/**
 * @author Kenny Root
 * @author modified by raaar
 */
public abstract class AbsTransport {

  TerminalBridge bridge;
  TerminalManager manager;

  /**
   * Causes transport to connect to the target host. After connecting but before a session is
   * started, must call back to {@link TerminalBridge#onConnected()}. After that call a session may
   * be opened.
   */
  public abstract void connect();

  /**
   * Reads from the transport. Transport must support reading into a the byte array
   * <code>buffer</code> at the start of <code>offset</code> and a maximum of <code>length</code>
   * bytes. If the remote host disconnects, throw an {@link IOException}.
   * 
   * @param buffer
   *          byte buffer to store read bytes into
   * @param offset
   *          where to start writing in the buffer
   * @param length
   *          maximum number of bytes to read
   * @return number of bytes read
   * @throws IOException
   *           when remote host disconnects
   */
  public abstract int read(byte[] buffer, int offset, int length) throws IOException;

  /**
   * Writes to the transport. If the host is not yet connected, simply return without doing
   * anything. An {@link IOException} should be thrown if there is an error after connection.
   * 
   * @param buffer
   *          bytes to write to transport
   * @throws IOException
   *           when there is a problem writing after connection
   */
  public abstract void write(byte[] buffer) throws IOException;

  /**
   * Writes to the transport. See {@link #write(byte[])} for behavior details.
   * 
   * @param c
   *          character to write to the transport
   * @throws IOException
   *           when there is a problem writing after connection
   */
  public abstract void write(int c) throws IOException;

  /**
   * Flushes the write commands to the transport.
   * 
   * @throws IOException
   *           when there is a problem writing after connection
   */
  public abstract void flush() throws IOException;

  /**
   * Closes the connection to the terminal. Note that the resulting failure to read should call
   * {@link TerminalBridge#dispatchDisconnect(boolean)}.
   */
  public abstract void close();

  /**
   * Tells the transport what dimensions the display is currently
   * 
   * @param columns
   *          columns of text
   * @param rows
   *          rows of text
   * @param width
   *          width in pixels
   * @param height
   *          height in pixels
   */
  public abstract void setDimensions(int columns, int rows, int width, int height);

  public void setBridge(TerminalBridge bridge) {
    this.bridge = bridge;
  }

  public void setManager(TerminalManager manager) {
    this.manager = manager;
  }

  public abstract boolean isConnected();

  public abstract boolean isSessionOpen();

}
