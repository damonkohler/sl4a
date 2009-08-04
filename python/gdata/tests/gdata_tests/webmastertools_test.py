#!/usr/bin/python
#
# Copyright (C) 2008 Yu-Jie Lin
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


__author__ = 'livibetter (Yu-Jie Lin)'


import unittest
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import gdata
from gdata import test_data
import gdata.webmastertools as webmastertools


class IndexedTest(unittest.TestCase):

  def setUp(self):
    self.indexed = webmastertools.Indexed()

  def testToAndFromString(self):
    self.indexed.text = 'true'
    self.assert_(self.indexed.text == 'true')
    new_indexed = webmastertools.IndexedFromString(self.indexed.ToString())
    self.assert_(self.indexed.text == new_indexed.text)


class CrawledTest(unittest.TestCase):

  def setUp(self):
    self.crawled = webmastertools.Crawled()

  def testToAndFromString(self):
    self.crawled.text = 'true'
    self.assert_(self.crawled.text == 'true')
    new_crawled = webmastertools.CrawledFromString(self.crawled.ToString())
    self.assert_(self.crawled.text == new_crawled.text)


class GeoLocationTest(unittest.TestCase):

  def setUp(self):
    self.geolocation = webmastertools.GeoLocation()

  def testToAndFromString(self):
    self.geolocation.text = 'US'
    self.assert_(self.geolocation.text == 'US')
    new_geolocation = webmastertools.GeoLocationFromString(
        self.geolocation.ToString())
    self.assert_(self.geolocation.text == new_geolocation.text)


class PreferredDomainTest(unittest.TestCase):

  def setUp(self):
    self.preferred_domain = webmastertools.PreferredDomain()

  def testToAndFromString(self):
    self.preferred_domain.text = 'none'
    self.assert_(self.preferred_domain.text == 'none')
    new_preferred_domain = webmastertools.PreferredDomainFromString(
        self.preferred_domain.ToString())
    self.assert_(self.preferred_domain.text == new_preferred_domain.text)


class CrawlRateTest(unittest.TestCase):

  def setUp(self):
    self.crawl_rate = webmastertools.CrawlRate()

  def testToAndFromString(self):
    self.crawl_rate.text = 'normal'
    self.assert_(self.crawl_rate.text == 'normal')
    new_crawl_rate = webmastertools.CrawlRateFromString(
        self.crawl_rate.ToString())
    self.assert_(self.crawl_rate.text == new_crawl_rate.text)


class EnhancedImageSearchTest(unittest.TestCase):

  def setUp(self):
    self.enhanced_image_search = webmastertools.EnhancedImageSearch()

  def testToAndFromString(self):
    self.enhanced_image_search.text = 'true'
    self.assert_(self.enhanced_image_search.text == 'true')
    new_enhanced_image_search = webmastertools.EnhancedImageSearchFromString(
        self.enhanced_image_search.ToString())
    self.assert_(self.enhanced_image_search.text == 
        new_enhanced_image_search.text)


class VerifiedTest(unittest.TestCase):

  def setUp(self):
    self.verified = webmastertools.Verified()

  def testToAndFromString(self):
    self.verified.text = 'true'
    self.assert_(self.verified.text == 'true')
    new_verified = webmastertools.VerifiedFromString(self.verified.ToString())
    self.assert_(self.verified.text == new_verified.text)


class VerificationMethodMetaTest(unittest.TestCase):

  def setUp(self):
    self.meta = webmastertools.VerificationMethodMeta()

  def testToAndFromString(self):
    self.meta.name = 'verify-vf1'
    self.meta.content = 'a2Ai'
    self.assert_(self.meta.name == 'verify-vf1')
    self.assert_(self.meta.content == 'a2Ai')
    new_meta = webmastertools.VerificationMethodMetaFromString(
        self.meta.ToString())
    self.assert_(self.meta.name == new_meta.name)
    self.assert_(self.meta.content == new_meta.content)


