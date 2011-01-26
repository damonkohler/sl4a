from btcommon import *
import socket
import struct
import threading
import os
import _widcomm

DEFAULT_MTU = 672

def dbg (*args):
    return
    sys.stdout.write (*args)
    sys.stdout.write ("\n")

def BD_ADDR_to_str (bda):
    return "%02X:%02X:%02X:%02X:%02X:%02X" % \
            (ord(bda[0]), ord(bda[1]), ord(bda[2]), 
             ord(bda[3]), ord(bda[4]), ord(bda[5])) 

def str_to_BD_ADDR (s):
    digits = [ int (c, 16) for c in s.split(":") ]
    return struct.pack ("6B", *digits)

class WCInquirer:
    DEVST_DOWN     = 0
    DEVST_UP       = 1
    DEVST_ERROR    = 2
    DEVST_UNLOADED = 3
    DEVST_RELOADED = 4
    
    def __init__ (self):
        self._wcinq = _widcomm._WCInquirer ()

        port = self._wcinq.get_sockport ()

        self.readsock = socket.socket (socket.AF_INET, socket.SOCK_STREAM)
        self.readsock.connect (("127.0.0.1", port))
        self._wcinq.accept_client ()
        
        self.recently_discovered = []
        self.inquiry_in_progress = False
        self.sdp_query_in_progress = False

    def fileno ():
        return self.readsock.fileno ()

    def start_inquiry (self):
        self.recently_discovered = []
        self.inquiry_in_progress = self._wcinq.start_inquiry ()

    def read_msg (self):
        intsize = struct.calcsize ("=i")
        msg_type = struct.unpack ("=i", self.readsock.recv (intsize))[0]

        if msg_type == _widcomm.INQ_DEVICE_RESPONDED:
            fmt = "=6s3s248si"
            data = self.readsock.recv (struct.calcsize (fmt))
            bda, devclass, bdname, connected = struct.unpack (fmt, data)

            bdaddr = BD_ADDR_to_str (bda)
            bdname = bdname.strip ("\0")
            self.recently_discovered.append ((bdaddr, devclass, bdname, 
                    connected))

        elif msg_type == _widcomm.INQ_INQUIRY_COMPLETE:
            fmt = "=ih"
            data = self.readsock.recv (struct.calcsize (fmt))
            success, num_responses = struct.unpack (fmt, data)

            self.inquiry_in_progress = False

        elif msg_type == _widcomm.INQ_DISCOVERY_COMPLETE:
            self.sdp_query_in_progress = False

        elif msg_type == _widcomm.INQ_STACK_STATUS_CHANGE:
            fmt = "=i"
            data = self.readsock.recv (struct.calcsize (fmt))
            new_status = struct.unpack (fmt, data)[0]

    def start_discovery (self, addr, uuid = None):
        bd_addr = str_to_BD_ADDR (addr)
        if uuid is not None:
            self.sdp_query_in_progress = \
                    self._wcinq.start_discovery (bd_addr, to_full_uuid (uuid))
        else:
            self.sdp_query_in_progress = \
                    self._wcinq.start_discovery (bd_addr)
        self.sdp_query_in_progress = True

    def read_discovery_records (self, addr, uuid = None):
        if not is_valid_address (addr):
            raise ValueError ("invalid Bluetooth address")
        bd_addr = str_to_BD_ADDR (addr)
        if uuid is not None:
            dbg ("read_discovery_records (%s, %s)" % (addr, uuid))
            return self._wcinq.read_discovery_records (bd_addr,
                    to_full_uuid (uuid))
        else:
            return self._wcinq.read_discovery_records (bd_addr)

    def is_device_ready (self):
        return self._wcinq.is_device_ready ()

    def get_local_device_address (self):
        return self._wcinq.get_local_device_address ()

inquirer = WCInquirer ()

