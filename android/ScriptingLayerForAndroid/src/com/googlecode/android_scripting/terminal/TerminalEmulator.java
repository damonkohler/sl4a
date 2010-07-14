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

import java.io.IOException;
import java.io.PrintStream;

import android.util.Log;

import com.googlecode.android_scripting.Sl4aLog;

/**
 * Renders text into a screen. Contains all the terminal-specific knowlege and state. Emulates a
 * subset of the X Window System xterm terminal, which in turn is an emulator for a subset of the
 * Digital Equipment Corporation vt100 terminal. Missing functionality: text attributes (bold,
 * underline, reverse video, color) alternate screen cursor key and keypad escape sequences.
 */
class TerminalEmulator {

  /**
   * The cursor row. Numbered 0..mRows-1.
   */
  private int mCursorRow;

  /**
   * The cursor column. Numbered 0..mColumns-1.
   */
  private int mCursorCol;

  /**
   * The number of character rows in the terminal screen.
   */
  private int mRows;

  /**
   * The number of character columns in the terminal screen.
   */
  private int mColumns;

  /**
   * Used to send data to the remote process. Needed to implement the various "report" escape
   * sequences.
   */
  private final PrintStream mTermOut;

  /**
   * Stores the characters that appear on the screen of the emulated terminal.
   */
  private final Screen mScreen;

  /**
   * Keeps track of the current argument of the current escape sequence. Ranges from 0 to
   * MAX_ESCAPE_PARAMETERS-1. (Typically just 0 or 1.)
   */
  private int mArgIndex;

  /**
   * The number of parameter arguments. This name comes from the ANSI standard for terminal escape
   * codes.
   */
  private static final int MAX_ESCAPE_PARAMETERS = 16;

  /**
   * Holds the arguments of the current escape sequence.
   */
  private final int[] mArgs = new int[MAX_ESCAPE_PARAMETERS];

  // Escape processing states:

  /**
   * Escape processing state: Not currently in an escape sequence.
   */
  private static final int ESC_NONE = 0;

  /**
   * Escape processing state: Have seen an ESC character
   */
  private static final int ESC = 1;

  /**
   * Escape processing state: Have seen ESC POUND
   */
  private static final int ESC_POUND = 2;

  /**
   * Escape processing state: Have seen ESC and a character-set-select char
   */
  private static final int ESC_SELECT_LEFT_PAREN = 3;

  /**
   * Escape processing state: Have seen ESC and a character-set-select char
   */
  private static final int ESC_SELECT_RIGHT_PAREN = 4;

  /**
   * Escape processing state: ESC [
   */
  private static final int ESC_LEFT_SQUARE_BRACKET = 5;

  /**
   * Escape processing state: ESC [ ?
   */
  private static final int ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK = 6;

  /**
   * True if the current escape sequence should continue, false if the current escape sequence
   * should be terminated. Used when parsing a single character.
   */
  private boolean mContinueSequence;

  /**
   * The current state of the escape sequence state machine.
   */
  private int mEscapeState;

  /**
   * Saved state of the cursor row, Used to implement the save/restore cursor position escape
   * sequences.
   */
  private int mSavedCursorRow;

  /**
   * Saved state of the cursor column, Used to implement the save/restore cursor position escape
   * sequences.
   */
  private int mSavedCursorCol;

  // DecSet booleans

  /**
   * This mask indicates 132-column mode is set. (As opposed to 80-column mode.)
   */
  private static final int K_132_COLUMN_MODE_MASK = 1 << 3;

  /**
   * This mask indicates that origin mode is set. (Cursor addressing is relative to the absolute
   * screen size, rather than the currently set top and bottom margins.)
   */
  private static final int K_ORIGIN_MODE_MASK = 1 << 6;

  /**
   * Holds multiple DECSET flags. The data is stored this way, rather than in separate booleans, to
   * make it easier to implement the save-and-restore semantics. The various k*ModeMask masks can be
   * used to extract and modify the individual flags current states.
   */
  private int mDecFlags;

