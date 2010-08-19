// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.interpreter.html;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.SingleThreadExecutor;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.ui.UiFacade;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.jsonrpc.JsonBuilder;
import com.googlecode.android_scripting.jsonrpc.JsonRpcResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class HtmlActivityTask extends FutureActivityTask<Void> {

  private static final String PREFIX = "file://";
  // TODO(raaar): put in a file.
  private static final String ANDROID_JS =
      "javascript:function Android(){ this.callbacks = [], this.id = 0, "
          + "this.call = function(){"
          + "this.id += 1;"
          + "var method = arguments[0]; var args = [];for (var i=1; i<arguments.length; i++){args[i-1]=arguments[i];}"
          + "var request = JSON.stringify({'id': this.id, 'method': method,'params': args});"
          + "var response = droid_rpc.call(request); return eval(\"(\" + response + \")\");},"
          + "this.register = function(event, receiver){"
          + "var id = this.callbacks.push(receiver)-1; droid_callback.register(event, id);},"
          + "this.callback = function(id, data){var receiver = this.callbacks[id];"
          + "receiver(data);}}; var droid = new Android();";

  private final RpcReceiverManager mReceiverManager;
  private final String mJsonSource;
  private final String mSource;
  private final JavaScriptWrapper mWrapper;
  private final HtmlEventObserver mObserver;
  private final UiFacade mUiFacade;
  private ChromeClient mChromeClient;
  private WebView mView;

  public HtmlActivityTask(RpcReceiverManager manager, String jsonSource, String file) {
    mReceiverManager = manager;
    mJsonSource = jsonSource;
    mSource = PREFIX + file;
    mWrapper = new JavaScriptWrapper();
    mObserver = new HtmlEventObserver();
    mReceiverManager.getReceiver(EventFacade.class).addEventObserver(mObserver);
    mUiFacade = mReceiverManager.getReceiver(UiFacade.class);
  }

  @Override
  public void onCreate() {
    mView = new WebView(getActivity());
    mView.setId(1);
    mView.getSettings().setJavaScriptEnabled(true);
    mView.addJavascriptInterface(mWrapper, "droid_rpc");
    mView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void register(String event, int id) {
        mObserver.register(event, id);
      }
    }, "droid_callback");
    getActivity().setContentView(mView);
    mView.setOnCreateContextMenuListener(getActivity());
    mChromeClient = new ChromeClient(getActivity());
    mView.setWebChromeClient(mChromeClient);
    mView.loadUrl("javascript:" + mJsonSource);
    mView.loadUrl(ANDROID_JS);
    mView.loadUrl(mSource);
  }

  @Override
  public void onDestroy() {
    mReceiverManager.getReceiver(EventFacade.class).removeEventObserver(mObserver);
    mReceiverManager.shutdown();
    mView.destroy();
    mView = null;
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    mUiFacade.onCreateContextMenu(menu, v, menuInfo);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    return mUiFacade.onPrepareOptionsMenu(menu);
  }

  private class JavaScriptWrapper {
    @SuppressWarnings("unused")
    public String call(String data) throws JSONException {
      Log.v("Received: " + data);
      JSONObject request = new JSONObject(data);
      int id = request.getInt("id");
      String method = request.getString("method");
      JSONArray params = request.getJSONArray("params");
      MethodDescriptor rpc = mReceiverManager.getMethodDescriptor(method);
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

  private class HtmlEventObserver implements EventFacade.EventObserver {
    private Map<String, Set<Integer>> mEventMap = new HashMap<String, Set<Integer>>();

    public void register(String eventName, Integer id) {
      if (mEventMap.containsKey(eventName)) {
        mEventMap.get(eventName).add(id);
      } else {
        Set<Integer> idSet = new HashSet<Integer>();
        idSet.add(id);
        mEventMap.put(eventName, idSet);
      }
    }

    @Override
    public void onEventReceived(String eventName, Object data) {
      String dataString = null;
      try {
        dataString = JsonBuilder.build(data).toString();
      } catch (JSONException e) {
        Log.e(e);
      }
      if (mEventMap.containsKey(eventName)) {
        for (Integer id : mEventMap.get(eventName)) {
          mView.loadUrl(String.format("javascript:droid.callback(%d, '%s');", id, dataString));
        }
      }
    }
  }

  private class ChromeClient extends WebChromeClient {
    private final static String JS_TITLE = "JavaScript Dialog";

    private final Activity mActivity;
    private final Resources mResources;
    private final ExecutorService mmExecutor;

    public ChromeClient(Activity activity) {
      mActivity = activity;
      mResources = mActivity.getResources();
      mmExecutor = new SingleThreadExecutor();
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
      mActivity.setTitle(title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
      mActivity.getWindow().requestFeature(Window.FEATURE_RIGHT_ICON);
      mActivity.getWindow().setFeatureDrawable(Window.FEATURE_RIGHT_ICON, new BitmapDrawable(icon));
    }

    @Override
    public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
      final UiFacade uiFacade = mReceiverManager.getReceiver(UiFacade.class);
      uiFacade.dialogCreateAlert(JS_TITLE, message);
      uiFacade.dialogSetPositiveButtonText(mResources.getString(android.R.string.ok));

      mmExecutor.execute(new Runnable() {

        @Override
        public void run() {
          try {
            uiFacade.dialogShow();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          uiFacade.dialogGetResponse();
          result.confirm();
        }
      });
      return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
      final UiFacade uiFacade = mReceiverManager.getReceiver(UiFacade.class);
      uiFacade.dialogCreateAlert(JS_TITLE, message);
      uiFacade.dialogSetPositiveButtonText(mResources.getString(android.R.string.ok));
      uiFacade.dialogSetNegativeButtonText(mResources.getString(android.R.string.cancel));

      mmExecutor.execute(new Runnable() {

        @Override
        public void run() {
          try {
            uiFacade.dialogShow();
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          Map<String, Object> mResultMap = (Map<String, Object>) uiFacade.dialogGetResponse();
          if ("positive".equals(mResultMap.get("which"))) {
            result.confirm();
          } else {
            result.cancel();
          }
        }
      });

      return true;
    }

    @Override
    public boolean onJsPrompt(WebView view, String url, final String message,
        final String defaultValue, final JsPromptResult result) {
      final UiFacade uiFacade = mReceiverManager.getReceiver(UiFacade.class);

      mmExecutor.execute(new Runnable() {

        @Override
        public void run() {
          String value = null;
          try {
            value = uiFacade.dialogGetInput(JS_TITLE, message, defaultValue);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
          if (value != null) {
            result.confirm(value);
          } else {
            result.cancel();
          }
        }
      });

      return true;
    }

  }
}
