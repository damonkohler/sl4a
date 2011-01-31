"""Say chat messages aloud as they are received."""

__author__ = 'Damon Kohler <damonkohler@gmail.com>'
__copyright__ = 'Copyright (c) 2009, Google Inc.'
__license__ = 'Apache License, Version 2.0'

import android
import xmpp

_SERVER = 'talk.google.com', 5223

def log(droid, message):
  print message
  self.droid.ttsSpeak(message)


class SayChat(object):

  def __init__(self):
    self.droid = android.Android()
    username = self.droid.getInput('Username').result
    password = self.droid.getInput('Password').result
    jid = xmpp.protocol.JID(username)
    self.client = xmpp.Client(jid.getDomain(), debug=[])
    self.client.connect(server=_SERVER)
    self.client.RegisterHandler('message', self.message_cb)
    if not self.client:
      log('Connection failed!')
      return
    auth = self.client.auth(jid.getNode(), password, 'botty')
    if not auth:
      log('Authentication failed!')
      return
    self.client.sendInitPresence()

  def message_cb(self, session, message):
    jid = xmpp.protocol.JID(message.getFrom())
    username = jid.getNode()
    text = message.getBody()
    self.droid.ttsSpeak('%s says %s' % (username, text))

  def run(self):
    try:
      while True:
        self.client.Process(1)
    except KeyboardInterrupt:
      pass


saychat = SayChat()
saychat.run()
