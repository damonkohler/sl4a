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

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Exec;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.OnGestureListener;

import com.google.ase.R;
import com.google.ase.interpreter.InterpreterProcessInterface;

/**
 * A view on a transcript and a terminal emulator. Displays the text of the transcript and the
 * current cursor position of the terminal emulator.
 */
class EmulatorView extends View implements OnGestureListener {

  public static final String TAG = "EmulatorView";

  /**
   * Our transcript. Contains the screen and the transcript.
   */
  private TranscriptScreen mTranscriptScreen;

  /**
   * Number of rows in the transcript.
   */
  private static final int TRANSCRIPT_ROWS = 10000;

  /**
   * Total width of each character, in pixels
   */
  private int mCharacterWidth;

  /**
   * Total height of each character, in pixels
   */
  private int mCharacterHeight;

  /**
   * Used to render text
   */
  private TextRenderer mTextRenderer;

  /**
   * Text size. Zero means 4 x 8 font.
   */
  private int mTextSize;

  /**
   * Foreground color.
   */
  private int mForeground;

  /**
   * Background color.
   */
  private int mBackground;

  /**
   * Used to paint the cursor
   */
  private Paint mCursorPaint;

  private Paint mBackgroundPaint;

  /**
   * Our terminal emulator. We use this to get the current cursor position.
   */
  private TerminalEmulator mEmulator;

  /**
   * The number of rows of text to display.
   */
  private int mRows;

  /**
   * The number of columns of text to display.
   */
  private int mColumns;

  /**
   * The number of columns that are visible on the display.
   */

  private int mVisibleColumns;

  /**
   * The top row of text to display. Ranges from -activeTranscriptRows to 0
   */
  private int mTopRow;

  private int mLeftColumn;

  /**
   * Used to communicate with the process.
   */
  private Reader mTermIn;
  private PrintStream mTermOut;
  private FileDescriptor mTermFd;

  private final static int MAX_BYTES_PER_UPDATE = 512;
  private final Queue<Character> mReceiveBuffer =
      new ArrayBlockingQueue<Character>(MAX_BYTES_PER_UPDATE);

  /**
   * Our private message id, which we use to receive new input from the remote process.
   */
  private static final int UPDATE = 1;

  /**
   * Thread that polls for input from the remote process
   */
  private Thread mPollingThread;

  private GestureDetector mGestureDetector;

  private float mScrollRemainder;

  /**
   * Our message handler class. Implements a periodic callback.
   */
  private final Handler mHandler = new Handler() {
    @Override
    public void handleMessage(Message msg) {
      if (msg.what == UPDATE) {
        char[] buffer = new char[MAX_BYTES_PER_UPDATE];
        int index = 0;
        while (!mReceiveBuffer.isEmpty() && index < MAX_BYTES_PER_UPDATE) {
          buffer[index++] = mReceiveBuffer.poll();
        }
        append(buffer, 0, index);
      }
    }
  };

  public EmulatorView(Context context) {
    super(context);
    initEmulatorView();
  }

