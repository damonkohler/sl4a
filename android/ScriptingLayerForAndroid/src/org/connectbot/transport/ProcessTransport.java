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

import com.googlecode.android_scripting.Exec;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.Process;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProcessTransport extends AbsTransport {

  private FileDescriptor shellFd;
  private final Process mProcess;
  private InputStream is;
  private OutputStream os;
  private boolean mConnected = false;

  public ProcessTransport(Process process) {
    mProcess = process;
    shellFd = process.getFd();
    is = process.getIn();
    os = process.getOut();
  }

  @Override
  public void close() {
    mProcess.kill();
  }

  @Override
  public void connect() {
    mConnected = true;
  }

  @Override
  public void flush() throws IOException {
    os.flush();
  }

  @Override
  public boolean isConnected() {
    return mConnected && mProcess.isAlive();
  }

  @Override
  public boolean isSessionOpen() {
    return mProcess.isAlive();
  }

  @Override
  public int read(byte[] buffer, int start, int len) throws IOException {
    if (is == null) {
      mConnected = false;
      bridge.dispatchDisconnect(false);
      throw new IOException("session closed");
    }
    try {
      return is.read(buffer, start, len);
    } catch (IOException e) {
      mConnected = false;
      bridge.dispatchDisconnect(false);
      throw new IOException("session closed");
    }
  }

  @Override
  public void setDimensions(int columns, int rows, int width, int height) {
    try {
      Exec.setPtyWindowSize(shellFd, rows, columns, width, height);
    } catch (Exception e) {
      Log.e("Couldn't resize pty", e);
    }
  }

  @Override
  public void write(byte[] buffer) throws IOException {
    if (os != null) {
      os.write(buffer);
    }
  }

  @Override
  public void write(int c) throws IOException {
    if (os != null) {
      os.write(c);
    }
  }

}
