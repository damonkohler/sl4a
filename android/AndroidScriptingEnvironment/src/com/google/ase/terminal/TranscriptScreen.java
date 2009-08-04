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

import android.graphics.Canvas;

/**
 * A TranscriptScreen is a screen that remembers data that's been scrolled. The old data is stored
 * in a ring buffer to minimize the amount of copying that needs to be done. The transcript does its
 * own drawing, to avoid having to expose its internal data structures.
 */
public class TranscriptScreen implements Screen {

  /**
   * The width of the transcript, in characters. Fixed at initialization.
   */
  private int mColumns;

  /**
   * The total number of rows in the transcript and the screen. Fixed at initialization.
   */
  private int mTotalRows;

  /**
   * The number of rows in the active portion of the transcript. Doesn't include the screen.
   */
  private int mActiveTranscriptRows;

  /**
   * Which row is currently the topmost line of the transcript. Used to implement a circular buffer.
   */
  private int mHead;

  /**
   * The number of active rows, includes both the transcript and the screen.
   */
  private int mActiveRows;

  /**
   * The number of rows in the screen.
   */
  private int mScreenRows;

  /**
   * The data for both the screen and the transcript. The first mScreenRows * mLineWidth characters
   * are the screen, the rest are the transcript. The low byte encodes the ASCII character, the high
   * byte encodes the foreground and background colors, plus underline and bold.
   */
  private char[] mData;

  /**
   * The data's stored as color-encoded chars, but the drawing routines require chars, so we need a
   * temporary buffer to hold a row's worth of characters.
   */
  private char[] mRowBuffer;

  /**
   * Flags that keep track of whether the current line logically wraps to the next line. This is
   * used when resizing the screen and when copying to the clipboard or an email attachment
   */

  private boolean[] mLineWrap;

  /**
   * Create a transcript screen.
   *
   * @param columns
   *          the width of the screen in characters.
   * @param totalRows
   *          the height of the entire text area, in rows of text.
   * @param screenRows
   *          the height of just the screen, not including the transcript that holds lines that have
   *          scrolled off the top of the screen.
   */
  public TranscriptScreen(int columns, int totalRows, int screenRows, int foreColor, int backColor) {
    init(columns, totalRows, screenRows, foreColor, backColor);
  }

  private void init(int columns, int totalRows, int screenRows, int foreColor, int backColor) {
    mColumns = columns;
    mTotalRows = totalRows;
    mActiveTranscriptRows = 0;
    mHead = 0;
    mActiveRows = screenRows;
    mScreenRows = screenRows;
    int totalSize = columns * totalRows;
    mData = new char[totalSize];
    blockSet(0, 0, mColumns, mScreenRows, ' ', foreColor, backColor);
    mRowBuffer = new char[columns];
    mLineWrap = new boolean[totalRows];
    consistencyCheck();
  }

  /**
   * Convert a row value from the public external coordinate system to our internal private
   * coordinate system. External coordinate system: -mActiveTranscriptRows to mScreenRows-1, with
   * the screen being 0..mScreenRows-1 Internal coordinate system: 0..mScreenRows-1 rows of mData
   * are the visible rows. mScreenRows..mActiveRows - 1 are the transcript, stored as a circular
   * buffer.
   *
   * @param row
   *          a row in the external coordinate system.
   * @return The row corresponding to the input argument in the private coordinate system.
   */
  private int externalToInternalRow(int row) {
    if (row < -mActiveTranscriptRows || row >= mScreenRows) {
      throw new IllegalArgumentException("Invalid row: " + row);
    }
    if (row >= 0) {
      return row; // This is a visible row.
    }
    return mScreenRows + ((mHead + mActiveTranscriptRows + row) % mActiveTranscriptRows);
  }

  private int getOffset(int externalLine) {
    return externalToInternalRow(externalLine) * mColumns;
  }

  private int getOffset(int x, int y) {
    return getOffset(y) + x;
  }

  public void setLineWrap(int row) {
    mLineWrap[externalToInternalRow(row)] = true;
  }

  /**
   * Store byte b into the screen at location (x, y)
   *
   * @param x
   *          X coordinate (also known as column)
   * @param y
   *          Y coordinate (also known as row)
   * @param b
   *          ASCII character to store
   * @param foreColor
   *          the foreground color
   * @param backColor
   *          the background color
   */
  public void set(int x, int y, char c, int foreColor, int backColor) {
    mData[getOffset(x, y)] = encode(c, foreColor, backColor);
  }

