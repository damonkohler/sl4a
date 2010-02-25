#!/usr/bin/env python
#
#   Copyright (C) 2009 Google Inc.
#
#   Licensed under the Apache License, Version 2.0 (the "License"); you may not
#   use this file except in compliance with the License. You may obtain a copy of
#   the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
#   WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
#   License for the specific language governing permissions and limitations under
#   the License.

__author__ = 'MeanEYE.rcf <meaneye.rcf@gmail.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'

__all__ = ['AlertDialog', 'ProgressDialog']


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
	commonly used methods to other child classes.

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
		assert self._uuid is not None

		self._title = title
		self._proxy.dialogSetTitle(self._uuid, title)

	def set_message(self, message):
		"""Set dialog message"""
		assert self._uuid is not None

		self._message = message
		self._proxy.dialogSetMessage(self._uuid, message)


class AlertDialog(Dialog):
	"""Alert dialog with additional options"""

	def __init__(self, AndroidProxy, title='', message='', cancelable=False):
		Dialog.__init__(self, AndroidProxy)

		# create alert dialog
		self._uuid = self._proxy.dialogCreateAlert(title, message, cancelable)['result']

	def set_button(self, text, position=BUTTON_LEFT):
		"""Add button to alert dialog"""
		assert self._uuid is not None

		self._proxy.dialogSetButton(self._uuid, position, text)

	def get_response(self):
		"""Wait for user response on dialog"""
		assert self._uuid is not None

		result = self._proxy.dialogGetResponse(self._uuid)['result']
		return result


class ProgressDialog(Dialog):
	"""Progress dialog"""

	def __init__(self, AndroidProxy, type=PROGRESS_SPINNER, title='', message='', cancelable=False):
		Dialog.__init__(self, AndroidProxy)

		# create progress dialog
		self._type = type;

		self._uuid = self._proxy.dialogCreateProgress(self._type, title, message, cancelable)['result']

	def set_max(self, max):
		"""Set progress maximum value"""
		assert self._uuid is not None

		self._proxy.dialogProgressSetMax(self._uuid, max)

	def set_progress(self, progress):
		"""Set current dialog progress"""
		assert self._uuid is not None

		self._proxy.dialogProgressSetCurrent(self._uuid, progress)
