#!/usr/bin/python
# -*-*- encoding: utf-8 -*-*-
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


__author__ = 'api.jscudder@gmail.com (Jeff Scudder)'


import sys
import unittest
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom.data
import atom.core
import gdata.test_config as conf


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

class AuthorTest(unittest.TestCase):
  
  def setUp(self):
    self.author = atom.data.Author()
    
  def testEmptyAuthorShouldHaveEmptyExtensionLists(self):
    self.assertTrue(isinstance(self.author._other_elements, list))
    self.assertEqual(len(self.author._other_elements), 0)
    self.assertTrue(isinstance(self.author._other_attributes, dict))
    self.assertEqual(len(self.author._other_attributes), 0)
    
  def testNormalAuthorShouldHaveNoExtensionElements(self):
    self.author.name = atom.data.Name(text='Jeff Scudder')
    self.assertEqual(self.author.name.text, 'Jeff Scudder')
    self.assertEqual(len(self.author._other_elements), 0)
    new_author = atom.core.XmlElementFromString(self.author.ToString(),
        atom.data.Author)
    self.assertEqual(len(new_author._other_elements), 0)
    self.assertEqual(new_author.name.text, 'Jeff Scudder')
    
    self.author.extension_elements.append(atom.data.ExtensionElement(
        'foo', text='bar'))
    self.assertEqual(len(self.author.extension_elements), 1)
    self.assertEqual(self.author.name.text, 'Jeff Scudder')
    new_author = atom.core.parse(self.author.ToString(), atom.data.Author)
    self.assertEqual(len(self.author.extension_elements), 1)
    self.assertEqual(new_author.name.text, 'Jeff Scudder')

  def testEmptyAuthorToAndFromStringShouldMatch(self):
    string_from_author = self.author.ToString()
    new_author = atom.core.XmlElementFromString(string_from_author,
        atom.data.Author)
    string_from_new_author = new_author.ToString()
    self.assertEqual(string_from_author, string_from_new_author)
    
  def testAuthorWithNameToAndFromStringShouldMatch(self):
    self.author.name = atom.data.Name()
    self.author.name.text = 'Jeff Scudder'
    string_from_author = self.author.ToString()
    new_author = atom.core.XmlElementFromString(string_from_author,
        atom.data.Author)
    string_from_new_author = new_author.ToString()
    self.assertEqual(string_from_author, string_from_new_author)
    self.assertEqual(self.author.name.text, new_author.name.text)
  
  def testExtensionElements(self):
    self.author.extension_attributes['foo1'] = 'bar'
    self.author.extension_attributes['foo2'] = 'rab'
    self.assertEqual(self.author.extension_attributes['foo1'], 'bar')
    self.assertEqual(self.author.extension_attributes['foo2'], 'rab')
    new_author = atom.core.parse(str(self.author), atom.data.Author)
    self.assertEqual(new_author.extension_attributes['foo1'], 'bar')
    self.assertEqual(new_author.extension_attributes['foo2'], 'rab')
    
  def testConvertFullAuthorToAndFromString(self):
    TEST_AUTHOR = """<?xml version="1.0" encoding="utf-8"?>
        <author xmlns="http://www.w3.org/2005/Atom">
          <name xmlns="http://www.w3.org/2005/Atom">John Doe</name>
          <email xmlns="http://www.w3.org/2005/Atom">john@example.com</email>
          <uri>http://www.google.com</uri>
        </author>"""
    author = atom.core.parse(TEST_AUTHOR, atom.data.Author)
    self.assertEqual(author.name.text, 'John Doe')
    self.assertEqual(author.email.text, 'john@example.com')
    self.assertEqual(author.uri.text, 'http://www.google.com')
    
    
class EmailTest(unittest.TestCase):
  
  def setUp(self):
    self.email = atom.data.Email()
    
  def testEmailToAndFromString(self):
    self.email.text = 'This is a test'
    new_email = atom.core.parse(self.email.to_string(), atom.data.Email)
    self.assertEqual(self.email.text, new_email.text)
    self.assertEqual(self.email.extension_elements, 
        new_email.extension_elements)
    
  
class NameTest(unittest.TestCase):

  def setUp(self):
    self.name = atom.data.Name()
    
  def testEmptyNameToAndFromStringShouldMatch(self):
    string_from_name = self.name.ToString()
    new_name = atom.core.XmlElementFromString(string_from_name,
        atom.data.Name)
    string_from_new_name = new_name.ToString()
    self.assertEqual(string_from_name, string_from_new_name)
    
  def testText(self):
    self.assertTrue(self.name.text is None)
    self.name.text = 'Jeff Scudder'
    self.assertEqual(self.name.text, 'Jeff Scudder')
    new_name = atom.core.parse(self.name.to_string(), atom.data.Name)
    self.assertEqual(new_name.text, self.name.text)
    
  def testExtensionElements(self):
    self.name.extension_attributes['foo'] = 'bar'
    self.assertEqual(self.name.extension_attributes['foo'], 'bar')
    new_name = atom.core.parse(self.name.ToString(), atom.data.Name)
    self.assertEqual(new_name.extension_attributes['foo'], 'bar')
    
    
