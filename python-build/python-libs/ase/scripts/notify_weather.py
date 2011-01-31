"""Display the weather report in a notification."""

__author__ = 'Damon Kohler <damonkohler@gmail.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'

import android
import weather


def notify_weather(droid):
  """Display the weather at the current location in a notification."""
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
    msg = '%(temperature)s degrees and %(conditions)s, in %(city)s.' % result
  droid.notify('Weather Report', msg)


if __name__ == '__main__':
  droid = android.Android()
  notify_weather(droid)
