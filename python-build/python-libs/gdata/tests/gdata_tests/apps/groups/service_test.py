#!/usr/bin/python
#
# Copyright (C) 2008 Google
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

"""Test for Groups service."""


__author__ = 'google-apps-apis@googlegroups.com'


import unittest
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom
import gdata.apps
import gdata.apps.service
import gdata.apps.groups.service
import getpass
import time


domain = ''
admin_email = ''
admin_password = ''
username = ''


class GroupsTest(unittest.TestCase):
  """Test for the GroupsService."""

  def setUp(self):
    self.postfix = time.strftime("%Y%m%d%H%M%S")
    self.apps_client = gdata.apps.service.AppsService(
        email=admin_email, domain=domain, password=admin_password,
        source='GroupsClient "Unit" Tests')
    self.apps_client.ProgrammaticLogin()
    self.groups_client = gdata.apps.groups.service.GroupsService(
        email=admin_email, domain=domain, password=admin_password,
        source='GroupsClient "Unit" Tests')
    self.groups_client.ProgrammaticLogin()  
    self.created_users = []
    self.created_groups = []
    self.createUsers();

  def createUsers(self):
    user_name = 'yujimatsuo-' + self.postfix
    family_name = 'Matsuo'
    given_name = 'Yuji'
    password = '123$$abc'
    suspended = 'false'

    try:
      self.user_yuji = self.apps_client.CreateUser(
          user_name=user_name, family_name=family_name, given_name=given_name,
          password=password, suspended=suspended)
      print 'User ' + user_name + ' created'
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.created_users.append(self.user_yuji)

    user_name = 'taromatsuo-' + self.postfix
    family_name = 'Matsuo'
    given_name = 'Taro'
    password = '123$$abc'
    suspended = 'false'

    try:
      self.user_taro = self.apps_client.CreateUser(
          user_name=user_name, family_name=family_name, given_name=given_name,
          password=password, suspended=suspended)
      print 'User ' + user_name + ' created'
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.created_users.append(self.user_taro)

  def tearDown(self):
    print '\n'
    for user in self.created_users:
      try:
        self.apps_client.DeleteUser(user.login.user_name)
        print 'User ' + user.login.user_name + ' deleted'
      except Exception, e:
        print e
    for group in self.created_groups:
      try:
        self.groups_client.DeleteGroup(group)
        print 'Group ' + group + ' deleted'
      except Exception, e:
        print e

  def test001GroupsMethods(self):
    # tests CreateGroup method
    group01_id = 'group01-' + self.postfix
    group02_id = 'group02-' + self.postfix
    try:
      created_group01 = self.groups_client.CreateGroup(group01_id, 'US Sales 1',
          'Testing', gdata.apps.groups.service.PERMISSION_OWNER)
      created_group02 = self.groups_client.CreateGroup(group02_id, 'US Sales 2',
          'Testing', gdata.apps.groups.service.PERMISSION_OWNER)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.assertEquals(created_group01['groupId'], group01_id)
    self.assertEquals(created_group02['groupId'], group02_id)
    self.created_groups.append(group01_id)
    self.created_groups.append(group02_id)
   
    # tests UpdateGroup method
    try:
      updated_group = self.groups_client.UpdateGroup(group01_id, 'Updated!',
          'Testing', gdata.apps.groups.service.PERMISSION_OWNER)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.assertEquals(updated_group['groupName'], 'Updated!')

    # tests RetrieveGroup method
    try:
      retrieved_group = self.groups_client.RetrieveGroup(group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.assertEquals(retrieved_group['groupId'], group01_id + '@' + domain)
   
    # tests RetrieveAllGroups method
    try:
      retrieved_groups = self.groups_client.RetrieveAllGroups()
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.assertEquals(len(retrieved_groups),
                      len(self.apps_client.RetrieveAllEmailLists().entry))
   
    # tests AddMemberToGroup
    try:
      added_member = self.groups_client.AddMemberToGroup(
          self.user_yuji.login.user_name, group01_id)
      self.groups_client.AddMemberToGroup(
          self.user_taro.login.user_name, group02_id)
      self.groups_client.AddMemberToGroup(
          group01_id, group02_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(added_member['memberId'],
                      self.user_yuji.login.user_name)
   
    # tests RetrieveGroups method
    try:
      retrieved_direct_groups = self.groups_client.RetrieveGroups(
          self.user_yuji.login.user_name, True)
      retrieved_groups = self.groups_client.RetrieveGroups(
          self.user_yuji.login.user_name, False)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)

    self.assertEquals(len(retrieved_direct_groups), 1)
    # TODO: Enable this test after a directOnly bug is fixed
    #self.assertEquals(len(retrieved_groups), 2)

    # tests IsMember method
    try:
      result = self.groups_client.IsMember(
          self.user_yuji.login.user_name, group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(result, True)
   
    # tests RetrieveMember method
    try:
      retrieved_member = self.groups_client.RetrieveMember(
          self.user_yuji.login.user_name, group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(retrieved_member['memberId'],
                      self.user_yuji.login.user_name + '@' + domain)
   
    # tests RetrieveAllMembers method
    try:
      retrieved_members = self.groups_client.RetrieveAllMembers(group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(len(retrieved_members), 1)
   
    # tests RemoveMemberFromGroup method
    try:
      self.groups_client.RemoveMemberFromGroup(self.user_yuji.login.user_name,
                                               group01_id)
      retrieved_members = self.groups_client.RetrieveAllMembers(group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(len(retrieved_members), 0)
   
    # tests AddOwnerToGroup
    try:
      added_owner = self.groups_client.AddOwnerToGroup(
          self.user_yuji.login.user_name, group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(added_owner['email'],
                      self.user_yuji.login.user_name)

    # tests IsOwner method
    try:
      result = self.groups_client.IsOwner(
          self.user_yuji.login.user_name, group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(result, True)
   
    # tests RetrieveOwner method
    try:
      retrieved_owner = self.groups_client.RetrieveOwner(
          self.user_yuji.login.user_name, group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(retrieved_owner['email'],
                      self.user_yuji.login.user_name + '@' + domain)
   
    # tests RetrieveAllOwners method
    try:
      retrieved_owners = self.groups_client.RetrieveAllOwners(group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(len(retrieved_owners), 1)
   
    # tests RemoveOwnerFromGroup method
    try:
      self.groups_client.RemoveOwnerFromGroup(self.user_yuji.login.user_name,
                                               group01_id)
      retrieved_owners = self.groups_client.RetrieveAllOwners(group01_id)
    except Exception, e:
      self.fail('Unexpected exception occurred: %s' % e)
     
    self.assertEquals(len(retrieved_owners), 0)


if __name__ == '__main__':
  print("""Google Apps Groups Service Tests

NOTE: Please run these tests only with a test user account.
""")
  domain = raw_input('Google Apps domain: ')
  admin_email = '%s@%s' % (raw_input('Administrator username: '), domain)
  admin_password = getpass.getpass('Administrator password: ')
  unittest.main()
