# Copyright (C) 2010 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not
# use this file except in compliance with the License. You may obtain a copy of
# the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations under
# the License.

__author__ = 'MeanEYE.rcf <meaneye.rcf@gmail.com>'
__copyright__ = 'Copyright (c) 2010, Google Inc.'
__license__ = 'Apache License, Version 2.0'

__all__ = [
    'AlertDialog',
    'ProgressDialog',
    'SpinnerProgress',
    'BUTTON_LEFT',
    'BUTTON_RIGHT',
    'BUTTON_MIDDLE',
    'PROGRESS_SPINNER',
    'PROGRESS_HORIZONTAL',
    ]


# button position constants
BUTTON_LEFT = 0
BUTTON_RIGHT = 1
BUTTON_MIDDLE = 2

# progress dialog types
PROGRESS_SPINNER = 0
PROGRESS_HORIZONTAL = 1


class Dialog:
  """Base dialog

  This class is not meant for regular users. It provides
  commonly used methods to other classes.

  Do *NOT* use this class in your programs!

  """
  def __init__(self, AndroidProxy):
    self._uuid = None
    self._proxy = AndroidProxy

    self._title = None
    self._message = None

  def show(self):
    """Show dialog

    Note: Once dialog is displayed you
    can't change anything!

    """
    assert self._uuid is not None
    self._proxy.dialogShow(self._uuid)

  def dismiss(self):
    """Dismiss dialog"""
    assert self._uuid is not None

    self._proxy.dialogDismiss(self._uuid)
    self._uuid = None;

  def set_title(self, title):
    """Set dialog title"""
    self._title = title

  def set_message(self, message):
    """Set dialog message"""
    self._message = message

  def set_cancelable(self, cancelable=True):
    """Set dialog cancelable"""
    self._cancelable = cancelable


class AlertDialog(Dialog):
  """Alert dialog with additional options"""

  def __init__(self, AndroidProxy, title='', message='', cancelable=False):
    Dialog.__init__(self, AndroidProxy)

    self._title = title
    self._message = message
    self._cancelable = cancelable

  def set_button(self, text, position=BUTTON_LEFT):
    """Add button to alert dialog"""
    assert self._uuid is not None

    self._proxy.dialogSetButton(self._uuid, position, text)

  def get_response(self):
    """Wait for user response on dialog"""
    assert self._uuid is not None

    result = self._proxy.dialogGetResponse(self._uuid).result
    return result

  def show(self):
    """Show dialog"""

    # create alert dialog
    self._uuid = self._proxy.dialogCreateAlert(
                        self._title,
                        self._message,
                        self._cancelable
                      ).result

    # show newly created dialog
    Dialog.show(self)


class ProgressDialog(Dialog):
  """Progress dialog"""

  def __init__(self, AndroidProxy, type=PROGRESS_SPINNER, title='', message='', cancelable=False):
    Dialog.__init__(self, AndroidProxy)

    self._type = type
    self._title = title
    self._message = message
    self._cancelable = cancelable
    self._max = 100
    self._current = 0

  def set_max(self, max):
    """Set progress maximum value"""
    self._max = max

    if self._uuid is not None:
      self._proxy.dialogProgressSetMax(self._uuid, self._max)

  def set_current(self, current):
    """Set current dialog progress"""
    assert self._uuid is not None

    self._current = current
    self._proxy.dialogProgressSetCurrent(self._uuid, self._current)

  def show(self):
    """Show dialog"""

    method = {
        PROGRESS_SPINNER: self._proxy.dialogCreateSpinnerProgress,
        PROGRESS_HORIZONTAL: self._proxy.dialogCreateHorizontalProgress,
      }

    # create dialog
    self._uuid = method[self._type](
                    self._title,
                    self._message,
                    self._max,
                    self._cancelable
                  ).result

    # show newly created dialog
    Dialog.show(self)


class SpinnerProgress(ProgressDialog):
  """Spinner progress dialog class"""

  def __init__(self, AndroidProxy, title='', message='', max=100, cancelable=False):
    ProgressDialog.__init__(
                self,
                AndroidProxy,
                PROGRESS_SPINNER,
                title,
                message,
                cancelable
              )

    self.set_max(max)


class HorizontalProgress(ProgressDialog):
  """Horizontal progress dialog class"""

  def __init__(self, AndroidProxy, title='', message='', max=100, cancelable=False):
    ProgressDialog.__init__(
                self,
                AndroidProxy,
                PROGRESS_HORIZONTAL,
                title,
                message,
                cancelable
              )

    self.set_max(max)
