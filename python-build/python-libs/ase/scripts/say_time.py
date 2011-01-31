"""Speak the time."""

__author__ = 'T.V. Raman <raman@google.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'

import android
import time

droid = android.Android()
droid.ttsSpeak(time.strftime("%_I %M %p on %A, %B %_e, %Y "))
