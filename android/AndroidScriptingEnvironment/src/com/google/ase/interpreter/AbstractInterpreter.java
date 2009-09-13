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

import com.google.ase.Constants;

public abstract class AbstractInterpreter implements InterpreterInterface {

  @Override
  public boolean isInstalled() {
    return InterpreterManager.checkInstalled(getName());
  }

  @Override
  public String getContentTemplate() {
    return "";
  }

  @Override
  public String getInterpreterArchiveName(String version) {
    return String.format("%s-r%s.zip", getName(), version);
  }

  @Override
  public String getInterpreterExtrasArchiveName(String version) {
    return String.format("%s-r%s_extras.zip", getName(), version);
  }

  @Override
  public String getScriptsArchiveName(String version) {
    return String.format("%s-r%s_scripts.zip", getName(), version);
  }

  @Override
  public String getInterpreterArchiveUrl(String version) {
    return Constants.BASE_INSTALL_URL + getInterpreterArchiveName(version);
  }

  @Override
  public String getScriptsArchiveUrl(String version) {
    return Constants.BASE_INSTALL_URL + getScriptsArchiveName(version);
  }

  @Override
  public String getInterpreterExtrasArchiveUrl(String version) {
    return Constants.BASE_INSTALL_URL + getInterpreterExtrasArchiveName(version);
  }

}
