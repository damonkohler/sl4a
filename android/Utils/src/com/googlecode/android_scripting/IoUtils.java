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

package com.googlecode.android_scripting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class IoUtils {
  private static final int BUFFER_SIZE = 1024 * 8;

  private IoUtils() {
    // Utility class.
  }

  public static int copy(InputStream input, OutputStream output) throws Exception, IOException {
    byte[] buffer = new byte[BUFFER_SIZE];

    BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
    BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
    int count = 0, n = 0;
    try {
      while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
        out.write(buffer, 0, n);
        count += n;
      }
      out.flush();
    } finally {
      try {
        out.close();
      } catch (IOException e) {
        Log.e(e.getMessage(), e);
      }
      try {
        in.close();
      } catch (IOException e) {
        Log.e(e.getMessage(), e);
      }
    }
    return count;
  }

}
