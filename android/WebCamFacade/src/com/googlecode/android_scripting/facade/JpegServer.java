package com.googlecode.android_scripting.facade;

import java.net.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;

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
    DefaultHttpServerConnection connection = new DefaultHttpServerConnection();
    connection.bind(socket, new BasicHttpParams());
    connection.receiveRequestHeader();
    HttpResponse response = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    // TODO(damonkohler): Add caching header.
    response.addHeader("Content-Type", "image/jpg");
    response.addHeader("Content-Length", "" + data.length);
    response.setEntity(new ByteArrayEntity(data));
    connection.sendResponseHeader(response);
    connection.sendResponseEntity(response);
    connection.flush();
  }
}