class ExtensionElementTest(unittest.TestCase):

  def setUp(self):
    self.ee = atom.data.ExtensionElement('foo')
    self.EXTENSION_TREE = """<?xml version="1.0" encoding="utf-8"?>
        <feed xmlns="http://www.w3.org/2005/Atom">
          <g:author xmlns:g="http://www.google.com">
            <g:name>John Doe
              <g:foo yes="no" up="down">Bar</g:foo>
            </g:name>
          </g:author>
        </feed>"""
    
  def testEmptyEEShouldProduceEmptyString(self):
    pass
    
  def testEEParsesTreeCorrectly(self):
    deep_tree = atom.core.xml_element_from_string(self.EXTENSION_TREE,
        atom.data.ExtensionElement)
    self.assertEqual(deep_tree.tag, 'feed')
    self.assertEqual(deep_tree.namespace, 'http://www.w3.org/2005/Atom')
    self.assert_(deep_tree.children[0].tag == 'author')
    self.assert_(deep_tree.children[0].namespace == 'http://www.google.com')
    self.assert_(deep_tree.children[0].children[0].tag == 'name')
    self.assert_(deep_tree.children[0].children[0].namespace == 
        'http://www.google.com')
    self.assert_(deep_tree.children[0].children[0].text.strip() == 'John Doe')
    self.assert_(deep_tree.children[0].children[0].children[0].text.strip() ==
        'Bar')
    foo = deep_tree.children[0].children[0].children[0]
    self.assert_(foo.tag == 'foo')
    self.assert_(foo.namespace == 'http://www.google.com')
    self.assert_(foo.attributes['up'] == 'down')
    self.assert_(foo.attributes['yes'] == 'no')
    self.assert_(foo.children == [])
  
  def testEEToAndFromStringShouldMatch(self):
    string_from_ee = self.ee.ToString()
    new_ee = atom.core.xml_element_from_string(string_from_ee,
        atom.data.ExtensionElement)
    string_from_new_ee = new_ee.ToString()
    self.assert_(string_from_ee == string_from_new_ee)
    
    deep_tree = atom.core.xml_element_from_string(self.EXTENSION_TREE,
        atom.data.ExtensionElement)
    string_from_deep_tree = deep_tree.ToString()
    new_deep_tree = atom.core.xml_element_from_string(string_from_deep_tree,
        atom.data.ExtensionElement)
    string_from_new_deep_tree = new_deep_tree.ToString()
    self.assert_(string_from_deep_tree == string_from_new_deep_tree)
    
    
class LinkTest(unittest.TestCase):
  
  def setUp(self):
    self.link = atom.data.Link()
    
  def testLinkToAndFromString(self):
    self.link.href = 'test href'
    self.link.hreflang = 'english'
    self.link.type = 'text/html'
    self.link.extension_attributes['foo'] = 'bar'
    self.assert_(self.link.href == 'test href')
    self.assert_(self.link.hreflang == 'english')
    self.assert_(self.link.type == 'text/html')
    self.assert_(self.link.extension_attributes['foo'] == 'bar')
    new_link = atom.core.parse(self.link.ToString(), atom.data.Link)
    self.assert_(self.link.href == new_link.href)
    self.assert_(self.link.type == new_link.type)
    self.assert_(self.link.hreflang == new_link.hreflang)
    self.assert_(self.link.extension_attributes['foo'] == 
        new_link.extension_attributes['foo'])

  def testLinkType(self):
    test_link = atom.data.Link(type='text/html')
    self.assertEqual(test_link.type, 'text/html')


class GeneratorTest(unittest.TestCase):

  def setUp(self):
    self.generator = atom.data.Generator()

  def testGeneratorToAndFromString(self):
    self.generator.uri = 'www.google.com'
    self.generator.version = '1.0'
    self.generator.extension_attributes['foo'] = 'bar'
    self.assert_(self.generator.uri == 'www.google.com')
    self.assert_(self.generator.version == '1.0')
    self.assert_(self.generator.extension_attributes['foo'] == 'bar')
    new_generator = atom.core.parse(self.generator.ToString(), atom.data.Generator)
    self.assert_(self.generator.uri == new_generator.uri)
    self.assert_(self.generator.version == new_generator.version)
    self.assert_(self.generator.extension_attributes['foo'] ==
        new_generator.extension_attributes['foo'])


