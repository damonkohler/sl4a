#!/usr/bin/python
""" The example of using xmpppy's Ad-Hoc Commands (JEP-0050) implementation.
"""
import xmpp
from xmpp.protocol import *

options = {
	'JID': 'circles@example.com',
	'Password': '********',
}

class TestCommand(xmpp.commands.Command_Handler_Prototype):
	""" Example class. You should read source if you wish to understate how it works. This one
	    actually does some calculations."""
	name = 'testcommand'
	description = 'Circle calculations'
	def __init__(self, jid=''):
		""" Initialize some internals. Set the first request handler to self.calcTypeForm.
		"""
		xmpp.commands.Command_Handler_Prototype.__init__(self,jid)
		self.initial = {
			'execute': self.initialForm
		}

	def initialForm(self, conn, request):
		""" Assign a session id and send the first form. """
		sessionid = self.getSessionID()
		self.sessions[sessionid] = {
			'jid':request.getFrom(),
			'data':{'type':None}
		}

		# simulate that the client sent sessionid, so calcTypeForm will be able
		# to continue
		request.getTag(name="command").setAttr('sessionid', sessionid)

		return self.calcTypeForm(conn, request)
			
	def calcTypeForm(self, conn, request):
		""" Send first form to the requesting user. """
		# get the session data
		sessionid = request.getTagAttr('command','sessionid')
		session = self.sessions[sessionid]

		# What to do when a user sends us a response? Note, that we should always
		# include 'execute', as it is a default action when requester does not send
		# exact action to do (should be set to the same as 'next' or 'complete' fields)
		session['actions'] = {
			'cancel': self.cancel,
			'next': self.calcTypeFormAccept,
			'execute': self.calcTypeFormAccept,
		}

		# The form to send
		calctypefield = xmpp.DataField(
			name='calctype',
			desc='Calculation Type',
			value=session['data']['type'],
			options=[
				['Calculate the diameter of a circle','circlediameter'],
				['Calculate the area of a circle','circlearea']
			],
			typ='list-single',
			required=1)

		# We set label attribute... seems that the xmpppy.DataField cannot do that
		calctypefield.setAttr('label', 'Calculation Type')

		form = xmpp.DataForm(
			title='Select type of operation',
			data=[
				'Use the combobox to select the type of calculation you would like'\
				'to do, then click Next.',
				calctypefield])

		# Build a reply with the form
		reply = request.buildReply('result')
		replypayload = [
			xmpp.Node('actions',
				attrs={'execute':'next'},
				payload=[xmpp.Node('next')]),
			form]
		reply.addChild(
			name='command',
			namespace=NS_COMMANDS,
			attrs={
				'node':request.getTagAttr('command','node'),
				'sessionid':sessionid,
				'status':'executing'},
			payload=replypayload)
		self._owner.send(reply)	# Question: self._owner or conn?
		raise xmpp.NodeProcessed

	def calcTypeFormAccept(self, conn, request):
		""" Load the calcType form filled in by requester, then reply with
		    the second form. """
		# get the session data
		sessionid = request.getTagAttr('command','sessionid')
		session = self.sessions[sessionid]

		# load the form
		node = request.getTag(name='command').getTag(name='x',namespace=NS_DATA)
		form = xmpp.DataForm(node=node)

		# retrieve the data
		session['data']['type'] = form.getField('calctype').getValue()

		# send second form
		return self.calcDataForm(conn, request)

	def calcDataForm(self, conn, request, notavalue=None):
		""" Send a form asking for diameter. """
		# get the session data
		sessionid = request.getTagAttr('command','sessionid')
		session = self.sessions[sessionid]

		# set the actions taken on requester's response
		session['actions'] = {
			'cancel': self.cancel,
			'prev': self.calcTypeForm,
			'next': self.calcDataFormAccept,
			'execute': self.calcDataFormAccept
		}

		# create a form
		radiusfield = xmpp.DataField(desc='Radius',name='radius',typ='text-single')
		radiusfield.setAttr('label', 'Radius')

		form = xmpp.DataForm(
			title = 'Enter the radius',
			data=[
				'Enter the radius of the circle (numbers only)',
				radiusfield])

		# build a reply stanza
		reply = request.buildReply('result')
		replypayload = [
			xmpp.Node('actions',
				attrs={'execute':'complete'},
				payload=[xmpp.Node('complete'),xmpp.Node('prev')]),
			form]

		if notavalue:
			replypayload.append(xmpp.Node('note',
				attrs={'type': 'warn'},
				payload=['You have to enter valid number.']))

		reply.addChild(
			name='command',
			namespace=NS_COMMANDS,
			attrs={
				'node':request.getTagAttr('command','node'),
				'sessionid':request.getTagAttr('command','sessionid'),
				'status':'executing'},
			payload=replypayload)

		self._owner.send(reply)
		raise xmpp.NodeProcessed

	def calcDataFormAccept(self, conn, request):
		""" Load the calcType form filled in by requester, then reply with the result. """
		# get the session data
		sessionid = request.getTagAttr('command','sessionid')
		session = self.sessions[sessionid]

		# load the form
		node = request.getTag(name='command').getTag(name='x',namespace=NS_DATA)
		form = xmpp.DataForm(node=node)

		# retrieve the data; if the entered value is not a number, return to second stage
		try:
			value = float(form.getField('radius').getValue())
		except:
			self.calcDataForm(conn, request, notavalue=True)

		# calculate the answer
		from math import pi
		if session['data']['type'] == 'circlearea':
			result = (value**2) * pi
		else:
			result = 2 * value * pi

		# build the result form
		form = xmpp.DataForm(
			typ='result',
			data=[xmpp.DataField(desc='result', name='result', value=result)])

		# build the reply stanza
		reply = request.buildReply('result')
		reply.addChild(
			name='command',
			namespace=NS_COMMANDS,
			attrs={
				'node':request.getTagAttr('command','node'),
				'sessionid':sessionid,
				'status':'completed'},
			payload=[form])

		self._owner.send(reply)

		# erase the data about session
		del self.sessions[sessionid]

		raise xmpp.NodeProcessed

	def cancel(self, conn, request):
		""" Requester canceled the session, send a short reply. """
		# get the session id
		sessionid = request.getTagAttr('command','sessionid')

		# send the reply
		reply = request.buildReply('result')
		reply.addChild(
			name='command',
			namespace=NS_COMMANDS,
			attrs={
				'node':request.getTagAttr('command','node'),
				'sessionid':sessionid,
				'status':'cancelled'})
		self._owner.send(reply)

		# erase the data about session
		del self.sessions[sessionid]

		raise xmpp.NodeProcessed

