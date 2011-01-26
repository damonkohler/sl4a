#!/usr/bin/python
#
# Copyright (C) 2006 Google Inc.
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
import atom.service
import atom.mock_http_core
import gdata.test_config as conf

class AtomServiceUnitTest(unittest.TestCase):
  
  def testBuildUriWithNoParams(self):
    x = atom.service.BuildUri('/base/feeds/snippets')
    self.assert_(x == '/base/feeds/snippets')

  def testBuildUriWithParams(self):
    # Add parameters to a URI
    x = atom.service.BuildUri('/base/feeds/snippets', url_params={'foo': 'bar', 
                                                     'bq': 'digital camera'})
    self.assert_(x == '/base/feeds/snippets?foo=bar&bq=digital+camera')
    self.assert_(x.startswith('/base/feeds/snippets'))
    self.assert_(x.count('?') == 1)
    self.assert_(x.count('&') == 1)
    self.assert_(x.index('?') < x.index('&'))
    self.assert_(x.index('bq=digital+camera') != -1)

    # Add parameters to a URI that already has parameters
    x = atom.service.BuildUri('/base/feeds/snippets?bq=digital+camera', 
                             url_params={'foo': 'bar', 'max-results': '250'})
    self.assert_(x.startswith('/base/feeds/snippets?bq=digital+camera'))
    self.assert_(x.count('?') == 1)
    self.assert_(x.count('&') == 2)
    self.assert_(x.index('?') < x.index('&'))
    self.assert_(x.index('max-results=250') != -1)
    self.assert_(x.index('foo=bar') != -1)


  def testBuildUriWithoutParameterEscaping(self):
    x = atom.service.BuildUri('/base/feeds/snippets', 
            url_params={'foo': ' bar', 'bq': 'digital camera'}, 
            escape_params=False)
    self.assert_(x.index('foo= bar') != -1)
    self.assert_(x.index('bq=digital camera') != -1)

  def testParseHttpUrl(self):
    atom_service = atom.service.AtomService('code.google.com')
    self.assertEquals(atom_service.server, 'code.google.com')
    (host, port, ssl, path) =  atom.service.ProcessUrl(atom_service,
        'http://www.google.com/service/subservice?name=value')

    self.assertEquals(ssl, False)
    self.assertEquals(host, 'www.google.com')
    self.assertEquals(port, 80)
    self.assertEquals(path, '/service/subservice?name=value')

  def testParseHttpUrlWithPort(self):
    atom_service = atom.service.AtomService('code.google.com')
    self.assertEquals(atom_service.server, 'code.google.com')
    (host, port, ssl, path) =  atom.service.ProcessUrl(atom_service,
        'http://www.google.com:12/service/subservice?name=value&newname=newvalue')

    self.assertEquals(ssl, False)
    self.assertEquals(host, 'www.google.com')
    self.assertEquals(port, 12)
    self.assert_(path.startswith('/service/subservice?'))
    self.assert_(path.find('name=value') >= len('/service/subservice?'))
    self.assert_(path.find('newname=newvalue') >= len('/service/subservice?'))

  def testParseHttpsUrl(self):
    atom_service = atom.service.AtomService('code.google.com')
    self.assertEquals(atom_service.server, 'code.google.com')
    (host, port, ssl, path) =  atom.service.ProcessUrl(atom_service,
        'https://www.google.com/service/subservice?name=value&newname=newvalue')

    self.assertEquals(ssl, True)
    self.assertEquals(host, 'www.google.com')
    self.assertEquals(port, 443)
    self.assert_(path.startswith('/service/subservice?'))
    self.assert_(path.find('name=value') >= len('/service/subservice?'))
    self.assert_(path.find('newname=newvalue') >= len('/service/subservice?'))

  def testParseHttpsUrlWithPort(self):
    atom_service = atom.service.AtomService('code.google.com')
    self.assertEquals(atom_service.server, 'code.google.com')
    (host, port, ssl, path) =  atom.service.ProcessUrl(atom_service,
        'https://www.google.com:13981/service/subservice?name=value&newname=newvalue')

    self.assertEquals(ssl, True)
    self.assertEquals(host, 'www.google.com')
    self.assertEquals(port, 13981)
    self.assert_(path.startswith('/service/subservice?'))
    self.assert_(path.find('name=value') >= len('/service/subservice?'))
    self.assert_(path.find('newname=newvalue') >= len('/service/subservice?'))

  def testSetBasicAuth(self):
    client = atom.service.AtomService()
    client.UseBasicAuth('foo', 'bar')
    token = client.token_store.find_token('http://')
    self.assert_(isinstance(token, atom.service.BasicAuthToken))
    self.assertEquals(token.auth_header, 'Basic Zm9vOmJhcg==')
    client.UseBasicAuth('','')
    token = client.token_store.find_token('http://')
    self.assert_(isinstance(token, atom.service.BasicAuthToken))
    self.assertEquals(token.auth_header, 'Basic Og==')

  def testProcessUrlWithStringForService(self):
    (server, port, ssl, uri) = atom.service.ProcessUrl(
        service='www.google.com', url='/base/feeds/items')
    self.assertEquals(server, 'www.google.com')
    self.assertEquals(port, 80)
    self.assertEquals(ssl, False)
    self.assert_(uri.startswith('/base/feeds/items'))

    client = atom.service.AtomService()
    client.server = 'www.google.com'
    client.ssl = True
    (server, port, ssl, uri) = atom.service.ProcessUrl(
        service=client, url='/base/feeds/items')
    self.assertEquals(server, 'www.google.com')
    self.assertEquals(ssl, True)
    self.assert_(uri.startswith('/base/feeds/items'))

    (server, port, ssl, uri) = atom.service.ProcessUrl(service=None,
        url='https://www.google.com/base/feeds/items')
    self.assertEquals(server, 'www.google.com')
    self.assertEquals(port, 443)
    self.assertEquals(ssl, True)
    self.assert_(uri.startswith('/base/feeds/items'))

  def testHostHeaderContainsNonDefaultPort(self):
    client = atom.service.AtomService()
    client.http_client.v2_http_client = atom.mock_http_core.EchoHttpClient()
    response = client.Get('http://example.com')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:None')
    response = client.Get('https://example.com')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:None')
    response = client.Get('https://example.com:8080')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:8080')
    response = client.Get('http://example.com:1234')
    self.assertEqual(response.getheader('Echo-Host'), 'example.com:1234')


def suite():
  return conf.build_suite([AtomServiceUnitTest])


if __name__ == '__main__':
  unittest.main()