class TitleTest(unittest.TestCase):

  def setUp(self):
    self.title = atom.data.Title()

  def testTitleToAndFromString(self):
    self.title.type = 'text'
    self.title.text = 'Less: &lt;'
    self.assert_(self.title.type == 'text')
    self.assert_(self.title.text == 'Less: &lt;')
    new_title = atom.core.parse(str(self.title), atom.data.Title)
    self.assert_(self.title.type == new_title.type)
    self.assert_(self.title.text == new_title.text)


class SubtitleTest(unittest.TestCase):

  def setUp(self):
    self.subtitle = atom.data.Subtitle()

  def testTitleToAndFromString(self):
    self.subtitle.type = 'text'
    self.subtitle.text = 'sub & title'
    self.assert_(self.subtitle.type == 'text')
    self.assert_(self.subtitle.text == 'sub & title')
    new_subtitle = atom.core.parse(self.subtitle.ToString(),
                                   atom.data.Subtitle)
    self.assert_(self.subtitle.type == new_subtitle.type)
    self.assert_(self.subtitle.text == new_subtitle.text)


class SummaryTest(unittest.TestCase):

  def setUp(self):
    self.summary = atom.data.Summary()

  def testTitleToAndFromString(self):
    self.summary.type = 'text'
    self.summary.text = 'Less: &lt;'
    self.assert_(self.summary.type == 'text')
    self.assert_(self.summary.text == 'Less: &lt;')
    new_summary = atom.core.parse(self.summary.ToString(), atom.data.Summary)
    self.assert_(self.summary.type == new_summary.type)
    self.assert_(self.summary.text == new_summary.text)


class CategoryTest(unittest.TestCase):

  def setUp(self):
    self.category = atom.data.Category()

  def testCategoryToAndFromString(self):
    self.category.term = 'x'
    self.category.scheme = 'y'
    self.category.label = 'z'
    self.assert_(self.category.term == 'x')
    self.assert_(self.category.scheme == 'y')
    self.assert_(self.category.label == 'z')
    new_category = atom.core.parse(self.category.to_string(),
                                   atom.data.Category)
    self.assert_(self.category.term == new_category.term)
    self.assert_(self.category.scheme == new_category.scheme)
    self.assert_(self.category.label == new_category.label)


class ContributorTest(unittest.TestCase):

  def setUp(self):
    self.contributor = atom.data.Contributor()

  def testContributorToAndFromString(self):
    self.contributor.name = atom.data.Name(text='J Scud')
    self.contributor.email = atom.data.Email(text='nobody@nowhere')
    self.contributor.uri = atom.data.Uri(text='http://www.google.com')
    self.assert_(self.contributor.name.text == 'J Scud')
    self.assert_(self.contributor.email.text == 'nobody@nowhere')
    self.assert_(self.contributor.uri.text == 'http://www.google.com')
    new_contributor = atom.core.parse(self.contributor.ToString(),
                                      atom.data.Contributor)
    self.assert_(self.contributor.name.text == new_contributor.name.text)
    self.assert_(self.contributor.email.text == new_contributor.email.text)
    self.assert_(self.contributor.uri.text == new_contributor.uri.text)


class IdTest(unittest.TestCase):

  def setUp(self):
    self.my_id = atom.data.Id()

  def testIdToAndFromString(self):
    self.my_id.text = 'my nifty id'
    self.assert_(self.my_id.text == 'my nifty id')
    new_id = atom.core.parse(self.my_id.ToString(), atom.data.Id)
    self.assert_(self.my_id.text == new_id.text)


class IconTest(unittest.TestCase):

  def setUp(self):
    self.icon = atom.data.Icon()

  def testIconToAndFromString(self):
    self.icon.text = 'my picture'
    self.assert_(self.icon.text == 'my picture')
    new_icon = atom.core.parse(str(self.icon), atom.data.Icon)
    self.assert_(self.icon.text == new_icon.text)


class LogoTest(unittest.TestCase):

  def setUp(self):
    self.logo = atom.data.Logo()

  def testLogoToAndFromString(self):
    self.logo.text = 'my logo'
    self.assert_(self.logo.text == 'my logo')
    new_logo = atom.core.parse(self.logo.ToString(), atom.data.Logo)
    self.assert_(self.logo.text == new_logo.text)


class RightsTest(unittest.TestCase):

  def setUp(self):
    self.rights = atom.data.Rights()

  def testContributorToAndFromString(self):
    self.rights.text = 'you have the right to remain silent'
    self.rights.type = 'text'
    self.assert_(self.rights.text == 'you have the right to remain silent')
    self.assert_(self.rights.type == 'text')
    new_rights = atom.core.parse(self.rights.ToString(), atom.data.Rights)
    self.assert_(self.rights.text == new_rights.text)
    self.assert_(self.rights.type == new_rights.type)