  /**
   * Saves away a snapshot of the DECSET flags. Used to implement save and restore escape sequences.
   */
  private int mSavedDecFlags;

  // Modes set with Set Mode / Reset Mode

  /**
   * True if insert mode (as opposed to replace mode) is active. In insert mode new characters are
   * inserted, pushing existing text to the right.
   */
  private boolean mInsertMode;

  /**
   * An array of tab stops. mTabStop[i] is true if there is a tab stop set for column i.
   */
  private boolean[] mTabStops;

  // The margins allow portions of the screen to be locked.

  /**
   * The top margin of the screen, for scrolling purposes. Ranges from 0 to mRows-2.
   */
  private int mTopMargin;

  /**
   * The bottom margin of the screen, for scrolling purposes. Ranges from mTopMargin + 2 to mRows.
   * (Defines the first row after the scrolling region.
   */
  private int mBottomMargin;

  /**
   * True if the next character to be emitted will be automatically wrapped to the next line. Used
   * to disambiguate the case where the cursor is positioned on column mColumns-1.
   */
  private boolean mAboutToAutoWrap;

  /**
   * Used for debugging, counts how many chars have been processed.
   */
  private int mProcessedCharCount;

  /**
   * Foreground color, 0..7, mask with 8 for bold
   */
  private int mForeColor;

  /**
   * Background color, 0..7, mask with 8 for underline
   */
  private int mBackColor;

  private boolean mInverseColors;

  private boolean mbKeypadApplicationMode;

  /**
   * Construct a terminal emulator that uses the supplied screen
   * 
   * @param screen
   *          the screen to render characters into.
   * @param columns
   *          the number of columns to emulate
   * @param rows
   *          the number of rows to emulate
   * @param termOut
   *          the output file descriptor that talks to the pseudo-tty.
   */
  public TerminalEmulator(Screen screen, int columns, int rows, PrintStream termOut) {
    mScreen = screen;
    mRows = rows;
    mColumns = columns;
    mTermOut = termOut;
    reset();
  }

  public void updateSize(int columns, int rows) {
    if (mRows == rows && mColumns == columns) {
      return;
    }

    // Save transcript text for replay after size update.
    String transcriptText = mScreen.getTranscriptText();
    mScreen.resize(columns, rows, mForeColor, mBackColor);

    mRows = rows;
    mColumns = columns;
    mTopMargin = 0;
    mBottomMargin = mRows;
    mCursorRow = 0;
    mCursorCol = 0;
    mAboutToAutoWrap = false;

    boolean[] oldTabStops = mTabStops;
    // Reinitialize with default tab stops.
    setDefaultTabStops();
    // Update with any existing tab stops that fit into the current display.
    System.arraycopy(oldTabStops, 0, mTabStops, 0, Math.min(oldTabStops.length, mTabStops.length));

    // Replay old transcript text.
    // TODO(damonkohler): Theoretically, this whole bit could be replaced with this simple append:
    // append(transcriptText.toCharArray(), 0, transcriptText.length());
    // However, it seems that the transcript text includes a lot of empty lines...
    int end = transcriptText.length() - 1;
    while ((end >= 0) && transcriptText.charAt(end) == '\n') {
      end--;
    }
    for (int i = 0; i <= end; i++) {
      char c = transcriptText.charAt(i);
      if (c == '\n') {
        setCursorCol(0);
        doLinefeed();
      } else {
        emit(c);
      }
    }
  }

  /**
   * Get the cursor's current row.
   * 
   * @return the cursor's current row.
   */
  public final int getCursorRow() {
    return mCursorRow;
  }

  /**
   * Get the cursor's current column.
   * 
   * @return the cursor's current column.
   */
  public final int getCursorCol() {
    return mCursorCol;
  }

  public final boolean getKeypadApplicationMode() {
    return mbKeypadApplicationMode;
  }