def discover_devices (duration=8, flush_cache=True, lookup_names=False):
    inquirer.start_inquiry ()

    while inquirer.inquiry_in_progress:
        inquirer.read_msg ()

    discovered = inquirer.recently_discovered[:]

    if not lookup_names:
        return [ tup[0] for tup in discovered ]
    if lookup_names:
        result = []
        for bdaddr, devClass, bdName, bConnected in discovered:
            if bdName:
                result.append ((bdaddr, bdName))
            else:
                result.append ((bdAddr, None))
        return result

def lookup_name (address, timeout=10):
    discover_devices ()
    for bdaddr, devClass, bdName, bConnected in inquirer.recently_discovered:
        if bdaddr == address:
            return bdName

def advertise_service (sock, name, service_id = "", service_classes = [], \
        profiles = [], provider = "", description = "", protocols = []):
    sock._advertise_service (name, service_id, service_classes,
            profiles, provider, description, protocols)

def stop_advertising (sock):
    sock._stop_advertising ()

def find_service (name = None, uuid = None, address = None):
    if address:
        if address == "localhost": raise NotImplementedError
        if not is_valid_address (address):
            raise ValueError ("invalid Bluetooth address")
        addresses = [ address ]
    else:
        addresses = discover_devices ()

    if uuid and not is_valid_uuid (uuid):
        raise ValueError ("invalid uuid ", uuid)

    results = []
    for addr in addresses:
        inquirer.start_discovery (addr, uuid)
        while inquirer.sdp_query_in_progress:
            inquirer.read_msg ()
        results.extend (inquirer.read_discovery_records (addr, uuid))

    return results

def _port_return_code_to_str (code):
    k = { _widcomm.RFCOMM_SUCCESS : "Success",
          _widcomm.RFCOMM_ALREADY_OPENED : "Port already opened",
          _widcomm.RFCOMM_NOT_OPENED : "Connection not open",
          _widcomm.RFCOMM_HANDLE_ERROR: "This error should never occur " \
                                        "(HANDLE_ERROR) and is a stack bug",
          _widcomm.RFCOMM_LINE_ERR: "Line error",
          _widcomm.RFCOMM_START_FAILED: "Connection attempt failed",
          _widcomm.RFCOMM_PAR_NEG_FAILED: "Parameter negotion (MTU) failed",
          _widcomm.RFCOMM_PORT_NEG_FAILED: "Port negotiation failed",
          _widcomm.RFCOMM_PEER_CONNECTION_FAILED: "Connection ended by remote "\
                                                  "side",
          _widcomm.RFCOMM_PEER_TIMEOUT: "Timeout by remote side",
          _widcomm.RFCOMM_INVALID_PARAMETER: "Invalid parameter",
          _widcomm.RFCOMM_UNKNOWN_ERROR: "Unknown error" }
    if code in k:
        return k[code]
    else:
        return "Invalid RFCOMM error code %s" % str (code)

def _port_ev_code_to_str (code):
    d = { _widcomm.PORT_EV_RXFLAG : "Received certain character",
          _widcomm.PORT_EV_TXEMPTY : "Transmit Queue Empty",
          _widcomm.PORT_EV_CTS : "CTS changed state",
          _widcomm.PORT_EV_DSR : "DSR changed state",
          _widcomm.PORT_EV_RLSD : "RLSD changed state",
          _widcomm.PORT_EV_BREAK : "BREAK received",
          _widcomm.PORT_EV_ERR : "Line status error occurred",
          _widcomm.PORT_EV_RING : "Ring signal detected",
          _widcomm.PORT_EV_CTSS : "CTS state",
          _widcomm.PORT_EV_DSRS : "DSR state",
          _widcomm.PORT_EV_RLSDS : "RLSD state",
          _widcomm.PORT_EV_OVERRUN : "Receiver buffer overrun",
          _widcomm.PORT_EV_TXCHAR : "Any character transmitted",
          _widcomm.PORT_EV_CONNECTED : "RFCOMM connection established",
          _widcomm.PORT_EV_CONNECT_ERR : "Was not able to establish " \
                                         "connection or disconnected",
          _widcomm.PORT_EV_FC : "Flow control enabled flag changed by remote",
          _widcomm.PORT_EV_FCS : "Flow control status true = enabled" }
    result = []
    for k, v in d.items ():
        if code & k:
            result.append (v)
    if len (result) == 0:
        return "Invalid event code %d" % code
    else:
        return "\n".join (result)

