# -*- coding: utf-8 -*-
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

# main python imports
import os
import time
import datetime
import random
import md5
import Cookie
import pickle
import __main__
from time import strftime
import logging

# google appengine imports
from google.appengine.ext import db
from google.appengine.api import memcache

#django simplejson import, used for flash
from django.utils import simplejson

from rotmodel import ROTModel

# settings, if you have these set elsewhere, such as your django settings file,
# you'll need to adjust the values to pull from there.


class _AppEngineUtilities_Session(db.Model):
    """
    Model for the sessions in the datastore. This contains the identifier and
    validation information for the session.
    """

    sid = db.StringListProperty()
    session_key = db.FloatProperty()
    ip = db.StringProperty()
    ua = db.StringProperty()
    last_activity = db.DateTimeProperty()
    dirty = db.BooleanProperty(default=False)
    working = db.BooleanProperty(default=False)
    deleted = db.BooleanProperty(default=False) # used for cases where
                                                # datastore delete doesn't
                                                # work

    def put(self):
        """
        Extend put so that it writes vaules to memcache as well as the datastore,
        and keeps them in sync, even when datastore writes fails.
        """
        if self.session_key:
            memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), self)
        else:
            # new session, generate a new key, which will handle the put and set the memcache
            self.create_key()

        self.last_activity = datetime.datetime.now()

        try:
            self.dirty = False
            logging.info("doing a put")
            db.put(self)
            memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), self)
        except:
            self.dirty = True
            memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), self)

        return self

    @classmethod
    def get_session(cls, session_obj=None):
        """
        Uses the passed sid to get a session object from memcache, or datastore
        if a valid one exists.
        """
        if session_obj.sid == None:
            return None
        session_key = session_obj.sid.split('_')[0]
        session = memcache.get("_AppEngineUtilities_Session_" + str(session_key))
        if session:
            if session.deleted == True:
                session.delete()
                return None
            if session.dirty == True and session.working != False:
                # the working bit is used to make sure multiple requests, which can happen
                # with ajax oriented sites, don't try to put at the same time
                session.working = True
                memcache.set("_AppEngineUtilities_Session_" + str(session_key), session)
                session.put()
            if session_obj.sid in session.sid:
                logging.info('grabbed session from memcache')
                sessionAge = datetime.datetime.now() - session.last_activity
                if sessionAge.seconds > session_obj.session_expire_time:
                    session.delete()
                    return None
                return session
            else:
                return None
 
        # Not in memcache, check datastore
        query = _AppEngineUtilities_Session.all()
        query.filter("sid = ", session_obj.sid)
        results = query.fetch(1)
        if len(results) > 0:
            sessionAge = datetime.datetime.now() - results[0].last_activity
            if sessionAge.seconds > self.session_expire_time:
                results[0].delete()
                return None
            memcache.set("_AppEngineUtilities_Session_" + str(session_key), results[0])
            memcache.set("_AppEngineUtilities_SessionData_" + str(session_key), results[0].get_items_ds())
            logging.info('grabbed session from datastore')
            return results[0]
        else:
            return None

    def get_items(self):
        """
        Returns all the items stored in a session
        """
        items = memcache.get("_AppEngineUtilities_SessionData_" + str(self.session_key))
        if items:
            for item in items:
                if item.deleted == True:
                    item.delete()
                    items.remove(item)
            return items

        query = _AppEngineUtilities_SessionData.all()
        query.filter('session_key', self.session_key)
        results = query.fetch(1000)
        return results

    def get_item(self, keyname = None):
        """
        Returns a single item from the memcache or datastore
        """
        mc = memcache.get("_AppEngineUtilities_SessionData_" + str(self.session_key))
        if mc:
            for item in mc:
                if item.keyname == keyname:
                    if item.deleted == True:
                        item.delete()
                        return None
                    return item
        query = _AppEngineUtilities_SessionData.all()
        query.filter("session_key = ", self.session_key)
        query.filter("keyname = ", keyname)
        results = query.fetch(1)
        if len(results) > 0:
            memcache.set("_AppEngineUtilities_SessionData_" + str(self.session_key), self.get_items_ds())
            return results[0]
        return None

    def get_items_ds(self):
        """
        This gets all the items straight from the datastore, does not
        interact with the memcache.
        """
        query = _AppEngineUtilities_SessionData.all()
        query.filter('session_key', self.session_key)
        results = query.fetch(1000)
        return results

    def delete(self):
        try:
            query = _AppEngineUtilities_SessionData.all()
            query.filter("session_key = ", self.session_key)
            results = query.fetch(1000)
            db.delete(results)
            db.delete(self)
            memcache.delete_multi(["_AppEngineUtilities_Session_" + str(self.session_key), "_AppEngineUtilities_SessionData_" + str(self.session_key)])
        except:
            mc = memcache.get("_AppEngineUtilities_Session_" + str(self.session_key))
            mc.deleted = True
            memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), mc)

    def create_key(self):
        """
        Creates a unique key for the session.
        """
        self.session_key = time.time()
        valid = False
        while valid == False:
            # verify session_key is unique
            if memcache.get("_AppEngineUtilities_Session_" + str(self.session_key)):
                self.session_key = self.session_key + 0.001
            else:
                query = _AppEngineUtilities_Session.all()
                query.filter("session_key = ", self.session_key)
                results = query.fetch(1)
                if len(results) > 0:
                    self.session_key = self.session_key + 0.001
                else:
                    try:
                        self.put()
                        memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), self)
                    except:
                        self.dirty = True
                        memcache.set("_AppEngineUtilities_Session_" + str(self.session_key), self)
                    valid = True
            