  private char encode(int b, int foreColor, int backColor) {
    return (char) ((foreColor << 12) | (backColor << 8) | b);
  }

  /**
   * Scroll the screen down one line. To scroll the whole screen of a 24 line screen, the arguments
   * would be (0, 24).
   *
   * @param topMargin
   *          First line that is scrolled.
   * @param bottomMargin
   *          One line after the last line that is scrolled.
   */
  public void scroll(int topMargin, int bottomMargin, int foreColor, int backColor) {
    if (topMargin > bottomMargin - 2 || topMargin > mScreenRows - 2 || bottomMargin > mScreenRows) {
      throw new IllegalArgumentException();
    }

    // Adjust the transcript so that the last line of the transcript
    // is ready to receive the newly scrolled data
    consistencyCheck();
    int expansionRows = Math.min(1, mTotalRows - mActiveRows);
    int rollRows = 1 - expansionRows;
    mActiveRows += expansionRows;
    mActiveTranscriptRows += expansionRows;
    if (mActiveTranscriptRows > 0) {
      mHead = (mHead + rollRows) % mActiveTranscriptRows;
    }
    consistencyCheck();

    // Block move the scroll line to the transcript
    int topOffset = getOffset(topMargin);
    int destOffset = getOffset(-1);
    System.arraycopy(mData, topOffset, mData, destOffset, mColumns);

    int topLine = externalToInternalRow(topMargin);
    int destLine = externalToInternalRow(-1);
    System.arraycopy(mLineWrap, topLine, mLineWrap, destLine, 1);

    // Block move the scrolled data up
    int numScrollChars = (bottomMargin - topMargin - 1) * mColumns;
    System.arraycopy(mData, topOffset + mColumns, mData, topOffset, numScrollChars);
    int numScrollLines = (bottomMargin - topMargin - 1);
    System.arraycopy(mLineWrap, topLine + 1, mLineWrap, topLine, numScrollLines);

    // Erase the bottom line of the scroll region
    blockSet(0, bottomMargin - 1, mColumns, 1, ' ', foreColor, backColor);
    mLineWrap[externalToInternalRow(bottomMargin - 1)] = false;
  }

  private void consistencyCheck() {
    checkPositive(mColumns);
    checkPositive(mTotalRows);
    checkRange(0, mActiveTranscriptRows, mTotalRows);
    if (mActiveTranscriptRows == 0) {
      checkEqual(mHead, 0);
    } else {
      checkRange(0, mHead, mActiveTranscriptRows - 1);
    }
    checkEqual(mScreenRows + mActiveTranscriptRows, mActiveRows);
    checkRange(0, mScreenRows, mTotalRows);

    checkEqual(mTotalRows, mLineWrap.length);
    checkEqual(mTotalRows * mColumns, mData.length);
    checkEqual(mColumns, mRowBuffer.length);
  }

  private void checkPositive(int n) {
    if (n < 0) {
      throw new IllegalArgumentException("checkPositive " + n);
    }
  }

  private void checkRange(int a, int b, int c) {
    if (a > b || b > c) {
      throw new IllegalArgumentException("checkRange " + a + " <= " + b + " <= " + c);
    }
  }

  private void checkEqual(int a, int b) {
    if (a != b) {
      throw new IllegalArgumentException("checkEqual " + a + " == " + b);
    }
  }

  /**
   * Block copy characters from one position in the screen to another. The two positions can
   * overlap. All characters of the source and destination must be within the bounds of the screen,
   * or else an InvalidParemeterException will be thrown.
   *
   * @param sx
   *          source X coordinate
   * @param sy
   *          source Y coordinate
   * @param w
   *          width
   * @param h
   *          height
   * @param dx
   *          destination X coordinate
   * @param dy
   *          destination Y coordinate
   */
  public void blockCopy(int sx, int sy, int w, int h, int dx, int dy) {
    if (sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows || dx < 0
        || dx + w > mColumns || dy < 0 || dy + h > mScreenRows) {
      throw new IllegalArgumentException();
    }
    if (sy <= dy) {
      // Move in increasing order
      for (int y = 0; y < h; y++) {
        int srcOffset = getOffset(sx, sy + y);
        int dstOffset = getOffset(dx, dy + y);
        System.arraycopy(mData, srcOffset, mData, dstOffset, w);
      }
    } else {
      // Move in decreasing order
      for (int y = 0; y < h; y++) {
        int y2 = h - (y + 1);
        int srcOffset = getOffset(sx, sy + y2);
        int dstOffset = getOffset(dx, dy + y2);
        System.arraycopy(mData, srcOffset, mData, dstOffset, w);
      }
    }
  }