class VerificationMethodTest(unittest.TestCase):

  def setUp(self):
    pass

  def testMetaTagToAndFromString(self):
    self.method = webmastertools.VerificationMethod()
    self.method.type = 'metatag'
    self.method.in_use = 'false'
    self.assert_(self.method.type == 'metatag')
    self.assert_(self.method.in_use == 'false')
    self.method.meta = webmastertools.VerificationMethodMeta(name='verify-vf1',
        content='a2Ai')
    self.assert_(self.method.meta.name == 'verify-vf1')
    self.assert_(self.method.meta.content == 'a2Ai')
    new_method = webmastertools.VerificationMethodFromString(
        self.method.ToString())
    self.assert_(self.method.type == new_method.type)
    self.assert_(self.method.in_use == new_method.in_use)
    self.assert_(self.method.meta.name == new_method.meta.name)
    self.assert_(self.method.meta.content == new_method.meta.content)

  def testHtmlPageToAndFromString(self):
    self.method = webmastertools.VerificationMethod()
    self.method.type = 'htmlpage'
    self.method.in_use = 'false'
    self.method.text = '456456-google.html'
    self.assert_(self.method.type == 'htmlpage')
    self.assert_(self.method.in_use == 'false')
    self.assert_(self.method.text == '456456-google.html')
    self.assert_(self.method.meta is None)
    new_method = webmastertools.VerificationMethodFromString(
        self.method.ToString())
    self.assert_(self.method.type == new_method.type)
    self.assert_(self.method.in_use == new_method.in_use)
    self.assert_(self.method.text == new_method.text)
    self.assert_(self.method.meta is None)

  def testConvertActualData(self):
    feed = webmastertools.SitesFeedFromString(test_data.SITES_FEED)
    self.assert_(len(feed.entry[0].verification_method) == 2)
    check = 0
    for method in feed.entry[0].verification_method:
      self.assert_(isinstance(method, webmastertools.VerificationMethod))
      if method.type == 'metatag':
        self.assert_(method.in_use == 'false')
        self.assert_(method.text is None)
        self.assert_(method.meta.name == 'verify-v1')
        self.assert_(method.meta.content == 'a2Ai')
        check = check | 1
      elif method.type == 'htmlpage':
        self.assert_(method.in_use == 'false')
        self.assert_(method.text == '456456-google.html')
        check = check | 2
      else:
        self.fail('Wrong Verification Method: %s' % method.type)
    self.assert_(check == 2 ** 2 - 1,
        'Should only have two Verification Methods, metatag and htmlpage')


class MarkupLanguageTest(unittest.TestCase):

  def setUp(self):
    self.markup_language = webmastertools.MarkupLanguage()

  def testToAndFromString(self):
    self.markup_language.text = 'HTML'
    self.assert_(self.markup_language.text == 'HTML')
    new_markup_language = webmastertools.MarkupLanguageFromString(
        self.markup_language.ToString())
    self.assert_(self.markup_language.text == new_markup_language.text)


class SitemapMobileTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_mobile = webmastertools.SitemapMobile()

  def testToAndFromString(self):
    self.sitemap_mobile.markup_language.append(webmastertools.MarkupLanguage(
        text = 'HTML'))
    self.assert_(self.sitemap_mobile.text is None)
    self.assert_(self.sitemap_mobile.markup_language[0].text == 'HTML')
    new_sitemap_mobile = webmastertools.SitemapMobileFromString(
        self.sitemap_mobile.ToString())
    self.assert_(new_sitemap_mobile.text is None)
    self.assert_(self.sitemap_mobile.markup_language[0].text == 
        new_sitemap_mobile.markup_language[0].text)

  def testConvertActualData(self):
    feed = webmastertools.SitemapsFeedFromString(test_data.SITEMAPS_FEED)
    self.assert_(feed.sitemap_mobile.text.strip() == '')
    self.assert_(len(feed.sitemap_mobile.markup_language) == 2)
    check = 0
    for markup_language in feed.sitemap_mobile.markup_language:
      self.assert_(isinstance(markup_language, webmastertools.MarkupLanguage))
      if markup_language.text == "HTML":
        check = check | 1
      elif markup_language.text == "WAP":
        check = check | 2
      else:
        self.fail('Unexpected markup language: %s' % markup_language.text)
    self.assert_(check == 2 ** 2 - 1, "Something is wrong with markup language")


class SitemapMobileMarkupLanguageTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_mobile_markup_language =\
        webmastertools.SitemapMobileMarkupLanguage()

  def testToAndFromString(self):
    self.sitemap_mobile_markup_language.text = 'HTML'
    self.assert_(self.sitemap_mobile_markup_language.text == 'HTML')
    new_sitemap_mobile_markup_language =\
        webmastertools.SitemapMobileMarkupLanguageFromString(
            self.sitemap_mobile_markup_language.ToString())
    self.assert_(self.sitemap_mobile_markup_language.text ==\
        new_sitemap_mobile_markup_language.text)


class PublicationLabelTest(unittest.TestCase):

  def setUp(self):
    self.publication_label = webmastertools.PublicationLabel()

  def testToAndFromString(self):
    self.publication_label.text = 'Value1'
    self.assert_(self.publication_label.text == 'Value1')
    new_publication_label = webmastertools.PublicationLabelFromString(
        self.publication_label.ToString())
    self.assert_(self.publication_label.text == new_publication_label.text)


class SitemapNewsTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_news = webmastertools.SitemapNews()

  def testToAndFromString(self):
    self.sitemap_news.publication_label.append(webmastertools.PublicationLabel(
        text = 'Value1'))
    self.assert_(self.sitemap_news.text is None)
    self.assert_(self.sitemap_news.publication_label[0].text == 'Value1')
    new_sitemap_news = webmastertools.SitemapNewsFromString(
        self.sitemap_news.ToString())
    self.assert_(new_sitemap_news.text is None)
    self.assert_(self.sitemap_news.publication_label[0].text == 
        new_sitemap_news.publication_label[0].text)

  def testConvertActualData(self):
    feed = webmastertools.SitemapsFeedFromString(test_data.SITEMAPS_FEED)
    self.assert_(len(feed.sitemap_news.publication_label) == 3)
    check = 0
    for publication_label in feed.sitemap_news.publication_label:
      if publication_label.text == "Value1":
        check = check | 1
      elif publication_label.text == "Value2":
        check = check | 2
      elif publication_label.text == "Value3":
        check = check | 4
      else:
        self.fail('Unexpected publication label: %s' % markup_language.text)
    self.assert_(check == 2 ** 3 - 1,
        'Something is wrong with publication label')


class SitemapNewsPublicationLabelTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_news_publication_label =\
        webmastertools.SitemapNewsPublicationLabel()

  def testToAndFromString(self):
    self.sitemap_news_publication_label.text = 'LabelValue'
    self.assert_(self.sitemap_news_publication_label.text == 'LabelValue')
    new_sitemap_news_publication_label =\
        webmastertools.SitemapNewsPublicationLabelFromString(
            self.sitemap_news_publication_label.ToString())
    self.assert_(self.sitemap_news_publication_label.text ==\
        new_sitemap_news_publication_label.text)


class SitemapLastDownloadedTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_last_downloaded = webmastertools.SitemapLastDownloaded()

  def testToAndFromString(self):
    self.sitemap_last_downloaded.text = '2006-11-18T19:27:32.543Z'
    self.assert_(self.sitemap_last_downloaded.text ==\
        '2006-11-18T19:27:32.543Z')
    new_sitemap_last_downloaded =\
        webmastertools.SitemapLastDownloadedFromString(
            self.sitemap_last_downloaded.ToString())
    self.assert_(self.sitemap_last_downloaded.text ==\
        new_sitemap_last_downloaded.text)


class SitemapTypeTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_type = webmastertools.SitemapType()

  def testToAndFromString(self):
    self.sitemap_type.text = 'WEB'
    self.assert_(self.sitemap_type.text == 'WEB')
    new_sitemap_type = webmastertools.SitemapTypeFromString(
        self.sitemap_type.ToString())
    self.assert_(self.sitemap_type.text == new_sitemap_type.text)


class SitemapStatusTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_status = webmastertools.SitemapStatus()

  def testToAndFromString(self):
    self.sitemap_status.text = 'Pending'
    self.assert_(self.sitemap_status.text == 'Pending')
    new_sitemap_status = webmastertools.SitemapStatusFromString(
        self.sitemap_status.ToString())
    self.assert_(self.sitemap_status.text == new_sitemap_status.text)


class SitemapUrlCountTest(unittest.TestCase):

  def setUp(self):
    self.sitemap_url_count = webmastertools.SitemapUrlCount()

  def testToAndFromString(self):
    self.sitemap_url_count.text = '0'
    self.assert_(self.sitemap_url_count.text == '0')
    new_sitemap_url_count = webmastertools.SitemapUrlCountFromString(
        self.sitemap_url_count.ToString())
    self.assert_(self.sitemap_url_count.text == new_sitemap_url_count.text)


class SitesEntryTest(unittest.TestCase):

  def setUp(self):
    pass
    
  def testToAndFromString(self):
    entry = webmastertools.SitesEntry(
        indexed=webmastertools.Indexed(text='true'),
        crawled=webmastertools.Crawled(text='2008-09-14T08:59:28.000'),
        geolocation=webmastertools.GeoLocation(text='US'),
        preferred_domain=webmastertools.PreferredDomain(text='none'),
        crawl_rate=webmastertools.CrawlRate(text='normal'),
        enhanced_image_search=webmastertools.EnhancedImageSearch(text='true'),
        verified=webmastertools.Verified(text='false'),
        )
    self.assert_(entry.indexed.text == 'true')
    self.assert_(entry.crawled.text == '2008-09-14T08:59:28.000')
    self.assert_(entry.geolocation.text == 'US')
    self.assert_(entry.preferred_domain.text == 'none')
    self.assert_(entry.crawl_rate.text == 'normal')
    self.assert_(entry.enhanced_image_search.text == 'true')
    self.assert_(entry.verified.text == 'false')
    new_entry = webmastertools.SitesEntryFromString(entry.ToString())
    self.assert_(new_entry.indexed.text == 'true')
    self.assert_(new_entry.crawled.text == '2008-09-14T08:59:28.000')
    self.assert_(new_entry.geolocation.text == 'US')
    self.assert_(new_entry.preferred_domain.text == 'none')
    self.assert_(new_entry.crawl_rate.text == 'normal')
    self.assert_(new_entry.enhanced_image_search.text == 'true')
    self.assert_(new_entry.verified.text == 'false')
    
  def testConvertActualData(self):
    feed = webmastertools.SitesFeedFromString(test_data.SITES_FEED)
    self.assert_(len(feed.entry) == 1)
    entry = feed.entry[0]
    self.assert_(isinstance(entry, webmastertools.SitesEntry))
    self.assert_(entry.indexed.text == 'true')
    self.assert_(entry.crawled.text == '2008-09-14T08:59:28.000')
    self.assert_(entry.geolocation.text == 'US')
    self.assert_(entry.preferred_domain.text == 'none')
    self.assert_(entry.crawl_rate.text == 'normal')
    self.assert_(entry.enhanced_image_search.text == 'true')
    self.assert_(entry.verified.text == 'false')


class SitesFeedTest(unittest.TestCase):
  
  def setUp(self):
    self.feed = gdata.webmastertools.SitesFeedFromString(test_data.SITES_FEED)
    
  def testToAndFromString(self):
    self.assert_(len(self.feed.entry) == 1)
    for entry in self.feed.entry:
      self.assert_(isinstance(entry, webmastertools.SitesEntry))
    new_feed = webmastertools.SitesFeedFromString(self.feed.ToString())
    self.assert_(len(new_feed.entry) == 1)
    for entry in new_feed.entry:
      self.assert_(isinstance(entry, webmastertools.SitesEntry))


