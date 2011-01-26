#!/usr/bin/env python
#
# Copyright (C) 2009 Google Inc.
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


# This module is used for version 2 of the Google Data APIs.


__author__ = 'j.s@google.com (Jeff Scudder)'


import unittest
import gdata.data
from gdata import test_data
import gdata.test_config as conf
import atom.core
import atom.data


SIMPLE_V2_FEED_TEST_DATA = """<feed xmlns='http://www.w3.org/2005/Atom'
    xmlns:gd='http://schemas.google.com/g/2005'
    gd:etag='W/"CUMBRHo_fip7ImA9WxRbGU0."'>
  <title>Elizabeth Bennet's Contacts</title>
  <link rel='next' type='application/atom+xml'
        href='http://www.google.com/m8/feeds/contacts/.../more' />
  <entry gd:etag='"Qn04eTVSLyp7ImA9WxRbGEUORAQ."'>
    <id>http://www.google.com/m8/feeds/contacts/liz%40gmail.com/base/c9e</id>
    <title>Fitzwilliam</title>
    <link rel='http://schemas.google.com/contacts/2008/rel#photo' 
     type='image/*'
     href='http://www.google.com/m8/feeds/photos/media/liz%40gmail.com/c9e'
     gd:etag='"KTlcZWs1bCp7ImBBPV43VUV4LXEZCXERZAc."' />
    <link rel='self' type='application/atom+xml'
     href='Changed to ensure we are really getting the edit URL.'/>
    <link rel='edit' type='application/atom+xml'
     href='http://www.google.com/m8/feeds/contacts/liz%40gmail.com/full/c9e'/>
  </entry>
  <entry gd:etag='&quot;123456&quot;'>
    <link rel='edit' href='http://example.com/1' />
  </entry>
</feed>"""


XML_ENTRY_1 = """<?xml version='1.0'?>
    <entry xmlns='http://www.w3.org/2005/Atom'
           xmlns:g='http://base.google.com/ns/1.0'>
      <category scheme="http://base.google.com/categories/itemtypes"
                term="products"/>
      <id>    http://www.google.com/test/id/url   </id>
      <title type='text'>Testing 2000 series laptop</title>
      <content type='xhtml'>
        <div xmlns='http://www.w3.org/1999/xhtml'>A Testing Laptop</div>
      </content>
      <link rel='alternate' type='text/html'
            href='http://www.provider-host.com/123456789'/>
      <link rel='license'
            href='http://creativecommons.org/licenses/by-nc/2.5/rdf'/>
      <g:label>Computer</g:label>
      <g:label>Laptop</g:label>
      <g:label>testing laptop</g:label>
      <g:item_type>products</g:item_type>
    </entry>"""


def parse(xml_string, target_class):
  """Convenience wrapper for converting an XML string to an XmlElement."""
  return atom.core.xml_element_from_string(xml_string, target_class)


class StartIndexTest(unittest.TestCase):

  def setUp(self):
    self.start_index = gdata.data.StartIndex()

  def testToAndFromString(self):
    self.start_index.text = '1'
    self.assert_(self.start_index.text == '1')
    new_start_index = parse(self.start_index.ToString(),
        gdata.data.StartIndex)
    self.assert_(self.start_index.text == new_start_index.text)


class ItemsPerPageTest(unittest.TestCase):

  def setUp(self):
    self.items_per_page = gdata.data.ItemsPerPage()

  def testToAndFromString(self):
    self.items_per_page.text = '10'
    self.assert_(self.items_per_page.text == '10')
    new_items_per_page = parse(self.items_per_page.ToString(),
        gdata.data.ItemsPerPage)
    self.assert_(self.items_per_page.text == new_items_per_page.text)


class GDataEntryTest(unittest.TestCase):

  def testIdShouldBeCleaned(self):
    entry = parse(XML_ENTRY_1, gdata.data.GDEntry)
    tree = parse(XML_ENTRY_1, atom.core.XmlElement)
    self.assertFalse(tree.get_elements('id', 
        'http://www.w3.org/2005/Atom' == entry.id.text))
    self.assertEqual(entry.get_id(), 'http://www.google.com/test/id/url')

  def testGeneratorShouldBeCleaned(self):
    feed = parse(test_data.GBASE_FEED, gdata.data.GDFeed)
    tree = parse(test_data.GBASE_FEED, atom.core.XmlElement)
    self.assertFalse(tree.get_elements('generator', 
        'http://www.w3.org/2005/Atom')[0].text == feed.get_generator())
    self.assertEqual(feed.get_generator(), 'GoogleBase')

  def testAllowsEmptyId(self):
    entry = gdata.data.GDEntry()
    try:
      entry.id = atom.data.Id()
    except AttributeError:
      self.fail('Empty id should not raise an attribute error.')