class _AppEngineUtilities_SessionData(db.Model):
    """
    Model for the session data in the datastore.
    """

    session_key = db.FloatProperty()
    keyname = db.StringProperty()
    content = db.BlobProperty()
    dirty = db.BooleanProperty(default=False)
    deleted = db.BooleanProperty(default=False)

    def put(self):
        """
        Adds a keyname/value for session to the datastore and memcache
        """
        # update or insert in datastore
        try:
            db.put(self)
            self.dirty = False
        except:
            self.dirty = True

        # update or insert in memcache
        mc_items = memcache.get("_AppEngineUtilities_SessionData_" + str(self.session_key))
        if mc_items:
            value_updated = False
            for item in mc_items:
                if value_updated == True:
                    break
                if item.keyname == self.keyname:
                    logging.info("updating " + self.keyname)
                    item.content = self.content
                    memcache.set("_AppEngineUtilities_SessionData_" + str(self.session_key), mc_items)
                    value_updated = True
                    break
            if value_updated == False:
                #logging.info("adding " + self.keyname)
                mc_items.append(self)
                memcache.set("_AppEngineUtilities_SessionData_" + str(self.session_key), mc_items)

    def delete(self):
        """
        Deletes an entity from the session in memcache and the datastore
        """
        try:
            db.delete(self)
        except:
            self.deleted = True
        mc_items = memcache.get("_AppEngineUtilities_SessionData_" + str(self.session_key))
        value_handled = False
        for item in mc_items:
            if value_handled == True:
                break
            if item.keyname == self.keyname:
                if self.deleted == True:
                    item.deleted = True
                else:
                    mc_items.remove(item)
                memcache.set("_AppEngineUtilities_SessionData_" + str(self.session_key), mc_items)
        

