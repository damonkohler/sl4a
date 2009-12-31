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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.ase.RpcFacade;

/**
 * A JSON RPC server that forwards RPC calls to a specified receiver object.
 * 
 * @author Damon Kohler (damonkohler@gmail.com)
 */
public class JsonRpcServer {

  /**
   * Instances of this class describe specific RPCs on the server.
   * An RPC on the server is described by a triple consisting of:
   * - a receiving object of the call
   * - the method of the object to call
   * - an {@link RpcInvoker} object that knows to parse a {@link JSONArray}
   *   for the parameters
   * 
   * @author Felix Arends (felix.arends@gmail.com)
   * 
   */
  private static class RpcInfo {
    private final Object mReceiver;
    private final Method mMethod;
    private final RpcInvoker mInvoker;

    public RpcInfo(final Object receiver, final Method method, final RpcInvoker invoker) {
      this.mReceiver = receiver;
      this.mMethod = method;
      this.mInvoker = invoker;
    }

    /**
     * Invokes the call that belongs to this object with the given parameters.
     * Wraps the response (possibly an exception) in a JSONObject.
     * 
     * @param parameters {@code JSONArray} containing the parameters
     * @return RPC response
     */
    public JSONObject invoke(final JSONArray parameters) {
      try {
        return mInvoker.invoke(mMethod, mReceiver, parameters);
      } catch (JSONException e) {
        return JsonRpcResult.error("Remote Exception", e);
      }
    }

    /**
     * Appends the name of the given type to the {@link StringBuilder}.
     * 
     * @param builder string builder to append to
     * @param type type whose name to append
     */
    private void appendTypeName(final StringBuilder builder, final Type type) {
      if (type instanceof Class<?>) {
        builder.append(((Class <?>)type).getSimpleName());
      } else {
        ParameterizedType parametrizedType = (ParameterizedType) type;
        builder.append(((Class<?>) parametrizedType.getRawType()).getSimpleName());
        builder.append("<");

        Type[] arguments = parametrizedType.getActualTypeArguments();
        for (int i = 0; i < arguments.length; i++) {
          if (i > 0) {
            builder.append(", ");
          }
          appendTypeName(builder, arguments[i]);
        }
        builder.append(">");
      }
    }

    /**
     * Returns the help string for one particular parameter.  This respects
     * parameters of type {@code OptionalParameter<T>}. 
     * 
     * @param parameterType (generic) type of the parameter
     * @param annotation {@link RpcParameter} annotation of the type, may be null
     * @return string describing the parameter based on source code annotaitons
     */
    private String getHelpForParameter(Type parameterType, Annotation[] annotations) {
      StringBuilder result = new StringBuilder();

      Object defaultValue = RpcAnnotationHelper.getDefaultValue(annotations);
      String description = RpcAnnotationHelper.getRPCDescription(annotations);
      boolean isOptionalParameter = RpcAnnotationHelper.isOptionalParameter(annotations);

      appendTypeName(result, parameterType);
      if (isOptionalParameter) {
        result.append("[optional, default " + defaultValue + "]: ");
      } else {
        result.append(":");
      }

      result.append(description);

      return result.toString();
    }

    /**
     * Returns a human-readable help text for this RPC, based on annotations in
     * the source code.
     * 
     * @return derived help string
     */
    public String getHelp() {
      final StringBuilder helpBuilder = new StringBuilder();

      final Rpc rpcAnnotation = mMethod.getAnnotation(Rpc.class);

      helpBuilder.append(mMethod.getName());
      helpBuilder.append("(");
      final Class<?>[] parameterTypes = mMethod.getParameterTypes();
      final Type[] genericParameterTypes = mMethod.getGenericParameterTypes();
      final Annotation[][] annotations = mMethod.getParameterAnnotations();
      for (int i = 0; i < parameterTypes.length; i++) {
        if (i == 0) {
          helpBuilder.append("\n  ");
        } else {
          helpBuilder.append(",\n  ");
        }

        helpBuilder.append(getHelpForParameter(genericParameterTypes[i], annotations[i]));
      }
      helpBuilder.append("):\n");
      helpBuilder.append(rpcAnnotation.description());
      helpBuilder.append("\n");
      if (rpcAnnotation.returns() != "") {
        helpBuilder.append("returns: " + rpcAnnotation.returns());
      }

      return helpBuilder.toString();
    }
  }

  static final String TAG = "JsonRpcServer";
  private ServerSocket mServer;

  /**
   * A map of strings to known RPCs.
   */
  private final Map<String, RpcInfo> mKnownRpcs = new ConcurrentHashMap<String, RpcInfo>();
  
  /**
   * The list of RPC receiving objects.
   */
  private final List<RpcFacade> mReceivers = new ArrayList<RpcFacade>();

  /**
   * The network thread that receives RPCs. 
   */
  private Thread mServerThread;

  /**
   * The set of active threads spawned for each client connection.
   */
  private final CopyOnWriteArraySet<Thread> mNetworkThreads = new CopyOnWriteArraySet<Thread>();
  
  private JsonRpcServer() { }

  /**
   * Builds a JSON RPC server which forwards RPC calls to the receiving objects.
   */
  public static JsonRpcServer create(final RpcFacade... receivers) {
    JsonRpcServer result = new JsonRpcServer();
    for (RpcFacade receiver : receivers) {
      result.registerRpcReceiver(receiver);
    }
    return result;
  }
  
