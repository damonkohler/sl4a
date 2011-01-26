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
import cgi
import re
import datetime
import pickle
from google.appengine.ext import db
from google.appengine.api import urlfetch
from google.appengine.api import memcache

APPLICATION_PORT = '8080'
CRON_PORT = '8081'

class _AppEngineUtilities_Cron(db.Model):
    """
    Model for the tasks in the datastore. This contains the scheduling and
    url information, as well as a field that sets the next time the instance
    should run.
    """

    cron_entry = db.StringProperty()
    next_run = db.DateTimeProperty()
    cron_compiled = db.BlobProperty()
    url = db.LinkProperty()

class Cron(object):
    """
    Cron is a scheduling utility built for appengine, modeled after
    crontab for unix systems. While true scheduled tasks are not
    possible within the Appengine environment currently, this
    is an attmempt to provide a request based alternate. You
    configure the tasks in an included interface, and the import
    the class on any request you want capable of running tasks.

    On each request where Cron is imported, the list of tasks
    that need to be run will be pulled and run. A task is a url
    within your application. It's important to make sure that these
    requests fun quickly, or you could risk timing out the actual
    request.

    See the documentation for more information on configuring
    your application to support Cron and setting up tasks.
    """

    def __init__(self):
        # Check if any tasks need to be run
        query = _AppEngineUtilities_Cron.all()
        query.filter('next_run <= ', datetime.datetime.now())
        results = query.fetch(1000)
        if len(results) > 0:
            one_second = datetime.timedelta(seconds = 1)
            before  = datetime.datetime.now()
            for r in results:
                if re.search(':' + APPLICATION_PORT, r.url):
                    r.url = re.sub(':' + APPLICATION_PORT, ':' + CRON_PORT, r.url)
                #result = urlfetch.fetch(r.url)
                diff = datetime.datetime.now() - before
                if int(diff.seconds) < 1:
                    if memcache.add(str(r.key), "running"):
                        result = urlfetch.fetch(r.url)
                        r.next_run = self._get_next_run(pickle.loads(r.cron_compiled))
                        r.put()
                        memcache.delete(str(r.key))
                else:
                    break

    def add_cron(self, cron_string):
        cron = cron_string.split(" ")
        if len(cron) is not 6:
            raise ValueError, 'Invalid cron string. Format: * * * * * url'
        cron = {
            'min': cron[0],
            'hour': cron[1],
            'day': cron[2],
            'mon': cron[3],
            'dow': cron[4],
            'url': cron[5],
        }
        cron_compiled = self._validate_cron(cron)
        next_run = self._get_next_run(cron_compiled)
        cron_entry = _AppEngineUtilities_Cron()
        cron_entry.cron_entry = cron_string
        cron_entry.next_run = next_run
        cron_entry.cron_compiled = pickle.dumps(cron_compiled)
        cron_entry.url = cron["url"]
        cron_entry.put()

    def _validate_cron(self, cron):
        """
        Parse the field to determine whether it is an integer or lists,
        also converting strings to integers where necessary. If passed bad
        values, raises a ValueError.
        """
        parsers = {
            'dow': self._validate_dow,
            'mon': self._validate_mon,
            'day': self._validate_day,
            'hour': self._validate_hour,
            'min': self._validate_min,
            'url': self. _validate_url,
        }
        for el in cron:
            parse = parsers[el]
            cron[el] = parse(cron[el])
        return cron

    def _validate_type(self, v, t):
        """
        Validates that the number (v) passed is in the correct range for the
        type (t). Raise ValueError, if validation fails.

        Valid ranges:
        day of week = 0-7
        month = 1-12
        day = 1-31
        hour = 0-23
        minute = 0-59

        All can * which will then return the range for that entire type.
        """
        if t == "dow":
            if v >= 0 and v <= 7:
                return [v]
            elif v == "*":
                return "*"
            else:
                raise ValueError, "Invalid day of week."
        elif t == "mon":
            if v >= 1 and v <= 12:
                return [v]
            elif v == "*":
                return range(1, 12)
            else:
                raise ValueError, "Invalid month."
        elif t == "day":
            if v >= 1 and v <= 31:
                return [v]
            elif v == "*":
                return range(1, 31)
            else:
                raise ValueError, "Invalid day."
        elif t == "hour":
            if v >= 0 and v <= 23:
                return [v]
            elif v == "*":
                return range(0, 23)
            else:
                raise ValueError, "Invalid hour."
        elif t == "min":
            if v >= 0 and v <= 59:
                return [v]
            elif v == "*":
                return range(0, 59)
            else:
                raise ValueError, "Invalid minute."

    def _validate_list(self, l, t):
        """
        Validates a crontab list. Lists are numerical values seperated
        by a comma with no spaces. Ex: 0,5,10,15

        Arguments:
            l: comma seperated list of numbers
            t: type used for validation, valid values are
                dow, mon, day, hour, min
        """
        elements = l.split(",")
        return_list = []
        # we have a list, validate all of them
        for e in elements:
            if "-" in e:
                return_list.extend(self._validate_range(e, t))
            else:
                try:
                    v = int(e)
                    self._validate_type(v, t)
                    return_list.append(v)
                except:
                    raise ValueError, "Names are not allowed in lists."
        # return a list of integers
        return return_list

    def _validate_range(self, r, t):
        """
        Validates a crontab range. Ranges are 2 numerical values seperated
        by a dash with no spaces. Ex: 0-10

        Arguments:
            r: dash seperated list of 2 numbers
            t: type used for validation, valid values are
                dow, mon, day, hour, min
        """
        elements = r.split('-')
        # a range should be 2 elements
        if len(elements) is not 2:
            raise ValueError, "Invalid range passed: " + str(r)
        # validate the minimum and maximum are valid for the type
        for e in elements:
            self._validate_type(int(e), t)
        # return a list of the numbers in the range.
        # +1 makes sure the end point is included in the return value
        return range(int(elements[0]), int(elements[1]) + 1)

    def _validate_step(self, s, t):
        """
        Validates a crontab step. Steps are complicated. They can
        be based on a range 1-10/2 or just step through all valid
        */2. When parsing times you should always check for step first
        and see if it has a range or not, before checking for ranges because
        this will handle steps of ranges returning the final list. Steps
        of lists is not supported.

        Arguments:
            s: slash seperated string
            t: type used for validation, valid values are
                dow, mon, day, hour, min
        """
        elements = s.split('/')
        # a range should be 2 elements
        if len(elements) is not 2:
            raise ValueError, "Invalid step passed: " + str(s)
        try:
            step = int(elements[1])
        except:
            raise ValueError, "Invalid step provided " + str(s)
        r_list = []
        # if the first element is *, use all valid numbers
        if elements[0] is "*" or elements[0] is "":
            r_list.extend(self._validate_type('*', t))
        # check and see if there is a list of ranges
        elif "," in elements[0]:
            ranges = elements[0].split(",")
            for r in ranges:
                # if it's a range, we need to manage that
                if "-" in r:
                    r_list.extend(self._validate_range(r, t))
                else:
                    try:
                        r_list.extend(int(r))
                    except:
                        raise ValueError, "Invalid step provided " + str(s)
        elif "-" in elements[0]:
            r_list.extend(self._validate_range(elements[0], t))
        return range(r_list[0], r_list[-1] + 1, step)

    def _validate_dow(self, dow):
        """
        """
        # if dow is * return it. This is for date parsing where * does not mean
        # every day for crontab entries.
        if dow is "*":
            return dow
        days = {
        'mon': 1,
        'tue': 2,
        'wed': 3,
        'thu': 4,
        'fri': 5,
        'sat': 6,
        # per man crontab sunday can be 0 or 7.
        'sun': [0, 7],
        }
        if dow in days:
            dow = days[dow]
            return [dow]
        # if dow is * return it. This is for date parsing where * does not mean
        # every day for crontab entries.
        elif dow is "*":
            return dow
        elif "/" in dow:
            return(self._validate_step(dow, "dow"))
        elif "," in dow:
            return(self._validate_list(dow, "dow"))
        elif "-" in dow:
            return(self._validate_range(dow, "dow"))
        else:
            valid_numbers = range(0, 8)
            if not int(dow) in valid_numbers:
                raise ValueError, "Invalid day of week " + str(dow)
            else:
                return [int(dow)]

    def _validate_mon(self, mon):
        months = {
        'jan': 1,
        'feb': 2,
        'mar': 3,
        'apr': 4,
        'may': 5,
        'jun': 6,
        'jul': 7,
        'aug': 8,
        'sep': 9,
        'oct': 10,
        'nov': 11,
        'dec': 12,
        }
        if mon in months:
            mon = months[mon]
            return [mon]
        elif mon is "*":
            return range(1, 13)
        elif "/" in mon:
            return(self._validate_step(mon, "mon"))
        elif "," in mon:
            return(self._validate_list(mon, "mon"))
        elif "-" in mon:
            return(self._validate_range(mon, "mon"))
        else:
            valid_numbers = range(1, 13)
            if not int(mon) in valid_numbers:
                raise ValueError, "Invalid month " + str(mon)
            else:
                return [int(mon)]

    def _validate_day(self, day):
        if day is "*":
            return range(1, 32)
        elif "/" in day:
            return(self._validate_step(day, "day"))
        elif "," in day:
            return(self._validate_list(day, "day"))
        elif "-" in day:
            return(self._validate_range(day, "day"))
        else:
            valid_numbers = range(1, 31)
            if not int(day) in valid_numbers:
                raise ValueError, "Invalid day " + str(day)
            else:
                return [int(day)]

    def _validate_hour(self, hour):
        if hour is "*":
            return range(0, 24)
        elif "/" in hour:
            return(self._validate_step(hour, "hour"))
        elif "," in hour:
            return(self._validate_list(hour, "hour"))
        elif "-" in hour:
            return(self._validate_range(hour, "hour"))
        else:
            valid_numbers = range(0, 23)
            if not int(hour) in valid_numbers:
                raise ValueError, "Invalid hour " + str(hour)
            else:
                return [int(hour)]

    def _validate_min(self, min):
        if min is "*":
            return range(0, 60)
        elif "/" in min:
            return(self._validate_step(min, "min"))
        elif "," in min:
            return(self._validate_list(min, "min"))
        elif "-" in min:
            return(self._validate_range(min, "min"))
        else:
            valid_numbers = range(0, 59)
            if not int(min) in valid_numbers:
                raise ValueError, "Invalid min " + str(min)
            else:
                return [int(min)]

    def _validate_url(self, url):
        # kludge for issue 842, right now we use request headers
        # to set the host.
        if url[0] is not "/":
            url = "/" + url
        url = 'http://' + str(os.environ['HTTP_HOST']) + url
        return url
        # content below is for when that issue gets fixed
        #regex = re.compile("^(http|https):\/\/([a-z0-9-]\.+)*", re.IGNORECASE)
        #if regex.match(url) is not None:
        #    return url
        #else:
        #    raise ValueError, "Invalid url " + url

    def _calc_month(self, next_run, cron):
        while True:
            if cron["mon"][-1] < next_run.month:
                next_run = next_run.replace(year=next_run.year+1, \
                month=cron["mon"][0], \
                day=1,hour=0,minute=0)
            else:
                if next_run.month in cron["mon"]:
                    return next_run
                else:
                    one_month = datetime.timedelta(months=1)
                    next_run = next_run + one_month

    def _calc_day(self, next_run, cron):
        # start with dow as per cron if dow and day are set
        # then dow is used if it comes before day. If dow
        # is *, then ignore it.
        if str(cron["dow"]) != str("*"):
            # convert any integers to lists in order to easily compare values
            m = next_run.month
            while True:
                if next_run.month is not m:
                    next_run = next_run.replace(hour=0, minute=0)
                    next_run = self._calc_month(next_run, cron)
                if next_run.weekday() in cron["dow"] or next_run.day in cron["day"]:
                    return next_run
                else:
                    one_day = datetime.timedelta(days=1)
                    next_run = next_run + one_day
        else:
            m = next_run.month
            while True:
                if next_run.month is not m:
                    next_run = next_run.replace(hour=0, minute=0)
                    next_run = self._calc_month(next_run, cron)
                # if cron["dow"] is next_run.weekday() or cron["day"] is next_run.day:
                if next_run.day in cron["day"]:
                    return next_run
                else:
                    one_day = datetime.timedelta(days=1)
                    next_run = next_run + one_day

    def _calc_hour(self, next_run, cron):
        m = next_run.month
        d = next_run.day
        while True:
            if next_run.month is not m:
                next_run = next_run.replace(hour=0, minute=0)
                next_run = self._calc_month(next_run, cron)
            if next_run.day is not d:
                next_run = next_run.replace(hour=0)
                next_run = self._calc_day(next_run, cron)
            if next_run.hour in cron["hour"]:
                return next_run
            else:
                m = next_run.month
                d = next_run.day
                one_hour = datetime.timedelta(hours=1)
                next_run = next_run + one_hour

    def _calc_minute(self, next_run, cron):
        one_minute = datetime.timedelta(minutes=1)

        m = next_run.month
        d = next_run.day
        h = next_run.hour
        while True:
            if next_run.month is not m:
                next_run = next_run.replace(minute=0)
                next_run = self._calc_month(next_run, cron)
            if next_run.day is not d:
                next_run = next_run.replace(minute=0)
                next_run = self._calc_day(next_run, cron)
            if next_run.hour is not h:
                next_run = next_run.replace(minute=0)
                next_run = self._calc_day(next_run, cron)
            if next_run.minute in cron["min"]:
                return next_run
            else:
                m = next_run.month
                d = next_run.day
                h = next_run.hour
                next_run = next_run + one_minute

    def _get_next_run(self, cron):
        one_minute = datetime.timedelta(minutes=1)
        # go up 1 minute because it shouldn't happen right when added
        now = datetime.datetime.now() + one_minute
        next_run = now.replace(second=0, microsecond=0)

        # start with month, which will also help calculate year
        next_run = self._calc_month(next_run, cron)
        next_run = self._calc_day(next_run, cron)
        next_run = self._calc_hour(next_run, cron)
        next_run = self._calc_minute(next_run, cron)
        return next_run
