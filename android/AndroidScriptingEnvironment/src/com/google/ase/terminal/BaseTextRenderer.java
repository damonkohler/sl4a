/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.google.ase.terminal;

abstract class BaseTextRenderer implements TextRenderer {
  protected int[] mForePaint = { 0xff000000, // Black
      0xffff0000, // Red
      0xff00ff00, // green
      0xffffff00, // yellow
      0xff0000ff, // blue
      0xffff00ff, // magenta
      0xff00ffff, // cyan
      0xffffffff // white -- is overridden by constructor
  };

  protected int[] mBackPaint = { 0xff000000, // Black -- is overridden by
      // constructor
      0xffcc0000, // Red
      0xff00cc00, // green
      0xffcccc00, // yellow
      0xff0000cc, // blue
      0xffff00cc, // magenta
      0xff00cccc, // cyan
      0xffffffff // white
  };

  protected final static int mCursorPaint = 0xff808080;

  public BaseTextRenderer(int forePaintColor, int backPaintColor) {
    mForePaint[7] = forePaintColor;
    mBackPaint[0] = backPaintColor;
  }
}
