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

print 'Logging in'
client.ProgrammaticLogin()

print 'Retrieving Sites feed'
feed = client.GetSitesFeed()

# Format the feed
print
print 'You have %d site(s), last updated at %s' % (
    len(feed.entry), feed.updated.text)
print
print "%-25s %25s %25s" % ('Site', 'Last Updated', 'Last Crawled')
print '='*80


def safeElementText(element):
  if hasattr(element, 'text'):
     return element.text
  return ''


# Format each site
for entry in feed.entry:
  print "%-25s %25s %25s" % (
      entry.title.text.replace('http://', '')[:25], entry.updated.text[:25],
      safeElementText(entry.crawled)[:25])
  print "  Preferred: %-23s Indexed: %5s        GeoLoc: %10s" % (
      safeElementText(entry.preferred_domain)[:30], entry.indexed.text[:5],
      safeElementText(entry.geolocation)[:10])
  print "  Crawl rate: %-10s            Verified: %5s" % (
      safeElementText(entry.crawl_rate)[:10], entry.verified.text[:5])

print
