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

import android.webkit.WebView;

import com.googlecode.android_scripting.facade.EventFacade;
import com.googlecode.android_scripting.future.FutureActivityTask;

public class WebViewTask extends FutureActivityTask<Void> {

  private WebView mView;
  private JavaScriptWrapper mWrapper;
  private final String mSource;

  public WebViewTask(String source, EventFacade facade) {
    mSource = source;
    mWrapper = new JavaScriptWrapper(facade);
  }

  @Override
  public void onCreate() {
    mView = new WebView(getActivity());
    mView.getSettings().setJavaScriptEnabled(true);
    mView.addJavascriptInterface(mWrapper, "droid_events");
    getActivity().setContentView(mView);
    mView.loadUrl(mSource);
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
