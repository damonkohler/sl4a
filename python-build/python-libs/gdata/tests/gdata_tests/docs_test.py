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


__author__ = ('api.jfisher (Jeff Fisher), '
              'api.eric@google.com (Eric Bidelman)')

import unittest
from gdata import test_data
import gdata.docs

class DocumentListEntryTest(unittest.TestCase):

  def setUp(self):
    self.dl_entry = gdata.docs.DocumentListEntryFromString(
        test_data.DOCUMENT_LIST_ENTRY)

  def testToAndFromStringWithData(self):
    entry = gdata.docs.DocumentListEntryFromString(str(self.dl_entry))

    self.assertEqual(entry.author[0].name.text, 'test.user')
    self.assertEqual(entry.author[0].email.text, 'test.user@gmail.com')
    self.assertEqual(entry.GetDocumentType(), 'spreadsheet')
    self.assertEqual(entry.id.text,
        'http://docs.google.com/feeds/documents/private/full/' +\
        'spreadsheet%3Asupercalifragilisticexpealidocious')
    self.assertEqual(entry.title.text,'Test Spreadsheet')
    self.assertEqual(entry.resourceId.text,
                     'spreadsheet:supercalifragilisticexpealidocious')
    self.assertEqual(entry.lastModifiedBy.name.text,'test.user')
    self.assertEqual(entry.lastModifiedBy.email.text,'test.user@gmail.com')
    self.assertEqual(entry.lastViewed.text,'2009-03-05T07:48:21.493Z')
    self.assertEqual(entry.writersCanInvite.value, 'true')


class DocumentListFeedTest(unittest.TestCase):

  def setUp(self):
    self.dl_feed = gdata.docs.DocumentListFeedFromString(
        test_data.DOCUMENT_LIST_FEED)

  def testToAndFromString(self):
    self.assert_(len(self.dl_feed.entry) == 2)
    for an_entry in self.dl_feed.entry:
      self.assert_(isinstance(an_entry, gdata.docs.DocumentListEntry))
    new_dl_feed = gdata.docs.DocumentListFeedFromString(str(self.dl_feed))
    for an_entry in new_dl_feed.entry:
      self.assert_(isinstance(an_entry, gdata.docs.DocumentListEntry))

  def testConvertActualData(self):
    for an_entry in self.dl_feed.entry:
      self.assertEqual(an_entry.author[0].name.text, 'test.user')
      self.assertEqual(an_entry.author[0].email.text, 'test.user@gmail.com')
      self.assertEqual(an_entry.lastModifiedBy.name.text, 'test.user')
      self.assertEqual(an_entry.lastModifiedBy.email.text,
                       'test.user@gmail.com')
      self.assertEqual(an_entry.lastViewed.text,'2009-03-05T07:48:21.493Z')
      if(an_entry.GetDocumentType() == 'spreadsheet'):
        self.assertEqual(an_entry.title.text, 'Test Spreadsheet')
        self.assertEqual(an_entry.writersCanInvite.value, 'true')
      elif(an_entry.GetDocumentType() == 'document'):
        self.assertEqual(an_entry.title.text, 'Test Document')
        self.assertEqual(an_entry.writersCanInvite.value, 'false')

  def testLinkFinderFindsLinks(self):
    for entry in self.dl_feed.entry:
      # All Document List entries should have a self link
      self.assert_(entry.GetSelfLink() is not None)
      # All Document List entries should have an HTML link
      self.assert_(entry.GetHtmlLink() is not None)
      self.assert_(entry.feedLink.href is not None)


class DocumentListAclEntryTest(unittest.TestCase):

  def setUp(self):
    self.acl_entry = gdata.docs.DocumentListAclEntryFromString(
        test_data.DOCUMENT_LIST_ACL_ENTRY)


  def testToAndFromString(self):
    self.assert_(isinstance(self.acl_entry, gdata.docs.DocumentListAclEntry))
    self.assert_(isinstance(self.acl_entry.role, gdata.docs.Role))
    self.assert_(isinstance(self.acl_entry.scope, gdata.docs.Scope))
    self.assertEqual(self.acl_entry.scope.value, 'user@gmail.com')
    self.assertEqual(self.acl_entry.scope.type, 'user')
    self.assertEqual(self.acl_entry.role.value, 'writer')

    acl_entry_str = str(self.acl_entry)
    new_acl_entry = gdata.docs.DocumentListAclEntryFromString(acl_entry_str)
    self.assert_(isinstance(new_acl_entry, gdata.docs.DocumentListAclEntry))
    self.assert_(isinstance(new_acl_entry.role, gdata.docs.Role))
    self.assert_(isinstance(new_acl_entry.scope, gdata.docs.Scope))
    self.assertEqual(new_acl_entry.scope.value, self.acl_entry.scope.value)
    self.assertEqual(new_acl_entry.scope.type, self.acl_entry.scope.type)
    self.assertEqual(new_acl_entry.role.value, self.acl_entry.role.value)

  def testCreateNewAclEntry(self):
    cat = gdata.atom.Category(
        term='http://schemas.google.com/acl/2007#accessRule',
        scheme='http://schemas.google.com/g/2005#kind')
    acl_entry = gdata.docs.DocumentListAclEntry(category=[cat])
    acl_entry.scope = gdata.docs.Scope(value='user@gmail.com', type='user')
    acl_entry.role = gdata.docs.Role(value='writer')
    self.assert_(isinstance(acl_entry, gdata.docs.DocumentListAclEntry))
    self.assert_(isinstance(acl_entry.role, gdata.docs.Role))
    self.assert_(isinstance(acl_entry.scope, gdata.docs.Scope))
    self.assertEqual(acl_entry.scope.value, 'user@gmail.com')
    self.assertEqual(acl_entry.scope.type, 'user')
    self.assertEqual(acl_entry.role.value, 'writer')

class DocumentListAclFeedTest(unittest.TestCase):

  def setUp(self):
    self.feed = gdata.docs.DocumentListAclFeedFromString(
        test_data.DOCUMENT_LIST_ACL_FEED)

  def testToAndFromString(self):
    for entry in self.feed.entry:
      self.assert_(isinstance(entry, gdata.docs.DocumentListAclEntry))

    feed = gdata.docs.DocumentListAclFeedFromString(str(self.feed))
    for entry in feed.entry:
      self.assert_(isinstance(entry, gdata.docs.DocumentListAclEntry))

  def testConvertActualData(self):
    entries = self.feed.entry
    self.assert_(len(entries) == 2)
    self.assertEqual(entries[0].title.text,
                     'Document Permission - user@gmail.com')
    self.assertEqual(entries[0].role.value, 'owner')
    self.assertEqual(entries[0].scope.type, 'user')
    self.assertEqual(entries[0].scope.value, 'user@gmail.com')
    self.assert_(entries[0].GetSelfLink() is not None)
    self.assert_(entries[0].GetEditLink() is not None)
    self.assertEqual(entries[1].title.text,
                     'Document Permission - user2@google.com')
    self.assertEqual(entries[1].role.value, 'writer')
    self.assertEqual(entries[1].scope.type, 'domain')
    self.assertEqual(entries[1].scope.value, 'google.com')
    self.assert_(entries[1].GetSelfLink() is not None)
    self.assert_(entries[1].GetEditLink() is not None)

if __name__ == '__main__':
  unittest.main()
