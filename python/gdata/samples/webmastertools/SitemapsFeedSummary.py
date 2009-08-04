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
site_uri = ''

username = raw_input('Please enter your username: ')
password = getpass.getpass()
site_uri = raw_input('Please enter your site url: ')

client = gdata.webmastertools.service.GWebmasterToolsService(
    email=username, 
    password=password, source='PythonWebmasterToolsSample-1')

print 'Logging in'
client.ProgrammaticLogin()

print 'Retrieving Sitemaps feed'
feed = client.GetSitemapsFeed(site_uri)

# Format the feed
print
print 'You have %d sitemap(s), last updated at %s' % (
    len(feed.entry), feed.updated.text)
print
print '='*80


def safeElementText(element):
  if hasattr(element, 'text'):
     return element.text
  return ''


# Format each site
for entry in feed.entry:
  print entry.title.text.replace('http://', '')[:80]
  print "  Last Updated   : %29s              Status: %10s" % (
      entry.updated.text[:29], entry.sitemap_status.text[:10])
  print "  Last Downloaded: %29s           URL Count: %10s" % (
      safeElementText(entry.sitemap_last_downloaded)[:29],
      safeElementText(entry.sitemap_url_count)[:10])
print