  /**
   * Sets the tab stops to be every 8 spaces.
   */
  private void setDefaultTabStops() {
    mTabStops = new boolean[mColumns];
    mTabStops[0] = false;
    for (int i = 1; i < mColumns; i++) {
      mTabStops[i] = (i % 8) == 0;
    }
  }

  /**
   * Accept bytes (typically from the pty) and process them.
   * 
   * @param receiveBuffer
   *          a byte array containing the bytes to be processed
   * @param offset
   *          the first index of the array to process
   * @param length
   *          the number of bytes in the array to process
   */
  public void append(char[] receiveBuffer, int offset, int length) {
    for (int i = 0; i < length; i++) {
      char c = receiveBuffer[offset + i];
      try {
        if (Terminal.LOG_CHARACTERS_FLAG) {
          if (c < 32 || c > 126) {
            c = ' ';
          }
          Log.w(Terminal.TAG, "'" + c + "' (" + Integer.toString(c) + ")");
        }
        process(c);
        mProcessedCharCount++;
      } catch (Exception e) {
        Log.e(Terminal.TAG, "Exception while processing character "
            + Integer.toString(mProcessedCharCount) + " code " + Integer.toString(c), e);
      }
    }
  }

  private void process(char c) {
    switch (c) {
    case 0: // NUL
      // Do nothing
      break;

    case 7: // BEL
      // Do nothing
      break;

    case 8: // BS
      setCursorCol(Math.max(0, mCursorCol - 1));
      break;

    case 9: // HT
      // Move to next tab stop, but not past edge of screen
      setCursorCol(nextTabStop(mCursorCol));
      break;

    case 10: // LF
    case 11: // VT
    case 12: // FF
      setCursorCol(0);
      doLinefeed();
      break;

    case 13: // CR
      setCursorCol(0);
      break;

    case 14: // SO:
      break;

    case 15: // SI:
      break;

    case 24: // CAN
    case 26: // SUB
      if (mEscapeState != ESC_NONE) {
        mEscapeState = ESC_NONE;
        emit((char) 127);
      }
      break;

    case 27: // ESC
      // Always starts an escape sequence
      startEscapeSequence(ESC);
      break;

    case (char) 0x9b: // CSI
      startEscapeSequence(ESC_LEFT_SQUARE_BRACKET);
      break;

    default:
      mContinueSequence = false;
      switch (mEscapeState) {
      case ESC_NONE:
        if (c >= 32) {
          emit(c);
        }
        break;

      case ESC:
        doEsc(c);
        break;

      case ESC_POUND:
        doEscPound(c);
        break;

      case ESC_SELECT_LEFT_PAREN:
        doEscSelectLeftParen(c);
        break;

      case ESC_SELECT_RIGHT_PAREN:
        doEscSelectRightParen(c);
        break;

      case ESC_LEFT_SQUARE_BRACKET:
        doEscLeftSquareBracket(c);
        break;

      case ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK:
        doEscLSBQuest(c);
        break;

      default:
        unknownSequence(c);
        break;
      }
      if (!mContinueSequence) {
        mEscapeState = ESC_NONE;
      }
      break;
    }
  }

  private int nextTabStop(int cursorCol) {
    for (int i = cursorCol; i < mColumns; i++) {
      if (mTabStops[i]) {
        return i;
      }
    }
    return mColumns - 1;
  }