  /**
   * Registers an RPC receiving object with this {@link JsonRpcServer} object.
   * @param receiver the receiving object
   */
  private void registerRpcReceiver(final RpcFacade receiver) {
    final Class<?> clazz = receiver.getClass();
    for (Method m : clazz.getMethods()) {
      if (m.getAnnotation(Rpc.class) != null) {
        if (mKnownRpcs.containsKey(m.getName())) {
          // We already know an RPC of the same name.
          throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
        }
        mKnownRpcs.put(m.getName(), new RpcInfo(receiver, m, RpcInvokerFactory
            .createInvoker(m.getGenericParameterTypes())));
      }
    }
    mReceivers.add(receiver);
  }

  private InetAddress getPublicInetAddress() throws UnknownHostException, SocketException {
    Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
    for (NetworkInterface netint : Collections.list(nets)) {
      Enumeration<InetAddress> addresses = netint.getInetAddresses();
      for (InetAddress address : Collections.list(addresses)) {
        if (!address.getHostAddress().equals("127.0.0.1")) {
          return address;
        }
      }
    }
    return InetAddress.getLocalHost();
  }

  /**
   * Starts the RPC server bound to the localhost address.
   * 
   * @return the port that was allocated by the OS
   */
  public InetSocketAddress startLocal() {
    InetAddress address;
    try {
      address = InetAddress.getLocalHost();
      mServer = new ServerSocket(0 /* port */, 5 /* backlog */, address);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start server.", e);
      return null;
    }
    int port = start(address);
    return new InetSocketAddress(address, port);
  }

  /**
   * Starts the RPC server bound to the public facing address.
   * 
   * @return the port that was allocated by the OS
   */
  public InetSocketAddress startPublic() {
    InetAddress address;
    try {
      address = getPublicInetAddress();
      mServer = new ServerSocket(0 /* port */, 5 /* backlog */, address);
    } catch (Exception e) {
      Log.e(TAG, "Failed to start server.", e);
      return null;
    }
    int port = start(address);
    return new InetSocketAddress(address, port);
  }
  
  /**
   * Shuts down the RPC server.
   */
  public void shutdown() {
    // Interrupt the server thread to ensure that beyond this point there are
    // no incoming requests.
    mServerThread.interrupt();
    
    // Since the server thread is not running, the mNetworkThreads set can only
    // shrink from this point onward.  We can just cancel all of the running
    // threads.  In the worst case, one of the running threads will already have
    // shut down.  Since this is a CopyOnWriteSet, we don't have to worry about
    // concurrency issues while iterating over the set of threads.
    for (Thread networkThread : mNetworkThreads) {
      networkThread.interrupt();
    }

    // Notify all RPC receiving objects.  They may have to clean up some of
    // their state.
    for (RpcFacade receiver : mReceivers) {
      receiver.shutdown();
    }
  }

  private int start(InetAddress address) {
    mServerThread = new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            Socket sock = mServer.accept();
            Log.v(TAG, "Connected!");
            startConnectionThread(sock);
          } catch (IOException e) {
            Log.e(TAG, "Failed to accept connection.", e);
          }
        }
      }
    };
    
    mServerThread.start();

    Log.v(TAG, "Bound to " + address.getHostAddress() + ":" + mServer.getLocalPort());
    return mServer.getLocalPort();
  }

  private void startConnectionThread(final Socket sock) {
    final Thread networkThread = new Thread() {
      @Override
      public void run() {
        try {
          BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
          PrintWriter out = new PrintWriter(sock.getOutputStream(), true);
          String data;
          while ((data = in.readLine()) != null) {
            Log.v(TAG, "Received: " + data.toString());
            JSONObject result = call(data);
            out.write(result.toString() + "\n");
            out.flush();
          }
        } catch (Exception e) {
          Log.e(TAG, "Communication with client failed.", e);
        } finally {
          mNetworkThreads.remove(this);
        }
      }
    };

    mNetworkThreads.add(networkThread);
    networkThread.start();
  }

  private JSONObject call(String json) throws JSONException, NoSuchMethodException,
      IllegalAccessException, InvocationTargetException {
    JSONObject jsonRequest = new JSONObject(json);
    // The JSON RPC spec says that id can be any object. To make our lives a
    // little easier, we'll assume it's always a number.
    int id = jsonRequest.getInt("id");
    String methodName = jsonRequest.getString("method");
    JSONArray params = jsonRequest.getJSONArray("params");
    if (methodName.equals("_help")) {
      return help(id, params);
    }
    return dispatch(id, methodName, params);
  }

  private JSONObject help(int id, JSONArray params) throws JSONException {
    JSONObject result = JsonRpcResult.empty();
    result.put("id", id);
    JSONArray methods = new JSONArray();
    result.put("result", methods);

    try {
      String methodName = params.optString(0);
      if (!methodName.equals("")) {
        // Lookup help for one specific method.
        final RpcInfo rpcInfo = mKnownRpcs.get(methodName);
        if (rpcInfo == null) { // Method not found.
          methods.put("Unknown Function.");
        } else {
          methods.put(rpcInfo.getHelp());
        }
      } else {
        // Lookup help for all available RPC methods.
        for (RpcInfo rpcInfo : mKnownRpcs.values()) {
          methods.put(rpcInfo.getHelp() + "\n");
        }
      }
    } catch (Exception e) {
      result = JsonRpcResult.error("RPC Error", e);
    }

    return result;
  }

  private JSONObject dispatch(final int id, final String methodName, final JSONArray params)
      throws JSONException {
    JSONObject result = null;
    final RpcInfo rpcInfo = mKnownRpcs.get(methodName);

    if (rpcInfo == null) {
      result = JsonRpcResult.error("Unknown RPC.");
    } else {
      result = rpcInfo.invoke(params);
    }

    result.put("id", id);
    Log.v(TAG, "Sending reply " + result.toString());
    return result;
  }
}