def _sdp_checkraise (code):
    if code == _widcomm.SDP_OK: return
    elif code == _widcomm.SDP_COULD_NOT_ADD_RECORD:
        raise BluetoothError ("Could not add SDP record")
    elif code == _widcomm.SDP_INVALID_RECORD:
        raise BluetoothError ("Invalid SDP record")
    elif code == _widcomm.SDP_INVALID_PARAMETERS:
        raise BluetoothError ("SDP: invalid parameters")
    raise RuntimeError ("unknown SDP status code %s" % code)

class BluetoothSocket:
    def __init__ (self, proto = RFCOMM, _sockdata = None):
        if not proto in [ RFCOMM, L2CAP ]:
            raise ValueError ("invalid protocol")

        self.proto = proto

        if proto == RFCOMM:
            self.bind            = self.rfcomm_bind
            self.listen          = self.rfcomm_listen
            self.accept          = self.rfcomm_accept
            self.connect         = self.rfcomm_connect
            self.send            = self.rfcomm_send
            self.recv            = self.rfcomm_recv
            self.close           = self.rfcomm_close
            self.getsockname     = self.rfcomm_getsockname
            self.setblocking     = self.rfcomm_setblocking
            self.settimeout      = self.rfcomm_settimeout
            self.gettimeout      = self.rfcomm_gettimeout
            self.dup             = self.rfcomm_dup
            self.makefile        = self.rfcomm_makefile
            self.fileno          = self.rfcomm_fileno
            self.__make_cobjects = self.__rfcomm_make_cobjects
            self._advertise_service = self.__rfcomm_advertise_service

            if _sockdata:
                self._wc, self._if, self.readsock = _sockdata
            else:
                self.__make_cobjects ()

            self.connected = self._wc.is_connected ()

        elif proto == L2CAP:
            dbg ("creating l2cap socket")
            self.bind            = self.l2cap_bind
            self.listen          = self.l2cap_listen
            self.accept          = self.l2cap_accept
            self.connect         = self.l2cap_connect
            self.send            = self.l2cap_send
            self.recv            = self.l2cap_recv
            self.close           = self.l2cap_close
            self.getsockname     = self.l2cap_getsockname
            self.setblocking     = self.l2cap_setblocking
            self.settimeout      = self.l2cap_settimeout
            self.gettimeout      = self.l2cap_gettimeout
            self.dup             = self.l2cap_dup
            self.makefile        = self.l2cap_makefile
            self.fileno          = self.l2cap_fileno
            self.__make_cobjects = self.__l2cap_make_cobjects
            self._advertise_service = self.__l2cap_advertise_service

            if _sockdata:
                self._wc, self._if, self.readsock = _sockdata
                self.connected = True
            else:
                self.__make_cobjects ()
                self.connected = False
        else:
            raise NotImplementedError ()

        self.nonblocking = False
        self.connecting = False
        self.listening = False
        self.bound = False
        self.received_data = []
        self.last_event_code = None
        self.port = 0
        self._sdpservice = None
    
    def _stop_advertising (self):
        if not self._sdpservice:
            raise BluetoothError ("not advertising any services")
        self._sdpservice = None

    def __rfcomm_make_cobjects (self):
        self._wc = _widcomm._WCRfCommPort ()
        self._if = _widcomm._WCRfCommIf ()
        self.readsock = socket.socket (socket.AF_INET, socket.SOCK_STREAM)
        self.readsock.connect (("127.0.0.1", self._wc.get_sockport ()))
        self._wc.accept_client ()
        
    def rfcomm_read_msg (self):
        intsize = struct.calcsize ("=i")
        msg_type_data = self.readsock.recv (intsize)
        msg_type = struct.unpack ("=i", msg_type_data)[0]

        if msg_type == _widcomm.RFCOMM_DATA_RECEIVED:
            datalen_fmt = "=i"
            datalen_data = self.readsock.recv (struct.calcsize (datalen_fmt))
            datalen = struct.unpack (datalen_fmt, datalen_data)[0]

            self.received_data.append (self.readsock.recv (datalen))

        elif msg_type == _widcomm.RFCOMM_EVENT_RECEIVED:
            fmt = "=I"
            data = self.readsock.recv (struct.calcsize (fmt))
            code = struct.unpack (fmt, data)[0]
            dbg ("event %X received" % code)
            
            if code & _widcomm.PORT_EV_CONNECTED:
                self.connecting = False
                self.listening = False
                self.connected = True
            if code & _widcomm.PORT_EV_CONNECT_ERR:
                self.connecting = False
                self.listening = False
                self.connected = False
                raise BluetoothError ("Connection failed")
            if code & _widcomm.PORT_EV_RXFLAG:
                dbg ("Rx flag")
            if code & _widcomm.PORT_EV_TXEMPTY:
                dbg ("Tx queue empty")
            if code & _widcomm.PORT_EV_CTS:
                dbg ("CTS changed state")
            if code & _widcomm.PORT_EV_DSR:
                dbg ("DSR changed state")
            if code & _widcomm.PORT_EV_RLSD:
                dbg ("RLSD changed state")
            if code & _widcomm.PORT_EV_BREAK:
                dbg ("BREAK received")
            if code & _widcomm.PORT_EV_ERR:
                dbg ("Line status error")
            if code & _widcomm.PORT_EV_RING:
                dbg ("Ring")
            if code & _widcomm.PORT_EV_CTSS:
                dbg ("CTS state")
            if code & _widcomm.PORT_EV_DSRS:
                dbg ("DSR state")
            if code & _widcomm.PORT_EV_RLSDS:
                dbg ("RLSD state")
            if code & _widcomm.PORT_EV_OVERRUN:
                dbg ("Receive buffer overrun")
            if code & _widcomm.PORT_EV_TXCHAR:
                dbg ("Data transmitted")
            if code & _widcomm.PORT_EV_FC:
                dbg ("Flow control changed by remote")
            if code & _widcomm.PORT_EV_FCS:
                dbg ("Flow control status true = enabled")

            self.last_event_code = code

    def rfcomm_bind (self, addrport):
        addr, port = addrport

        if len (addr): 
            raise ValueError ("Widcomm stack can't bind to " \
            "user-specified adapter")

        result = self._if.assign_scn_value (RFCOMM_UUID, port)
        if not result:
            raise BluetoothError ("unable to bind to port")
        self.bound = True
        self.port = self._if.get_scn ()

    def rfcomm_listen (self, backlog):
        if self.connected: 
            raise BluetoothError ("already connected")
        if self.listening: 
            raise BluetoothError ("already listening/connecting")
        if backlog != 1:
            raise ValueError ("Widcomm stack requires backlog == 1")

        port = self._if.get_scn ()
        self._if.set_security_level ("", _widcomm.BTM_SEC_NONE, True)
        if not port:
            raise BluetoothError ("not bound to a port")
        result = self._wc.open_server (port, DEFAULT_MTU)
        if result != _widcomm.RFCOMM_SUCCESS:
            raise BluetoothError (_port_return_code_to_str (result))
        self.listening = True

    def rfcomm_accept (self):
        if self.connected:
            raise BluetoothError ("already connected")

        while self.listening and not self.connected:
            dbg ("waiting for connection")
            self.rfcomm_read_msg ()

        if self.connected:
            port = self._if.get_scn ()

            client_bdaddr = BD_ADDR_to_str (self._wc.is_connected ())
            # XXX widcomm API doesn't provide a way to determine the RFCOMM
            # channel number of the client
            client_port = 0

            # create a new socket object and give it ownership of the 
            # wrapped C++ objects, since those are the ones actually connected
            _sockdata = self._wc, self._if, self.readsock
            clientsock = BluetoothSocket (RFCOMM, _sockdata)

            # now create new C++ objects
            self.__rfcomm_make_cobjects ()