class UpdatedTest(unittest.TestCase):

  def setUp(self):
    self.updated = atom.data.Updated()

  def testUpdatedToAndFromString(self):
    self.updated.text = 'my time'
    self.assert_(self.updated.text == 'my time')
    new_updated = atom.core.parse(self.updated.ToString(), atom.data.Updated)
    self.assert_(self.updated.text == new_updated.text)


class PublishedTest(unittest.TestCase):

  def setUp(self):
    self.published = atom.data.Published()

  def testPublishedToAndFromString(self):
    self.published.text = 'pub time'
    self.assert_(self.published.text == 'pub time')
    new_published = atom.core.parse(self.published.ToString(),
                                    atom.data.Published)
    self.assert_(self.published.text == new_published.text)


class FeedEntryParentTest(unittest.TestCase):
  """The test accesses hidden methods in atom.FeedEntryParent"""

  def testConvertToAndFromElementTree(self):
    # Use entry because FeedEntryParent doesn't have a tag or namespace.
    original = atom.data.Entry()
    copy = atom.data.FeedEntryParent()
 
    original.author.append(atom.data.Author(name=atom.data.Name(
        text='J Scud')))
    self.assert_(original.author[0].name.text == 'J Scud')
    self.assert_(copy.author == [])

    original.id = atom.data.Id(text='test id')
    self.assert_(original.id.text == 'test id')
    self.assert_(copy.id is None)

    copy._harvest_tree(original._to_tree())
    self.assert_(original.author[0].name.text == copy.author[0].name.text)
    self.assert_(original.id.text == copy.id.text)


class EntryTest(unittest.TestCase):

  def testConvertToAndFromString(self):
    entry = atom.data.Entry()
    entry.author.append(atom.data.Author(name=atom.data.Name(text='js')))
    entry.title = atom.data.Title(text='my test entry')
    self.assert_(entry.author[0].name.text == 'js')
    self.assert_(entry.title.text == 'my test entry')
    new_entry = atom.core.parse(entry.ToString(), atom.data.Entry)
    self.assert_(new_entry.author[0].name.text == 'js')
    self.assert_(new_entry.title.text == 'my test entry')

  def testEntryCorrectlyConvertsActualData(self):
    entry = atom.core.parse(XML_ENTRY_1, atom.data.Entry)
    self.assert_(entry.category[0].scheme == 
        'http://base.google.com/categories/itemtypes')
    self.assert_(entry.category[0].term == 'products')
    self.assert_(entry.id.text == '    http://www.google.com/test/id/url   ')
    self.assert_(entry.title.text == 'Testing 2000 series laptop')
    self.assert_(entry.title.type == 'text')
    self.assert_(entry.content.type == 'xhtml')
    #TODO check all other values for the test entry

  def testAppControl(self):
    TEST_BASE_ENTRY = """<?xml version='1.0'?>
        <entry xmlns='http://www.w3.org/2005/Atom'
               xmlns:g='http://base.google.com/ns/1.0'>
          <category scheme="http://base.google.com/categories/itemtypes"
                    term="products"/>
          <title type='text'>Testing 2000 series laptop</title>
          <content type='xhtml'>
            <div xmlns='http://www.w3.org/1999/xhtml'>A Testing Laptop</div>
          </content>
          <app:control xmlns:app='http://purl.org/atom/app#'>
            <app:draft>yes</app:draft>
            <gm:disapproved 
                xmlns:gm='http://base.google.com/ns-metadata/1.0'/>
          </app:control>
          <link rel='alternate' type='text/html'
                href='http://www.provider-host.com/123456789'/>
          <g:label>Computer</g:label>
          <g:label>Laptop</g:label>
          <g:label>testing laptop</g:label>
          <g:item_type>products</g:item_type>
        </entry>""" 
    entry = atom.core.parse(TEST_BASE_ENTRY, atom.data.Entry)
    self.assertEquals(entry.control.draft.text, 'yes')
    self.assertEquals(len(entry.control.extension_elements), 1)
    self.assertEquals(entry.control.extension_elements[0].tag, 'disapproved')


