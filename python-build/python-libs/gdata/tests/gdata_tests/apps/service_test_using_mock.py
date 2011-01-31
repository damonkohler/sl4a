#!/usr/bin/python
#
# Copyright (C) 2007 SIOS Technology, Inc.
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

__author__ = 'tmatsuo@sios.com (Takashi Matsuo)'

import unittest
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom
import atom.service
import atom.mock_service
import gdata.apps
import gdata.apps.service
import getpass
import time, os

apps_domain = 'test.shehas.net'
apps_username = 'xxxxx'
apps_password = 'xxxxx'

class AppsServiceUsingMockUnitTest01(unittest.TestCase):
  
  def setUp(self):
    email = apps_username + '@' + apps_domain
    self.apps_client = gdata.apps.service.AppsService(
      email=email, domain=apps_domain, password=apps_password,
      source='AppsClient "Unit" Tests')
    self.apps_client.handler = atom.mock_service
    datafile = os.path.join(os.path.dirname(os.path.abspath(__file__)),
                                 "TestDataForGeneratorTest.p")
    f = open(datafile, "r")
    atom.mock_service.LoadRecordings(f)
    f.close()
    self.user_names = []
    for i in range(100):
      self.user_names += ['testuser-20080307140302-%03d' % i]
    self.user_names += ['tmatsuo']
    self.user_num = 101
    
  def tearDown(self):
    pass

  def test001GetGeneratorForAllUsers(self):
    """Tests GetGeneratorForAllUsers method"""
    generator = self.apps_client.GetGeneratorForAllUsers()
    i = 0
    for user_feed in generator:
      for a_user in user_feed.entry:
        self.assert_(a_user.login.user_name == self.user_names[i])
        i = i + 1
    self.assert_(i == self.user_num)

if __name__ == '__main__':
  unittest.main()
