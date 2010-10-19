package com.googlecode.android_scripting.facade;

import java.io.OutputStream;
import java.net.Socket;

import com.googlecode.android_scripting.SimpleServer;

class JpegServer extends SimpleServer {

  private final JpegProvider mProvider;

  public JpegServer(JpegProvider provider) {
    super();
    mProvider = provider;
  }

  @Override
  protected void handleConnection(Socket socket) throws Exception {
    byte[] data = mProvider.getJpeg();
    if (data == null) {
      return;
    }
    OutputStream outputStream = socket.getOutputStream();
    outputStream.write((
        "HTTP/1.0 200 OK\r\n" +
        "Server: SL4A\r\n" +
        "Connection: close\r\n" +
        "Max-Age: 0\r\n" +
        "Expires: 0\r\n" +
        "Cache-Control: no-cache, private\r\n" + 
        "Pragma: no-cache\r\n" + 
        "Content-Type: multipart/x-mixed-replace; boundary=--BoundaryString\r\n\r\n").getBytes());
    while (true) {
      data = mProvider.getJpeg();
      if (data == null) {
        return;
      }
      outputStream.write("--BoundaryString\r\n".getBytes());
      outputStream.write("Content-type: image/jpg\r\n".getBytes());
      outputStream.write(("Content-Length: " + data.length + "\r\n\r\n").getBytes());
      outputStream.write(data);
      outputStream.write("\r\n\r\n".getBytes());
      outputStream.flush();
    }
  }
}