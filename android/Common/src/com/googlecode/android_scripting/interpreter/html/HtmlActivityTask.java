// Copyright 2010 Google Inc. All Rights Reserved.

package com.googlecode.android_scripting.interpreter.html;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.JsPromptResult;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import com.googlecode.android_scripting.FileUtils;
import com.googlecode.android_scripting.Log;
import com.googlecode.android_scripting.SingleThreadExecutor;
import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.facade.ui.UiFacade;
import com.googlecode.android_scripting.future.FutureActivityTask;
import com.googlecode.android_scripting.interpreter.InterpreterConstants;
import com.googlecode.android_scripting.jsonrpc.JsonBuilder;
import com.googlecode.android_scripting.jsonrpc.JsonRpcResult;
import com.googlecode.android_scripting.jsonrpc.RpcReceiver;
import com.googlecode.android_scripting.jsonrpc.RpcReceiverManager;
import com.googlecode.android_scripting.rpc.MethodDescriptor;
import com.googlecode.android_scripting.rpc.RpcError;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HtmlActivityTask extends FutureActivityTask<Void> {

  private static final String ANDROID_PROTOTYPE_JS =
      "Android.prototype.%1$s=function(){return this._call(\"%1$s\", arguments)};";

  private static final String PREFIX = "file://";
  private static final String BASE_URL = PREFIX + InterpreterConstants.SCRIPTS_ROOT;

  private final RpcReceiverManager mReceiverManager;
  private final String mJsonSource;
  private final String mAndroidJsSource;
  private final String mSource;
  private final String mBaseUrl;
  private final JavaScriptWrapper mWrapper;
  private final HtmlEventObserver mObserver;
  private final UiFacade mUiFacade;
  private ChromeClient mChromeClient;
  private WebView mView;

  public HtmlActivityTask(RpcReceiverManager manager, String androidJsSource, String jsonSource,
      String file) {
    mReceiverManager = manager;
    mJsonSource = jsonSource;
    mAndroidJsSource = androidJsSource;
    mBaseUrl = PREFIX + file;
    mWrapper = new JavaScriptWrapper();
    mObserver = new HtmlEventObserver();
    mReceiverManager.getReceiver(EventFacade.class).addEventObserver(mObserver);
    mUiFacade = mReceiverManager.getReceiver(UiFacade.class);
    String source = null;
    try {
      source = FileUtils.readFile(Uri.parse(file).getPath());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    mSource = source;
  }

  @Override
  public void onCreate() {
    mView = new WebView(getActivity());
    mView.setId(1);
    mView.getSettings().setJavaScriptEnabled(true);
    mView.addJavascriptInterface(mWrapper, "_rpc_wrapper");
    mView.addJavascriptInterface(new Object() {
      @SuppressWarnings("unused")
      public void register(String event, int id) {
        mObserver.register(event, id);
      }
    }, "_callback_wrapper");
    getActivity().setContentView(mView);
    mView.setOnCreateContextMenuListener(getActivity());
    mChromeClient = new ChromeClient(getActivity());
    mView.setWebChromeClient(mChromeClient);
    mView.loadUrl("javascript:" + mJsonSource);
    mView.loadUrl("javascript:" + mAndroidJsSource);
    mView.loadUrl("javascript:" + generateAPIWrapper());

    mView.loadDataWithBaseURL(BASE_URL, mSource, "text/html", "utf-8", null);
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

  private String generateAPIWrapper() {
    StringBuilder wrapper = new StringBuilder();
    for (Class<? extends RpcReceiver> clazz : mReceiverManager.getRpcReceiverClasses()) {
      for (MethodDescriptor rpc : MethodDescriptor.collectFrom(clazz)) {
        wrapper.append(String.format(ANDROID_PROTOTYPE_JS, rpc.getName()));
      }
    }
    return wrapper.toString();
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
      JSONObject json = new JSONObject();
      try {
        json.put("data", JsonBuilder.build(data));
      } catch (JSONException e) {
        Log.e(e);
      }
      if (mEventMap.containsKey(eventName)) {
        for (Integer id : mEventMap.get(eventName)) {
          mView.loadUrl(String.format("javascript:droid._callback(%d, %s);", id, json));
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
