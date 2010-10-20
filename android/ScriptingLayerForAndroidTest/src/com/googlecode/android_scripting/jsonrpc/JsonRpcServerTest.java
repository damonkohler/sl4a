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

package com.googlecode.android_scripting.jsonrpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JsonRpcServerTest extends TestCase {

  private String buildRequest(int id, String method, List<String> arguments) throws JSONException {
    JSONObject request = new JSONObject();
    request.put("id", id);
    request.put("method", method);
    request.put("params", new JSONArray(arguments));
    return request.toString();
  }

  public void testValidHandshake() throws IOException, JSONException {
    JsonRpcServer server = new JsonRpcServer(null, "foo");
    InetSocketAddress address = server.startLocal(0);
    Socket client = new Socket();
    client.connect(address);
    PrintStream out = new PrintStream(client.getOutputStream());
    out.println(buildRequest(0, "_authenticate", Lists.newArrayList("foo")));
    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    JSONObject response = new JSONObject(in.readLine());
    Object error = response.get("error");
    assertEquals(JSONObject.NULL, error);
    client.close();
    server.shutdown();
  }

  public void testInvalidHandshake() throws IOException, JSONException, InterruptedException {
    JsonRpcServer server = new JsonRpcServer(null, "foo");
    InetSocketAddress address = server.startLocal(0);
    Socket client = new Socket();
    client.connect(address);
    PrintStream out = new PrintStream(client.getOutputStream());
    out.println(buildRequest(0, "_authenticate", Lists.newArrayList("bar")));
    BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
    JSONObject response = new JSONObject(in.readLine());
    Object error = response.get("error");
    assertTrue(JSONObject.NULL != error);
    while (!out.checkError()) {
      out.println(buildRequest(0, "makeToast", Lists.newArrayList("baz")));
    }
    client.close();
    // Further connections should fail;
    client = new Socket();
    try {
      client.connect(address);
      fail();
    } catch (IOException e) {
    }
  }
}
