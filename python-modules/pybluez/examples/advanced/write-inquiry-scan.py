import os
import sys
import struct
import _bluetooth as bluez

def read_inquiry_scan_activity(sock):
    """returns the current inquiry scan interval and window, 
    or -1 on failure"""
    # save current filter
    old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

    # Setup socket filter to receive only events related to the
    # read_inquiry_mode command
    flt = bluez.hci_filter_new()
    opcode = bluez.cmd_opcode_pack(bluez.OGF_HOST_CTL, 
            bluez.OCF_READ_INQ_ACTIVITY)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    bluez.hci_filter_set_event(flt, bluez.EVT_CMD_COMPLETE);
    bluez.hci_filter_set_opcode(flt, opcode)
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

    # first read the current inquiry mode.
    bluez.hci_send_cmd(sock, bluez.OGF_HOST_CTL, 
            bluez.OCF_READ_INQ_ACTIVITY )

    pkt = sock.recv(255)

    status,interval,window = struct.unpack("!xxxxxxBHH", pkt)
    interval = bluez.btohs(interval)
    interval = (interval >> 8) | ( (interval & 0xFF) << 8 )
    window = (window >> 8) | ( (window & 0xFF) << 8 )
    if status != 0: mode = -1

    # restore old filter
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )
    return interval, window

def write_inquiry_scan_activity(sock, interval, window):
    """returns 0 on success, -1 on failure"""
    # save current filter
    old_filter = sock.getsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, 14)

    # Setup socket filter to receive only events related to the
    # write_inquiry_mode command
    flt = bluez.hci_filter_new()
    opcode = bluez.cmd_opcode_pack(bluez.OGF_HOST_CTL, 
            bluez.OCF_WRITE_INQ_ACTIVITY)
    bluez.hci_filter_set_ptype(flt, bluez.HCI_EVENT_PKT)
    bluez.hci_filter_set_event(flt, bluez.EVT_CMD_COMPLETE);
    bluez.hci_filter_set_opcode(flt, opcode)
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, flt )

    # send the command!
    bluez.hci_send_cmd(sock, bluez.OGF_HOST_CTL, 
            bluez.OCF_WRITE_INQ_ACTIVITY, struct.pack("HH", 
                interval, window) )

    pkt = sock.recv(255)

    status = struct.unpack("xxxxxxB", pkt)[0]

    # restore old filter
    sock.setsockopt( bluez.SOL_HCI, bluez.HCI_FILTER, old_filter )
    if status != 0: return -1
    return 0

dev_id = 0
try:
    sock = bluez.hci_open_dev(dev_id)
except:
    print "error accessing bluetooth device..."
    sys.exit(1)

try:
    interval, window = read_inquiry_scan_activity(sock)
except Exception, e:
    print "error reading inquiry scan activity.  "
    print e
    sys.exit(1)
print "current inquiry scan interval: %d (0x%X) window: %d (0x%X)" % \
        (interval, interval, window, window)

if len(sys.argv) == 3:
    interval = int(sys.argv[1])
    window = int(sys.argv[2])
    print "target interval: %d window %d" % (interval, window)
    write_inquiry_scan_activity(sock, interval, window)
    interval, window = read_inquiry_scan_activity(sock)
    print "current inquiry scan interval: %d (0x%X) window: %d (0x%X)" % \
            (interval, interval, window, window)