  private void doEscLSBQuest(char c) {
    int mask = getDecFlagsMask(getArg0(0));
    switch (c) {
    case 'h': // Esc [ ? Pn h - DECSET
      mDecFlags |= mask;
      break;

    case 'l': // Esc [ ? Pn l - DECRST
      mDecFlags &= ~mask;
      break;

    case 'r': // Esc [ ? Pn r - restore
      mDecFlags = (mDecFlags & ~mask) | (mSavedDecFlags & mask);
      break;

    case 's': // Esc [ ? Pn s - save
      mSavedDecFlags = (mSavedDecFlags & ~mask) | (mDecFlags & mask);
      break;

    default:
      parseArg(c);
      break;
    }

    // 132 column mode
    if ((mask & K_132_COLUMN_MODE_MASK) != 0) {
      // We don't actually set 132 cols, but we do want the
      // side effect of clearing the screen and homing the cursor.
      blockClear(0, 0, mColumns, mRows);
      setCursorRowCol(0, 0);
    }

    // origin mode
    if ((mask & K_ORIGIN_MODE_MASK) != 0) {
      // Home the cursor.
      setCursorPosition(0, 0);
    }
  }

  private int getDecFlagsMask(int argument) {
    if (argument >= 1 && argument <= 9) {
      return (1 << argument);
    }

    return 0;
  }

  private void startEscapeSequence(int escapeState) {
    mEscapeState = escapeState;
    mArgIndex = 0;
    for (int j = 0; j < MAX_ESCAPE_PARAMETERS; j++) {
      mArgs[j] = -1;
    }
  }

  private void doLinefeed() {
    int newCursorRow = mCursorRow + 1;
    if (newCursorRow >= mBottomMargin) {
      scroll();
      newCursorRow = mBottomMargin - 1;
    }
    setCursorRow(newCursorRow);
  }

  private void continueSequence() {
    mContinueSequence = true;
  }

  private void continueSequence(int state) {
    mEscapeState = state;
    mContinueSequence = true;
  }

  private void doEscSelectLeftParen(char c) {
    doSelectCharSet(true, c);
  }

  private void doEscSelectRightParen(char c) {
    doSelectCharSet(false, c);
  }

  private void doSelectCharSet(boolean isG0CharSet, char c) {
    switch (c) {
    case 'A': // United Kingdom character set
      break;
    case 'B': // ASCII set
      break;
    case '0': // Special Graphics
      break;
    case '1': // Alternate character set
      break;
    case '2':
      break;
    default:
      unknownSequence(c);
    }
  }

  private void doEscPound(char c) {
    switch (c) {
    case '8': // Esc # 8 - DECALN alignment test
      mScreen.blockSet(0, 0, mColumns, mRows, 'E', getForeColor(), getBackColor());
      break;

    default:
      unknownSequence(c);
      break;
    }
  }

  private void doEsc(char c) {
    switch (c) {
    case '#':
      continueSequence(ESC_POUND);
      break;

    case '(':
      continueSequence(ESC_SELECT_LEFT_PAREN);
      break;

    case ')':
      continueSequence(ESC_SELECT_RIGHT_PAREN);
      break;

    case '7': // DECSC save cursor
      mSavedCursorRow = mCursorRow;
      mSavedCursorCol = mCursorCol;
      break;

    case '8': // DECRC restore cursor
      setCursorRowCol(mSavedCursorRow, mSavedCursorCol);
      break;

    case 'D': // INDEX
      doLinefeed();
      break;

    case 'E': // NEL
      setCursorCol(0);
      doLinefeed();
      break;

    case 'F': // Cursor to lower-left corner of screen
      setCursorRowCol(0, mBottomMargin - 1);
      break;

    case 'H': // Tab set
      mTabStops[mCursorCol] = true;
      break;

    case 'M': // Reverse index
      if (mCursorRow == 0) {
        mScreen.blockCopy(0, mTopMargin + 1, mColumns, mBottomMargin - (mTopMargin + 1), 0,
            mTopMargin);
        blockClear(0, mBottomMargin - 1, mColumns);
      } else {
        mCursorRow--;
      }

      break;

    case 'N': // SS2
      unimplementedSequence(c);
      break;

    case '0': // SS3
      unimplementedSequence(c);
      break;

    case 'P': // Device control string
      unimplementedSequence(c);
      break;

    case 'Z': // return terminal ID
      sendDeviceAttributes();
      break;

    case '[':
      continueSequence(ESC_LEFT_SQUARE_BRACKET);
      break;

    case '=': // DECKPAM
      mbKeypadApplicationMode = true;
      break;

    case '>': // DECKPNM
      mbKeypadApplicationMode = false;
      break;

    default:
      unknownSequence(c);
      break;
    }
  }

