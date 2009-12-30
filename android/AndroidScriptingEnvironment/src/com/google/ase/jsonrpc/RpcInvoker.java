package com.google.ase.jsonrpc;

import java.lang.reflect.Method;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Implementations of this interface are used to parse the JSONArray arriving
 * from the client and invoking a particular RPC.
 * 
 * @author Felix Arends (felix.arends@gmail.com)
 * 
 */
public interface RpcInvoker {
  /**
   * Parses a {@code JSONArray} of parameters and invokes an RPC on the server
   * side.
   * 
   * @param m the method to invoke
   * @param receiver the object containing the method to invoke
   * @param parameters array of parameters as received by the client
   * @return the {@code JsonRpcResult} object with the appropriate result
   */
  public JSONObject invoke(Method m, Object receiver, JSONArray parameters) throws JSONException;
}
