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

package com.googlecode.jrubyforandroid;

import com.google.ase.interpreter.AseHostedInterpreter;

public class JRubyDescriptor extends AseHostedInterpreter {

  private final static String JRUBY_EXEC =
      "dalvikvm -Xss128k " + "-classpath %1$s%2$s org.jruby.Main -X-C "
          +
          // Fix include path.
          "-e \"\\$LOAD_PATH.push('file:%1$s%2$s!/META-INF/jruby.home/lib/ruby/1.8'); "
          + "require 'android'; %3$s";

  private final static String JRUBY_BIN = "jruby-complete-1.4.jar";

  public String getExtension() {
    // TODO(psycho): Add support for multiple interpreters for the same
    // extension later.
    return ".rb";
  }

  public String getName() {
    return "jruby";
  }

  public String getNiceName() {
    return "JRuby-1.4";
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

  public int getVersion() {
    return 2;
  }

  @Override
  public int getScriptsVersion() {
    return 1;
  }

  public String getBinary() {
    return JRUBY_BIN;
  }

  public String getEmptyParams() {
    return "require 'irb'; IRB.conf[:USE_READLINE] = false; IRB.start\"";
  }

  public String getExecuteParams() {
    return "load('%s')\"";
  }

  @Override
  public String getExecuteCommand() {
    return JRUBY_EXEC;
  }
}
