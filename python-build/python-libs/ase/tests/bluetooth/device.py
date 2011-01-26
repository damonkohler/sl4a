import android
import sys
import threading
import time

droid = android.Android()
droid.toggleBluetoothState(True)
droid.bluetoothMakeDiscoverable()

print 'Connecting...'
result = droid.bluetoothAccept()
if result.error is not None:
  print 'Connection failed!'
  time.sleep(1)
  sys.exit(1)

def receiver():
  while True:
    print droid.bluetoothReadLine()
    print droid.bluetoothRead()
    time.sleep(0.25)

def sender():
  while True:
    droid.bluetoothWrite('test\n')
    time.sleep(0.25)

receiver_thread = threading.Thread(target=receiver)
receiver_thread.daemon = True
receiver_thread.start()

sender_thread = threading.Thread(target=sender)
sender_thread.daemon = True
sender_thread.start()

while True:
  time.sleep(1)
