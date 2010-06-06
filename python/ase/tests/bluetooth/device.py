import android
import threading
import time

droid = android.Android()
droid.toggleBluetoothState(True)
droid.bluetoothMakeDiscoverable()
droid.bluetoothAccept()

def receiver():
  while True:
    print droid.bluetoothReadLine()
    print droid.bluetoothRead()
    time.sleep(0.25)

def sender():
  while True:
    droid.bluetoothWrite('test\n')
    time.sleep(0.25)

while not droid.bluetoothReady().result:
  print 'Waiting for connection...'
  time.sleep(1)

receiver_thread = threading.Thread(target=receiver)
receiver_thread.daemon = True
receiver_thread.start()

sender_thread = threading.Thread(target=sender)
sender_thread.daemon = True
sender_thread.start()

time.sleep(10)