class ControlTest(unittest.TestCase):

  def testVersionRuleGeneration(self):
    self.assertEqual(atom.core._get_qname(atom.data.Control, 1),
                     '{http://purl.org/atom/app#}control')
    self.assertEqual(atom.data.Control._get_rules(1)[0],
                     '{http://purl.org/atom/app#}control')


  def testVersionedControlFromString(self):
    xml_v1 = """<control xmlns="http://purl.org/atom/app#">
        <draft>no</draft></control>"""
    xml_v2 = """<control xmlns="http://www.w3.org/2007/app">
        <draft>no</draft></control>"""
    control_v1 = atom.core.parse(xml_v1, atom.data.Control, 1)
    control_v2 = atom.core.parse(xml_v2, atom.data.Control, 2)
    self.assertFalse(control_v1 is None)
    self.assertFalse(control_v2 is None)
    # Parsing with mismatched version numbers should return None.
    self.assertTrue(atom.core.parse(xml_v1, atom.data.Control, 2) is None)
    self.assertTrue(atom.core.parse(xml_v2, atom.data.Control, 1) is None)

  def testConvertToAndFromString(self):
    control = atom.data.Control()
    control.text = 'some text'
    control.draft = atom.data.Draft(text='yes')
    self.assertEquals(control.draft.text, 'yes')
    self.assertEquals(control.text, 'some text')
    self.assertTrue(isinstance(control.draft, atom.data.Draft))
    new_control = atom.core.parse(str(control), atom.data.Control)
    self.assertEquals(control.draft.text, new_control.draft.text)
    self.assertEquals(control.text, new_control.text)
    self.assertTrue(isinstance(new_control.draft, atom.data.Draft))


class DraftTest(unittest.TestCase):

  def testConvertToAndFromString(self):
    draft = atom.data.Draft()
    draft.text = 'maybe'
    draft.extension_attributes['foo'] = 'bar'
    self.assertEquals(draft.text, 'maybe')
    self.assertEquals(draft.extension_attributes['foo'], 'bar')
    new_draft = atom.core.parse(str(draft), atom.data.Draft)
    self.assertEquals(draft.text, new_draft.text)
    self.assertEquals(draft.extension_attributes['foo'], 
        new_draft.extension_attributes['foo'])
    
    
    
class SourceTest(unittest.TestCase):

  def testConvertToAndFromString(self):
    source = atom.data.Source()
    source.author.append(atom.data.Author(name=atom.data.Name(text='js')))
    source.title = atom.data.Title(text='my test source')
    source.generator = atom.data.Generator(text='gen')
    self.assert_(source.author[0].name.text == 'js')
    self.assert_(source.title.text == 'my test source')
    self.assert_(source.generator.text == 'gen')
    new_source = atom.core.parse(source.ToString(), atom.data.Source)
    self.assert_(new_source.author[0].name.text == 'js')
    self.assert_(new_source.title.text == 'my test source')
    self.assert_(new_source.generator.text == 'gen')


class FeedTest(unittest.TestCase):

  def testConvertToAndFromString(self):
    feed = atom.data.Feed()
    feed.author.append(atom.data.Author(name=atom.data.Name(text='js')))
    feed.title = atom.data.Title(text='my test source')
    feed.generator = atom.data.Generator(text='gen')
    feed.entry.append(atom.data.Entry(author=[atom.data.Author(
        name=atom.data.Name(text='entry author'))]))
    self.assert_(feed.author[0].name.text == 'js')
    self.assert_(feed.title.text == 'my test source')
    self.assert_(feed.generator.text == 'gen')
    self.assert_(feed.entry[0].author[0].name.text == 'entry author')
    new_feed = atom.core.parse(feed.ToString(), atom.data.Feed)
    self.assert_(new_feed.author[0].name.text == 'js')
    self.assert_(new_feed.title.text == 'my test source')
    self.assert_(new_feed.generator.text == 'gen')    
    self.assert_(new_feed.entry[0].author[0].name.text == 'entry author')

  def testPreserveEntryOrder(self):
    test_xml = (
        '<feed xmlns="http://www.w3.org/2005/Atom">'
          '<entry><id>0</id></entry>'
          '<entry><id>1</id></entry>'
          '<title>Testing Order</title>'
          '<entry><id>2</id></entry>'
          '<entry><id>3</id></entry>'
          '<entry><id>4</id></entry>'
          '<entry><id>5</id></entry>'
          '<entry><id>6</id></entry>'
          '<entry><id>7</id></entry>'
          '<author/>'
          '<entry><id>8</id></entry>'
          '<id>feed_id</id>'
          '<entry><id>9</id></entry>'
        '</feed>')
    feed = atom.core.parse(test_xml, atom.data.Feed)
    for i in xrange(10):
      self.assertEqual(feed.entry[i].id.text, str(i))
    feed = atom.core.parse(feed.ToString(), atom.data.Feed)
    for i in xrange(10):
      self.assertEqual(feed.entry[i].id.text, str(i))
    temp = feed.entry[3]
    feed.entry[3] = feed.entry[4]
    feed.entry[4] = temp
    self.assert_(feed.entry[2].id.text == '2')
    self.assert_(feed.entry[3].id.text == '4')
    self.assert_(feed.entry[4].id.text == '3')
    self.assert_(feed.entry[5].id.text == '5')
    feed = atom.core.parse(feed.to_string(), atom.data.Feed)
    self.assertEqual(feed.entry[2].id.text, '2')
    self.assertEqual(feed.entry[3].id.text, '4')
    self.assertEqual(feed.entry[4].id.text, '3')
    self.assertEqual(feed.entry[5].id.text, '5')


