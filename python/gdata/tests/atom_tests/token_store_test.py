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
import atom.token_store
import atom.http_interface
import atom.service
import atom.url

class TokenStoreTest(unittest.TestCase):

  def setUp(self):
    self.token = atom.service.BasicAuthToken('aaa1', scopes=[
        'http://example.com/', 'http://example.org'])
    self.tokens = atom.token_store.TokenStore()
    self.tokens.add_token(self.token)

  def testAddAndFindTokens(self):
    self.assert_(self.tokens.find_token('http://example.com/') == self.token)
    self.assert_(self.tokens.find_token('http://example.org/') == self.token)
    self.assert_(self.tokens.find_token('http://example.org/foo?ok=1') == (
        self.token))
    self.assert_(isinstance(self.tokens.find_token('http://example.net/'),
        atom.http_interface.GenericToken))
    self.assert_(isinstance(self.tokens.find_token('example.com/'), 
        atom.http_interface.GenericToken))

  def testFindTokenUsingMultipleUrls(self):
    self.assert_(self.tokens.find_token(
        'http://example.com/') == self.token)
    self.assert_(self.tokens.find_token(
        'http://example.org/bar') == self.token)
    self.assert_(isinstance(self.tokens.find_token(''), 
        atom.http_interface.GenericToken))
    self.assert_(isinstance(self.tokens.find_token(
            'http://example.net/'), 
        atom.http_interface.GenericToken))

  def testFindTokenWithPartialScopes(self):
    token = atom.service.BasicAuthToken('aaa1', 
        scopes=[atom.url.Url(host='www.example.com', path='/foo'), 
                atom.url.Url(host='www.example.net')])
    token_store = atom.token_store.TokenStore()
    token_store.add_token(token)
    self.assert_(token_store.find_token(
        'http://www.example.com/foobar') == token)
    self.assert_(token_store.find_token(
        'https://www.example.com:443/foobar') == token)
    self.assert_(token_store.find_token(
        'http://www.example.net/xyz') == token)
    self.assert_(token_store.find_token('http://www.example.org/') != token)
    self.assert_(isinstance(token_store.find_token('http://example.org/'), 
        atom.http_interface.GenericToken))


def suite():
  return unittest.TestSuite((unittest.makeSuite(TokenStoreTest,'test'),))


if __name__ == '__main__':
  unittest.main()
