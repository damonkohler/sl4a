package com.google.ase.jsonrpc;

import junit.framework.TestCase;

import org.json.JSONArray;
import org.json.JSONException;

import com.google.ase.rpc.RpcError;

public class RpcInfoTest extends TestCase {

  public void testConvertParameter() throws JSONException, RpcError {
    Class<?>[] types =
        new Class[] { Object.class, Boolean.class, Boolean.class, Boolean.class, Integer.class,
          Double.class, String.class };
    JSONArray parameters = new JSONArray();
    parameters.put(null);
    parameters.put(true);
    parameters.put(0);
    parameters.put(1);
    parameters.put(42);
    parameters.put(3.14159);
    parameters.put("Hello, world!");
    // TODO(damonkohler): Test converting JSONArrays into Lists.
    Object[] args = new Object[parameters.length()];
    for (int i = 0; i < parameters.length(); i++) {
      args[i] = RpcInfo.convertParameter(parameters, i, types[i]);
    }
    assertNull(args[0]);
    assertEquals(Boolean.TRUE, args[1]);
    assertEquals(Boolean.FALSE, args[2]);
    assertEquals(Boolean.TRUE, args[3]);
    assertEquals(42, args[4]);
    assertEquals(3.14159, args[5]);
    assertEquals("Hello, world!", args[6]);
  }
}