  private void doEscLeftSquareBracket(char c) {
    switch (c) {
    case '@': // ESC [ Pn @ - ICH Insert Characters
    {
      int charsAfterCursor = mColumns - mCursorCol;
      int charsToInsert = Math.min(getArg0(1), charsAfterCursor);
      int charsToMove = charsAfterCursor - charsToInsert;
      mScreen.blockCopy(mCursorCol, mCursorRow, charsToMove, 1, mCursorCol + charsToInsert,
          mCursorRow);
      blockClear(mCursorCol, mCursorRow, charsToInsert);
    }
      break;

    case 'A': // ESC [ Pn A - Cursor Up
      setCursorRow(Math.max(mTopMargin, mCursorRow - getArg0(1)));
      break;

    case 'B': // ESC [ Pn B - Cursor Down
      setCursorRow(Math.min(mBottomMargin - 1, mCursorRow + getArg0(1)));
      break;

    case 'C': // ESC [ Pn C - Cursor Right
      setCursorCol(Math.min(mColumns - 1, mCursorCol + getArg0(1)));
      break;

    case 'D': // ESC [ Pn D - Cursor Left
      setCursorCol(Math.max(0, mCursorCol - getArg0(1)));
      break;

    case 'G': // ESC [ Pn G - Cursor Horizontal Absolute
      setCursorCol(Math.min(Math.max(1, getArg0(1)), mColumns) - 1);
      break;

    case 'H': // ESC [ Pn ; H - Cursor Position
      setHorizontalVerticalPosition();
      break;

    case 'J': // ESC [ Pn J - Erase in Display
      switch (getArg0(0)) {
      case 0: // Clear below
        blockClear(mCursorCol, mCursorRow, mColumns - mCursorCol);
        blockClear(0, mCursorRow + 1, mColumns, mBottomMargin - (mCursorRow + 1));
        break;

      case 1: // Erase from the start of the screen to the cursor.
        blockClear(0, mTopMargin, mColumns, mCursorRow - mTopMargin);
        blockClear(0, mCursorRow, mCursorCol + 1);
        break;

      case 2: // Clear all
        blockClear(0, mTopMargin, mColumns, mBottomMargin - mTopMargin);
        break;

      default:
        unknownSequence(c);
        break;
      }
      break;

    case 'K': // ESC [ Pn K - Erase in Line
      switch (getArg0(0)) {
      case 0: // Clear to right
        blockClear(mCursorCol, mCursorRow, mColumns - mCursorCol);
        break;

      case 1: // Erase start of line to cursor (including cursor)
        blockClear(0, mCursorRow, mCursorCol + 1);
        break;

      case 2: // Clear whole line
        blockClear(0, mCursorRow, mColumns);
        break;

      default:
        unknownSequence(c);
        break;
      }
      break;

    case 'L': // Insert Lines
    {
      int linesAfterCursor = mBottomMargin - mCursorRow;
      int linesToInsert = Math.min(getArg0(1), linesAfterCursor);
      int linesToMove = linesAfterCursor - linesToInsert;
      mScreen.blockCopy(0, mCursorRow, mColumns, linesToMove, 0, mCursorRow + linesToInsert);
      blockClear(0, mCursorRow, mColumns, linesToInsert);
    }
      break;

    case 'M': // Delete Lines
    {
      int linesAfterCursor = mBottomMargin - mCursorRow;
      int linesToDelete = Math.min(getArg0(1), linesAfterCursor);
      int linesToMove = linesAfterCursor - linesToDelete;
      mScreen.blockCopy(0, mCursorRow + linesToDelete, mColumns, linesToMove, 0, mCursorRow);
      blockClear(0, mCursorRow + linesToMove, mColumns, linesToDelete);
    }
      break;

    case 'P': // Delete Characters
    {
      int charsAfterCursor = mColumns - mCursorCol;
      int charsToDelete = Math.min(getArg0(1), charsAfterCursor);
      int charsToMove = charsAfterCursor - charsToDelete;
      mScreen.blockCopy(mCursorCol + charsToDelete, mCursorRow, charsToMove, 1, mCursorCol,
          mCursorRow);
      blockClear(mCursorCol + charsToMove, mCursorRow, charsToDelete);
    }
      break;

    case 'T': // Mouse tracking
      unimplementedSequence(c);
      break;

    case '?': // Esc [ ? -- start of a private mode set
      continueSequence(ESC_LEFT_SQUARE_BRACKET_QUESTION_MARK);
      break;

    case 'c': // Send device attributes
      sendDeviceAttributes();
      break;

    case 'd': // ESC [ Pn d - Vert Position Absolute
      setCursorRow(Math.min(Math.max(1, getArg0(1)), mRows) - 1);
      break;

    case 'f': // Horizontal and Vertical Position
      setHorizontalVerticalPosition();
      break;

    case 'g': // Clear tab stop
      switch (getArg0(0)) {
      case 0:
        mTabStops[mCursorCol] = false;
        break;

      case 3:
        for (int i = 0; i < mColumns; i++) {
          mTabStops[i] = false;
        }
        break;

      default:
        // Specified to have no effect.
        break;
      }
      break;

    case 'h': // Set Mode
      doSetMode(true);
      break;

    case 'l': // Reset Mode
      doSetMode(false);
      break;

    case 'm': // Esc [ Pn m - character attributes.
      selectGraphicRendition();
      break;

    case 'r': // Esc [ Pn ; Pn r - set top and bottom margins
    {
      // The top margin defaults to 1, the bottom margin
      // (unusually for arguments) defaults to mRows.
      //
      // The escape sequence numbers top 1..23, but we
      // number top 0..22.
      // The escape sequence numbers bottom 2..24, and
      // so do we (because we use a zero based numbering
      // scheme, but we store the first line below the
      // bottom-most scrolling line.
      // As a result, we adjust the top line by -1, but
      // we leave the bottom line alone.
      //
      // Also require that top + 2 <= bottom

      int top = Math.max(0, Math.min(getArg0(1) - 1, mRows - 2));
      int bottom = Math.max(top + 2, Math.min(getArg1(mRows), mRows));
      mTopMargin = top;
      mBottomMargin = bottom;

      // The cursor is placed in the home position
      setCursorRowCol(mTopMargin, 0);
    }
      break;

    default:
      parseArg(c);
      break;
    }
  }

