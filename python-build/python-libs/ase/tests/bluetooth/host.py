#!/usr/bin/python
import bluetooth
import threading
import time

for host, name in bluetooth.discover_devices(lookup_names=True):
  print host, name
  if name == 'Nexus One':
    break

services = bluetooth.find_service(address=host)

for service in services:
  if service['name'] == 'SL4A':
    port = service['port']
    break

sock = bluetooth.BluetoothSocket(bluetooth.RFCOMM)
sock.connect((host, port))

def receiver():
  while True:
    print sock.recv(5)
    time.sleep(0.25)

def sender():
  while True:
    sock.send('test\n')
    time.sleep(0.25)

receiver_thread = threading.Thread(target=receiver)
receiver_thread.daemon = True
receiver_thread.start()

sender_thread = threading.Thread(target=sender)
sender_thread.daemon = True
sender_thread.start()

while True:
  time.sleep(1)
