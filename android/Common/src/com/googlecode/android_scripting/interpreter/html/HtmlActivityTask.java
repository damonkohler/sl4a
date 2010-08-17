// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.interpreter.html;

import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.JsonRpcResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HtmlActivityTask extends FutureActivityTask<Void> {

  private static final String PREFIX = "file://";

  private final Map<String, MethodDescriptor> mKnownRpcs = new HashMap<String, MethodDescriptor>();
  private final RpcReceiverManager mReceiverManager;
  private final String mJsonSource;
  private final String mSource;
  private final JavaScriptWrapper mWrapper;
  private WebView mView;

  public HtmlActivityTask(RpcReceiverManager manager, String jsonSource, String file) {
    mReceiverManager = manager;
    mJsonSource = jsonSource;
    mSource = PREFIX + file;
    mWrapper = new JavaScriptWrapper();
    for (Class<? extends RpcReceiver> receiverClass : manager.getRpcReceiverClasses()) {
      Collection<MethodDescriptor> methodList = MethodDescriptor.collectFrom(receiverClass);
      for (MethodDescriptor m : methodList) {
        if (mKnownRpcs.containsKey(m.getName())) {
          // We already know an RPC of the same name. We don't catch this anywhere because this is a
          // programming error.
          throw new RuntimeException("An RPC with the name " + m.getName() + " is already known.");
        }
        mKnownRpcs.put(m.getName(), m);
      }
    }
  }

  @Override
  public void onCreate() {
    mView = new WebView(getActivity());
    mView.getSettings().setJavaScriptEnabled(true);

    mView.setWebViewClient(new WebViewClient() {
      @Override
      public void onPageFinished(WebView view, String url) {
        mView.loadUrl("javascript:" + mJsonSource);
        mView
            .loadUrl("javascript:function Android(){ this.id = 0, "
                + "this.call = function(){"
                + "var method = arguments[0]; var args = [];for (var i=1; i<arguments.length; i++){args[i-1]=arguments[i];}"
                + "var request = JSON.stringify({'id': this.id, 'method': method,'params': args});"
                + "var response = droid_rpc.call(request);"
                + "return eval(\"(\" + response + \")\");" + "}}");
      }
    });
    mView.addJavascriptInterface(mWrapper, "droid_rpc");
    getActivity().setContentView(mView);
    mView.loadUrl(mSource);
  }

  private class JavaScriptWrapper {

    @SuppressWarnings("unused")
    public String call(String data) throws JSONException {
      JSONObject request = new JSONObject(data);
      int id = request.getInt("id");
      String method = request.getString("method");
      JSONArray params = request.getJSONArray("params");
      MethodDescriptor rpc = mKnownRpcs.get(method);
      if (rpc == null) {
        return JsonRpcResult.error(id, new RpcError("Unknown RPC.")).toString();
      }
      try {
        return JsonRpcResult.result(id, rpc.invoke(mReceiverManager, params)).toString();
      } catch (Throwable t) {
        Log.e("Invocation error.", t);
        return JsonRpcResult.error(id, t).toString();
      }
    }
  }
}
