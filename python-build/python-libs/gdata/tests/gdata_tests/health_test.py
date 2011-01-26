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


import unittest
from gdata import test_data
import gdata.health
import gdata.health.service


class ProfileEntryTest(unittest.TestCase):

  def setUp(self):
    self.profile_entry = gdata.health.ProfileEntryFromString(
        test_data.HEALTH_PROFILE_ENTRY_DIGEST)

  def testToAndFromStringWithData(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))

    self.assert_(isinstance(entry, gdata.health.ProfileEntry))
    self.assert_(isinstance(entry.ccr, gdata.health.Ccr))
    self.assertEqual(len(entry.ccr.GetMedications()), 3)
    self.assertEqual(len(entry.ccr.GetImmunizations()), 1)
    self.assertEqual(len(entry.ccr.GetAlerts()), 2)
    self.assertEqual(len(entry.ccr.GetResults()), 1)
    self.assertEqual(len(entry.ccr.GetProblems()), 2)
    self.assertEqual(len(entry.ccr.GetProcedures()), 2)

  def testGetResultsTextFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    result = entry.ccr.GetResults()[0].FindChildren('Test')[0]
    test_desc = result.FindChildren('Description')[0].FindChildren('Text')[0]
    self.assertEqual(test_desc.text, 'Acetaldehyde - Blood')

  def testGetMedicationNameFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    product = entry.ccr.GetMedications()[1].FindChildren('Product')[0]
    prod_name = product.FindChildren('ProductName')[0].FindChildren('Text')[0]
    self.assertEqual(prod_name.text, 'A-Fil')

  def testGetProblemCodeValueFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    problem_desc = entry.ccr.GetProblems()[1].FindChildren('Description')[0]
    code = problem_desc.FindChildren('Code')[0].FindChildren('Value')[0]
    self.assertEqual(code.text, '136.9')

  def testGetGetImmunizationActorIdFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    immun_source = entry.ccr.GetImmunizations()[0].FindChildren('Source')[0]
    actor_id = immun_source.FindChildren('Actor')[0].FindChildren('ActorID')[0]
    self.assertEqual(actor_id.text, 'user@gmail.com')

  def testGetGetProceduresNameFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    proc_desc = entry.ccr.GetProcedures()[1].FindChildren('Description')[0]
    proc_name = proc_desc.FindChildren('Text')[0]
    self.assertEqual(proc_name.text, 'Abdominoplasty')

  def testGetAlertsFromCcr(self):
    entry = gdata.health.ProfileEntryFromString(str(self.profile_entry))
    alert_type = entry.ccr.GetAlerts()[0].FindChildren('Type')[0]
    self.assertEqual(alert_type.FindChildren('Text')[0].text, 'Allergy')


class ProfileListEntryTest(unittest.TestCase):

  def setUp(self):
    self.entry = gdata.health.ProfileListEntryFromString(
        test_data.HEALTH_PROFILE_LIST_ENTRY)

  def testToAndFromString(self):
    self.assert_(isinstance(self.entry, gdata.health.ProfileListEntry))
    self.assertEqual(self.entry.GetProfileId(), 'vndCn5sdfwdEIY')
    self.assertEqual(self.entry.GetProfileName(), 'profile name')


class ProfileFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.health.ProfileFeedFromString(
        test_data.HEALTH_PROFILE_FEED)

  def testToAndFromString(self):
    self.assert_(len(self.feed.entry) == 15)
    for an_entry in self.feed.entry:
      self.assert_(isinstance(an_entry, gdata.health.ProfileEntry))
    new_profile_feed = gdata.health.ProfileFeedFromString(str(self.feed))
    for an_entry in new_profile_feed.entry:
      self.assert_(isinstance(an_entry, gdata.health.ProfileEntry))

  def testConvertActualData(self):
    for an_entry in self.feed.entry:
      self.assert_(an_entry.ccr is not None)


class HealthProfileQueryTest(unittest.TestCase):

  def testHealthQueryToString(self):
    query = gdata.health.service.HealthProfileQuery()
    self.assertEqual(query.ToUri(), '/health/feeds/profile/default')

    query = gdata.health.service.HealthProfileQuery(feed='feeds/profile')
    self.assertEqual(query.ToUri(), '/health/feeds/profile/default')

    query = gdata.health.service.HealthProfileQuery(categories=['medication'])
    self.assertEqual(query.ToUri(),
                     '/health/feeds/profile/default/-/medication')

    query = gdata.health.service.HealthProfileQuery(projection='ui',
                                                    profile_id='12345')
    self.assertEqual(query.ToUri(), '/health/feeds/profile/ui/12345')

    query = gdata.health.service.HealthProfileQuery()
    query.categories.append('medication|condition')
    self.assertEqual(query.ToUri(),
                     '/health/feeds/profile/default/-/medication%7Ccondition')

  def testH9QueryToString(self):
    query = gdata.health.service.HealthProfileQuery(service='h9')
    self.assertEqual(query.ToUri(), '/h9/feeds/profile/default')

    query = gdata.health.service.HealthProfileQuery(
        service='h9', feed='feeds/profile',
        projection='ui', profile_id='12345')
    self.assertEqual(query.ToUri(), '/h9/feeds/profile/ui/12345')

  def testDigestParam(self):
    query = gdata.health.service.HealthProfileQuery(params={'digest': 'true'})
    self.assertEqual(query.ToUri(), '/health/feeds/profile/default?digest=true')

    query.profile_id = '12345'
    query.projection = 'ui'
    self.assertEqual(
        query.ToUri(), '/health/feeds/profile/ui/12345?digest=true')


class HealthProfileListQueryTest(unittest.TestCase):

  def testHealthProfileListQueryToString(self):
    query = gdata.health.service.HealthProfileListQuery()
    self.assertEqual(query.ToUri(), '/health/feeds/profile/list')

    query = gdata.health.service.HealthProfileListQuery(service='health')
    self.assertEqual(query.ToUri(), '/health/feeds/profile/list')

    query = gdata.health.service.HealthProfileListQuery(
        feed='feeds/profile/list')
    self.assertEqual(query.ToUri(), '/health/feeds/profile/list')

    query = gdata.health.service.HealthProfileListQuery(
        service='health', feed='feeds/profile/list')
    self.assertEqual(query.ToUri(), '/health/feeds/profile/list')

  def testH9ProfileListQueryToString(self):
    query = gdata.health.service.HealthProfileListQuery(service='h9')
    self.assertEqual(query.ToUri(), '/h9/feeds/profile/list')

    query = gdata.health.service.HealthProfileListQuery(
        service='h9', feed='feeds/profile/list')
    self.assertEqual(query.ToUri(), '/h9/feeds/profile/list')

if __name__ == '__main__':
  unittest.main()
