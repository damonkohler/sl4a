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

"""Test for Email Settings service."""


__author__ = 'google-apps-apis@googlegroups.com'


import getpass
import gdata.apps.emailsettings.service
import unittest


domain = ''
admin_email = ''
admin_password = ''
username = ''


class EmailSettingsTest(unittest.TestCase):
  """Test for the EmailSettingsService."""

  def setUp(self):
    self.es = gdata.apps.emailsettings.service.EmailSettingsService(
      email=admin_email, password=admin_password, domain=domain)
    self.es.ProgrammaticLogin()

  def testCreateLabel(self):
    result = self.es.CreateLabel(username, label='New label!!!')
    self.assertEquals(result['label'], 'New label!!!')

  def testCreateFilter(self):
    result = self.es.CreateFilter(username,
                                  from_='from_foo',
                                  to='to_foo',
                                  subject='subject_foo',
                                  has_the_word='has_the_words_foo',
                                  does_not_have_the_word='doesnt_have_foo',
                                  has_attachment=True,
                                  label='label_foo',
                                  should_mark_as_read=True,
                                  should_archive=True)
    self.assertEquals(result['from'], 'from_foo')
    self.assertEquals(result['to'], 'to_foo')
    self.assertEquals(result['subject'], 'subject_foo')

  def testCreateSendAsAlias(self):
    result = self.es.CreateSendAsAlias(username,
                                       name='Send-as Alias',
                                       address='user2@sizzles.org',
                                       reply_to='user3@sizzles.org',
                                       make_default=True)
    self.assertEquals(result['name'], 'Send-as Alias')

  def testUpdateWebClipSettings(self):
    result = self.es.UpdateWebClipSettings(username, enable=True)
    self.assertEquals(result['enable'], 'true')

  def testUpdateForwarding(self):
    result = self.es.UpdateForwarding(username,
                                      enable=True,
                                      forward_to='user4@sizzles.org',
                                      action=gdata.apps.emailsettings.service.KEEP)
    self.assertEquals(result['enable'], 'true')

  def testUpdatePop(self):
    result = self.es.UpdatePop(username,
                               enable=True,
                               enable_for=gdata.apps.emailsettings.service.ALL_MAIL,
                               action=gdata.apps.emailsettings.service.ARCHIVE)
    self.assertEquals(result['enable'], 'true')

  def testUpdateImap(self):
    result = self.es.UpdateImap(username, enable=True)
    self.assertEquals(result['enable'], 'true')

  def testUpdateVacation(self):
    result = self.es.UpdateVacation(username,
                                    enable=True,
                                    subject='Hawaii',
                                    message='Wish you were here!',
                                    contacts_only=True)
    self.assertEquals(result['subject'], 'Hawaii')

  def testUpdateSignature(self):
    result = self.es.UpdateSignature(username, signature='Signature')
    self.assertEquals(result['signature'], 'Signature')

  def testUpdateLanguage(self):
    result = self.es.UpdateLanguage(username, language='fr')
    self.assertEquals(result['language'], 'fr')

  def testUpdateGeneral(self):
    result = self.es.UpdateGeneral(username,
                                   page_size=100,
                                   shortcuts=True,
                                   arrows=True,
                                   snippets=True,
                                   unicode=True)
    self.assertEquals(result['pageSize'], '100')


if __name__ == '__main__':
  print("""Google Apps Email Settings Service Tests

NOTE: Please run these tests only with a test user account.
""")
  domain = raw_input('Google Apps domain: ')
  admin_email = '%s@%s' % (raw_input('Administrator username: '), domain)
  admin_password = getpass.getpass('Administrator password: ')
  username = raw_input('Test username: ')
  unittest.main()