class LinkFinderTest(unittest.TestCase):

  def setUp(self):
    self.entry = parse(XML_ENTRY_1, gdata.data.GDEntry)

  def testLinkFinderGetsLicenseLink(self):
    self.assertEquals(isinstance(self.entry.FindLicenseLink(), str),
                      True)
    self.assertEquals(self.entry.FindLicenseLink(),
        'http://creativecommons.org/licenses/by-nc/2.5/rdf')

  def testLinkFinderGetsAlternateLink(self):
    self.assertTrue(isinstance(self.entry.FindAlternateLink(), str))
    self.assertEquals(self.entry.FindAlternateLink(),
        'http://www.provider-host.com/123456789')


class GDataFeedTest(unittest.TestCase):

  def testCorrectConversionToElementTree(self):
    test_feed = parse(test_data.GBASE_FEED, gdata.data.GDFeed)
    self.assert_(test_feed.total_results is not None)
    self.assertTrue(test_feed.get_elements('totalResults', 
        'http://a9.com/-/spec/opensearchrss/1.0/') is not None)
    self.assertTrue(len(test_feed.get_elements('totalResults',
        'http://a9.com/-/spec/opensearchrss/1.0/')) > 0)

  def testAllowsEmptyId(self):
    feed = gdata.data.GDFeed()
    try:
      feed.id = atom.data.Id()
    except AttributeError:
      self.fail('Empty id should not raise an attribute error.')


class BatchEntryTest(unittest.TestCase):

  def testCorrectConversionFromAndToString(self):
    batch_entry = parse(test_data.BATCH_ENTRY, gdata.data.BatchEntry)

    self.assertEquals(batch_entry.batch_id.text, 'itemB')
    self.assertEquals(batch_entry.id.text,
                      'http://www.google.com/base/feeds/items/'
                      '2173859253842813008')
    self.assertEquals(batch_entry.batch_operation.type, 'insert')
    self.assertEquals(batch_entry.batch_status.code, '201')
    self.assertEquals(batch_entry.batch_status.reason, 'Created')

    new_entry = parse(str(batch_entry), gdata.data.BatchEntry)

    self.assertEquals(batch_entry.batch_id.text, new_entry.batch_id.text)
    self.assertEquals(batch_entry.id.text, new_entry.id.text)
    self.assertEquals(batch_entry.batch_operation.type,
                      new_entry.batch_operation.type)
    self.assertEquals(batch_entry.batch_status.code,
                      new_entry.batch_status.code)
    self.assertEquals(batch_entry.batch_status.reason,
                      new_entry.batch_status.reason)


