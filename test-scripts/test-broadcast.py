from android2 import API
import socket
from json import loads

API(debug=True)

ACTION="android.bluetooth.adapter.action.SCAN_MODE_CHANGED"
EXTRA="android.bluetooth.adapter.extra.SCAN_MODE"

print API().eventRegisterForBroadcast(ACTION, False)
print API().eventGetBrodcastCategories()

def parseEvent(line):
  out = loads(line)
  out.update(loads(out["data"]))
  return out

p=API().startEventDispatcher()
s=socket.socket()
s.connect(("localhost", p))
f=s.makefile()

API().bluetoothMakeDiscoverable(10)
while True:
  event = parseEvent(f.readline())
  API().log(str(event))
  print "got", event
  if EXTRA in event and event[EXTRA] < 23:
    break

print API().eventUnregisterForBroadcast(ACTION)
print API().eventGetBrodcastCategories()
