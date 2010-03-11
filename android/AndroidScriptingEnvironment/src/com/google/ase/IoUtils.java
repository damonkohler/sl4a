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

package com.google.ase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

  private IoUtils() {
    // Utility class.
  }

  public static int copy(InputStream input, OutputStream output) throws InterruptedException,
      IOException {
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    int count = 0;
    int n = 0;
    while (-1 != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
      if (Thread.interrupted()) {
        throw new InterruptedException();
      }
    }
    return count;
  }

}
