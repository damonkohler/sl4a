"""Retrieve the weather report for the current location."""

__author__ = 'T.V. Raman <raman@google.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'


import string
import urllib
import urllib2
from xml.dom import minidom

WEATHER_URL = 'http://www.google.com/ig/api?weather=%s&hl=%s'


def extract_value(dom, parent, child):
  """Convenience function to dig out weather values."""
  return dom.getElementsByTagName(parent)[0].getElementsByTagName(child)[0].getAttribute('data')


def fetch_weather(location, hl=''):
  """Fetches weather report from Google

  Args:
    location: a zip code (94041); city name, state (weather=Mountain View,CA);...
    hl: the language parameter (language code)

  Returns:
    a dict of weather data.

  """
  url = WEATHER_URL % (urllib.quote(location), hl)
  handler = urllib2.urlopen(url)
  data = handler.read()
  dom = minidom.parseString(data)
  handler.close()

  data = {}
  weather_dom = dom.getElementsByTagName('weather')[0]
  data['city'] = extract_value(weather_dom, 'forecast_information','city')
  data['temperature'] = extract_value(weather_dom, 'current_conditions','temp_f')
  data['conditions'] = extract_value(weather_dom, 'current_conditions', 'condition')
  dom.unlink()
  return data
