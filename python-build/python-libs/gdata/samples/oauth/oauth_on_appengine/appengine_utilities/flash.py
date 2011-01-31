"""
Copyright (c) 2008, appengine-utilities project
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
- Neither the name of the appengine-utilities project nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
"""

import os
import sys
import Cookie
import pickle
from time import strftime

from django.utils import simplejson

COOKIE_NAME = 'appengine-utilities-flash'


class Flash(object):
    """
    Send messages to the user between pages.

    When you instantiate the class, the attribute 'msg' will be set from the
    cookie, and the cookie will be deleted. If there is no flash cookie, 'msg'
    will default to None.

    To set a flash message for the next page, simply set the 'msg' attribute.

    Example psuedocode:

        if new_entity.put():
            flash = Flash()
            flash.msg = 'Your new entity has been created!'
            return redirect_to_entity_list()

    Then in the template on the next page:

        {% if flash.msg %}
            <div class="flash-msg">{{ flash.msg }}</div>
        {% endif %}
    """

    def __init__(self, cookie=None):
        """
        Load the flash message and clear the cookie.
        """
        self.no_cache_headers()
       # load cookie
        if cookie is None:
            browser_cookie = os.environ.get('HTTP_COOKIE', '')
            self.cookie = Cookie.SimpleCookie()
            self.cookie.load(browser_cookie)
        else:
            self.cookie = cookie
        # check for flash data
        if self.cookie.get(COOKIE_NAME):
            # set 'msg' attribute
            cookie_val = self.cookie[COOKIE_NAME].value
            # we don't want to trigger __setattr__(), which creates a cookie
            try:
                self.__dict__['msg'] = simplejson.loads(cookie_val)
            except:
                # not able to load the json, so do not set message. This should
                # catch for when the browser doesn't delete the cookie in time for
                # the next request, and only blanks out the content.
                pass
            # clear the cookie
            self.cookie[COOKIE_NAME] = ''
            self.cookie[COOKIE_NAME]['path'] = '/'
            self.cookie[COOKIE_NAME]['expires'] = 0
            print self.cookie[COOKIE_NAME]
        else:
            # default 'msg' attribute to None
            self.__dict__['msg'] = None

    def __setattr__(self, name, value):
        """
        Create a cookie when setting the 'msg' attribute.
        """
        if name == 'cookie':
            self.__dict__['cookie'] = value
        elif name == 'msg':
            self.__dict__['msg'] = value
            self.__dict__['cookie'][COOKIE_NAME] = simplejson.dumps(value)
            self.__dict__['cookie'][COOKIE_NAME]['path'] = '/'
            print self.cookie
        else:
            raise ValueError('You can only set the "msg" attribute.')

    def no_cache_headers(self):
        """
        Adds headers, avoiding any page caching in the browser. Useful for highly
        dynamic sites.
        """
        print "Expires: Tue, 03 Jul 2001 06:00:00 GMT"
        print strftime("Last-Modified: %a, %d %b %y %H:%M:%S %Z")
        print "Cache-Control: no-store, no-cache, must-revalidate, max-age=0"
        print "Cache-Control: post-check=0, pre-check=0"
        print "Pragma: no-cache"
