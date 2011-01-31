import sys
import fcntl
import struct
import array
import bluetooth
import _bluetooth as bt   # low level bluetooth wrappers.

def __get_acl_conn_handle(sock, addr):
    hci_fd = sock.fileno()
    reqstr = struct.pack( "6sB17s", bt.str2ba(addr), bt.ACL_LINK, "\0" * 17)
    request = array.array( "c", reqstr )
    fcntl.ioctl( hci_fd, bt.HCIGETCONNINFO, request, 1 )
    handle = struct.unpack("8xH14x", request.tostring())[0]
    return handle

def write_flush_timeout( addr, timeout ):
    hci_sock = bt.hci_open_dev()
    # get the ACL connection handle to the remote device
    handle = __get_acl_conn_handle(hci_sock, addr)
    pkt = struct.pack("HH", handle, bt.htobs(timeout))
    response = bt.hci_send_req(hci_sock, bt.OGF_HOST_CTL, 
        0x0028, bt.EVT_CMD_COMPLETE, 3, pkt)
    status = struct.unpack("B", response[0])[0]
    rhandle = struct.unpack("H", response[1:3])[0]
    assert rhandle == handle 
    assert status == 0

def read_flush_timeout( addr ):
    hci_sock = bt.hci_open_dev()
    # get the ACL connection handle to the remote device
    handle = __get_acl_conn_handle(hci_sock, addr)
    pkt = struct.pack("H", handle)
    response = bt.hci_send_req(hci_sock, bt.OGF_HOST_CTL, 
        0x0027, bt.EVT_CMD_COMPLETE, 5, pkt)
    status = struct.unpack("B", response[0])[0]
    rhandle = struct.unpack("H", response[1:3])[0]
    assert rhandle == handle
    assert status == 0
    fto = struct.unpack("H", response[3:5])[0]
    return fto

# Create the client socket
sock=bluetooth.BluetoothSocket(bluetooth.L2CAP)

if len(sys.argv) < 4:
    print "usage: l2capclient.py <addr> <timeout> <num_packets>"
    print "  address - device that l2-unreliable-server is running on"
    print "  timeout - wait timeout * 0.625ms before dropping unACK'd packets"
    print "  num_packets - number of 627-byte packets to send on connect"
    sys.exit(2)

bt_addr=sys.argv[1]
timeout = int(sys.argv[2])
num_packets = int(sys.argv[3])

print "trying to connect to %s:1001" % bt_addr
port = 0x1001
sock.connect((bt_addr, port))

print "connected.  Adjusting link parameters."
print "current flush timeout is %d ms" % read_flush_timeout( bt_addr )
try:
    write_flush_timeout( bt_addr, timeout )
except bt.error, e:
    print "error setting flush timeout.  are you sure you're superuser?"
    print e
    sys.exit(1)
print "new flush timeout is %d ms" % read_flush_timeout( bt_addr )

totalsent = 0 
for i in range(num_packets):
    pkt = "0" * 672
    totalsent += sock.send(pkt)
print "sent %d bytes total" % totalsent

sock.close()