class ConnectionError: pass
class AuthorizationError: pass
class NotImplemented: pass

class Bot:
	""" The main bot class. """

	def __init__(self, JID, Password):
		""" Create a new bot. Connect to the server and log in. """

		# connect...
		jid = xmpp.JID(JID)
		self.connection = xmpp.Client(jid.getDomain(), debug=['always', 'browser', 'testcommand'])

		result = self.connection.connect()

		if result is None:
			raise ConnectionError

		# authorize
		result = self.connection.auth(jid.getNode(), Password)

		if result is None:
			raise AuthorizationError

		# plugins
		# disco - needed by commands

		# warning: case of "plugin" method names are important!
		# to attach a command to Commands class, use .plugin()
		# to attach anything to Client class, use .PlugIn()
		self.disco = xmpp.browser.Browser()
		self.disco.PlugIn(self.connection)
		self.disco.setDiscoHandler({
			'info': {
				'ids': [{
					'category': 'client',
					'type': 'pc',
					'name': 'Bot'
					}],
				'features': [NS_DISCO_INFO],
				}
			})

		self.commands = xmpp.commands.Commands(self.disco)
		self.commands.PlugIn(self.connection)

		self.command_test = TestCommand()
		self.command_test.plugin(self.commands)

		# presence
		self.connection.sendInitPresence(requestRoster=0)

	def loop(self):
		""" Do nothing except handling new xmpp stanzas. """
		try:
			while self.connection.Process(1):
				pass
		except KeyboardInterrupt:
			pass

bot = Bot(**options)
bot.loop()