class BatchFeedTest(unittest.TestCase):

  def setUp(self):
    self.batch_feed = gdata.data.BatchFeed()
    self.example_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/1'), text='This is a test')

  def testConvertRequestFeed(self):
    batch_feed = parse(test_data.BATCH_FEED_REQUEST, gdata.data.BatchFeed)

    self.assertEquals(len(batch_feed.entry), 4)
    for entry in batch_feed.entry:
      self.assert_(isinstance(entry, gdata.data.BatchEntry))
    self.assertEquals(batch_feed.title.text, 'My Batch Feed')

    new_feed = parse(batch_feed.to_string(), gdata.data.BatchFeed)

    self.assertEquals(len(new_feed.entry), 4)
    for entry in new_feed.entry:
      self.assert_(isinstance(entry, gdata.data.BatchEntry))
    self.assertEquals(new_feed.title.text, 'My Batch Feed')

  def testConvertResultFeed(self):
    batch_feed = parse(test_data.BATCH_FEED_RESULT, gdata.data.BatchFeed)

    self.assertEquals(len(batch_feed.entry), 4)
    for entry in batch_feed.entry:
      self.assert_(isinstance(entry, gdata.data.BatchEntry))
      if entry.id.text == ('http://www.google.com/base/feeds/items/'
                           '2173859253842813008'):
        self.assertEquals(entry.batch_operation.type, 'insert')
        self.assertEquals(entry.batch_id.text, 'itemB')
        self.assertEquals(entry.batch_status.code, '201')
        self.assertEquals(entry.batch_status.reason, 'Created')
    self.assertEquals(batch_feed.title.text, 'My Batch')

    new_feed = parse(str(batch_feed), gdata.data.BatchFeed)

    self.assertEquals(len(new_feed.entry), 4)
    for entry in new_feed.entry:
      self.assert_(isinstance(entry, gdata.data.BatchEntry))
      if entry.id.text == ('http://www.google.com/base/feeds/items/'
                           '2173859253842813008'):
        self.assertEquals(entry.batch_operation.type, 'insert')
        self.assertEquals(entry.batch_id.text, 'itemB')
        self.assertEquals(entry.batch_status.code, '201')
        self.assertEquals(entry.batch_status.reason, 'Created')
    self.assertEquals(new_feed.title.text, 'My Batch')

  def testAddBatchEntry(self):
    try:
      self.batch_feed.AddBatchEntry(batch_id_string='a')
      self.fail('AddBatchEntry with neither entry or URL should raise Error')
    except gdata.data.MissingRequiredParameters:
      pass

    new_entry = self.batch_feed.AddBatchEntry(
        id_url_string='http://example.com/1')
    self.assertEquals(len(self.batch_feed.entry), 1)
    self.assertEquals(self.batch_feed.entry[0].get_id(),
                      'http://example.com/1')
    self.assertEquals(self.batch_feed.entry[0].batch_id.text, '0')
    self.assertEquals(new_entry.id.text, 'http://example.com/1')
    self.assertEquals(new_entry.batch_id.text, '0')

    to_add = gdata.data.BatchEntry(id=atom.data.Id(text='originalId'))
    new_entry = self.batch_feed.AddBatchEntry(entry=to_add,
                                              batch_id_string='foo')
    self.assertEquals(new_entry.batch_id.text, 'foo')
    self.assertEquals(new_entry.id.text, 'originalId')

    to_add = gdata.data.BatchEntry(id=atom.data.Id(text='originalId'),
                              batch_id=gdata.data.BatchId(text='bar'))
    new_entry = self.batch_feed.AddBatchEntry(entry=to_add,
                                              id_url_string='newId',
                                              batch_id_string='foo')
    self.assertEquals(new_entry.batch_id.text, 'foo')
    self.assertEquals(new_entry.id.text, 'originalId')

    to_add = gdata.data.BatchEntry(id=atom.data.Id(text='originalId'),
                              batch_id=gdata.data.BatchId(text='bar'))
    new_entry = self.batch_feed.AddBatchEntry(entry=to_add,
                                              id_url_string='newId')
    self.assertEquals(new_entry.batch_id.text, 'bar')
    self.assertEquals(new_entry.id.text, 'originalId')

    to_add = gdata.data.BatchEntry(id=atom.data.Id(text='originalId'),
                              batch_id=gdata.data.BatchId(text='bar'),
                              batch_operation=gdata.data.BatchOperation(
                                  type=gdata.data.BATCH_INSERT))
    self.assertEquals(to_add.batch_operation.type, gdata.data.BATCH_INSERT)
    new_entry = self.batch_feed.AddBatchEntry(entry=to_add,
        id_url_string='newId', batch_id_string='foo',
        operation_string=gdata.data.BATCH_UPDATE)
    self.assertEquals(new_entry.batch_operation.type, gdata.data.BATCH_UPDATE)

  def testAddInsert(self):

    first_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/1'), text='This is a test1')
    self.batch_feed.AddInsert(first_entry)
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_INSERT)
    self.assertEquals(self.batch_feed.entry[0].batch_id.text, '0')

    second_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/2'), text='This is a test2')
    self.batch_feed.AddInsert(second_entry, batch_id_string='foo')
    self.assertEquals(self.batch_feed.entry[1].batch_operation.type,
                      gdata.data.BATCH_INSERT)
    self.assertEquals(self.batch_feed.entry[1].batch_id.text, 'foo')

    third_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/3'), text='This is a test3')
    third_entry.batch_operation = gdata.data.BatchOperation(
        type=gdata.data.BATCH_DELETE)
    # Add an entry with a delete operation already assigned.
    self.batch_feed.AddInsert(third_entry)
    # The batch entry should not have the original operation, it should
    # have been changed to an insert.
    self.assertEquals(self.batch_feed.entry[2].batch_operation.type,
                      gdata.data.BATCH_INSERT)
    self.assertEquals(self.batch_feed.entry[2].batch_id.text, '2')

  def testAddDelete(self):
    # Try deleting an entry
    delete_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/1'), text='This is a test')
    self.batch_feed.AddDelete(entry=delete_entry)
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_DELETE)
    self.assertEquals(self.batch_feed.entry[0].get_id(),
                      'http://example.com/1')
    self.assertEquals(self.batch_feed.entry[0].text, 'This is a test')

    # Try deleting a URL
    self.batch_feed.AddDelete(url_string='http://example.com/2')
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_DELETE)
    self.assertEquals(self.batch_feed.entry[1].id.text,
                      'http://example.com/2')
    self.assert_(self.batch_feed.entry[1].text is None)

  def testAddQuery(self):
    # Try querying with an existing batch entry
    delete_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/1'))
    self.batch_feed.AddQuery(entry=delete_entry)
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_QUERY)
    self.assertEquals(self.batch_feed.entry[0].get_id(),
                      'http://example.com/1')

    # Try querying a URL
    self.batch_feed.AddQuery(url_string='http://example.com/2')
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_QUERY)
    self.assertEquals(self.batch_feed.entry[1].id.text,
                      'http://example.com/2')

  def testAddUpdate(self):
    # Try updating an entry
    delete_entry = gdata.data.BatchEntry(
        id=atom.data.Id(text='http://example.com/1'), text='This is a test')
    self.batch_feed.AddUpdate(entry=delete_entry)
    self.assertEquals(self.batch_feed.entry[0].batch_operation.type,
                      gdata.data.BATCH_UPDATE)
    self.assertEquals(self.batch_feed.entry[0].get_id(),
                      'http://example.com/1')
    self.assertEquals(self.batch_feed.entry[0].text, 'This is a test')