  private void selectGraphicRendition() {
    for (int i = 0; i <= mArgIndex; i++) {
      int code = mArgs[i];
      if (code < 0) {
        if (mArgIndex > 0) {
          continue;
        } else {
          code = 0;
        }
      }
      if (code == 0) { // reset
        mInverseColors = false;
        mForeColor = 7;
        mBackColor = 0;
      } else if (code == 1) { // bold
        mForeColor |= 0x8;
      } else if (code == 4) { // underscore
        mBackColor |= 0x8;
      } else if (code == 7) { // inverse
        mInverseColors = true;
      } else if (code >= 30 && code <= 37) { // foreground color
        mForeColor = (mForeColor & 0x8) | (code - 30);
      } else if (code >= 40 && code <= 47) { // background color
        mBackColor = (mBackColor & 0x8) | (code - 40);
      } else {
        if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
          Log.w(Terminal.TAG, String.format("SGR unknown code %d", code));
        }
      }
    }
  }

  private void blockClear(int sx, int sy, int w) {
    blockClear(sx, sy, w, 1);
  }

  private void blockClear(int sx, int sy, int w, int h) {
    mScreen.blockSet(sx, sy, w, h, ' ', getForeColor(), getBackColor());
  }

  private int getForeColor() {
    return mInverseColors ? ((mBackColor & 0x7) | (mForeColor & 0x8)) : mForeColor;
  }

  private int getBackColor() {
    return mInverseColors ? ((mForeColor & 0x7) | (mBackColor & 0x8)) : mBackColor;
  }

  private void doSetMode(boolean newValue) {
    int modeBit = getArg0(0);
    switch (modeBit) {
    case 4:
      mInsertMode = newValue;
      break;
    case 20:
      break;
    default:
      unknownParameter(modeBit);
      break;
    }
  }

  private void setHorizontalVerticalPosition() {

    // Parameters are Row ; Column

    setCursorPosition(getArg1(1) - 1, getArg0(1) - 1);
  }

  private void setCursorPosition(int x, int y) {
    int effectiveTopMargin = 0;
    int effectiveBottomMargin = mRows;
    if ((mDecFlags & K_ORIGIN_MODE_MASK) != 0) {
      effectiveTopMargin = mTopMargin;
      effectiveBottomMargin = mBottomMargin;
    }
    int newRow =
        Math.max(effectiveTopMargin, Math.min(effectiveTopMargin + y, effectiveBottomMargin - 1));
    int newCol = Math.max(0, Math.min(x, mColumns - 1));
    setCursorRowCol(newRow, newCol);
  }

  private void sendDeviceAttributes() {
    // This identifies us as a DEC vt100 with advanced
    // video options. This is what the xterm terminal
    // emulator sends.
    byte[] attributes = {
    /* VT100 */
    (byte) 27, (byte) '[', (byte) '?', (byte) '1', (byte) ';', (byte) '2', (byte) 'c'

    /*
     * VT220 (byte) 27, (byte) '[', (byte) '?', (byte) '6', (byte) '0', (byte) ';', (byte) '1',
     * (byte) ';', (byte) '2', (byte) ';', (byte) '6', (byte) ';', (byte) '8', (byte) ';', (byte)
     * '9', (byte) ';', (byte) '1', (byte) '5', (byte) ';', (byte) 'c'
     */
    };

    write(attributes);
  }

  /**
   * Send data to the shell process
   * 
   * @param data
   */
  private void write(byte[] data) {
    try {
      mTermOut.write(data);
      mTermOut.flush();
    } catch (IOException e) {
      // Ignore exception
      // We don't really care if the receiver isn't listening.
      // We just make a best effort to answer the query.
    }
  }

  private void scroll() {
    try {
      mScreen.scroll(mTopMargin, mBottomMargin, getForeColor(), getBackColor());
    } catch (IllegalArgumentException e) {
      Sl4aLog.e("Scrolling failed", e);
    }
  }

  /**
   * Process the next ASCII character of a parameter.
   * 
   * @param c
   *          The next ASCII character of the paramater sequence.
   */
  private void parseArg(char c) {
    if (c >= '0' && c <= '9') {
      if (mArgIndex < mArgs.length) {
        int oldValue = mArgs[mArgIndex];
        int thisDigit = c - '0';
        int value;
        if (oldValue >= 0) {
          value = oldValue * 10 + thisDigit;
        } else {
          value = thisDigit;
        }
        mArgs[mArgIndex] = value;
      }
      continueSequence();
    } else if (c == ';') {
      if (mArgIndex < mArgs.length) {
        mArgIndex++;
      }
      continueSequence();
    } else {
      unknownSequence(c);
    }
  }

  private int getArg0(int defaultValue) {
    return getArg(0, defaultValue);
  }

  private int getArg1(int defaultValue) {
    return getArg(1, defaultValue);
  }

  private int getArg(int index, int defaultValue) {
    int result = mArgs[index];
    if (result < 0) {
      result = defaultValue;
    }
    return result;
  }

  private void unimplementedSequence(char c) {
    if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
      logError("unimplemented", c);
    }
    finishSequence();
  }

  private void unknownSequence(char c) {
    if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
      logError("unknown", c);
    }
    finishSequence();
  }

  private void unknownParameter(int parameter) {
    if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
      StringBuilder buf = new StringBuilder();
      buf.append("Unknown parameter");
      buf.append(parameter);
      logError(buf.toString());
    }
  }

  private void logError(String errorType, char c) {
    if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
      StringBuilder buf = new StringBuilder();
      buf.append(errorType);
      buf.append(" sequence ");
      buf.append(" EscapeState: ");
      buf.append(mEscapeState);
      buf.append(" char: '");
      buf.append(c);
      buf.append("' (");
      buf.append((int) c);
      buf.append(")");
      boolean firstArg = true;
      for (int i = 0; i <= mArgIndex; i++) {
        int value = mArgs[i];
        if (value >= 0) {
          if (firstArg) {
            firstArg = false;
            buf.append("args = ");
          }
          buf.append(String.format("%d; ", value));
        }
      }
      logError(buf.toString());
    }
  }

  private void logError(String error) {
    if (Terminal.LOG_UNKNOWN_ESCAPE_SEQUENCES) {
      Log.e(Terminal.TAG, error);
    }
    finishSequence();
  }

  private void finishSequence() {
    mEscapeState = ESC_NONE;
  }

  private boolean autoWrapEnabled() {
    // Always enable auto wrap, because it's useful on a small screen
    return true;
    // return (mDecFlags & K_WRAPAROUND_MODE_MASK) != 0;
  }

  /**
   * Send an ASCII character to the screen.
   * 
   * @param c
   *          the ASCII character to display.
   */
  private void emit(char c) {
    boolean autoWrap = autoWrapEnabled();

    if (autoWrap) {
      if (mCursorCol == mColumns - 1 && mAboutToAutoWrap) {
        mScreen.setLineWrap(mCursorRow);
        mCursorCol = 0;
        if (mCursorRow + 1 < mBottomMargin) {
          mCursorRow++;
        } else {
          scroll();
        }
      }
    }

    if (mInsertMode) { // Move character to right one space
      int destCol = mCursorCol + 1;
      if (destCol < mColumns) {
        mScreen.blockCopy(mCursorCol, mCursorRow, mColumns - destCol, 1, destCol, mCursorRow);
      }
    }

    mScreen.set(mCursorCol, mCursorRow, c, getForeColor(), getBackColor());

    if (autoWrap) {
      mAboutToAutoWrap = (mCursorCol == mColumns - 1);
    }

    mCursorCol = Math.min(mCursorCol + 1, mColumns - 1);
  }

  private void setCursorRow(int row) {
    mCursorRow = row;
    mAboutToAutoWrap = false;
  }

  private void setCursorCol(int col) {
    mCursorCol = col;
    mAboutToAutoWrap = false;
  }

  private void setCursorRowCol(int row, int col) {
    mCursorRow = Math.min(row, mRows - 1);
    mCursorCol = Math.min(col, mColumns - 1);
    mAboutToAutoWrap = false;
  }

  /**
   * Reset the terminal emulator to its initial state.
   */
  public void reset() {
    mCursorRow = 0;
    mCursorCol = 0;
    mArgIndex = 0;
    mContinueSequence = false;
    mEscapeState = ESC_NONE;
    mSavedCursorRow = 0;
    mSavedCursorCol = 0;
    mDecFlags = 0;
    mSavedDecFlags = 0;
    mInsertMode = false;
    mTopMargin = 0;
    mBottomMargin = mRows;
    mAboutToAutoWrap = false;
    mForeColor = 7;
    mBackColor = 0;
    mInverseColors = false;
    mbKeypadApplicationMode = false;
    // mProcessedCharCount is preserved unchanged.
    setDefaultTabStops();
    blockClear(0, 0, mColumns, mRows);
  }

  public String getTranscriptText() {
    return mScreen.getTranscriptText();
  }
}
