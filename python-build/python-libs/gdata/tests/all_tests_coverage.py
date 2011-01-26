#!/usr/bin/env python
#
#    Copyright (C) 2009 Google Inc.
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.


# This module is used for version 2 of the Google Data APIs.


__author__ = 'j.s@google.com (Jeff Scudder)'


import unittest
import coverage
import all_tests
import atom.core
import atom.http_core
import atom.mock_http_core
import atom.auth
import atom.client
import gdata.gauth
import gdata.client
import gdata.data
import gdata.blogger.data
import gdata.blogger.client
from gdata.test_config import settings


# Ensure that coverage tests execute the live requests to the servers, but
# allow use of cached server responses to speed up repeated runs.
settings.RUN_LIVE_TESTS = True
settings.CLEAR_CACHE = False


def suite():
  return unittest.TestSuite((atom_tests.core_test.suite(),))

if __name__ == '__main__':
  coverage.erase()
  coverage.start()
  unittest.TextTestRunner().run(all_tests.suite())
  coverage.stop()
  coverage.report([atom.core, atom.http_core, atom.auth, atom.data,
      atom.mock_http_core, atom.client, gdata.gauth, gdata.client,
      gdata.data, gdata.blogger.data, gdata.blogger.client])
