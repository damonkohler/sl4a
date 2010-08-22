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

package com.googlecode.android_scripting.language;

import com.googlecode.android_scripting.rpc.ParameterDescriptor;

import junit.framework.TestCase;

/**
 * Tests languages support.
 * 
 * @author igor.v.karp@gmail.com (Igor Karp)
 */
public class LanguageTest extends TestCase {

  private final Language beanShell = new BeanShellLanguage();
  private final Language javaScript = new JavaScriptLanguage();
  private final Language lua = new LuaLanguage();
  private final Language perl = new PerlLanguage();
  private final Language python = new PythonLanguage();
  private final Language ruby = new RubyLanguage();
  private final Language shell = new ShellLanguage();
  private final Language tcl = new TclLanguage();

  public void testContentTemplate() {
    checkContentTemplate(
        "source(\"/sdcard/com.googlecode.bshforandroid/extras/bsh/android.bsh\");\n\ndroid = Android();\n",
        beanShell);
    checkContentTemplate(
        "load(\"/sdcard/com.googlecode.rhinoforandroid/extras/rhino/android.js\");\n\nvar droid = Android();\n",
        javaScript);
    checkContentTemplate("require \"android\"\n\n", lua);
    checkContentTemplate("use Android;\n\nmy $droid = Android->new();\n", perl);
    checkContentTemplate("import android\n\ndroid = android.Android()\n", python);
    checkContentTemplate("require \"android\";\n\ndroid = Droid.new\n", ruby);
    checkContentTemplate("", shell);
    checkContentTemplate("package require android\n\nset droid [android new]\n", tcl);
  }

  public void testMethodCall() {
    ParameterDescriptor[] params =
        new ParameterDescriptor[] { new ParameterDescriptor("1", Integer.class),
          new ParameterDescriptor("abc", String.class),
          new ParameterDescriptor(null, String.class), new ParameterDescriptor(null, Object.class),
          new ParameterDescriptor("true", Boolean.class),
          new ParameterDescriptor("isComplete", Boolean.class), };
    checkMethodCall("droid.call(\"method\", 1, \"abc\", null, null, true, isComplete)", beanShell,
        params);
    checkMethodCall("droid.method(1, \"abc\", null, null, true, isComplete)", javaScript, params);
    checkMethodCall("android.method(1, \"abc\", null, null, true, isComplete)", lua, params);
    checkMethodCall("$droid->method(1, \"abc\", null, null, true, isComplete)", perl, params);
    checkMethodCall("droid.method(1, 'abc', None, None, True, isComplete)", python, params);
    checkMethodCall("droid.method(1, \"abc\", null, null, true, isComplete)", ruby, params);
    checkMethodCall("droid.method(1, \"abc\", null, null, true, isComplete)", shell, params);
    checkMethodCall("$droid method 1 \"abc\" null null true isComplete", tcl, params);
  }

  public void testAutoComplete() {
    checkAutoComplete(beanShell);
    checkAutoComplete(javaScript);
    checkAutoComplete(lua);
    checkAutoComplete(perl);
    checkAutoComplete(python);
    checkAutoComplete(ruby);
    checkAutoComplete(shell);
    checkAutoComplete(tcl);
  }

  private void checkContentTemplate(String expectedContent, Language language) {
    assertEquals(expectedContent, language.getContentTemplate());
  }

  private void checkMethodCall(String expectedContent, Language language,
      ParameterDescriptor... params) {
    assertEquals(expectedContent, language.getMethodCallText(language.getDefaultRpcReceiver(),
        "method", params));
  }

  private void checkAutoComplete(Language language) {
    checkAutoComplete(language, '[', "[]");
    checkAutoComplete(language, '{', "{}");
    checkAutoComplete(language, '(', "()");
    checkAutoComplete(language, '\'', "''");
    checkAutoComplete(language, '"', "\"\"");
    checkAutoComplete(language, ']', null);
    checkAutoComplete(language, '*', null);
  }

  private void checkAutoComplete(Language language, char token, String expected) {
    assertEquals(language.autoClose(token), expected);
  }
}
