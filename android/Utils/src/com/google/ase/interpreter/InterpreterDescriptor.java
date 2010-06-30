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

package com.google.ase.interpreter;

import android.content.Context;

/**
 * Provides interpreter-specific info for installation/removal purposes.
 * 
 * @author Alexey Reznichenko (alexey.reznichenko@gmail.com)
 */
public interface InterpreterDescriptor {

  public String getName();

  public String getNiceName();

  public String getExtension();

  public int getVersion();

  public String getBinary();

  public String getPath(Context context);

  public String getExecuteCommand();

  public String getEmptyParams();

  public String getExecuteParams();

  public boolean hasInterpreterArchive();

  public boolean hasExtrasArchive();

  public boolean hasScriptsArchive();

  public String getInterpreterArchiveName();

  public String getExtrasArchiveName();

  public String getScriptsArchiveName();

  public String getInterpreterArchiveUrl();

  public String getScriptsArchiveUrl();

  public String getExtrasArchiveUrl();

}