#            self.bind (("", port))
#            self.listen (1)

            return clientsock, (client_bdaddr, client_port)

    def rfcomm_connect (self, addrport):
        addr, port = addrport
        dbg ("connecting to %s port %d" % (addr, port))
        if not is_valid_address (addr):
            raise ValueError ("invalid address %s" % addr)

        self._if.assign_scn_value (RFCOMM_UUID, port)
        self._if.set_security_level ("", _widcomm.BTM_SEC_NONE, False)

        result = self._wc.open_client (port, str_to_BD_ADDR (addr), DEFAULT_MTU)
        if result != _widcomm.RFCOMM_SUCCESS:
            raise BluetoothError (_port_return_code_to_str (result))

        self.connecting = True
        while self.connecting:
            self.rfcomm_read_msg ()

        if not self._wc.is_connected ():
            raise BluetoothError ("connection failed")


    def rfcomm_send (self, data):
        dbg ("sending: [%s]" % data)
        status, written = self._wc.write (data)
        if status == _widcomm.RFCOMM_SUCCESS:
            dbg ("sent okay")
            return written
        else:
            raise BluetoothError (_port_return_code_to_str (status))

    def rfcomm_recv (self, numbytes):
        if self.nonblocking and not self.received_data:
            # XXX are we supposed to raise an exception, or just return None?
            return None

        while not self.received_data and self._wc.is_connected ():
            self.rfcomm_read_msg ()

        if self.received_data:
            data = self.received_data.pop (0)
            if len(data) > numbytes:
                self.received_data.insert (0, data[numbytes:])
                return data[:numbytes]
            else:
                return data

    def rfcomm_close (self):
        self._wc.close ()
        self._wc = None
        self.bound = False
        self.connecting = False
        self.listening = False
        self.connected = False
