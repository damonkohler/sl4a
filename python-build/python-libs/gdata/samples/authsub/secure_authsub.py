#!/usr/bin/python
#
# Copyright 2008 Google Inc. All Rights Reserved.
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

"""Sample to demonstrate using secure AuthSub in the Google Data Python client.

This sample focuses on the Google Health Data API because it requires the use
of secure tokens.  This samples makes queries against the H9 Developer's
Sandbox (https://www.google.com/h9).  To run this sample:
  1.) Use Apache's mod_python
  2.) Run from your local webserver (e.g. http://localhost/...)
  3.) You need to have entered medication data into H9

  HealthAubSubHelper: Class to handle secure AuthSub tokens.
  GetMedicationHTML: Returns the user's medication formatted in HTML.
  index: Main entry point for the web app.
"""

__author__ = 'e.bidelman@google.com (Eric Bidelman)'


import os
import sys
import urllib
import gdata.auth
import gdata.service

H9_PROFILE_FEED_URL = 'https://www.google.com/h9/feeds/profile/default'

class HealthAuthSubHelper(object):
  """A secure AuthSub helper to interact with the Google Health Data API"""

  H9_AUTHSUB_HANDLER = 'https://www.google.com/h9/authsub'
  H9_SCOPE = 'https://www.google.com/h9/feeds/'

  def GetNextUrl(self, req):
    """Computes the current URL the web app is running from.

    Args:
      req: mod_python mp_request instance to build the URL from.

    Returns:
      A string representing the web app's URL.
    """
    if req.is_https():
      next_url = 'https://'
    else:
      next_url = 'http://'
    next_url += req.hostname + req.unparsed_uri
    return next_url

  def GenerateAuthSubRequestUrl(self, next, scopes=[H9_SCOPE],
                                secure=True, session=True, extra_params=None,
                                include_scopes_in_next=True):
    """Constructs the URL to the AuthSub token handler.

    Args:
      next: string The URL AuthSub will redirect back to.
          Use self.GetNextUrl() to return that URL.
      scopes: (optional) string or list of scopes the token will be valid for.
      secure: (optional) boolean True if the token should be a secure one
      session: (optional) boolean True if the token will be exchanged for a
          session token.
      extra_params: (optional) dict of additional parameters to pass to AuthSub.
      include_scopes_in_next: (optional) boolean True if the scopes in the
          scopes should be passed to AuthSub.

    Returns:
      A string (as a URL) to use for the AuthSubRequest endpoint.
    """
    auth_sub_url = gdata.service.GenerateAuthSubRequestUrl(
        next, scopes, hd='default', secure=secure, session=session,
        request_url=self.H9_AUTHSUB_HANDLER,
        include_scopes_in_next=include_scopes_in_next)
    if extra_params:
      auth_sub_url = '%s&%s' % (auth_sub_url, urllib.urlencode(extra_params))
    return auth_sub_url

  def SetPrivateKey(self, filename):
    """Reads the private key from the specified file.

    See http://code.google.com/apis/gdata/authsub.html#Registered for\
    information on how to create a RSA private key/public cert pair.

    Args:
      filename: string .pem file the key is stored in.

    Returns:
      The private key as a string.

    Raises:
      IOError: The file could not be read or does not exist.
    """
    try:
      f = open(filename)
      rsa_private_key = f.read()
      f.close()
    except IOError, (errno, strerror):
      raise 'I/O error(%s): %s' % (errno, strerror)
    self.rsa_key = rsa_private_key
    return rsa_private_key



def GetMedicationHTML(feed):
  """Prints out the user's medication to the console.

  Args:
    feed: A gdata.GDataFeed instance.

  Returns:
    An HTML formatted string containing the user's medication data.
  """
  if not feed.entry:
    return '<b>No entries in feed</b><br>'

  html = []
  for entry in feed.entry:
    try:
      ccr = entry.FindExtensions('ContinuityOfCareRecord')[0]
      body = ccr.FindChildren('Body')[0]
      meds = body.FindChildren('Medications')[0].FindChildren('Medication')
      for med in meds:
        name = med.FindChildren('Product')[0].FindChildren('ProductName')[0]
        html.append('<li>%s</li>' % name.FindChildren('Text')[0].text)
    except:
      html.append('<b>No medication data in this profile</b><br>')
  return '<ul>%s</ul>' % ''.join(html)

def index(req):
  req.content_type = 'text/html'

  authsub = HealthAuthSubHelper()
  client = gdata.service.GDataService(service='weaver')

  current_url = authsub.GetNextUrl(req)
  rsa_key = authsub.SetPrivateKey('/path/to/yourRSAPrivateKey.pem')

  # Strip token query parameter's value from URL if it exists
  token = gdata.auth.extract_auth_sub_token_from_url(current_url,
                                                     rsa_key=rsa_key)

  if not token:
    """STEP 1: No single use token in the URL or a saved session token.
    Generate the AuthSub URL to fetch a single use token."""

    params = {'permission': 1}
    authsub_url = authsub.GenerateAuthSubRequestUrl(current_url,
                                                    extra_params=params)
    req.write('<a href="%s">Link your Google Health Profile</a>' % authsub_url)
  else:
    """STEP 2: A single use token was extracted from the URL.
    Upgrade the one time token to a session token."""

    req.write('<b>Single use token</b>: %s<br>' % str(token))

    client.UpgradeToSessionToken(token)  # calls gdata.service.SetAuthSubToken()

    """STEP 3: Done with AuthSub :) Save the token for subsequent requests.
    Query the Health Data API"""
    req.write('<b>Token info</b>: %s<br>' % client.AuthSubTokenInfo())

    req.write('<b>Session token</b>: %s<br>' % client.GetAuthSubToken())

    # Query the Health Data API
    params = {'digest': 'true', 'strict': 'true'}
    uri = '%s?%s' % (H9_PROFILE_FEED_URL, urllib.urlencode(params))
    feed = client.GetFeed(uri)

    req.write('<h4>Listing medications</h4>')
    req.write(GetMedicationHTML(feed))

    """STEP 4: Revoke the session token."""
    req.write('Revoked session token')
    client.RevokeAuthSubToken()
