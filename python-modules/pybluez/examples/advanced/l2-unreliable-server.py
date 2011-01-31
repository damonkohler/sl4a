import sys
import bluetooth

if len(sys.argv) < 2:
    print "usage: l2-unreliable-server"
    sys.exit(2)

timeout = int(sys.argv[1])
assert timeout >= 0

server_sock=bluetooth.BluetoothSocket( bluetooth.L2CAP )
server_sock.bind(("",0x1001))
server_sock.listen(1)
while True:
    print "waiting for incoming connection"
    client_sock,address = server_sock.accept()
    print "Accepted connection from %s" % str(address)

    print "waiting for data"
    total = 0
    while True:
        try:
            data = client_sock.recv(1024)
        except bluetooth.BluetoothError, e:
            break
        if len(data) == 0: break
        total += len(data)
        print "total byte read: %d" % total

    client_sock.close()

    print "connection closed"

server_sock.close()
