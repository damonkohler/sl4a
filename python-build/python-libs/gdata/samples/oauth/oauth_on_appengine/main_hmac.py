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

import cgi
import os
import gdata.auth
import gdata.docs
import gdata.docs.service
import gdata.alt.appengine

from appengine_utilities.sessions import Session
from django.utils import simplejson
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app

SETTINGS = {
  'APP_NAME': 'google-GDataOAuthAppEngine-v1',
  'CONSUMER_KEY': 'YOUR_CONSUMER_KEY',
  'CONSUMER_SECRET': 'YOUR_CONSUMER_SECRET',
  'SIG_METHOD': gdata.auth.OAuthSignatureMethod.HMAC_SHA1,
  'SCOPES': ['http://docs.google.com/feeds/',
             'https://docs.google.com/feeds/']
  }

gdocs = gdata.docs.service.DocsService(source=SETTINGS['APP_NAME'])
gdocs.SetOAuthInputParameters(SETTINGS['SIG_METHOD'], SETTINGS['CONSUMER_KEY'],
                              consumer_secret=SETTINGS['CONSUMER_SECRET'])
gdata.alt.appengine.run_on_appengine(gdocs)

class MainPage(webapp.RequestHandler):
  """Main page displayed to user."""

  # GET /
  def get(self):
    if not users.get_current_user():
      self.redirect(users.create_login_url(self.request.uri))

    access_token = gdocs.token_store.find_token('%20'.join(SETTINGS['SCOPES']))
    if isinstance(access_token, gdata.auth.OAuthToken):
      form_action = '/fetch_data'
      form_value = 'Now fetch my docs!'
      revoke_token_link = True
    else:
      form_action = '/get_oauth_token'
      form_value = 'Give this website access to my Google Docs'
      revoke_token_link = None
  
    template_values = {
      'form_action': form_action,
      'form_value': form_value,
      'user': users.get_current_user(),
      'revoke_token_link': revoke_token_link,
      'oauth_token': access_token,
      'consumer': gdocs.GetOAuthInputParameters().GetConsumer(),
      'sig_method': gdocs.GetOAuthInputParameters().GetSignatureMethod().get_name()
      }

    path = os.path.join(os.path.dirname(__file__), 'index.html')
    self.response.out.write(template.render(path, template_values))


class OAuthDance(webapp.RequestHandler):
  """Handler for the 3 legged OAuth dance, v1.0a."""

  """This handler is responsible for fetching an initial OAuth request token,
  redirecting the user to the approval page.  When the user grants access, they
  will be redirected back to this GET handler and their authorized request token
  will be exchanged for a long-lived access token."""

  # GET /get_oauth_token
  def get(self):
    """Invoked after we're redirected back from the approval page."""

    self.session = Session()
    oauth_token = gdata.auth.OAuthTokenFromUrl(self.request.uri)
    if oauth_token:
      oauth_token.secret = self.session['oauth_token_secret']
      oauth_token.oauth_input_params = gdocs.GetOAuthInputParameters()
      gdocs.SetOAuthToken(oauth_token)

      # 3.) Exchange the authorized request token for an access token
      oauth_verifier = self.request.get('oauth_verifier', default_value='')
      access_token = gdocs.UpgradeToOAuthAccessToken(
          oauth_verifier=oauth_verifier)

      # Remember the access token in the current user's token store
      if access_token and users.get_current_user():
        gdocs.token_store.add_token(access_token)
      elif access_token:
        gdocs.current_token = access_token
        gdocs.SetOAuthToken(access_token)

    self.redirect('/')

  # POST /get_oauth_token
  def post(self):
    """Fetches a request token and redirects the user to the approval page."""

    self.session = Session()
    
    if users.get_current_user():
      # 1.) REQUEST TOKEN STEP. Provide the data scope(s) and the page we'll
      # be redirected back to after the user grants access on the approval page.
      req_token = gdocs.FetchOAuthRequestToken(
          scopes=SETTINGS['SCOPES'], oauth_callback=self.request.uri)

      # When using HMAC, persist the token secret in order to re-create an
      # OAuthToken object coming back from the approval page.
      self.session['oauth_token_secret'] = req_token.secret

      # Generate the URL to redirect the user to.  Add the hd paramter for a
      # better user experience.  Leaving it off will give the user the choice
      # of what account (Google vs. Google Apps) to login with.
      domain = self.request.get('domain', default_value='default')
      approval_page_url = gdocs.GenerateOAuthAuthorizationURL(
          extra_params={'hd': domain})

      # 2.) APPROVAL STEP.  Redirect to user to Google's OAuth approval page.
      self.redirect(approval_page_url)


class FetchData(OAuthDance):
  """Fetches the user's data."""

  """This class inherits from OAuthDance in order to utilize OAuthDance.post()
  in case of a request error (e.g. the user has a bad token)."""

  # GET /fetch_data
  def get(self):
    self.redirect('/')

  # POST /fetch_data
  def post(self):
    """Fetches the user's data."""

    try:
      feed = gdocs.GetDocumentListFeed()
      json = []
      for entry in feed.entry:
        if entry.lastModifiedBy is not None:
          last_modified_by = entry.lastModifiedBy.email.text
        else:
          last_modified_by = ''
        if entry.lastViewed is not None:
          last_viewed = entry.lastViewed.text
        else:
          last_viewed = ''
        json.append({'title': entry.title.text,
                     'links': {'alternate': entry.GetHtmlLink().href},
                     'published': entry.published.text,
                     'updated': entry.updated.text,
                     'resourceId': entry.resourceId.text,
                     'type': entry.GetDocumentType(),
                     'lastModifiedBy': last_modified_by,
                     'lastViewed': last_viewed
                    })
      self.response.out.write(simplejson.dumps(json))
    except gdata.service.RequestError, error:
      OAuthDance.post(self)

class RevokeToken(webapp.RequestHandler):

  # GET /revoke_token
  def get(self):
    """Revokes the current user's OAuth access token."""

    try:
      gdocs.RevokeOAuthToken()
    except gdata.service.RevokingOAuthTokenFailed:
      pass

    gdocs.token_store.remove_all_tokens()
    self.redirect('/')

  
def main():
  application = webapp.WSGIApplication([('/', MainPage),
                                        ('/get_oauth_token', OAuthDance),
                                        ('/fetch_data', FetchData),
                                        ('/revoke_token', RevokeToken)],
                                        debug=True)
  run_wsgi_app(application)

