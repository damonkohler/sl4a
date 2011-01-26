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
# Tests for v2 features.
import atom_tests.core_test
import atom_tests.data_test
import atom_tests.http_core_test
import atom_tests.auth_test
import atom_tests.mock_http_core_test
import atom_tests.client_test
import gdata_tests.client_test
import gdata_tests.data_test
import gdata_tests.live_client_test
import gdata_tests.gauth_test
import gdata_tests.blogger.data_test
import gdata_tests.blogger.live_client_test
# Compatibility tests for requests to v1 feeds.
import gdata_tests.contacts.service_test
# Tests for v1 classes.
import atom_test
import gdata_test
import atom_tests.service_test
import atom_tests.auth_test
import atom_tests.http_interface_test
import atom_tests.mock_client_test
import atom_tests.mock_http_test
import atom_tests.token_store_test
import atom_tests.url_test


def suite():
  return unittest.TestSuite((atom_tests.core_test.suite(),
                             atom_tests.data_test.suite(),
                             atom_tests.http_core_test.suite(),
                             atom_tests.auth_test.suite(),
                             atom_tests.mock_http_core_test.suite(),
                             atom_tests.client_test.suite(),
                             gdata_tests.client_test.suite(),
                             gdata_tests.data_test.suite(),
                             gdata_tests.live_client_test.suite(),
                             gdata_tests.gauth_test.suite(),
                             gdata_tests.blogger.data_test.suite(),
                             gdata_tests.blogger.live_client_test.suite(),
                             gdata_tests.contacts.service_test.suite(),
                             atom_test.suite(), gdata_test.suite(),
                             atom_tests.service_test.suite(),
                             atom_tests.auth_test.suite(),
                             atom_tests.http_interface_test.suite(),
                             atom_tests.mock_client_test.suite(),
                             atom_tests.mock_http_test.suite(),
                             atom_tests.token_store_test.suite(),
                             atom_tests.url_test.suite()))


if __name__ == '__main__':
  unittest.TextTestRunner().run(suite())
