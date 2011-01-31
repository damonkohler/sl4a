# file: l2capclient.py
# desc: Demo L2CAP client for bluetooth module.
# $Id: l2capclient.py 524 2007-08-15 04:04:52Z albert $

import sys
import bluetooth

sock=bluetooth.BluetoothSocket(bluetooth.L2CAP)

if len(sys.argv) < 2:
    print "scanning"
    addr=bluetooth.discover_devices()
    if len(addr) >= 1:
	bt_addr=addr[0]
    else:
	print "usage: l2capclient.py <addr>"
	sys.exit(2)
else:
    bt_addr=sys.argv[1]

port = 0x1001

print "trying to connect to %s on PSM 0x%X" % (bt_addr, port)

sock.connect((bt_addr, port))

print "connected.  type stuff"
while True:
    data = raw_input()
    if(len(data) == 0): break
    sock.send(data)

sock.close()

