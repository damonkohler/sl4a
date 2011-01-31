"""Speak the weather."""

__author__ = 'T.V. Raman <raman@google.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'

import android
import weather


def say_weather(droid):
  """Speak the weather at the current location."""
  print 'Finding ZIP code.'
  location = droid.getLastKnownLocation().result
  if location['gps'] is not None:
    location = location['gps']
  else:
    location = location['network']
  addresses = droid.geocode(location['latitude'], location['longitude'])
  zip = addresses.result[0]['postal_code']
  if zip is None:
    msg = 'Failed to find location.'
  else:
    print 'Fetching weather report.'
    result = weather.fetch_weather(zip)
    # Format the result for speech.
    msg = '%(temperature)s degrees and %(conditions)s, in %(city)s.' % result
  droid.ttsSpeak(msg)


if __name__ == '__main__':
  droid = android.Android()
  say_weather(droid)
