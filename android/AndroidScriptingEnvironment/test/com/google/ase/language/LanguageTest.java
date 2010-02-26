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
    checkMethodCall("droid.method(1, b, 'c')", beanShell, "1", "b", "'c'");
    checkMethodCall("droid.method(1, b, 'c')", javaScript, "1", "b", "'c'");
    checkMethodCall("android.method(1, b, 'c')", lua, "1", "b", "'c'");
    checkMethodCall("$droid->method(1, b, 'c')", perl, "1", "b", "'c'");
    checkMethodCall("droid.method(1, b, 'c')", python, "1", "b", "'c'");
    checkMethodCall("droid.method(1, b, 'c')", ruby, "1", "b", "'c'");
    checkMethodCall("droid.method(1, b, 'c')", shell, "1", "b", "'c'");
    checkMethodCall("$droid method 1, b, 'c'", tcl, "1", "b", "'c'");
  }

  private void checkContentTemplate(String expectedContent, Language language) {
    assertEquals(expectedContent, language.getContentTemplate());
  }

  private void checkMethodCall(String expectedContent, Language language, String... params) {
    assertEquals(expectedContent, language.getMethodCallText(language.getDefaultRpcReceiver(),
        "method", params));
  }
}