class SitemapsEntryTest(unittest.TestCase):

  def testRegularToAndFromString(self):
    entry = webmastertools.SitemapsEntry(
       sitemap_type=webmastertools.SitemapType(text='WEB'),
       sitemap_status=webmastertools.SitemapStatus(text='Pending'),
       sitemap_last_downloaded=webmastertools.SitemapLastDownloaded(
          text='2006-11-18T19:27:32.543Z'),
       sitemap_url_count=webmastertools.SitemapUrlCount(text='102'),
       )
    self.assert_(entry.sitemap_type.text == 'WEB')
    self.assert_(entry.sitemap_status.text == 'Pending')
    self.assert_(entry.sitemap_last_downloaded.text ==\
        '2006-11-18T19:27:32.543Z')
    self.assert_(entry.sitemap_url_count.text == '102')
    new_entry = webmastertools.SitemapsEntryFromString(entry.ToString())
    self.assert_(new_entry.sitemap_type.text == 'WEB')
    self.assert_(new_entry.sitemap_status.text == 'Pending')
    self.assert_(new_entry.sitemap_last_downloaded.text ==\
        '2006-11-18T19:27:32.543Z')
    self.assert_(new_entry.sitemap_url_count.text == '102')

  def testConvertActualData(self):
    feed = gdata.webmastertools.SitemapsFeedFromString(test_data.SITEMAPS_FEED)
    self.assert_(len(feed.entry) == 3)
    for entry in feed.entry:
      self.assert_(entry, webmastertools.SitemapsEntry)
      self.assert_(entry.sitemap_status, webmastertools.SitemapStatus)
      self.assert_(entry.sitemap_last_downloaded,
          webmastertools.SitemapLastDownloaded)
      self.assert_(entry.sitemap_url_count, webmastertools.SitemapUrlCount)
      self.assert_(entry.sitemap_status.text == 'StatusValue')
      self.assert_(entry.sitemap_last_downloaded.text ==\
          '2006-11-18T19:27:32.543Z')
      self.assert_(entry.sitemap_url_count.text == '102')
      if entry.id.text == 'http://www.example.com/sitemap-index.xml':
        self.assert_(entry.sitemap_type, webmastertools.SitemapType)
        self.assert_(entry.sitemap_type.text == 'WEB')
        self.assert_(entry.sitemap_mobile_markup_language is None)
        self.assert_(entry.sitemap_news_publication_label is None)
      elif entry.id.text == 'http://www.example.com/mobile/sitemap-index.xml':
        self.assert_(entry.sitemap_mobile_markup_language,
            webmastertools.SitemapMobileMarkupLanguage)
        self.assert_(entry.sitemap_mobile_markup_language.text == 'HTML')
        self.assert_(entry.sitemap_type is None)
        self.assert_(entry.sitemap_news_publication_label is None)
      elif entry.id.text == 'http://www.example.com/news/sitemap-index.xml':
        self.assert_(entry.sitemap_news_publication_label,
            webmastertools.SitemapNewsPublicationLabel)
        self.assert_(entry.sitemap_news_publication_label.text == 'LabelValue')
        self.assert_(entry.sitemap_type is None)
        self.assert_(entry.sitemap_mobile_markup_language is None)


class SitemapsFeedTest(unittest.TestCase):
  
  def setUp(self):
    self.feed = gdata.webmastertools.SitemapsFeedFromString(
        test_data.SITEMAPS_FEED)
    
  def testToAndFromString(self):
    self.assert_(len(self.feed.entry) == 3)
    for entry in self.feed.entry:
      self.assert_(isinstance(entry, webmastertools.SitemapsEntry))
    new_feed = webmastertools.SitemapsFeedFromString(self.feed.ToString())
    self.assert_(len(new_feed.entry) == 3)
    for entry in new_feed.entry:
      self.assert_(isinstance(entry, webmastertools.SitemapsEntry))


if __name__ == '__main__':
  unittest.main()
