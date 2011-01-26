#!/usr/bin/python
#
# Copyright 2009 Google Inc. All Rights Reserved.
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

__author__ = 'api.eric@google.com (Eric Bidelman)'

import getpass
import unittest
from gdata import test_data
import gdata.health
import gdata.health.service

username = ''
password = ''

class HealthQueryProfileListTest(unittest.TestCase):

  def setUp(self):
    self.health = gdata.health.service.HealthService()
    self.health.ClientLogin(username, password, source='Health Client Unit Tests')
    self.profile_list_feed = self.health.GetProfileListFeed()

  def testGetProfileListFeed(self):
    self.assert_(isinstance(self.profile_list_feed,
                            gdata.health.ProfileListFeed))
    self.assertEqual(self.profile_list_feed.id.text,
                     'https://www.google.com/health/feeds/profile/list')
    first_entry = self.profile_list_feed.entry[0]
    self.assert_(isinstance(first_entry, gdata.health.ProfileListEntry))
    self.assert_(first_entry.GetProfileId() is not None)
    self.assert_(first_entry.GetProfileName() is not None)

    query = gdata.health.service.HealthProfileListQuery()
    profile_list = self.health.GetProfileListFeed(query)
    self.assertEqual(first_entry.GetProfileId(),
                     profile_list.entry[0].GetProfileId())
    self.assertEqual(profile_list.id.text,
                     'https://www.google.com/health/feeds/profile/list')


class H9QueryProfileListTest(unittest.TestCase):

  def setUp(self):
    self.h9 = gdata.health.service.HealthService(use_h9_sandbox=True)
    self.h9.ClientLogin(username, password, source='H9 Client Unit Tests')
    self.profile_list_feed = self.h9.GetProfileListFeed()

  def testGetProfileListFeed(self):
    self.assert_(isinstance(self.profile_list_feed,
                            gdata.health.ProfileListFeed))
    self.assertEqual(self.profile_list_feed.id.text,
                     'https://www.google.com/h9/feeds/profile/list')
    first_entry = self.profile_list_feed.entry[0]
    self.assert_(isinstance(first_entry, gdata.health.ProfileListEntry))
    self.assert_(first_entry.GetProfileId() is not None)
    self.assert_(first_entry.GetProfileName() is not None)

    query = gdata.health.service.HealthProfileListQuery()
    profile_list = self.h9.GetProfileListFeed(query)
    self.assertEqual(first_entry.GetProfileId(),
                     profile_list.entry[0].GetProfileId())
    self.assertEqual(profile_list.id.text,
                     'https://www.google.com/h9/feeds/profile/list')


class HealthQueryProfileTest(unittest.TestCase):

  def setUp(self):
    self.health = gdata.health.service.HealthService()
    self.health.ClientLogin(username, password, source='Health Client Unit Tests')
    self.profile_list_feed = self.health.GetProfileListFeed()
    self.profile_id = self.profile_list_feed.entry[0].GetProfileId()

  def testGetProfileFeed(self):
    feed = self.health.GetProfileFeed(profile_id=self.profile_id)
    self.assert_(isinstance(feed, gdata.health.ProfileFeed))
    self.assert_(isinstance(feed.entry[0].ccr, gdata.health.Ccr))

  def testGetProfileFeedByQuery(self):
    query = gdata.health.service.HealthProfileQuery(
        projection='ui', profile_id=self.profile_id)
    feed = self.health.GetProfileFeed(query=query)
    self.assert_(isinstance(feed, gdata.health.ProfileFeed))
    self.assert_(feed.entry[0].ccr is not None)

  def testGetProfileDigestFeed(self):
    query = gdata.health.service.HealthProfileQuery(
        projection='ui', profile_id=self.profile_id,
        params={'digest': 'true'})
    feed = self.health.GetProfileFeed(query=query)
    self.assertEqual(len(feed.entry), 1)

  def testGetMedicationsAndConditions(self):
    query = gdata.health.service.HealthProfileQuery(
        projection='ui', profile_id=self.profile_id,
        params={'digest': 'true'}, categories=['medication|condition'])
    feed = self.health.GetProfileFeed(query=query)
    self.assertEqual(len(feed.entry), 1)
    if feed.entry[0].ccr.GetMedications() is not None:
      self.assert_(feed.entry[0].ccr.GetMedications()[0] is not None)
    self.assert_(feed.entry[0].ccr.GetConditions()[0] is not None)
    self.assert_(feed.entry[0].ccr.GetAllergies() is None)
    self.assert_(feed.entry[0].ccr.GetAlerts() is None)
    self.assert_(feed.entry[0].ccr.GetResults() is None)


