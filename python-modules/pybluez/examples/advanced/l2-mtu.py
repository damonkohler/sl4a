import sys
import struct
import bluetooth

def usage():
    print "usage: l2-mtu < server | client > [options]"
    print ""
    print "l2-mtu server            to start in server mode"
    print "l2-mtu client <addr>     to start in client mode and connect to addr"
    sys.exit(2)

if len(sys.argv) < 2: usage()

mode = sys.argv[1]
if mode not in [ "client", "server" ]: usage()

if mode == "server":
    server_sock=bluetooth.BluetoothSocket( bluetooth.L2CAP )
    server_sock.bind(("",0x1001))
    server_sock.listen(1)
    while True:
        print "waiting for incoming connection"
        client_sock,address = server_sock.accept()
        print "Accepted connection from %s" % str(address)

        bluetooth.set_l2cap_mtu( client_sock, 65535 )

        print "waiting for data"
        total = 0
        while True:
            try:
                data = client_sock.recv(65535)
            except bluetooth.BluetoothError, e:
                break
            if len(data) == 0: break
            print "received packet of size %d" % len(data)

        client_sock.close()

        print "connection closed"

    server_sock.close()
else:
    sock=bluetooth.BluetoothSocket(bluetooth.L2CAP)

    bt_addr = sys.argv[2]
    print "trying to connect to %s:1001" % bt_addr
    port = 0x1001
    sock.connect((bt_addr, port))

    print "connected.  Adjusting link parameters."
    bluetooth.set_l2cap_mtu( sock, 65535 )

    totalsent = 0 
    for i in range(1, 65535, 100):
        pkt = "0" * i
        sent = sock.send(pkt)
        print "sent packet of size %d (tried %d)" % (sent, len(pkt))

    sock.close()
