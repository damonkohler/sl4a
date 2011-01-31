# file: asynchronous-inquiry.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: demonstration of how to do asynchronous device discovery by subclassing
#       the DeviceDiscoverer class
# $Id: asynchronous-inquiry.py 405 2006-05-06 00:39:50Z albert $
#
# XXX Linux only (5/5/2006)

import bluetooth
import select

class MyDiscoverer(bluetooth.DeviceDiscoverer):
    
    def pre_inquiry(self):
        self.done = False
    
    def device_discovered(self, address, device_class, name):
        print "%s - %s" % (address, name)
        
        # get some information out of the device class and display it.
        # voodoo magic specified at:
        #
        # https://www.bluetooth.org/foundry/assignnumb/document/baseband
        major_classes = ( "Miscellaneous", 
                          "Computer", 
                          "Phone", 
                          "LAN/Network Access point", 
                          "Audio/Video", 
                          "Peripheral", 
                          "Imaging" )
        major_class = (device_class >> 8) & 0xf
        if major_class < 7:
            print "  %s" % major_classes[major_class]
        else:
            print "  Uncategorized"

        print "  services:"
        service_classes = ( (16, "positioning"), 
                            (17, "networking"), 
                            (18, "rendering"), 
                            (19, "capturing"),
                            (20, "object transfer"), 
                            (21, "audio"), 
                            (22, "telephony"), 
                            (23, "information"))

        for bitpos, classname in service_classes:
            if device_class & (1 << (bitpos-1)):
                print "    %s" % classname

    def inquiry_complete(self):
        self.done = True

d = MyDiscoverer()
d.find_devices(lookup_names = True)

readfiles = [ d, ]

while True:
    rfds = select.select( readfiles, [], [] )[0]

    if d in rfds:
        d.process_event()

    if d.done: break
