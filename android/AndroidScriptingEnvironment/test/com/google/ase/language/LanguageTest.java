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
package com.google.ase.language;

import com.google.ase.jsonrpc.ParameterDescriptor;

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
        "source(\"/sdcard/ase/extras/bsh/android.bsh\");\n\ndroid = Android();\n", beanShell);
    checkContentTemplate(
        "load(\"/sdcard/ase/extras/rhino/android.js\");\n\nvar droid = Android();\n", javaScript);
    checkContentTemplate("require \"android\"\n\n", lua);
    checkContentTemplate("use Android;\n\nmy $droid = Android()->new();\n", perl);
    checkContentTemplate("import android\n\ndroid = android.Android()\n", python);
    checkContentTemplate("require \"android\";\n\ndroid = Droid.new\n", ruby);
    checkContentTemplate("", shell);
    checkContentTemplate("package require android\n\nset droid [android new]\n", tcl);
  }

  public void testMethodCall() {
    ParameterDescriptor[] params = new ParameterDescriptor[] {
      new ParameterDescriptor("1", Integer.class),
      new ParameterDescriptor("abc", String.class),
      new ParameterDescriptor(null, Object.class),
    };
    checkMethodCall("droid.method(1, \"abc\", null)", beanShell, params);
    checkMethodCall("droid.method(1, \"abc\", null)", javaScript, params);
    checkMethodCall("android.method(1, \"abc\", null)", lua, params);
    checkMethodCall("$droid->method(1, \"abc\", null)", perl, params);
    checkMethodCall("droid.method(1, 'abc', null)", python, params);
    checkMethodCall("droid.method(1, \"abc\", null)", ruby, params);
    checkMethodCall("droid.method(1, \"abc\", null)", shell, params);
    checkMethodCall("$droid method 1 \"abc\" null", tcl, params);
  }

  private void checkContentTemplate(String expectedContent, Language language) {
    assertEquals(expectedContent, language.getContentTemplate());
  }

  private void checkMethodCall(String expectedContent, Language language,
      ParameterDescriptor... params) {
    assertEquals(expectedContent, language.getMethodCallText(language.getDefaultRpcReceiver(),
        "method", params));
  }
}