  public EmulatorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(R.styleable.EmulatorView);
    initializeScrollbars(a);
    a.recycle();
    initEmulatorView();
  }

  private void initEmulatorView() {
    mTextRenderer = null;
    mCursorPaint = new Paint();
    mCursorPaint.setARGB(255, 128, 128, 128);
    mBackgroundPaint = new Paint();
    mTopRow = 0;
    mLeftColumn = 0;
    mGestureDetector = new GestureDetector(this);
    mGestureDetector.setIsLongpressEnabled(false);
    setVerticalScrollBarEnabled(true);
    mTextSize = 10;
    mForeground = Terminal.WHITE;
    mBackground = Terminal.BLACK;
  }

  public void setColors(int foreground, int background) {
    mForeground = foreground;
    mBackground = background;
  }

  /**
   * Sets the text size, which in turn sets the number of rows and columns
   *
   * @param fontSize
   *          the new font size, in pixels.
   */
  public void setTextSize(int fontSize) {
    mTextSize = fontSize;
  }

  public String getTranscriptText() {
    return mEmulator.getTranscriptText();
  }

  public void resetTerminal() {
    mEmulator.reset();
    invalidate();
  }

  public boolean getKeypadApplicationMode() {
    return mEmulator.getKeypadApplicationMode();
  }

  @Override
  protected int computeVerticalScrollRange() {
    return mTranscriptScreen.getActiveRows();
  }

  @Override
  protected int computeVerticalScrollExtent() {
    return mRows;
  }

  @Override
  protected int computeVerticalScrollOffset() {
    return mTranscriptScreen.getActiveRows() + mTopRow - mRows;
  }

  /**
   * Configures the view to use the supplied interpreter process.
   */
  public void attachInterpreterProcess(InterpreterProcessInterface interpreter) {
    mTermOut = interpreter.getOut();
    mTermIn = interpreter.getIn();
    mTermFd = interpreter.getFd();
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (changed) {
      update();
      // A new thread is only created on startup (or if the polling thread dies for some reason).
      // During a layout/orientation change, this is a no-op.
      startInputPollingThread();
    }
  }

  /**
   * Accept a sequence of bytes (typically from the pseudo-tty) and process them.
   *
   * @param receiveBuffer
   *          a byte array containing bytes to be processed
   * @param base
   *          the index of the first byte in the buffer to process
   * @param length
   *          the number of bytes to process
   */
  public void append(char[] receiveBuffer, int base, int length) {
    mEmulator.append(receiveBuffer, base, length);
    ensureCursorVisible();
    invalidate();
  }

  /**
   * Page the terminal view (scroll it up or down by delta screenfulls.)
   *
   * @param delta
   *          the number of screens to scroll. Positive means scroll down, negative means scroll up.
   */
  public void page(int delta) {
    mTopRow =
        Math.min(0, Math.max(-(mTranscriptScreen.getActiveTranscriptRows()), mTopRow + mRows
            * delta));
    invalidate();
  }

  /**
   * Page the terminal view horizontally.
   *
   * @param deltaColumns
   *          the number of columns to scroll. Positive scrolls to the right.
   */
  public void pageHorizontal(int deltaColumns) {
    mLeftColumn = Math.max(0, Math.min(mLeftColumn + deltaColumns, mColumns - mVisibleColumns));
    invalidate();
  }

  @Override
  public boolean onSingleTapUp(MotionEvent e) {
    return true;
  }

  @Override
  public void onLongPress(MotionEvent e) {
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    distanceY += mScrollRemainder;
    int deltaRows = (int) (distanceY / mCharacterHeight);
    mScrollRemainder = distanceY - deltaRows * mCharacterHeight;
    mTopRow =
        Math.min(0, Math.max(-(mTranscriptScreen.getActiveTranscriptRows()), mTopRow + deltaRows));
    invalidate();

    return true;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    mScrollRemainder = 0.0f;
    onScroll(e1, e2, 2 * velocityX, -2 * velocityY);
    return true;
  }

  @Override
  public void onShowPress(MotionEvent e) {
  }

  @Override
  public boolean onDown(MotionEvent e) {
    mScrollRemainder = 0.0f;
    return true;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    return mGestureDetector.onTouchEvent(ev);
  }

  /**
   * Updates the emulator display to accommodate changes in window size, font, and color.
   */
  void update() {
    // An invalid width and height is an indication that update has been called before we're ready.
    // It's safe to ignore this call to update because we will call update again once we're ready.
    if (getWidth() > 0 && getHeight() > 0) {
      updateText();
      updateSize(getWidth(), getHeight());
    }
  }

  private void updateText() {
    if (mTextSize > 0) {
      mTextRenderer = new PaintRenderer(mTextSize, mForeground, mBackground);
    } else {
      mTextRenderer = new Bitmap4x8FontRenderer(getResources(), mForeground, mBackground);
    }
    mBackgroundPaint.setColor(mBackground);
    mCharacterWidth = mTextRenderer.getCharacterWidth();
    mCharacterHeight = mTextRenderer.getCharacterHeight();
  }

  private void updateSize(int w, int h) {
    mColumns = w / mCharacterWidth;
    mRows = h / mCharacterHeight;

    // If we're attached to a process, inform it of our new size.
    if (mTermFd != null) {
      Exec.setPtyWindowSize(mTermFd, mRows, mColumns, w, h);
    }

    if (mTranscriptScreen != null) {
      mEmulator.updateSize(mColumns, mRows);
    } else {
      mTranscriptScreen =
          new TranscriptScreen(mColumns, TRANSCRIPT_ROWS, mRows, mForeground, mBackground);
      mEmulator = new TerminalEmulator(mTranscriptScreen, mColumns, mRows, mTermOut);
    }

    // Reset our paging:
    mTopRow = 0;
    mLeftColumn = 0;

    invalidate();
  }

  /**
   * Set up a thread to read input from the pseudo-teletype.
   */
  private void startInputPollingThread() {
    if (mPollingThread != null && mPollingThread.isAlive()) {
      return;
    }
    mPollingThread = new Thread(new Runnable() {
      public void run() {
        while (true) {
          if (mTermIn == null) {
            Log.e("EmulatorView", "No terminal input. Exiting thread.", null);
            break;
          }
          try {
            int c = mTermIn.read();
            if (c > 0) {
              mReceiveBuffer.offer(Character.valueOf((char) c));
              mHandler.sendMessage(mHandler.obtainMessage(UPDATE));
            }
          } catch (IOException e) {
            Log.e("EmulatorView", "Failed to read. Exiting thread.", e);
            break;
          }
        }
      }
    });

    mPollingThread.setName("UpdateInput");
    mPollingThread.start();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    int w = getWidth();
    int h = getHeight();
    canvas.drawRect(0, 0, w, h, mBackgroundPaint);
    mVisibleColumns = w / mCharacterWidth;
    float x = -mLeftColumn * mCharacterWidth;
    float y = mCharacterHeight;
    int endLine = mTopRow + mRows;
    int cx = mEmulator.getCursorCol();
    int cy = mEmulator.getCursorRow();
    for (int i = mTopRow; i < endLine; i++) {
      int cursorX = -1;
      if (i == cy) {
        cursorX = cx;
      }
      mTranscriptScreen.drawText(i, canvas, x, y, mTextRenderer, cursorX);
      y += mCharacterHeight;
    }
  }

  private void ensureCursorVisible() {
    mTopRow = 0;
    if (mVisibleColumns > 0) {
      int cx = mEmulator.getCursorCol();
      int visibleCursorX = mEmulator.getCursorCol() - mLeftColumn;
      if (visibleCursorX < 0) {
        mLeftColumn = cx;
      } else if (visibleCursorX >= mVisibleColumns) {
        mLeftColumn = (cx - mVisibleColumns) + 1;
      }
    }
  }
}
