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

package com.googlecode.android_scripting.terminal;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

class PaintRenderer extends BaseTextRenderer {

  public PaintRenderer(int fontSize, int forePaintColor, int backPaintColor) {
    super(forePaintColor, backPaintColor);
    mTextPaint = new Paint();
    mTextPaint.setTypeface(Typeface.MONOSPACE);
    mTextPaint.setAntiAlias(true);
    mTextPaint.setTextSize(fontSize);

    mCharHeight = (int) Math.ceil(mTextPaint.getFontSpacing());
    mCharAscent = (int) Math.ceil(mTextPaint.ascent());
    mCharDescent = mCharHeight + mCharAscent;
    mCharWidth = (int) mTextPaint.measureText(EXAMPLE_CHAR, 0, 1);
  }

  public void drawTextRun(Canvas canvas, float x, float y, int lineOffset, char[] text, int index,
      int count, boolean cursor, int foreColor, int backColor) {
    if (cursor) {
      mTextPaint.setColor(mCursorPaint);
    } else {
      mTextPaint.setColor(mBackPaint[backColor & 0x7]);
    }
    float left = x + lineOffset * mCharWidth;
    canvas.drawRect(left, y + mCharAscent, left + count * mCharWidth, y + mCharDescent, mTextPaint);
    boolean bold = (foreColor & 0x8) != 0;
    boolean underline = (backColor & 0x8) != 0;
    if (bold) {
      mTextPaint.setFakeBoldText(true);
    }
    if (underline) {
      mTextPaint.setUnderlineText(true);
    }
    mTextPaint.setColor(mForePaint[foreColor & 0x7]);
    canvas.drawText(text, index, count, left, y, mTextPaint);
    if (bold) {
      mTextPaint.setFakeBoldText(false);
    }
    if (underline) {
      mTextPaint.setUnderlineText(false);
    }
  }

  public int getCharacterHeight() {
    return mCharHeight;
  }

  public int getCharacterWidth() {
    return mCharWidth;
  }

  private Paint mTextPaint;

  private int mCharWidth;

  private int mCharHeight;

  private int mCharAscent;

  private int mCharDescent;

  private static final char[] EXAMPLE_CHAR = { 'X' };
}
