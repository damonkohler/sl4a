#!/usr/bin/python
# $Id: xtalk.py,v 1.4 2008/08/09 17:00:18 normanr Exp $
import sys,os,xmpp,time,select

class Bot:

    def __init__(self,jabber,remotejid):
        self.jabber = jabber
        self.remotejid = remotejid

    def register_handlers(self):
        self.jabber.RegisterHandler('message',self.xmpp_message)

    def xmpp_message(self, con, event):
        type = event.getType()
        fromjid = event.getFrom().getStripped()
        body = event.getBody()
        if type in ['message', 'chat', None] and fromjid == self.remotejid and body:
            sys.stdout.write(body + '\n')

    def stdio_message(self, message):
        m = xmpp.protocol.Message(to=self.remotejid,body=message,typ='chat')
        self.jabber.send(m)

    def xmpp_connect(self):
        con=self.jabber.connect()
        if not con:
            sys.stderr.write('could not connect!\n')
            return False
        sys.stderr.write('connected with %s\n'%con)
        auth=self.jabber.auth(jid.getNode(),jidparams['password'],resource=jid.getResource())
        if not auth:
            sys.stderr.write('could not authenticate!\n')
            return False
        sys.stderr.write('authenticated using %s\n'%auth)
        self.register_handlers()
        return con

if __name__ == '__main__':

    if len(sys.argv) < 2:
        print "Syntax: xtalk JID"
        sys.exit(0)
    
    tojid=sys.argv[1]
    
    jidparams={}
    if os.access(os.environ['HOME']+'/.xtalk',os.R_OK):
        for ln in open(os.environ['HOME']+'/.xtalk').readlines():
            if not ln[0] in ('#',';'):
                key,val=ln.strip().split('=',1)
                jidparams[key.lower()]=val
    for mandatory in ['jid','password']:
        if mandatory not in jidparams.keys():
            open(os.environ['HOME']+'/.xtalk','w').write('#Uncomment fields before use and type in correct credentials.\n#JID=romeo@montague.net/resource (/resource is optional)\n#PASSWORD=juliet\n')
            print 'Please point ~/.xtalk config file to valid JID for sending messages.'
            sys.exit(0)
    
    jid=xmpp.protocol.JID(jidparams['jid'])
    cl=xmpp.Client(jid.getDomain())#,debug=[])
    
    bot=Bot(cl,tojid)

    if not bot.xmpp_connect():
        sys.stderr.write("Could not connect to server, or password mismatch!\n")
        sys.exit(1)

    #cl.SendInitPresence(requestRoster=0)   # you may need to uncomment this for old server
    
    socketlist = {cl.Connection._sock:'xmpp',sys.stdin:'stdio'}
    online = 1

    while online:
        (i , o, e) = select.select(socketlist.keys(),[],[],1)
        for each in i:
            if socketlist[each] == 'xmpp':
                cl.Process(1)
            elif socketlist[each] == 'stdio':
                msg = sys.stdin.readline().rstrip('\r\n')
                bot.stdio_message(msg)
            else:
                raise Exception("Unknown socket type: %s" % repr(socketlist[each]))
    #cl.disconnect()
