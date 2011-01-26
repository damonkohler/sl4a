#!/usr/bin/env python
#
# Copyright (C) 2009 Google Inc.
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


# This module is used for version 2 of the Google Data APIs.
# This test may make an actual HTTP request.


__author__ = 'j.s@google.com (Jeff Scudder)'


import unittest
import atom.http_core
import atom.auth
import atom.client
import atom.mock_http_core


class AtomPubClientEchoTest(unittest.TestCase):

  def test_simple_request_with_no_client_defaults(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    self.assert_(client.host is None)
    self.assert_(client.auth_token is None)
    # Make several equivalent requests.
    responses = [client.request('GET', 'http://example.org/'),
                 client.request(http_request=atom.http_core.HttpRequest(
                     uri=atom.http_core.Uri('http', 'example.org', path='/'),
                     method='GET')),
                 client.request('GET', 
                     http_request=atom.http_core.HttpRequest(
                         uri=atom.http_core.Uri('http', 'example.org', 
                                                path='/')))]
    for response in responses:
      self.assert_(response.getheader('Echo-Host') == 'example.org:None')
      self.assert_(response.getheader('Echo-Uri') == '/')
      self.assert_(response.getheader('Echo-Scheme') == 'http')
      self.assert_(response.getheader('Echo-Method') == 'GET')
      self.assertTrue(response.getheader('User-Agent').startswith('gdata-py/'))

  def test_auth_request_with_no_client_defaults(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    token = atom.auth.BasicAuth('Jeff', '123')
    response = client.request('POST', 'https://example.net:8080/', 
        auth_token=token)
    self.assert_(response.getheader('Echo-Host') == 'example.net:8080')
    self.assert_(response.getheader('Echo-Uri') == '/')
    self.assert_(response.getheader('Echo-Scheme') == 'https')
    self.assert_(response.getheader('Authorization') == 'Basic SmVmZjoxMjM=')
    self.assert_(response.getheader('Echo-Method') == 'POST')

  def test_request_with_client_defaults(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient(), 
        'example.com', atom.auth.BasicAuth('Jeff', '123'))
    self.assert_(client.host == 'example.com')
    self.assert_(client.auth_token is not None)
    self.assert_(client.auth_token.basic_cookie == 'SmVmZjoxMjM=')
    response = client.request('GET', 'http://example.org/')
    self.assert_(response.getheader('Echo-Host') == 'example.org:None')
    self.assert_(response.getheader('Echo-Uri') == '/')
    self.assert_(response.getheader('Echo-Scheme') == 'http')
    self.assert_(response.getheader('Echo-Method') == 'GET')
    self.assert_(response.getheader('Authorization') == 'Basic SmVmZjoxMjM=')
    response = client.request('GET', '/')
    self.assert_(response.getheader('Echo-Host') == 'example.com:None')
    self.assert_(response.getheader('Echo-Uri') == '/')
    self.assert_(response.getheader('Echo-Scheme') == 'http')
    self.assert_(response.getheader('Authorization') == 'Basic SmVmZjoxMjM=')
    response = client.request('GET', '/', 
        http_request=atom.http_core.HttpRequest(
            uri=atom.http_core.Uri(port=99)))
    self.assert_(response.getheader('Echo-Host') == 'example.com:99')
    self.assert_(response.getheader('Echo-Uri') == '/')

  def test_get(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    response = client.get('http://example.com/simple')
    self.assert_(response.getheader('Echo-Host') == 'example.com:None')
    self.assert_(response.getheader('Echo-Uri') == '/simple')
    self.assert_(response.getheader('Echo-Method') == 'GET')
    response = client.Get(uri='http://example.com/simple2')
    self.assert_(response.getheader('Echo-Uri') == '/simple2')
    self.assert_(response.getheader('Echo-Method') == 'GET')

  def test_modify_request_using_args(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    class RequestModifier(object):
      def modify_request(self, http_request):
        http_request.headers['Special'] = 'Set'
    response = client.get('http://example.com/modified', 
                          extra=RequestModifier())
    self.assert_(response.getheader('Echo-Host') == 'example.com:None')
    self.assert_(response.getheader('Echo-Uri') == '/modified')
    self.assert_(response.getheader('Echo-Method') == 'GET')
    self.assert_(response.getheader('Special') == 'Set')

  def test_post(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    class TestData(object): 
      def modify_request(self, http_request):
        http_request.add_body_part('test body', 'text/testdata')
    response = client.Post(uri='http://example.com/', data=TestData())
    self.assert_(response.getheader('Echo-Host') == 'example.com:None')
    self.assert_(response.getheader('Echo-Uri') == '/')
    self.assert_(response.getheader('Echo-Method') == 'POST')
    self.assert_(response.getheader('Content-Length') == str(len('test body')))
    self.assert_(response.getheader('Content-Type') == 'text/testdata')
    self.assert_(response.read(2) == 'te')
    self.assert_(response.read() == 'st body')
    response = client.post(data=TestData(), uri='http://example.com/')
    self.assert_(response.read() == 'test body')
    self.assert_(response.getheader('Content-Type') == 'text/testdata')
    # Don't pass in a body, but use an extra kwarg to add the body to the
    # http_request.
    response = client.post(x=TestData(), uri='http://example.com/')
    self.assert_(response.read() == 'test body')

  def test_put(self):
    body_text = '<put>test</put>' 
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient())
    class TestData(object): 
      def modify_request(self, http_request):
        http_request.add_body_part(body_text, 'application/xml')
    response = client.put('http://example.org', TestData())
    self.assert_(response.getheader('Echo-Host') == 'example.org:None')
    self.assert_(response.getheader('Echo-Uri') == '/')
    self.assert_(response.getheader('Echo-Method') == 'PUT')
    self.assert_(response.getheader('Content-Length') == str(len(body_text)))
    self.assert_(response.getheader('Content-Type') == 'application/xml')
    response = client.put(uri='http://example.org', data=TestData())
    self.assert_(response.getheader('Content-Length') == str(len(body_text)))
    self.assert_(response.getheader('Content-Type') == 'application/xml')

  def test_delete(self):
    client = atom.client.AtomPubClient(atom.mock_http_core.EchoHttpClient(),
                                       source='my new app')
    response = client.Delete('http://example.com/simple')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:None')
    self.assertEqual(response.getheader('Echo-Uri'), '/simple')
    self.assertEqual(response.getheader('Echo-Method'), 'DELETE')
    response = client.delete(uri='http://example.com/d')
    self.assertEqual(response.getheader('Echo-Uri'), '/d')
    self.assertEqual(response.getheader('Echo-Method'), 'DELETE')
    self.assertTrue(
        response.getheader('User-Agent').startswith('my new app gdata-py/'))

def suite():
  return unittest.TestSuite((unittest.makeSuite(AtomPubClientEchoTest, 'test'),
                             ))


if __name__ == '__main__':
  unittest.main()
