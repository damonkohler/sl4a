/*
 * Copyright (C) 2009 Google Inc.
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

package com.google.ase.jsonrpc;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.ase.AseLog;

/**
 * Represents a JSON RPC result.
 * 
 * @see http://json-rpc.org/wiki/specification
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcResult {
  private Object result;
  private Object error;

  // ID is left out because the current implementation of the server assumes
  // blocking methods and sets the ID automatically.

  /**
   * Sets the result object. Object must be marshalable to JSON.
   * 
   * @see http://www.json.org/javadoc/org/json/JSONObject.html
   */
  public void setResult(Object result) {
    this.result = result;
  }

  /**
   * Sets the error object. Object must be marshalable to JSON.
   * 
   * @see http://www.json.org/javadoc/org/json/JSONObject.html
   */
  public void setError(Object error) {
    this.error = error;
  }

  /**
   * Converts the result to a {@link JSONObject}.
   */
  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    try {
      json.put("result", result == null ? JSONObject.NULL : result);
      json.put("error", error == null ? JSONObject.NULL : error);
    } catch (JSONException e) {
      AseLog.e("Failed to build JSON result object.", e);
    }
    return json;
  }

  /**
   * Returns the result object.
   */
  public Object getResult() {
    return result;
  }

  public static JSONObject empty() {
    return (new JsonRpcResult()).toJson();
  }

  public static JSONObject result(Object result) {
    JsonRpcResult rpcResult = new JsonRpcResult();
    rpcResult.setResult(result);
    return rpcResult.toJson();
  }

  public static JSONObject error(String message) {
    return error(message, null);
  }

  public static JSONObject error(String message, Throwable e) {
    JsonRpcResult rpcResult = new JsonRpcResult();
    rpcResult.setError(message);
    return rpcResult.toJson();
  }
}
