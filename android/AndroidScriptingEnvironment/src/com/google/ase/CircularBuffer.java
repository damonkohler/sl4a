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

public class CircularBuffer<T> {
  private final T[] mBuffer;
  private int mStart;
  private int mEnd;

  @SuppressWarnings("unchecked")
  public CircularBuffer(int size) {
    mBuffer = (T[]) new Object[size];
    mStart = 0;
    mEnd = 0;
  }

  public void add(T value) {
    mBuffer[mEnd] = value;
    mEnd = (mEnd + 1) % mBuffer.length;
    if (mEnd == mStart) {
      // At this point, we've filled the buffer and start overwriting old entries.
      mStart = (mStart + 1) % mBuffer.length;
    }
  }

  public T get() {
    if (mStart == mEnd) {
      return null;
    }
    T value = mBuffer[mStart];
    mStart = (mStart + 1) % mBuffer.length;
    return value;
  }
}
