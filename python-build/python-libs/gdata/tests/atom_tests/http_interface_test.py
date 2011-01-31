#!/usr/bin/python
#
# Copyright (C) 2008 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


__author__ = 'api.jscudder (Jeff Scudder)'


import unittest
import atom.http_interface
import StringIO


class HttpResponseTest(unittest.TestCase):

  def testConstructorWithStrings(self):
    resp = atom.http_interface.HttpResponse(body='Hi there!', status=200, 
        reason='OK', headers={'Content-Length':'9'})
    self.assertEqual(resp.read(amt=1), 'H')
    self.assertEqual(resp.read(amt=2), 'i ')
    self.assertEqual(resp.read(), 'there!')
    self.assertEqual(resp.read(), '')
    self.assertEqual(resp.reason, 'OK')
    self.assertEqual(resp.status, 200)
    self.assertEqual(resp.getheader('Content-Length'), '9')
    self.assertTrue(resp.getheader('Missing') is None)
    self.assertEqual(resp.getheader('Missing', default='yes'), 'yes')


def suite():
  return unittest.TestSuite((unittest.makeSuite(HttpResponseTest,'test'),))


if __name__ == '__main__':
  unittest.main()
