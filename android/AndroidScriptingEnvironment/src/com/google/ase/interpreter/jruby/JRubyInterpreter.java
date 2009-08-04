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

import android.util.Log;

import com.google.ase.AndroidFacade;
import com.google.ase.interpreter.AbstractInterpreter;
import com.google.ase.interpreter.InterpreterProcessInterface;

public class JRubyInterpreter extends AbstractInterpreter {

  private static final String TAG = "JRubyInterpreter";

  @Override
  public String getExtension() {
    // TODO(psycho): Add support for multiple interpreters for the same extension later.
    return ".rb";
  }

  @Override
  public String getName() {
    return "jruby";
  }

  @Override
  public String getNiceName() {
    return "JRuby-1.2.0RC1";
  }

  @Override
  public InterpreterProcessInterface buildProcess(AndroidFacade facade, String scriptName) {
    try {
      return new JRubyInterpreterProcess(facade, scriptName);
    } catch (Exception e) {
      Log.e(TAG, "Failed to create JRuby interpreter.", e);
      return null;
    }
  }

}
