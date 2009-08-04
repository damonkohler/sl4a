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


__author__ = 'j.s@google.com (Jeff Scudder)'


import unittest
import atom.mock_http
import atom.http


class MockHttpClientUnitTest(unittest.TestCase):
  
  def setUp(self):
    self.client = atom.mock_http.MockHttpClient()

  def testRepondToGet(self):
    mock_response = atom.http_interface.HttpResponse(body='Hooray!',
         status=200, reason='OK')
    self.client.add_response(mock_response, 'GET', 
         'http://example.com/hooray')

    response = self.client.request('GET', 'http://example.com/hooray')

    self.assertEquals(len(self.client.recordings), 1)
    self.assertEquals(response.status, 200)
    self.assertEquals(response.read(), 'Hooray!')

  def testRecordResponse(self):
    # Turn on pass-through record mode.
    self.client.real_client = atom.http.ProxiedHttpClient()
    live_response = self.client.request('GET', 
        'http://www.google.com/base/feeds/snippets?max-results=1')
    live_response_body = live_response.read()
    self.assertEquals(live_response.status, 200)
    self.assertEquals(live_response_body.startswith('<?xml'), True)

    # Requery for the now canned data.
    self.client.real_client = None
    canned_response = self.client.request('GET',
        'http://www.google.com/base/feeds/snippets?max-results=1')

    # The canned response should be the stored response.
    canned_response_body = canned_response.read()
    self.assertEquals(canned_response.status, 200)
    self.assertEquals(canned_response_body, live_response_body)

  def testUnrecordedRequest(self):
    try:
      self.client.request('POST', 'http://example.org')
      self.fail()
    except atom.mock_http.NoRecordingFound:
      pass

def suite():
  return unittest.TestSuite(
      (unittest.makeSuite(MockHttpClientUnitTest,'test'),))

if __name__ == '__main__':
  unittest.main()
