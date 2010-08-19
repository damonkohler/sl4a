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

package com.googlecode.android_scripting.facade.ui;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.webkit.WebView;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.future.FutureActivityTask;

public class WebViewTask extends FutureActivityTask<Void> {

  private WebView mView;
  private JavaScriptWrapper mWrapper;
  private final String mSource;
  private final UiFacade mUiFacade;

  public WebViewTask(String source, UiFacade uiFacade, EventFacade eventFacade) {
    mSource = source;
    mWrapper = new JavaScriptWrapper(eventFacade);
    mUiFacade = uiFacade;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mView = new WebView(getActivity());
    mView.getSettings().setJavaScriptEnabled(true);
    mView.addJavascriptInterface(mWrapper, "droid_events");
    getActivity().setContentView(mView);
    mView.loadUrl(mSource);
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
    private final EventFacade mmEventFacade;

    private JavaScriptWrapper(EventFacade facade) {
      mmEventFacade = facade;
    }

    @SuppressWarnings("unused")
    public void post(String event) {
      mmEventFacade.postEvent(event, null);
    }
  }
}
