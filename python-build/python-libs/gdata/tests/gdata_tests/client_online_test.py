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
import getpass
import gdata.client
import gdata.service
import gdata


username = ''
password = ''


def Utf8String(my_string):
  return unicode(my_string, 'UTF-8')


class ClientLiveTest(unittest.TestCase):

  def setUp(self):
    self.client = gdata.client.GDataClient()

  def testUnauthenticatedReads(self):
    feed_str = self.client.Get('http://www.google.com/base/feeds/snippets', 
        parser=Utf8String)
    self.assert_(feed_str.startswith('<?xml'))
    try:
      feed_str = self.client.Get(
          'http://www.google.com/calendar/feeds/default/allcalendars/full', 
          parser=Utf8String)
      self.fail(
          'Should have received a 401 because feed requires authorization.')
    except gdata.service.RequestError, inst:
      self.assert_(inst[0]['status'] == 401)

  def testAuthenticatedReads(self):
    self.client.ClientLogin(username, password, 'cl')
    self.client.ClientLogin(username, password, 'cp')
    self.client.current_token = None
    feed_str = self.client.Get(
        'http://www.google.com/calendar/feeds/default/allcalendars/full', 
        parser=Utf8String)
    self.assert_(feed_str.startswith('<?xml'))
    feed_str = self.client.Get(
        'http://www.google.com/m8/feeds/contacts/default/full', 
        parser=Utf8String)
    self.assert_(feed_str.startswith('<?xml'))

  def testAuthenticatedWrites(self):
    self.client.ClientLogin(username, password, 'gbase')
    entry = """<entry xmlns='http://www.w3.org/2005/Atom'
                      xmlns:g='http://base.google.com/ns/1.0'>
                 <title type="text">Marie-Louise's chocolate butter</title>
                 <content type="xhtml">
                   <b>Ingredients:</b>
                   <ul>
                     <li>250g margarine,</li>
                     <li>200g sugar,</li>
                     <li>2 eggs, and</li>
                     <li>approx. 8 tsp cacao.</li>
                   </ul>
                 </content>
                 <g:item_language type="text">en</g:item_language>
                 <g:item_type type="text">testrecipes</g:item_type>
               </entry>"""
    new_entry = self.client.Post(entry, 
        'http://www.google.com/base/feeds/items', 
        parser=gdata.GDataEntryFromString)
    self.assert_(isinstance(new_entry, gdata.GDataEntry))
    self.client.Delete(new_entry.GetEditLink().href)
    

if __name__ == '__main__':
  print ('GData Client Unit Tests\nNOTE: Please run these tests only '
         'with a test  account. The tests may delete or update your data.')
  username = raw_input('Please enter your username: ')
  password = getpass.getpass()
  unittest.main()