class ContentEntryParentTest(unittest.TestCase):
  """The test accesses hidden methods in atom.FeedEntryParent"""

  def setUp(self):
    self.content = atom.data.Content()

  def testConvertToAndFromElementTree(self):
    self.content.text = 'my content'
    self.content.type = 'text'
    self.content.src = 'my source'
    self.assert_(self.content.text == 'my content')
    self.assert_(self.content.type == 'text')
    self.assert_(self.content.src == 'my source')
    new_content = atom.core.parse(self.content.ToString(), atom.data.Content)
    self.assert_(self.content.text == new_content.text)
    self.assert_(self.content.type == new_content.type)
    self.assert_(self.content.src == new_content.src)

  def testContentConstructorSetsSrc(self):
    new_content = atom.data.Content(src='abcd')
    self.assertEquals(new_content.src, 'abcd')

  def testContentFromString(self):
    content_xml = '<content xmlns="http://www.w3.org/2005/Atom" type="test"/>'
    content = atom.core.parse(content_xml, atom.data.Content)
    self.assertTrue(isinstance(content, atom.data.Content))
    self.assertEqual(content.type, 'test')


class PreserveUnkownElementTest(unittest.TestCase):
  """Tests correct preservation of XML elements which are non Atom"""
  
  def setUp(self):
    GBASE_ATTRIBUTE_FEED = """<?xml version='1.0' encoding='UTF-8'?>
        <feed xmlns='http://www.w3.org/2005/Atom'
              xmlns:openSearch='http://a9.com/-/spec/opensearchrss/1.0/'
              xmlns:gm='http://base.google.com/ns-metadata/1.0'>
          <id>http://www.google.com/base/feeds/attributes</id>
          <updated>2006-11-01T20:35:59.578Z</updated>
          <category scheme='http://base.google.com/categories/itemtypes'
                    term='online jobs'></category>
          <category scheme='http://base.google.com/categories/itemtypes'
                    term='jobs'></category>
          <title type='text'>histogram for query: [item type:jobs]</title>
          <link rel='alternate' type='text/html' 
                href='http://base.google.com'></link>
          <link rel='self' type='application/atom+xml'
                href='http://www.google.com/base/attributes/jobs'></link>
          <generator version='1.0'
                     uri='http://base.google.com'>GoogleBase</generator>
          <openSearch:totalResults>16</openSearch:totalResults>
          <openSearch:startIndex>1</openSearch:startIndex>
          <openSearch:itemsPerPage>16</openSearch:itemsPerPage>
          <entry>
            <id>http://www.google.com/base/feeds/attributes/job+industy</id>
            <updated>2006-11-01T20:36:00.100Z</updated>
            <title type='text'>job industry(text)</title>
            <content type='text'>Attribute"job industry" of type text.
            </content>
            <gm:attribute name='job industry' type='text' count='4416629'>
              <gm:value count='380772'>it internet</gm:value>
              <gm:value count='261565'>healthcare</gm:value>
              <gm:value count='142018'>information technology</gm:value>
              <gm:value count='124622'>accounting</gm:value>
              <gm:value count='111311'>clerical and administrative</gm:value>
              <gm:value count='82928'>other</gm:value>
              <gm:value count='77620'>sales and sales management</gm:value>
              <gm:value count='68764'>information systems</gm:value>
              <gm:value count='65859'>engineering and architecture</gm:value>
              <gm:value count='64757'>sales</gm:value>
            </gm:attribute>
          </entry>
        </feed>"""
    self.feed = atom.core.parse(GBASE_ATTRIBUTE_FEED,
                                atom.data.Feed)

  def testCaptureOpenSearchElements(self):
    self.assertEquals(self.feed.FindExtensions('totalResults')[0].tag,
        'totalResults')
    self.assertEquals(self.feed.FindExtensions('totalResults')[0].namespace,
        'http://a9.com/-/spec/opensearchrss/1.0/')
    open_search_extensions = self.feed.FindExtensions(
        namespace='http://a9.com/-/spec/opensearchrss/1.0/')
    self.assertEquals(len(open_search_extensions), 3)
    for element in open_search_extensions:
      self.assertEquals(element.namespace, 
          'http://a9.com/-/spec/opensearchrss/1.0/')

  def testCaptureMetaElements(self):
    meta_elements = self.feed.entry[0].FindExtensions(
        namespace='http://base.google.com/ns-metadata/1.0')
    self.assertEquals(len(meta_elements), 1)
    self.assertEquals(meta_elements[0].attributes['count'], '4416629')
    self.assertEquals(len(meta_elements[0].children), 10)

  def testCaptureMetaChildElements(self):
    meta_elements = self.feed.entry[0].FindExtensions(
        namespace='http://base.google.com/ns-metadata/1.0')
    meta_children = meta_elements[0].FindChildren(
        namespace='http://base.google.com/ns-metadata/1.0')
    self.assertEquals(len(meta_children), 10)
    for child in meta_children:
      self.assertEquals(child.tag, 'value')


