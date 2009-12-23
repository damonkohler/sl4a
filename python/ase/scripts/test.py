import sys
import types

# Test imports.
import android
import BeautifulSoup
import gdata.docs.service
import sqlite3
import termios
import time
import xmpp

droid = android.Android()


def event_loop():
  for i in range(10):
    e = droid.receiveEvent()
    if e['result'] is not None:
      return True
    time.sleep(2)
  return False


def test_gdata():
  # Create a client class which will make HTTP requests with Google Docs server.
  client = gdata.docs.service.DocsService()

  # Authenticate using your Google Docs email address and password.
  username = droid.getInput('Username')['result']
  password = droid.getInput('Password')['result']
  client.ClientLogin(username, password)

  # Query the server for an Atom feed containing a list of your documents.
  documents_feed = client.GetDocumentListFeed()
  # Loop through the feed and extract each document entry.
  return bool(list(documents_feed.entry))


def test_gps():
  droid.startLocating()
  try:
    return event_loop()
  finally:
    droid.stopLocating()


def test_sensors():
  droid.startSensing()
  try:
    return event_loop()
  finally:
    droid.stopSensing()


def test_speak():
  result = droid.speak('Hello, world!')
  return result['error'] is None


def test_phone_state():
  droid.startTrackingPhoneState()
  try:
    return event_loop()
  finally:
    droid.stopTrackingPhoneState()


def test_ringer_silent():
  result = droid.setRingerSilent()
  if result['error'] is not None:
    return False
  result = droid.setRingerSilent(False)
  return True


def test_ringer_volume():
  get_result = droid.getRingerVolume()
  if get_result['error'] is not None:
    return False
  droid.setRingerVolume(0)
  set_result = droid.setRingerVolume(get_result['result'])
  if set_result['error'] is not None:
    return False
  return True


def test_get_last_known_location():
  result = droid.getLastKnownLocation()
  return result['error'] is None


def test_geocode():
  result = droid.geocode(0, 0, 1)
  return result['error'] is None


def test_wifi():
  result = droid.setWifiEnabled()
  return result['error'] is None


def test_make_toast():
  result = droid.makeToast('Hello, world!')
  return result['error'] is None


def test_vibrate():
  result = droid.vibrate()
  return result['error'] is None


def test_notify():
  result = droid.notify('Hello, world!')
  return result['error'] is None


def test_get_running_packages():
  result = droid.getRunningPackages()
  return result['error'] is None


if __name__ == '__main__':
  for name, value in globals().items():
    if name.startswith('test_') and isinstance(value, types.FunctionType):
      print 'Running %s...' % name,
      sys.stdout.flush()
      if value():
        print ' PASS'
      else:
        print ' FAIL'
