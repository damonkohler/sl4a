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

package com.googlecode.android_scripting.interpreter.html;

import android.content.Context;
import android.content.res.AssetManager;

import com.googlecode.android_scripting.interpreter.Interpreter;
import com.googlecode.android_scripting.language.HtmlLanguage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HtmlInterpreter extends Interpreter {

  public static final String HTML = "html";
  public static final String HTML_EXTENSION = ".html";

  private static final String JSON_FILE = "json2.js";
  private static final String ANDROID_JS_FILE = "android.js";
  private static final String NICE_NAME = "HTML";

  private final String mJson;
  private final String mAndroidJs;

  public HtmlInterpreter(Context context) throws IOException {
    setExtension(HTML_EXTENSION);
    setName(HTML);
    setNiceName(NICE_NAME);
    setInteractiveCommand("");
    setScriptCommand("%s");
    setLanguage(new HtmlLanguage());
    setHasInteractiveMode(false);
    mJson = readFromAssetsFile(context, JSON_FILE);
    mAndroidJs = readFromAssetsFile(context, ANDROID_JS_FILE);
  }

  private String readFromAssetsFile(Context context, String name) throws IOException {
    AssetManager am = context.getAssets();
    BufferedReader reader = new BufferedReader(new InputStreamReader(am.open(name)));
    String line;
    StringBuilder builder = new StringBuilder();
    while ((line = reader.readLine()) != null) {
      builder.append(line);
    }
    reader.close();
    return builder.toString();
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public boolean hasExtrasArchive() {
    return false;
  }

  public boolean hasScriptsArchive() {
    return false;
  }

  public int getVersion() {
    return 0;
  }

  @Override
  public boolean isUninstallable() {
    return false;
  }

  @Override
  public boolean isInstalled() {
    return true;
  }

  public String getJsonSource() {
    return mJson;
  }

  public String getAndroidJsSource() {
    return mAndroidJs;
  }
}