class _DatastoreWriter(object):

    def put(self, keyname, value, session):
        """
        Insert a keyname/value pair into the datastore for the session.

        Args:
            keyname: The keyname of the mapping.
            value: The value of the mapping.
        """
        keyname = session._validate_key(keyname)
        if value is None:
            raise ValueError('You must pass a value to put.')

        # datestore write trumps cookie. If there is a cookie value
        # with this keyname, delete it so we don't have conflicting
        # entries.
        if session.cookie_vals.has_key(keyname):
            del(session.cookie_vals[keyname])
            session.output_cookie[session.cookie_name + '_data'] = \
                simplejson.dumps(session.cookie_vals)
            print session.output_cookie.output()

        sessdata = session._get(keyname=keyname)
        if sessdata is None:
            sessdata = _AppEngineUtilities_SessionData()
            sessdata.session_key = session.session.session_key
            sessdata.keyname = keyname
        sessdata.content = pickle.dumps(value)
        # UNPICKLING CACHE session.cache[keyname] = pickle.dumps(value)
        session.cache[keyname] = value
        sessdata.put()
        # todo _set_memcache() should be going away when this is done
        # session._set_memcache()


class _CookieWriter(object):
    def put(self, keyname, value, session):
        """
        Insert a keyname/value pair into the datastore for the session.

        Args:
            keyname: The keyname of the mapping.
            value: The value of the mapping.
        """
        keyname = session._validate_key(keyname)
        if value is None:
            raise ValueError('You must pass a value to put.')

        # Use simplejson for cookies instead of pickle.
        session.cookie_vals[keyname] = value
        # update the requests session cache as well.
        session.cache[keyname] = value
        session.output_cookie[session.cookie_name + '_data'] = \
            simplejson.dumps(session.cookie_vals)
        print session.output_cookie.output()

