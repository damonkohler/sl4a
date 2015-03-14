#!/usr/bin/python
# -= encoding=utf-8 =-
##############################################################################
#
# testusbserial.py $Rev$
# $Id$
# $URL: https://android-jp-kobe.googlecode.com/svn/trunk/pyAndyUI/uibt2.py $
#
# Copyright (c) 2011, shimoda as kuri65536 _dot_ hot mail _dot_ com
#                       ( email address: convert _dot_ to . and joint string )
# All rights reserved.
#
# Redistribution and use in source and binary forms, with or without modification, are permitted
# provided that the following conditions are met:
#
# - Redistributions of source code must retain the above copyright notice, this list of conditions and
#   the following disclaimer.
# - Redistributions in binary form must reproduce the above copyright notice, this list of conditions
#   and the following disclaimer in the documentation and/or other materials provided with the
#   distribution.
# - Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or
#   promote products derived from this software without specific prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
# IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
# FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
# CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
# IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
# OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
##############################################################################
import android as _android
import time
import json


def main():                     # {{{1
    global android
    android = _android.Android()

    h = None
    for k, v in android.usbserialGetDeviceList().result.items():
        print(k, v)
        if '"0409","FFFD"' in v:
            # v is '["device-name","VID","PID","hashCode"]'
            h = json.loads(v)[-1]
            print("device was found, try to connect =>", h)
    if h is None:
        print("device was not found")
        return

    ret = android.usbserialConnect(h, ",trg78k,")
    if not "OK" in ret.result:
        print("can't connect to device: ", ret.result)
        return
    # ret is '["OK","UUID"]'
    uuid = json.loads(ret.result)[-1]
    print("connected with ", h, uuid)

    # wait until the permission be allowed by user.
    n = 0
    while not android.usbserialReadReady(uuid).result:
        n += 1
        print("waiting for connect....", n)
        time.sleep(1)

    # now get data from USB-Serial.
    n = 1
    while True:
        time.sleep(5)

        buf = android.usbserialRead(uuid)
        print("Received: ", buf)

        if n >= 0:
            n += 10
            if n > 255:
                n = -1
        elif n < 0:
            n -= 10
            if n < -255:
                n = 0

        if n < 0:
            send = "B%03dB%03d" % (n, n)
        else:
            send = "F%03dF%03d" % (n, n)

        #send = "F000B%03d" % n
        #send = "F000B%03d" % n
        buf = android.usbserialWrite(send, uuid)


if __name__ == "__main__":      # {{{1
    main()

### end of script {{{1
# vi: ft=python:fdm=marker