class ExtendedPropertyTest(unittest.TestCase):

  def testXmlBlobRoundTrip(self):
    ep = gdata.data.ExtendedProperty(name='blobby')
    ep.SetXmlBlob('<some_xml attr="test"/>')
    extension = ep.GetXmlBlob()
    self.assertEquals(extension.tag, 'some_xml')
    self.assert_(extension.namespace is None)
    self.assertEquals(extension.attributes['attr'], 'test')

    ep2 = parse(ep.ToString(), gdata.data.ExtendedProperty)

    extension = ep2.GetXmlBlob()
    self.assertEquals(extension.tag, 'some_xml')
    self.assert_(extension.namespace is None)
    self.assertEquals(extension.attributes['attr'], 'test')

  def testGettersShouldReturnNoneWithNoBlob(self):
    ep = gdata.data.ExtendedProperty(name='no blob')
    self.assert_(ep.GetXmlBlob() is None)

  def testGettersReturnCorrectTypes(self):
    ep = gdata.data.ExtendedProperty(name='has blob')
    ep.SetXmlBlob('<some_xml attr="test"/>')
    self.assert_(isinstance(ep.GetXmlBlob(),
        atom.core.XmlElement))
    self.assert_(isinstance(ep.GetXmlBlob().to_string(), str))


class FeedLinkTest(unittest.TestCase):

  def testCorrectFromStringType(self):
    link = parse(
        '<feedLink xmlns="http://schemas.google.com/g/2005" countHint="5"/>',
        gdata.data.FeedLink)
    self.assertTrue(isinstance(link, gdata.data.FeedLink))
    self.assertEqual(link.count_hint, '5')