class H9QueryProfileTest(unittest.TestCase):

  def setUp(self):
    self.h9 = gdata.health.service.HealthService(use_h9_sandbox=True)
    self.h9.ClientLogin(username, password, source='H9 Client Unit Tests')
    self.profile_list_feed = self.h9.GetProfileListFeed()
    self.profile_id = self.profile_list_feed.entry[0].GetProfileId()

  def testGetProfileFeed(self):
    feed = self.h9.GetProfileFeed(profile_id=self.profile_id)
    self.assert_(isinstance(feed, gdata.health.ProfileFeed))
    self.assert_(feed.entry[0].ccr is not None)

  def testGetProfileFeedByQuery(self):
    query = gdata.health.service.HealthProfileQuery(
        service='h9', projection='ui', profile_id=self.profile_id)
    feed = self.h9.GetProfileFeed(query=query)
    self.assert_(isinstance(feed, gdata.health.ProfileFeed))
    self.assert_(feed.entry[0].ccr is not None)


class HealthNoticeTest(unittest.TestCase):

  def setUp(self):
    self.health = gdata.health.service.HealthService()
    self.health.ClientLogin(username, password, source='Health Client Unit Tests')
    self.profile_list_feed = self.health.GetProfileListFeed()
    self.profile_id = self.profile_list_feed.entry[0].GetProfileId()

  def testSendNotice(self):
    subject_line = 'subject line'
    body = 'Notice <b>body</b>.'
    ccr_xml = test_data.HEALTH_CCR_NOTICE_PAYLOAD
    created_entry = self.health.SendNotice(subject_line,
                                           body,
                                           ccr=ccr_xml,
                                           profile_id=self.profile_id)
    self.assertEqual(created_entry.title.text, subject_line)
    self.assertEqual(created_entry.content.text, body)
    self.assertEqual(created_entry.content.type, 'html')

    problem = created_entry.ccr.GetProblems()[0]
    problem_desc = problem.FindChildren('Description')[0]
    name = problem_desc.FindChildren('Text')[0]
    self.assertEqual(name.text, 'Aortic valve disorders')


class H9NoticeTest(unittest.TestCase):

  def setUp(self):
    self.h9 = gdata.health.service.HealthService(use_h9_sandbox=True)
    self.h9.ClientLogin(username, password, source='H9 Client Unit Tests')
    self.profile_list_feed = self.h9.GetProfileListFeed()
    self.profile_id = self.profile_list_feed.entry[0].GetProfileId()

  def testSendNotice(self):
    subject_line = 'subject line'
    body = 'Notice <b>body</b>.'
    ccr_xml = test_data.HEALTH_CCR_NOTICE_PAYLOAD
    created_entry = self.h9.SendNotice(subject_line, body, ccr=ccr_xml,
                                       profile_id=self.profile_id)
    self.assertEqual(created_entry.title.text, subject_line)
    self.assertEqual(created_entry.content.text, body)
    self.assertEqual(created_entry.content.type, 'html')

    problem = created_entry.ccr.GetProblems()[0]
    problem_desc = problem.FindChildren('Description')[0]
    name = problem_desc.FindChildren('Text')[0]
    self.assertEqual(name.text, 'Aortic valve disorders')


if __name__ == '__main__':
  print ('Health API Tests\nNOTE: Please run these tests only with a test '
         'account. The tests may delete or update your data.')
  username = raw_input('Please enter your username: ')
  password = getpass.getpass()
  unittest.main()