#        return bt.close (self._sockfd)

    def rfcomm_getsockname (self):
        if not self.bound:
            raise BluetoothError ("Socket not bound")
        addr = inquirer.get_local_device_address ()
        port = self._if.get_scn ()
        return addr, port

    def rfcomm_setblocking (self, blocking):
        self.nonblocking = not blocking
        self.readsock.setblocking (blocking)

    def rfcomm_settimeout (self, timeout):
        raise NotImplementedError
        pass
#        if timeout < 0: raise ValueError ("invalid timeout")
#
#        if timeout == 0:
#            self.setblocking (False)
#        else:
#            self.setblocking (True)
#            # XXX this doesn't look correct
#            timeout = 0 # winsock timeout still needs to be set 0
#
#        s = bt.settimeout (self._sockfd, timeout)
#        self._timeout = timeout

    def rfcomm_gettimeout (self):    
        raise NotImplementedError
#        if self._blocking and not self._timeout: return None
#        return bt.gettimeout (self._sockfd)

    def rfcomm_fileno (self):
        return self.readsock.fileno ()

    def rfcomm_dup (self):
        raise NotImplementedError

    def rfcomm_makefile (self):
        raise NotImplementedError

    def __rfcomm_advertise_service (self, name, service_id, 
            service_classes, profiles, provider, description, 
            protocols):
        if self._sdpservice is not None:
            raise BluetoothError ("Service already advertised")
        if not self.listening:
            raise BluetoothError ("Socket must be listening before advertised")
        if protocols:
            raise NotImplementedError ("extra protocols not yet supported in Widcomm stack")

        self._sdpservice = _widcomm._WCSdpService ()
        if service_classes:
            service_classes = [ to_full_uuid (s) for s in service_classes ]
            _sdp_checkraise (self._sdpservice.add_service_class_id_list ( \
                    service_classes))
        