class LinkFinderTest(unittest.TestCase):
  
  def setUp(self):
    self.entry = atom.core.parse(XML_ENTRY_1, atom.data.Entry)

  def testLinkFinderGetsLicenseLink(self):
    self.assertTrue(isinstance(self.entry.GetLink('license'), atom.data.Link))
    self.assertTrue(isinstance(self.entry.GetLicenseLink(), atom.data.Link))
    self.assertEquals(self.entry.GetLink('license').href,
                      'http://creativecommons.org/licenses/by-nc/2.5/rdf')
    self.assertEquals(self.entry.get_license_link().href,
                      'http://creativecommons.org/licenses/by-nc/2.5/rdf')
    self.assertEquals(self.entry.GetLink('license').rel, 'license')
    self.assertEquals(self.entry.FindLicenseLink(),
                      'http://creativecommons.org/licenses/by-nc/2.5/rdf')

  def testLinkFinderGetsAlternateLink(self):
    self.assertTrue(isinstance(self.entry.GetLink('alternate'),
                    atom.data.Link))
    self.assertEquals(self.entry.GetLink('alternate').href,
                      'http://www.provider-host.com/123456789')
    self.assertEquals(self.entry.FindAlternateLink(),
                      'http://www.provider-host.com/123456789')
    self.assertEquals(self.entry.GetLink('alternate').rel, 'alternate')


class AtomBaseTest(unittest.TestCase):

   def testAtomBaseConvertsExtensions(self):
     # Using Id because it adds no additional members.
     atom_base = atom.data.Id()
     extension_child = atom.data.ExtensionElement('foo',
         namespace='http://ns0.com')
     extension_grandchild = atom.data.ExtensionElement('bar', 
         namespace='http://ns0.com')
     extension_child.children.append(extension_grandchild)
     atom_base.extension_elements.append(extension_child)
     self.assertEquals(len(atom_base.extension_elements), 1)
     self.assertEquals(len(atom_base.extension_elements[0].children), 1)
     self.assertEquals(atom_base.extension_elements[0].tag, 'foo')
     self.assertEquals(atom_base.extension_elements[0].children[0].tag, 'bar')
     
     element_tree = atom_base._to_tree()
     self.assert_(element_tree.find('{http://ns0.com}foo') is not None)
     self.assert_(element_tree.find('{http://ns0.com}foo').find(
         '{http://ns0.com}bar') is not None)


