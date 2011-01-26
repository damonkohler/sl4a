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


import urllib

import gdata.webmastertools.service
import gdata.service
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom
import getpass


username = ''
password = ''

username = raw_input('Please enter your username: ')
password = getpass.getpass()

client = gdata.webmastertools.service.GWebmasterToolsService(
    email=username, 
    password=password, source='PythonWebmasterToolsSample-1')

EXAMPLE_SITE = 'http://www.example.com/'
EXAMPLE_SITEMAP = 'http://www.example.com/sitemap-index.xml'


def safeElementText(element):
  if hasattr(element, 'text'):
     return element.text
  return ''


print 'Logging in'
client.ProgrammaticLogin()

print
print 'Adding site: %s' % EXAMPLE_SITE
entry = client.AddSite(EXAMPLE_SITE)

print
print "%-25s %25s %25s" % ('Site', 'Last Updated', 'Last Crawled')
print '='*80
print "%-25s %25s %25s" % (
  entry.title.text.replace('http://', '')[:25], entry.updated.text[:25],
  safeElementText(entry.crawled)[:25])
print "  Preferred: %-23s Indexed: %5s        GeoLoc: %10s" % (
  safeElementText(entry.preferred_domain)[:30], entry.indexed.text[:5],
  safeElementText(entry.geolocation)[:10])
print "  Crawl rate: %-10s            Verified: %5s" % (
  safeElementText(entry.crawl_rate)[:10], entry.verified.text[:5])

# Verifying a site. This sample won't do this since we don't own example.com
#client.VerifySite(EXAMPLE_SITE, 'htmlpage')

# The following needs the ownership of the site
#client.UpdateGeoLocation(EXAMPLE_SITE, 'US')
#client.UpdateCrawlRate(EXAMPLE_SITE, 'normal')
#client.UpdatePreferredDomain(EXAMPLE_SITE, 'preferwww')
#client.UpdateEnhancedImageSearch(EXAMPLE_SITE, 'true')

print
print 'Adding sitemap: %s' % EXAMPLE_SITEMAP
entry = client.AddSitemap(EXAMPLE_SITE, EXAMPLE_SITEMAP)

print entry.title.text.replace('http://', '')[:80]
print "  Last Updated   : %29s              Status: %10s" % (
    entry.updated.text[:29], entry.sitemap_status.text[:10])
print "  Last Downloaded: %29s           URL Count: %10s" % (
    safeElementText(entry.sitemap_last_downloaded)[:29],
    safeElementText(entry.sitemap_url_count)[:10])

# Add a mobile sitemap
#entry = client.AddMobileSitemap(EXAMPLE_SITE, 'http://.../sitemap-mobile-example.xml', 'XHTML')

# Add a news sitemap, your site must be included in Google News.
# See also http://google.com/support/webmasters/bin/answer.py?answer=42738
#entry = client.AddNewsSitemap(EXAMPLE_SITE, 'http://.../sitemap-news-example.xml', 'Label')

print
print 'Deleting sitemap: %s' % EXAMPLE_SITEMAP
client.DeleteSitemap(EXAMPLE_SITE, EXAMPLE_SITEMAP)

print
print 'Deleting site: %s' % EXAMPLE_SITE
client.DeleteSite(EXAMPLE_SITE)
print