#        self._if.set_security_level (name, _widcomm.BTM_SEC_NONE, True)
        _sdp_checkraise (self._sdpservice.add_rfcomm_protocol_descriptor ( \
            self.port))
        if profiles:
            for uuid, version in profiles:
                uuid = to_full_uuid (uuid)
                _sdp_checkraise (self._sdpservice.add_profile_descriptor_list (\
                        uuid, version))
        _sdp_checkraise (self._sdpservice.add_service_name (name))
        _sdp_checkraise (self._sdpservice.make_public_browseable ())

    def __l2cap_make_cobjects (self):
        dbg ("__l2cap_make_cobjects")
        self._wc = _widcomm._WCL2CapConn ()
        self._if = _widcomm._WCL2CapIf ()
        self.readsock = socket.socket (socket.AF_INET, socket.SOCK_STREAM)
        self.readsock.connect (("127.0.0.1", self._wc.get_sockport ()))
        self._wc.accept_client ()
        
    def l2cap_read_msg (self):
        intsize = struct.calcsize ("=i")
        msg_type_data = self.readsock.recv (intsize)
        msg_type = struct.unpack ("=i", msg_type_data)[0]

        if msg_type == _widcomm.L2CAP_DATA_RECEIVED:
            datalen_fmt = "=i"
            datalen_data = self.readsock.recv (struct.calcsize (datalen_fmt))
            datalen = struct.unpack (datalen_fmt, datalen_data)[0]
            self.received_data.append (self.readsock.recv (datalen))

        elif msg_type == _widcomm.L2CAP_INCOMING_CONNECTION:
            result = self._wc.accept ()
            if not result: raise BluetoothError ("accept() failed")

        elif msg_type == _widcomm.L2CAP_REMOTE_DISCONNECTED:
            dbg ("L2CAP_REMOTE_DISCONNECTED")
            self.connecting = False
            self.listening = False
            self.connected = False

        elif msg_type == _widcomm.L2CAP_CONNECTED:
            self.connecting = False
            self.listening = False
            self.connected = True

#        elif msg_type == _widcomm.PORT_EV_CONNECT_ERR:
#            self.connecting = False
#            self.listening = False
#            raise BluetoothError ("Connection failed")

    def l2cap_bind (self, addrport):
        dbg ("l2cap_bind %s" % str(addrport))
        addr, port = addrport

        if len (addr): 
            raise ValueError ("Widcomm stack can't bind to " \
            "user-specified adapter")

        result = self._if.assign_psm_value (L2CAP_UUID, port)
        if not result:
            raise BluetoothError ("unable to bind to port")
        self.bound = True
        self.port = self._if.get_psm ()
        result = self._if.register ()
        if not result:
            raise BluetoothError ("register() failed")

    def l2cap_listen (self, backlog):
        dbg ("l2cap_listen %s" % backlog)
        if self.connected:
            raise BluetoothError ("already connected")
        if self.listening:
            raise BluetoothError ("already listening/connecting")
        if backlog != 1:
            raise ValueError ("Widcomm stack requires backlog == 1")

        port = self._if.get_psm ()
        self._if.set_security_level ("", _widcomm.BTM_SEC_NONE, True)
        if not port:
            raise BluetoothError ("not bound to a port")
        result = self._wc.listen (self._if)
        if not result:
            raise BluetoothError ("listen() failed.  don't know why")
        self.listening = True

    def l2cap_accept (self):
        dbg ("l2cap_accept")
        if self.connected:
            raise BluetoothError ("already connected")

        while self.listening and not self.connected:
            dbg ("waiting for connection")
            self.l2cap_read_msg ()

        if self.connected:
            port = self._if.get_psm ()

            client_bdaddr = BD_ADDR_to_str (self._wc.remote_bd_addr ())
            # XXX widcomm API doesn't provide a way to determine the L2CAP
            # PSM of the client
            client_port = 0

            # create a new socket object and give it ownership of the 
            # wrapped C++ objects, since those are the ones actually connected
            _sockdata = self._wc, self._if, self.readsock
            clientsock = BluetoothSocket (L2CAP, _sockdata)

            # now create new C++ objects
            self.__l2cap_make_cobjects ()