class UtfParsingTest(unittest.TestCase):
  
  def setUp(self):
    self.test_xml = u"""<?xml version="1.0" encoding="utf-8"?>
<entry xmlns='http://www.w3.org/2005/Atom'>
  <id>http://www.google.com/test/id/url</id>
  <title type='&#945;&#955;&#966;&#945;'>&#945;&#955;&#966;&#945;</title>
</entry>"""

  def testMemberStringEncoding(self):
    atom_entry = atom.core.parse(self.test_xml, atom.data.Entry)
    self.assertTrue(isinstance(atom_entry.title.type, unicode))
    self.assertEqual(atom_entry.title.type, u'\u03B1\u03BB\u03C6\u03B1')
    self.assertEqual(atom_entry.title.text, u'\u03B1\u03BB\u03C6\u03B1')

    # Setting object members to unicode strings is supported.
    atom_entry.title.type = u'\u03B1\u03BB\u03C6\u03B1'
    xml = atom_entry.ToString()
    # The unicode code points should be converted to XML escaped sequences.
    self.assertTrue('&#945;&#955;&#966;&#945;' in xml)

    # Make sure that we can use plain text when MEMBER_STRING_ENCODING is utf8
    atom_entry.title.type = "plain text"
    atom_entry.title.text = "more text"
    xml = atom_entry.ToString()
    self.assert_("plain text" in xml)
    self.assert_("more text" in xml)

    # Test something else than utf-8
    atom.core.STRING_ENCODING = 'iso8859_7'
    atom_entry = atom.core.parse(self.test_xml, atom.data.Entry)
    self.assert_(atom_entry.title.type == u'\u03B1\u03BB\u03C6\u03B1')
    self.assert_(atom_entry.title.text == u'\u03B1\u03BB\u03C6\u03B1')

    # Test using unicode strings directly for object members
    atom_entry = atom.core.parse(self.test_xml, atom.data.Entry)
    self.assert_(atom_entry.title.type == u'\u03B1\u03BB\u03C6\u03B1')
    self.assert_(atom_entry.title.text == u'\u03B1\u03BB\u03C6\u03B1')
    
    # Make sure that we can use plain text when MEMBER_STRING_ENCODING is 
    # unicode
    atom_entry.title.type = "plain text"
    atom_entry.title.text = "more text"
    xml = atom_entry.ToString()
    self.assert_("plain text" in xml)
    self.assert_("more text" in xml)

  def testConvertExampleXML(self):
    GBASE_STRING_ENCODING_ENTRY = """<?xml version='1.0' encoding='UTF-8'?>
        <entry xmlns='http://www.w3.org/2005/Atom'
               xmlns:gm='http://base.google.com/ns-metadata/1.0'
               xmlns:g='http://base.google.com/ns/1.0'
               xmlns:batch='http://schemas.google.com/gdata/batch'>
          <id>http://www.google.com/base/feeds/snippets/1749</id>
          <published>2007-12-09T03:13:07.000Z</published>
          <updated>2008-01-07T03:26:46.000Z</updated>
          <category scheme='http://base.google.com/categories/itemtypes'
                    term='Products'/>
          <title type='text'>Digital Camera Cord Fits DSC-R1 S40</title>
          <content type='html'>SONY \xC2\xB7 Cybershot Digital Camera Usb
              Cable DESCRIPTION This is a 2.5 USB 2.0 A to Mini B (5 Pin)
              high quality digital camera cable used for connecting your
              Sony Digital Cameras and Camcoders. Backward
              Compatible with USB 2.0, 1.0 and 1.1. Fully  ...</content>
          <link rel='alternate' type='text/html'
                href='http://adfarm.mediaplex.com/ad/ck/711-5256-8196-2mm'/>
          <link rel='self' type='application/atom+xml'
                href='http://www.google.com/base/feeds/snippets/1749'/>
          <author>
            <name>eBay</name>
          </author>
          <g:item_type type='text'>Products</g:item_type>
          <g:item_language type='text'>EN</g:item_language>
          <g:target_country type='text'>US</g:target_country>
          <g:price type='floatUnit'>0.99 usd</g:price>
          <g:image_link 
              type='url'>http://www.example.com/pict/27_1.jpg</g:image_link>
          <g:category type='text'>Cameras &amp; Photo&gt;Digital Camera 
              Accessories&gt;Cables</g:category>
          <g:category type='text'>Cords &amp; USB Cables</g:category>
          <g:customer_id type='int'>11729</g:customer_id>
          <g:id type='text'>270195049057</g:id>
          <g:expiration_date
              type='dateTime'>2008-02-06T03:26:46Z</g:expiration_date>
        </entry>"""
    try:
      entry = atom.core.parse(GBASE_STRING_ENCODING_ENTRY,
                              atom.data.Entry)
    except UnicodeDecodeError:
      self.fail('Error when converting XML')


class VersionedXmlTest(unittest.TestCase):

  def test_monoversioned_parent_with_multiversioned_child(self):
    v2_rules = atom.data.Entry._get_rules(2)
    self.assertTrue('{http://www.w3.org/2007/app}control' in v2_rules[1])

    entry_xml = """<entry xmlns='http://www.w3.org/2005/Atom'>
                     <app:control xmlns:app='http://www.w3.org/2007/app'>
                       <app:draft>yes</app:draft>
                     </app:control>
                   </entry>"""

    entry = e = atom.core.parse(entry_xml, atom.data.Entry, version=2)
    self.assertTrue(entry is not None)
    self.assertTrue(entry.control is not None)
    self.assertTrue(entry.control.draft is not None)
    self.assertEqual(entry.control.draft.text, 'yes')

    # v1 rules should not parse v2 XML.
    entry = e = atom.core.parse(entry_xml, atom.data.Entry, version=1)
    self.assertTrue(entry is not None)
    self.assertTrue(entry.control is None)
    
    # The default version should be v1.
    entry = e = atom.core.parse(entry_xml, atom.data.Entry)
    self.assertTrue(entry is not None)
    self.assertTrue(entry.control is None)


def suite():
  return conf.build_suite([AuthorTest, EmailTest, NameTest, 
                           ExtensionElementTest, LinkTest, GeneratorTest,
                           TitleTest, SubtitleTest, SummaryTest, IdTest,
                           IconTest, LogoTest, RightsTest, UpdatedTest,
                           PublishedTest, FeedEntryParentTest, EntryTest,
                           ContentEntryParentTest, PreserveUnkownElementTest,
                           FeedTest, LinkFinderTest, AtomBaseTest, 
                           UtfParsingTest, VersionedXmlTest])


if __name__ == '__main__':
  unittest.main()
