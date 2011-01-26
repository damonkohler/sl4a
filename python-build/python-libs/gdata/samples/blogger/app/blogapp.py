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


__author__ = 'j.s@google.com (Jeff Scudder)'


import os
import wsgiref.handlers
from google.appengine.api import users
from google.appengine.ext import webapp
from google.appengine.ext.webapp import template
from google.appengine.ext.webapp.util import run_wsgi_app
import gdata.gauth
import gdata.data
import gdata.blogger.client


def get_auth_token(request):
  """Retrieves the AuthSub token for the current user.

  Will first check the request URL for a token request parameter
  indicating that the user has been sent to this page after 
  authorizing the app. Auto-upgrades to a session token.

  If the token was not in the URL, which will usually be the case,
  looks for the token in the datastore.

  Returns:
    The token object if one was found for the current user. If there
    is no current user, it returns False, if there is a current user
    but no AuthSub token, it returns None.
  """
  current_user = users.get_current_user()
  if current_user is None or current_user.user_id() is None:
    return False
  # Look for the token string in the current page's URL.
  token_string, token_scopes = gdata.gauth.auth_sub_string_from_url(
     request.url)
  if token_string is None:
    # Try to find a previously obtained session token.
    return gdata.gauth.ae_load('blogger' + current_user.user_id())
  # If there was a new token in the current page's URL, convert it to
  # to a long lived session token and persist it to be used in future
  # requests.
  single_use_token = gdata.gauth.AuthSubToken(token_string, token_scopes)
  # Create a client to make the HTTP request to upgrade the single use token
  # to a long lived session token.
  client = gdata.client.GDClient()
  session_token = client.upgrade_token(single_use_token)
  gdata.gauth.ae_save(session_token, 'blogger' + current_user.user_id())
  return session_token


class ListBlogs(webapp.RequestHandler):
  """Requests the list of the user's blogs from the Blogger API."""

  def get(self):
    template_values = { 'sign_out': users.create_logout_url('/') }
    # See if we have an auth token for this user.
    token = get_auth_token(self.request)
    if token is None:
      template_values['auth_url'] = gdata.gauth.generate_auth_sub_url(
          self.request.url, ['http://www.blogger.com/feeds/'])
      path = os.path.join(os.path.dirname(__file__), 'auth_required.html')
      self.response.out.write(template.render(path, template_values))
      return    
  
    elif token == False:
      self.response.out.write(
          '<html><body><a href="%s">You must sign in first</a>'
          '</body></html>' % users.create_login_url('/blogs'))
      return

    client = gdata.blogger.client.BloggerClient()
    feed = client.get_blogs(auth_token=token)
    template_values['feed'] = feed
    path = os.path.join(os.path.dirname(__file__), 'list_blogs.html')
    self.response.out.write(template.render(path, template_values))


class WritePost(webapp.RequestHandler):

  def get(self):
    template_values = { 'sign_out': users.create_logout_url('/'),
                        'blog_id': self.request.get('id') }
    # We should have an auth token for this user.
    token = get_auth_token(self.request)
    if not token:
      self.redirect('/blogs')
      return
    path = os.path.join(os.path.dirname(__file__), 'post_editor.html')
    self.response.out.write(template.render(path, template_values))

  def post(self):
    token = get_auth_token(self.request)
    if not token:
      self.redirect('/blogs')
      return
    draft = False
    if self.request.get('draft') == 'true':
      draft = True
    client = gdata.blogger.client.BloggerClient()
    new_post = client.add_post(
        self.request.get('blog_id'), self.request.get('title'),
        self.request.get('body'), draft=draft, auth_token=token)
    if not draft:
      self.response.out.write(
          'See your new post <a href="%s">here</a>.' % (
              new_post.find_alternate_link()))
    else:
      self.response.out.write(
          'This was a draft blog post, visit '
          '<a href="http://blogger.com/">blogger.com</a> to publish')


def main():
  application = webapp.WSGIApplication([('/blogs', ListBlogs), 
                                        ('/write_post', WritePost)],
      debug=True)
  wsgiref.handlers.CGIHandler().run(application)


if __name__ == '__main__':
  main()

