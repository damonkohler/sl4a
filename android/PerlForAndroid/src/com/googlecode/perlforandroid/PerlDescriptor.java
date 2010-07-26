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

package com.googlecode.perlforandroid;

import java.io.File;

import android.content.Context;

import com.googlecode.android_scripting.interpreter.Sl4aHostedInterpreter;

public class PerlDescriptor extends Sl4aHostedInterpreter {

  private final static String PERL = "perl";

  public String getExtension() {
    return ".pl";
  }

  public String getName() {
    return PERL;
  }

  public String getNiceName() {
    return "Perl 5.10.1";
  }

  public boolean hasInterpreterArchive() {
    return true;
  }

  public boolean hasExtrasArchive() {
    return true;
  }

  public boolean hasScriptsArchive() {
    return true;
  }

  @Override
  public File getBinary(Context context) {
    return new File(getExtrasPath(context), PERL);
  }

  public int getVersion() {
    return 9;
  }

  @Override
  public int getExtrasVersion() {
    return 7;
  }

  @Override
  public int getScriptsVersion() {
    return 6;
  }

  @Override
  public String getInteractiveCommand(Context context) {
    return "-de 1";
  }
}
