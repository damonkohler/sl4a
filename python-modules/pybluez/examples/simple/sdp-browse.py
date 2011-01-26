# file: sdp-browse.py
# auth: Albert Huang <albert@csail.mit.edu>
# desc: displays services being advertised on a specified bluetooth device
# $Id: sdp-browse.py 393 2006-02-24 20:30:15Z albert $

import sys
import bluetooth

if len(sys.argv) < 2:
    print "usage: sdp-browse <addr>"
    print "   addr can be a bluetooth address, \"localhost\", or \"all\""
    sys.exit(2)

target = sys.argv[1]
if target == "all": target = None

services = bluetooth.find_service(address=target)

if len(services) > 0:
    print "found %d services on %s" % (len(services), sys.argv[1])
    print
else:
    print "no services found"

for svc in services:
    print "Service Name: %s"    % svc["name"]
    print "    Host:        %s" % svc["host"]
    print "    Description: %s" % svc["description"]
    print "    Provided By: %s" % svc["provider"]
    print "    Protocol:    %s" % svc["protocol"]
    print "    channel/PSM: %s" % svc["port"]
    print "    svc classes: %s "% svc["service-classes"]
    print "    profiles:    %s "% svc["profiles"]
    print "    service id:  %s "% svc["service-id"]
    print