#            self.bind (("", port))
#            self.listen (1)

            return clientsock, (client_bdaddr, client_port)

    def l2cap_connect (self, addrport):
        addr, port = addrport
        dbg ("connecting to %s port %d" % (addr, port))
        if not is_valid_address (addr):
            raise ValueError ("invalid address %s" % addr)

        if not self._if.assign_psm_value (L2CAP_UUID, port):
            raise BluetoothError ("Failed to assign PSM %d" % port)
        if not self._if.set_security_level ("", _widcomm.BTM_SEC_NONE, False):
            raise BluetoothError ("Failed to set security level")
        if not self._if.register ():
            raise BluetoothError ("Failed to register PSM")

        self.connecting = True

        if not self._wc.connect (self._if, str_to_BD_ADDR (addr)):
            raise BluetoothError ("Connect failed")

        while self.connecting:
            self.l2cap_read_msg ()

        if not self.connected:
            raise BluetoothError ("connection failed")


    def l2cap_send (self, data):
        dbg ("sending: [%s]" % data)
        status, written = self._wc.write (data)
        if status:
            dbg ("sent okay")
            return written
        else:
            raise BluetoothError (_port_return_code_to_str (status))

    def l2cap_recv (self, numbytes):
        if self.nonblocking and not self.received_data:
            # XXX are we supposed to raise an exception, or just return None?
            return None

        while not self.received_data and self.connected:
            self.l2cap_read_msg ()

        if self.received_data:
            data = self.received_data.pop (0)
            if len(data) > numbytes:
                self.received_data.insert (0, data[numbytes:])
                return data[:numbytes]
            else:
                return data

    def l2cap_close (self):
        self._wc.disconnect ()
        self._if.deregister ()
        self._wc = None
        self.bound = False
        self.connecting = False
        self.listening = False
        self.connected = False
#        return bt.close (self._sockfd)

    def l2cap_getsockname (self):
        if not self.bound:
            raise BluetoothError ("Socket not bound")
        addr = inquirer.get_local_device_address ()
        port = self._if.get_psm ()
        return addr, port

    def l2cap_setblocking (self, blocking):
        self.nonblocking = not blocking
        self.readsock.setblocking (blocking)

    def l2cap_settimeout (self, timeout):
        raise NotImplementedError
#        if timeout < 0: raise ValueError ("invalid timeout")
#
#        if timeout == 0:
#            self.setblocking (False)
#        else:
#            self.setblocking (True)
#            # XXX this doesn't look correct
#            timeout = 0 # winsock timeout still needs to be set 0
#
#        s = bt.settimeout (self._sockfd, timeout)
#        self._timeout = timeout

    def l2cap_gettimeout (self):    
        raise NotImplementedError
#        if self._blocking and not self._timeout: return None
#        return bt.gettimeout (self._sockfd)

    def l2cap_fileno (self):
        return self.readsock.fileno ()

    def l2cap_dup (self):
        raise NotImplementedError
#        return BluetoothSocket (self._proto, sockfd=bt.dup (self._sockfd))

    def l2cap_makefile (self):
        raise NotImplementedError

    def __l2cap_advertise_service (self, name, service_id, 
            service_classes, profiles, provider, description, 
            protocols):
        if self._sdpservice is not None:
            raise BluetoothError ("Service already advertised")
        if not self.listening:
            raise BluetoothError ("Socket must be listening before advertised")
        if protocols:
            raise NotImplementedError ("extra protocols not yet supported in Widcomm stack")

        self._sdpservice = _widcomm._WCSdpService ()
        if service_classes:
            service_classes = [ to_full_uuid (s) for s in service_classes ]
            _sdp_checkraise (self._sdpservice.add_service_class_id_list ( \
                service_classes))
        _sdp_checkraise (self._sdpservice.add_l2cap_protocol_descriptor ( \
                self.port))
        if profiles:
            for uuid, version in profiles:
                uuid = to_full_uuid (uuid)
                _sdp_checkraise (self._sdpservice.add_profile_descriptor_list (\
                        uuid, version))
        _sdp_checkraise (self._sdpservice.add_service_name (name))
        _sdp_checkraise (self._sdpservice.make_public_browseable ())


class DeviceDiscoverer:
    def __init__ (self):
        raise NotImplementedError
