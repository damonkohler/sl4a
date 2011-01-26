#!/usr/bin/python
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


__author__ = 'e.bidelman (Eric Bidelman)'

import gdata.contacts
import gdata.contacts.service
import gdata.docs
import gdata.docs.service


CONSUMER_KEY = 'yourdomain.com'
CONSUMER_SECRET = 'YOUR_CONSUMER_KEY'
SIG_METHOD = gdata.auth.OAuthSignatureMethod.HMAC_SHA1

requestor_id = 'any.user@yourdomain.com'

# Contacts Data API ============================================================
contacts = gdata.contacts.service.ContactsService()
contacts.SetOAuthInputParameters(
    SIG_METHOD, CONSUMER_KEY, consumer_secret=CONSUMER_SECRET,
    two_legged_oauth=True, requestor_id=requestor_id)

# GET - fetch user's contact list
print "\nList of contacts for %s:" % (requestor_id,)
feed = contacts.GetContactsFeed()
for entry in feed.entry:
  print entry.title.text
  
# GET - fetch another user's contact list
requestor_id = 'another_user@yourdomain.com'
print "\nList of contacts for %s:" % (requestor_id,)
contacts.GetOAuthInputParameters().requestor_id = requestor_id
feed = contacts.GetContactsFeed()
for entry in feed.entry:
  print entry.title.text


# Google Documents List Data API ===============================================
docs = gdata.docs.service.DocsService()
docs.SetOAuthInputParameters(
    SIG_METHOD, CONSUMER_KEY, consumer_secret=CONSUMER_SECRET,
    two_legged_oauth=True, requestor_id=requestor_id)

# POST - upload a document
print "\nUploading document to %s's Google Documents account:" % (requestor_id,)
ms = gdata.MediaSource(
    file_path='/path/to/test.txt',
    content_type=gdata.docs.service.SUPPORTED_FILETYPES['TXT'])

# GET - fetch user's document list
entry = docs.UploadDocument(ms, 'Company Perks')
print 'Document now accessible online at:', entry.GetAlternateLink().href

print "\nList of Google Documents for %s" % (requestor_id,)
feed = docs.GetDocumentListFeed()
for entry in feed.entry:
  print entry.title.text
