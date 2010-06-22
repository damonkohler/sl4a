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

package com.google.ase.interpreter;

import android.content.Context;

import com.google.ase.language.Language;
import com.google.ase.rpc.MethodDescriptor;

public interface InterpreterExecutionDescriptor {

  public Language getLanguage();

  public String getContentTemplate();

  public String getRpcText(String content, MethodDescriptor rpc, String[] values);
  
  public String getPath();
  
  public InterpreterProcess buildProcess(String launchScript, int port);

  public String getBinary();

  public String getExtension();

  public String getName();

  public String getNiceName();

  public boolean isInstalled(Context context);

  public boolean isUninstallable();
  
}
