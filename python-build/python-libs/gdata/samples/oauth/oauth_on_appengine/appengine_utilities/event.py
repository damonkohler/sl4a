"""
Copyright (c) 2008, appengine-utilities project
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
- Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer.
- Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.
- Neither the name of the appengine-utilities project nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
"""
import __main__


class Event(object):
    """
    Event is a simple publish/subscribe based event dispatcher
    It sets itself to the __main__    function. In order to use it,
    you must import it and __main__
    """

    def __init__(self):
        self.events = []

    def subscribe(self, event, callback, args = None):
        """
        This method will subscribe a callback function to an event name.
        """
        if not {"event": event, "callback": callback, "args": args, } \
            in self.events:
            self.events.append({"event": event, "callback": callback, \
                "args": args, })

    def unsubscribe(self, event, callback, args = None):
        """
        This method will unsubscribe a callback from an event.
        """
        if {"event": event, "callback": callback, "args": args, }\
            in self.events:
            self.events.remove({"event": event, "callback": callback,\
                "args": args, })

    def fire_event(self, event = None):
        """
        This method is what a method uses to fire an event,
        initiating all registered callbacks
        """
        for e in self.events:
            if e["event"] == event:
                if type(e["args"]) == type([]):
                    e["callback"](*e["args"])
                elif type(e["args"]) == type({}):
                    e["callback"](**e["args"])
                elif e["args"] == None:
                    e["callback"]()
                else:
                    e["callback"](e["args"])
"""
Assign to the event class to __main__
"""
__main__.AEU_Events = Event()
