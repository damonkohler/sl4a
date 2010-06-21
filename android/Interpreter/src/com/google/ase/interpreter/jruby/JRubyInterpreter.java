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

package com.google.ase.interpreter.jruby;

import com.google.ase.interpreter.DefaultInterpreter;
import com.google.ase.interpreter.InterpreterProcess;
import com.google.ase.language.RubyLanguage;

public class JRubyInterpreter extends DefaultInterpreter {

  private final static String JRUBY_BIN =
      "dalvikvm -Xss128k "
          + "-classpath /sdcard/ase/extras/jruby/jruby-complete-1.4.jar org.jruby.Main -X-C "
          +
          // Fix include path.
          "-e \"\\$LOAD_PATH.push('file:/sdcard/ase/extras/jruby/jruby-complete-1.4.jar!/META-INF/jruby.home/lib/ruby/1.8'); "
          + "require 'android';";

  public JRubyInterpreter() {
    super(new RubyLanguage());
  }

  @Override
  public String getExtension() {
    // TODO(psycho): Add support for multiple interpreters for the same
    // extension later.
    return ".rb";
  }

  @Override
  public String getName() {
    return "jruby";
  }

  @Override
  public String getNiceName() {
    return "JRuby-1.4";
  }

  @Override
  public InterpreterProcess buildProcess(String scriptName, int port) {
    return new JRubyInterpreterProcess(scriptName, port);
  }

  public boolean hasInterpreterArchive() {
    return false;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  @Override
  public String getBinary() {
    return JRUBY_BIN;
  }

  @Override
  public int getVersion() {
    return 1;
  }
}
