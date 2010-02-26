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

package com.google.ase;

@SuppressWarnings("serial")
public class AseRuntimeException extends RuntimeException {

  public AseRuntimeException(String message) {
    super(message);
  }

  public AseRuntimeException(String message, Exception wrapped_exception) {
    super(message + "\n" + wrapped_exception.toString());
  }

  public AseRuntimeException(Exception wrapped_exception) {
    super(wrapped_exception.toString());
  }
}
