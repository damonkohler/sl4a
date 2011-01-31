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


import gdata.base.service
import gdata.service
try:
  from xml.etree import ElementTree
except ImportError:
  from elementtree import ElementTree
import atom
import gdata.base

# Demonstrates queries to the itemtypes feed for specified locale.

gb_client = gdata.base.service.GBaseService()
locale = raw_input('Please enter locale (ex: en_US): ')
q = gdata.base.service.BaseQuery()
q.feed = '/base/feeds/itemtypes/%s' % locale

print q.ToUri()
feed = gb_client.QueryItemTypesFeed(q.ToUri())

print feed.title.text

for entry in feed.entry:
  print '\t' + entry.title.text
  for attr in entry.attributes.attribute:
    print '\t\tAttr name:%s, type:%s' % (attr.name, attr.type)
