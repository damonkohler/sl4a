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


__author__ = 'kunalmshah.userid (Kunal Shah)'

import sys
import os.path
import getopt
import gdata.auth
import gdata.docs.service


class OAuthSample(object):
  """An OAuthSample object demonstrates the three-legged OAuth process."""

  def __init__(self, consumer_key, consuer_secret):
    """Constructor for the OAuthSample object.
    
    Takes a consumer key and consumer secret, authenticates using OAuth
    mechanism and lists the document titles using Document List Data API.
    Uses HMAC-SHA1 signature method.
    
    Args:
      consumer_key: string Domain identifying third_party web application.
      consumer_secret: string Secret generated during registration.
    
    Returns:
      An OAuthSample object used to run the sample demonstrating the
      way to use OAuth authentication mode.
    """
    self.consumer_key = consumer_key
    self.consumer_secret = consuer_secret
    self.gd_client = gdata.docs.service.DocsService()

  def _PrintFeed(self, feed):
    """Prints out the contents of a feed to the console.
   
    Args:
      feed: A gdata.docs.DocumentListFeed instance.
    """
    if not feed.entry:
      print 'No entries in feed.\n'
    i = 1
    for entry in feed.entry:
      print '%d. %s\n' % (i, entry.title.text.encode('UTF-8'))
      i += 1

  def _ListAllDocuments(self):
    """Retrieves a list of all of a user's documents and displays them."""
    feed = self.gd_client.GetDocumentListFeed()
    self._PrintFeed(feed)
  
  def Run(self):
    """Demonstrates usage of OAuth authentication mode and retrieves a list of
    documents using Document List Data API."""
    print '\nSTEP 1: Set OAuth input parameters.'
    self.gd_client.SetOAuthInputParameters(
        gdata.auth.OAuthSignatureMethod.HMAC_SHA1,
        self.consumer_key, consumer_secret=self.consumer_secret)
    print '\nSTEP 2: Fetch OAuth Request token.'
    request_token = self.gd_client.FetchOAuthRequestToken()
    print 'Request Token fetched: %s' % request_token
    print '\nSTEP 3: Set the fetched OAuth token.'
    self.gd_client.SetOAuthToken(request_token)
    print 'OAuth request token set.'
    print '\nSTEP 4: Generate OAuth authorization URL.'
    auth_url = self.gd_client.GenerateOAuthAuthorizationURL()
    print 'Authorization URL: %s' % auth_url
    raw_input('Manually go to the above URL and authenticate.'
              'Press a key after authorization.')
    print '\nSTEP 5: Upgrade to an OAuth access token.'
    self.gd_client.UpgradeToOAuthAccessToken()
    print 'Access Token: %s' % (
        self.gd_client.token_store.find_token(request_token.scopes[0]))
    print '\nYour Documents:\n'
    self._ListAllDocuments()
    print 'STEP 6: Revoke the OAuth access token after use.'
    self.gd_client.RevokeOAuthToken()
    print 'OAuth access token revoked.'


def main():
  """Demonstrates usage of OAuth authentication mode.
  
  Prints a list of documents. This demo uses HMAC-SHA1 signature method.
  """
  # Parse command line options
  try:
    opts, args = getopt.getopt(sys.argv[1:], '', ['consumer_key=',
                                                  'consumer_secret='])
  except getopt.error, msg:
    print ('python oauth_example.py --consumer_key [oauth_consumer_key] '
           '--consumer_secret [consumer_secret] ')
    sys.exit(2)

  consumer_key = ''
  consumer_secret = ''
  # Process options
  for option, arg in opts:
    if option == '--consumer_key':
      consumer_key = arg
    elif option == '--consumer_secret':
      consumer_secret = arg

  while not consumer_key:
    consumer_key = raw_input('Please enter consumer key: ')
  while not consumer_secret:
    consumer_secret = raw_input('Please enter consumer secret: ')

  sample = OAuthSample(consumer_key, consumer_secret)
  sample.Run()


if __name__ == '__main__':
  main()
