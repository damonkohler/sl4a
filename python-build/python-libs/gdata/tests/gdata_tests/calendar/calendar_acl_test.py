#!/usr/bin/python
#
# Copyright (C) 2007 Google Inc.
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

__author__ = 'api.lliabraa@google.com (Lane LiaBraaten)'

import unittest
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom
import gdata.calendar
import gdata.calendar.service
import gdata.service
import random
import getpass
from gdata import test_data

username = ''
password = ''

class CalendarServiceAclUnitTest(unittest.TestCase):
  _aclFeedUri = "/calendar/feeds/default/acl/full"
  _aclEntryUri = "%s/user:%s" % (_aclFeedUri, "user@gmail.com",)
  
  def setUp(self):
    self.cal_client = gdata.calendar.service.CalendarService()
    self.cal_client.email = username 
    self.cal_client.password = password
    self.cal_client.source = 'GCalendarClient ACL "Unit" Tests'

  def tearDown(self):
    # No teardown needed
    pass  

  def _getRandomNumber(self):
    """Return a random number as a string for testing"""
    r = random.Random()
    r.seed()
    return str(r.randint(100000,1000000))

  def _generateAclEntry(self, role="owner", scope_type="user", scope_value=None):
    """Generates a ACL rule from parameters or makes a random user an owner by default"""
    if (scope_type=="user" and scope_value is None):
      scope_value = "user%s@gmail.com" % (self._getRandomNumber())
    rule = gdata.calendar.CalendarAclEntry()
    rule.title = atom.Title(text=role)
    rule.scope = gdata.calendar.Scope(value=scope_value, type="user")
    rule.role = gdata.calendar.Role(value="http://schemas.google.com/gCal/2005#%s" % (role))
    return rule

  def assertEqualAclEntry(self, expected, actual):
    """Compares the values of two ACL entries"""
    self.assertEqual(expected.role.value, actual.role.value)
    self.assertEqual(expected.scope.value, actual.scope.value)
    self.assertEqual(expected.scope.type, actual.scope.type)

  def testGetAclFeedUnauthenticated(self):
    """Fiendishly try to get an ACL feed without authenticating"""
    try:
      self.cal_client.GetCalendarAclFeed(self._aclFeedUri)
      self.fail("Unauthenticated request should fail")
    except gdata.service.RequestError, error:
      self.assertEqual(error[0]['status'], 401)
      self.assertEqual(error[0]['reason'], "Authorization required")

  def testGetAclFeed(self):
    """Get an ACL feed"""
    self.cal_client.ProgrammaticLogin()
    feed = self.cal_client.GetCalendarAclFeed(self._aclFeedUri)
    self.assertNotEqual(0,len(feed.entry))

  def testGetAclEntryUnauthenticated(self):
    """Fiendishly try to get an ACL entry without authenticating"""
    try:
      self.cal_client.GetCalendarAclEntry(self._aclEntryUri)
      self.fail("Unauthenticated request should fail");
    except gdata.service.RequestError, error:
      self.assertEqual(error[0]['status'], 401)
      self.assertEqual(error[0]['reason'], "Authorization required")
  
  def testGetAclEntry(self):
    """Get an ACL entry"""
    self.cal_client.ProgrammaticLogin()
    self.cal_client.GetCalendarAclEntry(self._aclEntryUri)
    
  def testCalendarAclFeedFromString(self):
    """Create an ACL feed from a hard-coded string"""
    aclFeed = gdata.calendar.CalendarAclFeedFromString(test_data.ACL_FEED)
    self.assertEqual("Elizabeth Bennet's access control list", aclFeed.title.text)
    self.assertEqual(2,len(aclFeed.entry))
    
  def testCalendarAclEntryFromString(self):
    """Create an ACL entry from a hard-coded string"""
    aclEntry = gdata.calendar.CalendarAclEntryFromString(test_data.ACL_ENTRY)
    self.assertEqual("owner", aclEntry.title.text)
    self.assertEqual("user", aclEntry.scope.type)
    self.assertEqual("liz@gmail.com", aclEntry.scope.value)
    self.assertEqual("http://schemas.google.com/gCal/2005#owner", aclEntry.role.value)

  def testCreateAndDeleteAclEntry(self):
    """Add an ACL rule and verify that is it returned in the ACL feed.  Then delete the rule and 
       verify that the rule is no longer included in the ACL feed."""
    # Get the current number of ACL rules   
    self.cal_client.ProgrammaticLogin()
    aclFeed = self.cal_client.GetCalendarAclFeed(self._aclFeedUri)
    original_rule_count = len(aclFeed.entry)
 
    # Insert entry 
    rule = self._generateAclEntry()
    returned_rule = self.cal_client.InsertAclEntry(rule, self._aclFeedUri)

    # Verify rule was added with correct ACL values
    aclFeed = self.cal_client.GetCalendarAclFeed(self._aclFeedUri)
    self.assertEqual(original_rule_count+1, len(aclFeed.entry))
    self.assertEqualAclEntry(rule, returned_rule)

    # Delete the event
    self.cal_client.DeleteAclEntry(returned_rule.GetEditLink().href)
    aclFeed = self.cal_client.GetCalendarAclFeed(self._aclFeedUri)
    self.assertEquals(original_rule_count, len(aclFeed.entry))

  def testUpdateAclChangeScopeValue(self):
    """Fiendishly try to insert a test ACL rule and attempt to change the scope value (i.e. username).
       Verify that an exception is thrown, then delete the test rule."""
    # Insert a user-scoped owner role ot random user
    aclEntry = self._generateAclEntry("owner","user");
    self.cal_client.ProgrammaticLogin()
    rule = self._generateAclEntry()
    returned_rule = self.cal_client.InsertAclEntry(rule, self._aclFeedUri)

    # Change the scope value (i.e. what user is the owner) and update the entry
    updated_rule = returned_rule
    updated_rule.scope.value = "user_%s@gmail.com" % (self._getRandomNumber())
    try:
      returned_rule = self.cal_client.UpdateAclEntry(returned_rule.GetEditLink().href, updated_rule)
    except gdata.service.RequestError, error:
      self.assertEqual(error[0]['status'], 403)
      self.assertEqual(error[0]['reason'], "Forbidden")

    self.cal_client.DeleteAclEntry(updated_rule.GetEditLink().href)

  
  def testUpdateAclChangeScopeType(self):
    """Fiendishly try to insert a test ACL rule and attempt to change the scope type (i.e. from 'user' to 'domain').
       Verify that an exception is thrown, then delete the test rule."""
    # Insert a user-scoped owner role ot random user
    aclEntry = self._generateAclEntry("owner","user");
    self.cal_client.ProgrammaticLogin()
    rule = self._generateAclEntry()
    returned_rule = self.cal_client.InsertAclEntry(rule, self._aclFeedUri)

    # Change the scope value (i.e. what user is the owner) and update the entry
    updated_rule = returned_rule
    updated_rule.scope.type = "domain"
    try:
      returned_rule = self.cal_client.UpdateAclEntry(returned_rule.GetEditLink().href, updated_rule)
    except gdata.service.RequestError, error:
      self.assertEqual(error[0]['status'], 403)
      self.assertEqual(error[0]['reason'], "Forbidden")

    self.cal_client.DeleteAclEntry(updated_rule.GetEditLink().href)

    
  def testUpdateAclChangeRoleValue(self):
    """Insert a test ACL rule and attempt to change the scope type (i.e. from 'owner' to 'editor').
       Verify that an exception is thrown, then delete the test rule."""
    # Insert a user-scoped owner role ot random user
    aclEntry = self._generateAclEntry("owner","user");
    self.cal_client.ProgrammaticLogin()
    rule = self._generateAclEntry()
    returned_rule = self.cal_client.InsertAclEntry(rule, self._aclFeedUri)

    # Change the scope value (i.e. what user is the owner) and update the entry
    updated_rule = returned_rule
    updated_rule.role.value = "http://schemas.google.com/gCal/2005#editor"
    returned_rule = self.cal_client.UpdateAclEntry(returned_rule.GetEditLink().href, updated_rule)
    self.assertEqualAclEntry(updated_rule, returned_rule)
    
    self.cal_client.DeleteAclEntry(updated_rule.GetEditLink().href)
    
if __name__ == '__main__':
  print ('NOTE: Please run these tests only with a test account. ' +
      'The tests may delete or update your data.')
  username = raw_input('Please enter your username: ')
  password = getpass.getpass()
  unittest.main()