class SimpleV2FeedTest(unittest.TestCase):

  def test_parsing_etags_and_edit_url(self):
    feed = atom.core.parse(SIMPLE_V2_FEED_TEST_DATA, gdata.data.GDFeed)

    # General parsing assertions.
    self.assertEqual(feed.get_elements('title')[0].text, 
                     'Elizabeth Bennet\'s Contacts')
    self.assertEqual(len(feed.entry), 2)
    for entry in feed.entry:
      self.assertTrue(isinstance(entry, gdata.data.GDEntry))
    self.assertEqual(feed.entry[0].GetElements('title')[0].text,
                     'Fitzwilliam')
    self.assertEqual(feed.entry[0].get_elements('id')[0].text,
        'http://www.google.com/m8/feeds/contacts/liz%40gmail.com/base/c9e')

    # ETags checks.
    self.assertEqual(feed.etag, 'W/"CUMBRHo_fip7ImA9WxRbGU0."')
    self.assertEqual(feed.entry[0].etag, '"Qn04eTVSLyp7ImA9WxRbGEUORAQ."')
    self.assertEqual(feed.entry[1].etag, '"123456"')

    # Look for Edit URLs.
    self.assertEqual(feed.entry[0].find_edit_link(), 
        'http://www.google.com/m8/feeds/contacts/liz%40gmail.com/full/c9e')
    self.assertEqual(feed.entry[1].FindEditLink(), 'http://example.com/1')

    # Look for Next URLs.
    self.assertEqual(feed.find_next_link(),
        'http://www.google.com/m8/feeds/contacts/.../more')

  def test_constructor_defauls(self):
    feed = gdata.data.GDFeed()
    self.assertTrue(feed.etag is None)
    self.assertEqual(feed.link, [])
    self.assertEqual(feed.entry, [])
    entry = gdata.data.GDEntry()
    self.assertTrue(entry.etag is None)
    self.assertEqual(entry.link, [])
    link = atom.data.Link()
    self.assertTrue(link.href is None)
    self.assertTrue(link.rel is None)
    link1 = atom.data.Link(href='http://example.com', rel='test')
    self.assertEqual(link1.href, 'http://example.com')
    self.assertEqual(link1.rel, 'test')
    link2 = atom.data.Link(href='http://example.org/', rel='alternate')
    entry = gdata.data.GDEntry(etag='foo', link=[link1, link2])
    feed = gdata.data.GDFeed(etag='12345', entry=[entry])
    self.assertEqual(feed.etag, '12345')
    self.assertEqual(len(feed.entry), 1)
    self.assertEqual(feed.entry[0].etag, 'foo')
    self.assertEqual(len(feed.entry[0].link), 2)


class DataClassSanityTest(unittest.TestCase):

  def test_basic_element_structure(self):
    conf.check_data_classes(self, [
        gdata.data.TotalResults, gdata.data.StartIndex,
        gdata.data.ItemsPerPage, gdata.data.ExtendedProperty,
        gdata.data.GDEntry, gdata.data.GDFeed, gdata.data.BatchId,
        gdata.data.BatchOperation, gdata.data.BatchStatus,
        gdata.data.BatchEntry, gdata.data.BatchInterrupted,
        gdata.data.BatchFeed, gdata.data.EntryLink, gdata.data.FeedLink,
        gdata.data.AdditionalName, gdata.data.Comments, gdata.data.Country,
        gdata.data.Email, gdata.data.FamilyName, gdata.data.Im,
        gdata.data.GivenName, gdata.data.NamePrefix, gdata.data.NameSuffix,
        gdata.data.FullName, gdata.data.Name, gdata.data.OrgDepartment,
        gdata.data.OrgName, gdata.data.OrgSymbol, gdata.data.OrgTitle,
        gdata.data.Organization, gdata.data.When, gdata.data.Who,
        gdata.data.OriginalEvent, gdata.data.PhoneNumber,
        gdata.data.PostalAddress, gdata.data.Rating, gdata.data.Recurrence,
        gdata.data.RecurrenceException, gdata.data.Reminder,
        gdata.data.Agent, gdata.data.HouseName, gdata.data.Street,
        gdata.data.PoBox, gdata.data.Neighborhood, gdata.data.City,
        gdata.data.Subregion, gdata.data.Region, gdata.data.Postcode,
        gdata.data.Country, gdata.data.FormattedAddress,
        gdata.data.StructuredPostalAddress, gdata.data.Where,
        gdata.data.AttendeeType, gdata.data.AttendeeStatus])

  def test_member_values(self):
    self.assertEqual(
        gdata.data.TotalResults._qname,
        '{http://a9.com/-/spec/opensearch/1.1/}totalResults')
    self.assertEqual(
        gdata.data.RecurrenceException._qname,
        '{http://schemas.google.com/g/2005}recurrenceException')
    self.assertEqual(gdata.data.RecurrenceException.specialized,
                     'specialized')


def suite():
  return conf.build_suite([StartIndexTest, StartIndexTest, GDataEntryTest,
      LinkFinderTest, GDataFeedTest, BatchEntryTest, BatchFeedTest,
      ExtendedPropertyTest, FeedLinkTest, SimpleV2FeedTest])


if __name__ == '__main__':
  unittest.main()


