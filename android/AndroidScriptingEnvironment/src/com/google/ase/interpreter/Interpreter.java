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

import java.io.File;

import com.google.ase.Constants;
import com.google.ase.RpcFacade;

public abstract class Interpreter {

  public boolean isInstalled() {
    return InterpreterUtils.checkInstalled(getName());
  }

  public String getContentTemplate() {
    return "";
  }

  public String getInterpreterArchiveName() {
    return String.format("%s_r%s.zip", getName(), getVersion());
  }

  public String getInterpreterExtrasArchiveName() {
    return String.format("%s_extras_r%s.zip", getName(), getVersion());
  }

  public String getScriptsArchiveName() {
    return String.format("%s_scripts_r%s.zip", getName(), getVersion());
  }

  public String getInterpreterArchiveUrl() {
    return Constants.BASE_INSTALL_URL + getInterpreterArchiveName();
  }

  public String getScriptsArchiveUrl() {
    return Constants.BASE_INSTALL_URL + getScriptsArchiveName();
  }

  public String getInterpreterExtrasArchiveUrl() {
    return Constants.BASE_INSTALL_URL + getInterpreterExtrasArchiveName();
  }

  public abstract InterpreterProcess buildProcess(String launchScript, RpcFacade... facades);

  public abstract File getBinary();

  public abstract String getExtension();

  public abstract String getName();

  public abstract String getNiceName();

  public abstract int getVersion();

  public abstract boolean hasInterpreterArchive();

  public abstract boolean hasInterpreterExtrasArchive();

  public abstract boolean hasScriptsArchive();

  public boolean isUninstallable() {
    return true;
  }

}