  /**
   * Block set characters. All characters must be within the bounds of the screen, or else and
   * InvalidParemeterException will be thrown. Typically this is called with a "val" argument of 32
   * to clear a block of characters.
   *
   * @param sx
   *          source X
   * @param sy
   *          source Y
   * @param w
   *          width
   * @param h
   *          height
   * @param val
   *          value to set.
   */
  public void blockSet(int sx, int sy, int w, int h, int val, int foreColor, int backColor) {
    if (sx < 0 || sx + w > mColumns || sy < 0 || sy + h > mScreenRows) {
      throw new IllegalArgumentException();
    }
    char[] data = mData;
    char encodedVal = encode(val, foreColor, backColor);
    for (int y = 0; y < h; y++) {
      int offset = getOffset(sx, sy + y);
      for (int x = 0; x < w; x++) {
        data[offset + x] = encodedVal;
      }
    }
  }

  /**
   * Draw a row of text. Out-of-bounds rows are blank, not errors.
   *
   * @param row
   *          The row of text to draw.
   * @param canvas
   *          The canvas to draw to.
   * @param x
   *          The x coordinate origin of the drawing
   * @param y
   *          The y coordinate origin of the drawing
   * @param renderer
   *          The renderer to use to draw the text
   * @param cx
   *          the cursor X coordinate, -1 means don't draw it
   */
  public final void drawText(int row, Canvas canvas, float x, float y, TextRenderer renderer, int cx) {

    // Out-of-bounds rows are blank.
    if (row < -mActiveTranscriptRows || row >= mScreenRows) {
      return;
    }

    // Copy the data from the byte array to a char array so they can
    // be drawn.

    int offset = getOffset(row);
    char[] rowBuffer = mRowBuffer;
    char[] data = mData;
    int columns = mColumns;
    int lastColors = 0;
    int lastRunStart = -1;
    final int CURSOR_MASK = 0x10000;
    for (int i = 0; i < columns; i++) {
      char c = data[offset + i];
      int colors = (char) (c & 0xff00);
      if (cx == i) {
        // Set cursor background color:
        colors |= CURSOR_MASK;
      }
      rowBuffer[i] = (char) (c & 0x00ff);
      if (colors != lastColors) {
        if (lastRunStart >= 0) {
          renderer.drawTextRun(canvas, x, y, lastRunStart, rowBuffer, lastRunStart, i
              - lastRunStart, (lastColors & CURSOR_MASK) != 0, 0xf & (lastColors >> 12),
              0xf & (lastColors >> 8));
        }
        lastColors = colors;
        lastRunStart = i;
      }
    }
    if (lastRunStart >= 0) {
      renderer.drawTextRun(canvas, x, y, lastRunStart, rowBuffer, lastRunStart, columns
          - lastRunStart, (lastColors & CURSOR_MASK) != 0, 0xf & (lastColors >> 12),
          0xf & (lastColors >> 8));
    }
  }

  /**
   * Get the count of active rows.
   *
   * @return the count of active rows.
   */
  public int getActiveRows() {
    return mActiveRows;
  }

  /**
   * Get the count of active transcript rows.
   *
   * @return the count of active transcript rows.
   */
  public int getActiveTranscriptRows() {
    return mActiveTranscriptRows;
  }

  public String getTranscriptText() {
    return internalGetTranscriptText(true);
  }

  private String internalGetTranscriptText(boolean stripColors) {
    StringBuilder builder = new StringBuilder();
    char[] rowBuffer = mRowBuffer;
    char[] data = mData;
    int columns = mColumns;
    for (int row = -mActiveTranscriptRows; row < mScreenRows; row++) {
      int offset = getOffset(row);
      int lastPrintingChar = -1;
      for (int column = 0; column < columns; column++) {
        char c = data[offset + column];
        if (stripColors) {
          c = (char) (c & 0xff);
        }
        if ((c & 0xff) != ' ') {
          lastPrintingChar = column;
        }
        rowBuffer[column] = c;
      }
      if (mLineWrap[externalToInternalRow(row)]) {
        builder.append(rowBuffer, 0, columns);
      } else {
        builder.append(rowBuffer, 0, lastPrintingChar + 1);
        builder.append('\n');
      }
    }
    return builder.toString();
  }

  public void resize(int columns, int rows, int foreColor, int backColor) {
    init(columns, mTotalRows, rows, foreColor, backColor);
  }
}