class Session(object):
    """
    Sessions used to maintain user presence between requests.

    Sessions store a unique id as a cookie in the browser and
    referenced in a datastore object. This maintains user presence
    by validating requests as visits from the same browser.

    You can add extra data to the session object by using it
    as a dictionary object. Values can be any python object that
    can be pickled.

    For extra performance, session objects are also store in
    memcache and kept consistent with the datastore. This
    increases the performance of read requests to session
    data.
    """

    COOKIE_NAME = 'appengine-utilities-session-sid' # session token
    DEFAULT_COOKIE_PATH = '/'
    SESSION_EXPIRE_TIME = 7200 # sessions are valid for 7200 seconds (2 hours)
    CLEAN_CHECK_PERCENT = 50 # By default, 50% of all requests will clean the database
    INTEGRATE_FLASH = True # integrate functionality from flash module?
    CHECK_IP = True # validate sessions by IP
    CHECK_USER_AGENT = True # validate sessions by user agent
    SET_COOKIE_EXPIRES = True # Set to True to add expiration field to cookie
    SESSION_TOKEN_TTL = 5 # Number of seconds a session token is valid for.
    UPDATE_LAST_ACTIVITY = 60 # Number of seconds that may pass before
                            # last_activity is updated
    WRITER = "datastore" # Use the datastore writer by default. cookie is the
                        # other option.


    def __init__(self, cookie_path=DEFAULT_COOKIE_PATH,
            cookie_name=COOKIE_NAME,
            session_expire_time=SESSION_EXPIRE_TIME,
            clean_check_percent=CLEAN_CHECK_PERCENT,
            integrate_flash=INTEGRATE_FLASH, check_ip=CHECK_IP,
            check_user_agent=CHECK_USER_AGENT,
            set_cookie_expires=SET_COOKIE_EXPIRES,
            session_token_ttl=SESSION_TOKEN_TTL,
            last_activity_update=UPDATE_LAST_ACTIVITY,
            writer=WRITER):
        """
        Initializer

        Args:
          cookie_name: The name for the session cookie stored in the browser.
          session_expire_time: The amount of time between requests before the
              session expires.
          clean_check_percent: The percentage of requests the will fire off a
              cleaning routine that deletes stale session data.
          integrate_flash: If appengine-utilities flash utility should be
              integrated into the session object.
          check_ip: If browser IP should be used for session validation
          check_user_agent: If the browser user agent should be used for
              sessoin validation.
          set_cookie_expires: True adds an expires field to the cookie so
              it saves even if the browser is closed.
          session_token_ttl: Number of sessions a session token is valid
              for before it should be regenerated.
        """

        self.cookie_path = cookie_path
        self.cookie_name = cookie_name
        self.session_expire_time = session_expire_time
        self.integrate_flash = integrate_flash
        self.check_user_agent = check_user_agent
        self.check_ip = check_ip
        self.set_cookie_expires = set_cookie_expires
        self.session_token_ttl = session_token_ttl
        self.last_activity_update = last_activity_update
        self.writer = writer

        # make sure the page is not cached in the browser
        self.no_cache_headers()
        # Check the cookie and, if necessary, create a new one.
        self.cache = {}
        string_cookie = os.environ.get('HTTP_COOKIE', '')
        self.cookie = Cookie.SimpleCookie()
        self.output_cookie = Cookie.SimpleCookie()
        self.cookie.load(string_cookie)
        try:
            self.cookie_vals = \
                simplejson.loads(self.cookie[self.cookie_name + '_data'].value)
                # sync self.cache and self.cookie_vals which will make those
                # values available for all gets immediately.
            for k in self.cookie_vals:
                self.cache[k] = self.cookie_vals[k]
                self.output_cookie[self.cookie_name + '_data'] = self.cookie[self.cookie_name + '_data']
            # sync the input cookie with the output cookie
        except:
            self.cookie_vals = {}


        if writer == "cookie":
            pass
        else:
            self.sid = None
            new_session = True

            # do_put is used to determine if a datastore write should
            # happen on this request.
            do_put = False

            # check for existing cookie
            if self.cookie.get(cookie_name):
                self.sid = self.cookie[cookie_name].value
                self.session = _AppEngineUtilities_Session.get_session(self) # will return None if
                                                                                 # sid expired
                if self.session:
                    new_session = False

            if new_session:
                # start a new session
                self.session = _AppEngineUtilities_Session()
                self.session.put()
                self.sid = self.new_sid()
                if 'HTTP_USER_AGENT' in os.environ:
                    self.session.ua = os.environ['HTTP_USER_AGENT']
                else:
                    self.session.ua = None
                if 'REMOTE_ADDR' in os.environ:
                    self.session.ip = os.environ['REMOTE_ADDR']
                else:
                    self.session.ip = None
                self.session.sid = [self.sid]
                # do put() here to get the session key
                self.session.put()
            else:
                # check the age of the token to determine if a new one
                # is required
                duration = datetime.timedelta(seconds=self.session_token_ttl)
                session_age_limit = datetime.datetime.now() - duration
                if self.session.last_activity < session_age_limit:
                    logging.info("UPDATING SID LA = " + str(self.session.last_activity) + " - TL = " + str(session_age_limit))
                    self.sid = self.new_sid()
                    if len(self.session.sid) > 2:
                        self.session.sid.remove(self.session.sid[0])
                    self.session.sid.append(self.sid)
                    do_put = True
                else:
                    self.sid = self.session.sid[-1]
                    # check if last_activity needs updated
                    ula = datetime.timedelta(seconds=self.last_activity_update)
                    if datetime.datetime.now() > self.session.last_activity + ula:
                        do_put = True

            self.output_cookie[cookie_name] = self.sid
            self.output_cookie[cookie_name]['path'] = cookie_path

            # UNPICKLING CACHE self.cache['sid'] = pickle.dumps(self.sid)
            self.cache['sid'] = self.sid

            if do_put:
                if self.sid != None or self.sid != "":
                    logging.info("doing put")
                    self.session.put()

        if self.set_cookie_expires:
            if not self.output_cookie.has_key(cookie_name + '_data'):
                self.output_cookie[cookie_name + '_data'] = ""
            self.output_cookie[cookie_name + '_data']['expires'] = \
                self.session_expire_time
        print self.output_cookie.output()

        # fire up a Flash object if integration is enabled
        if self.integrate_flash:
            import flash
            self.flash = flash.Flash(cookie=self.cookie)

        # randomly delete old stale sessions in the datastore (see
        # CLEAN_CHECK_PERCENT variable)
        if random.randint(1, 100) < clean_check_percent:
            self._clean_old_sessions() 

    def new_sid(self):
        """
        Create a new session id.
        """
        sid = str(self.session.session_key) + "_" +md5.new(repr(time.time()) + \
                str(random.random())).hexdigest()
        return sid

    '''
    # removed as model now has get_session classmethod
    def _get_session(self):
        """
        Get the user's session from the datastore
        """
        query = _AppEngineUtilities_Session.all()
        query.filter('sid', self.sid)
        if self.check_user_agent:
            query.filter('ua', os.environ['HTTP_USER_AGENT'])
        if self.check_ip:
            query.filter('ip', os.environ['REMOTE_ADDR'])
        results = query.fetch(1)
        if len(results) is 0:
            return None
        else:
            sessionAge = datetime.datetime.now() - results[0].last_activity
            if sessionAge.seconds > self.session_expire_time:
                results[0].delete()
                return None
            return results[0]
    '''
    def _get(self, keyname=None):
        """
        Return all of the SessionData object data from the datastore onlye,
        unless keyname is specified, in which case only that instance of 
        SessionData is returned.
        Important: This does not interact with memcache and pulls directly
        from the datastore. This also does not get items from the cookie
        store.

        Args:
            keyname: The keyname of the value you are trying to retrieve.
        """
        if keyname != None:
            return self.session.get_item(keyname)
        return self.session.get_items()
        """
        OLD
        query = _AppEngineUtilities_SessionData.all()
        query.filter('session', self.session)
        if keyname != None:
            query.filter('keyname =', keyname)
        results = query.fetch(1000)

        if len(results) is 0:
            return None
        if keyname != None:
            return results[0]
        return results
        """

    def _validate_key(self, keyname):
        """
        Validate the keyname, making sure it is set and not a reserved name.
        """
        if keyname is None:
            raise ValueError('You must pass a keyname for the session' + \
                ' data content.')
        elif keyname in ('sid', 'flash'):
            raise ValueError(keyname + ' is a reserved keyname.')

        if type(keyname) != type([str, unicode]):
            return str(keyname)
        return keyname

    def _put(self, keyname, value):
        """
        Insert a keyname/value pair into the datastore for the session.

        Args:
            keyname: The keyname of the mapping.
            value: The value of the mapping.
        """
        if self.writer == "datastore":
            writer = _DatastoreWriter()
        else:
            writer = _CookieWriter()

        writer.put(keyname, value, self)

    def _delete_session(self):
        """
        Delete the session and all session data.
        """
        if hasattr(self, "session"):
            self.session.delete()
        self.cookie_vals = {}
        self.cache = {}
        self.output_cookie[self.cookie_name + '_data'] = \
            simplejson.dumps(self.cookie_vals)
        print self.output_cookie.output()
        """
        OLD
        if hasattr(self, "session"):
            sessiondata = self._get()
            # delete from datastore
            if sessiondata is not None:
                for sd in sessiondata:
                    sd.delete()
            # delete from memcache
            memcache.delete('sid-'+str(self.session.key()))
            # delete the session now that all items that reference it are deleted.
            self.session.delete()
        # unset any cookie values that may exist
        self.cookie_vals = {}
        self.cache = {}
        self.output_cookie[self.cookie_name + '_data'] = \
            simplejson.dumps(self.cookie_vals)
        print self.output_cookie.output()
        """
        # if the event class has been loaded, fire off the sessionDeleted event
        if 'AEU_Events' in __main__.__dict__:
            __main__.AEU_Events.fire_event('sessionDelete')

    def delete(self):
        """
        Delete the current session and start a new one.

        This is useful for when you need to get rid of all data tied to a
        current session, such as when you are logging out a user.
        """
        self._delete_session()

    @classmethod
    def delete_all_sessions(cls):
        """
        Deletes all sessions and session data from the data store and memcache:

        NOTE: This is not fully developed. It also will not delete any cookie
        data as this does not work for each incoming request. Keep this in mind
        if you are using the cookie writer.
        """
        all_sessions_deleted = False
        all_data_deleted = False

        while not all_sessions_deleted:
            query = _AppEngineUtilities_Session.all()
            results = query.fetch(75)
            if len(results) is 0:
                all_sessions_deleted = True
            else:
                for result in results:
                    result.delete()


    def _clean_old_sessions(self):
        """
        Delete expired sessions from the datastore.

        This is only called for CLEAN_CHECK_PERCENT percent of requests because
        it could be rather intensive.
        """
        duration = datetime.timedelta(seconds=self.session_expire_time)
        session_age = datetime.datetime.now() - duration
        query = _AppEngineUtilities_Session.all()
        query.filter('last_activity <', session_age)
        results = query.fetch(50)
        for result in results:
            """
            OLD
            data_query = _AppEngineUtilities_SessionData.all()
            data_query.filter('session', result)
            data_results = data_query.fetch(1000)
            for data_result in data_results:
                data_result.delete()
            memcache.delete('sid-'+str(result.key()))
            """
            result.delete()

    # Implement Python container methods

    def __getitem__(self, keyname):
        """
        Get item from session data.

        keyname: The keyname of the mapping.
        """
        # flash messages don't go in the datastore

        if self.integrate_flash and (keyname == 'flash'):
            return self.flash.msg
        if keyname in self.cache:
            # UNPICKLING CACHE return pickle.loads(str(self.cache[keyname]))
            return self.cache[keyname]
        if keyname in self.cookie_vals:
            return self.cookie_vals[keyname]
        if hasattr(self, "session"):
            data = self._get(keyname)
            if data:
                #UNPICKLING CACHE self.cache[keyname] = data.content
                self.cache[keyname] = pickle.loads(data.content)
                return pickle.loads(data.content)
            else:
                raise KeyError(str(keyname))
        raise KeyError(str(keyname))

    def __setitem__(self, keyname, value):
        """
        Set item in session data.

        Args:
            keyname: They keyname of the mapping.
            value: The value of mapping.
        """

        if self.integrate_flash and (keyname == 'flash'):
            self.flash.msg = value
        else:
            keyname = self._validate_key(keyname)
            self.cache[keyname] = value
            # self._set_memcache() # commented out because this is done in the datestore put
            return self._put(keyname, value)

    def delete_item(self, keyname, throw_exception=False):
        """
        Delete item from session data, ignoring exceptions if
        necessary.

        Args:
            keyname: The keyname of the object to delete.
            throw_exception: false if exceptions are to be ignored.
        Returns:
            Nothing.
        """
        if throw_exception:
            self.__delitem__(keyname)
            return None
        else:
            try:
                self.__delitem__(keyname)
            except KeyError:
                return None

    def __delitem__(self, keyname):
        """
        Delete item from session data.

        Args:
            keyname: The keyname of the object to delete.
        """
        bad_key = False
        sessdata = self._get(keyname = keyname)
        if sessdata is None:
            bad_key = True
        else:
            sessdata.delete()
        if keyname in self.cookie_vals:
            del self.cookie_vals[keyname]
            bad_key = False
            self.output_cookie[self.cookie_name + '_data'] = \
                simplejson.dumps(self.cookie_vals)
            print self.output_cookie.output()
        if bad_key:
            raise KeyError(str(keyname))
        if keyname in self.cache:
            del self.cache[keyname]

    def __len__(self):
        """
        Return size of session.
        """
        # check memcache first
        if hasattr(self, "session"):
            results = self._get()
            if results is not None:
                return len(results) + len(self.cookie_vals)
            else:
                return 0
        return len(self.cookie_vals)

    def __contains__(self, keyname):
        """
        Check if an item is in the session data.

        Args:
            keyname: The keyname being searched.
        """
        try:
            r = self.__getitem__(keyname)
        except KeyError:
            return False
        return True

    def __iter__(self):
        """
        Iterate over the keys in the session data.
        """
        # try memcache first
        if hasattr(self, "session"):
            for k in self._get():
                yield k.keyname
        for k in self.cookie_vals:
            yield k

    def __str__(self):
        """
        Return string representation.
        """

        #if self._get():
        return '{' + ', '.join(['"%s" = "%s"' % (k, self[k]) for k in self]) + '}'
        #else:
        #    return []

    '''
    OLD
    def _set_memcache(self):
        """
        Set a memcache object with all the session data. Optionally you can
        add a key and value to the memcache for put operations.
        """
        # Pull directly from the datastore in order to ensure that the
        # information is as up to date as possible.
        if self.writer == "datastore":
            data = {}
            sessiondata = self._get()
            if sessiondata is not None:
                for sd in sessiondata:
                    data[sd.keyname] = pickle.loads(sd.content)

            memcache.set('sid-'+str(self.session.key()), data, \
                self.session_expire_time)
    '''

    def cycle_key(self):
        """
        Changes the session id.
        """
        self.sid = self.new_sid()
        if len(self.session.sid) > 2:
            self.session.sid.remove(self.session.sid[0])
        self.session.sid.append(self.sid)

    def flush(self):
        """
        Delete's the current session, creating a new one.
        """
        self._delete_session()
        self.__init__()

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

    def clear(self):
        """
        Remove all items
        """
        sessiondata = self._get()
        # delete from datastore
        if sessiondata is not None:
            for sd in sessiondata:
                sd.delete()
        # delete from memcache
        self.cache = {}
        self.cookie_vals = {}
        self.output_cookie[self.cookie_name + '_data'] = \
            simplejson.dumps(self.cookie_vals)
        print self.output_cookie.output()

    def has_key(self, keyname):
        """
        Equivalent to k in a, use that form in new code
        """
        return self.__contains__(keyname)

    def items(self):
        """
        A copy of list of (key, value) pairs
        """
        op = {}
        for k in self:
            op[k] = self[k]
        return op

    def keys(self):
        """
        List of keys.
        """
        l = []
        for k in self:
            l.append(k)
        return l

    def update(*dicts):
        """
        Updates with key/value pairs from b, overwriting existing keys, returns None
        """
        for dict in dicts:
            for k in dict:
                self._put(k, dict[k])
        return None

    def values(self):
        """
        A copy list of values.
        """
        v = []
        for k in self:
            v.append(self[k])
        return v

    def get(self, keyname, default = None):
        """
        a[k] if k in a, else x
        """
        try:
            return self.__getitem__(keyname)
        except KeyError:
            if default is not None:
                return default
            return None

    def setdefault(self, keyname, default = None):
        """
        a[k] if k in a, else x (also setting it)
        """
        try:
            return self.__getitem__(keyname)
        except KeyError:
            if default is not None:
                self.__setitem__(keyname, default)
                return default
            return None

    @classmethod
    def check_token(cls, cookie_name=COOKIE_NAME, delete_invalid=True):
        """
        Retrieves the token from a cookie and validates that it is
        a valid token for an existing cookie. Cookie validation is based
        on the token existing on a session that has not expired.

        This is useful for determining if datastore or cookie writer
        should be used in hybrid implementations.

        Args:
            cookie_name: Name of the cookie to check for a token.
            delete_invalid: If the token is not valid, delete the session
                            cookie, to avoid datastore queries on future
                            requests.

        Returns True/False

        NOTE: TODO This currently only works when the datastore is working, which of course
        is pointless for applications using the django middleware. This needs to be resolved
        before merging back into the main project.
        """

        string_cookie = os.environ.get('HTTP_COOKIE', '')
        cookie = Cookie.SimpleCookie()
        cookie.load(string_cookie)
        if cookie.has_key(cookie_name):
            query = _AppEngineUtilities_Session.all()
            query.filter('sid', cookie[cookie_name].value)
            results = query.fetch(1)
            if len(results) > 0:
                return True
            else:
                if delete_invalid:
                    output_cookie = Cookie.SimpleCookie()
                    output_cookie[cookie_name] = cookie[cookie_name]
                    output_cookie[cookie_name]['expires'] = 0
                    print output_cookie.output()
        return False
