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

import android.view.KeyEvent;

/**
 * An ASCII key listener. Supports control characters and escape. Keeps track of the current state
 * of the alt, shift, and control keys.
 */
class TermKeyListener {

  private final ModifierKey mAltKey = new ModifierKey();
  private final ModifierKey mShiftKey = new ModifierKey();
  private final ModifierKey mControlKey = new ModifierKey();

  public void handleControlKey(boolean down) {
    if (down) {
      mControlKey.onPress();
    } else {
      mControlKey.onRelease();
    }
  }

  /**
   * Handle a keyDown event.
   * 
   * @param keyCode
   *          the keycode of the keyDown event
   * @return the ASCII byte to transmit to the pty, or -1 if this event does not produce an ASCII
   *         byte.
   */
  public int keyDown(int keyCode, KeyEvent event) {
    int result = -1;

    switch (keyCode) {
    case KeyEvent.KEYCODE_ALT_RIGHT:
    case KeyEvent.KEYCODE_ALT_LEFT:
      mAltKey.onPress();
      break;

    case KeyEvent.KEYCODE_SHIFT_LEFT:
    case KeyEvent.KEYCODE_SHIFT_RIGHT:
      mShiftKey.onPress();
      break;

    case KeyEvent.KEYCODE_ENTER:
      // Convert newlines into returns. The vt100 sends a '\r' when the 'Return' key is pressed,
      // but our KeyEvent translates this as a '\n'.
      result = '\r';
      break;

    case KeyEvent.KEYCODE_DEL:
      // Convert DEL into 127 (instead of 8)
      result = 127;
      break;

    default:
      int methaShiftOn = mShiftKey.isActive() ? KeyEvent.META_SHIFT_ON : 0;
      int metaAltOn = mAltKey.isActive() ? KeyEvent.META_ALT_ON : 0;
      result = event.getUnicodeChar(methaShiftOn | metaAltOn);
      break;
    }

    if (mControlKey.isActive()) {
      // Search is the control key.
      if (result >= 'a' && result <= 'z') {
        result = (char) (result - 'a' + '\001');
      } else if (result == ' ') {
        result = 0;
      } else if ((result == '[') || (result == '1')) {
        result = 27;
      } else if ((result == '\\') || (result == '.')) {
        result = 28;
      } else if ((result == ']') || (result == '0')) {
        result = 29;
      } else if ((result == '^') || (result == '6')) {
        result = 30; // control-^
      } else if ((result == '_') || (result == '5')) {
        result = 31;
      }
    }

    if (result > -1) {
      mAltKey.adjustAfterKeypress();
      mShiftKey.adjustAfterKeypress();
      mControlKey.adjustAfterKeypress();
    }

    return result;
  }

  /**
   * Handle a keyUp event.
   * 
   * @param keyCode
   *          the keyCode of the keyUp event
   */
  public void keyUp(int keyCode) {
    switch (keyCode) {
    case KeyEvent.KEYCODE_ALT_LEFT:
    case KeyEvent.KEYCODE_ALT_RIGHT:
      mAltKey.onRelease();
      break;
    case KeyEvent.KEYCODE_SHIFT_LEFT:
    case KeyEvent.KEYCODE_SHIFT_RIGHT:
      mShiftKey.onRelease();
      break;
    default:
      // Ignore other keyUps
      break;
    }
  }
}
