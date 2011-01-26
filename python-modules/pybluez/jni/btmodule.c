/*

This module provides an interface to bluetooth.  A great deal of the code is
taken from the pyaffix project.

- there are three kinds of bluetooth addresses used here
  HCI address is a single int specifying the device id
  L2CAP address is a pair (host, port)
  RFCOMM address is a pair (host, channel)
  SCO address is just a host
  the host part of the address is always a string of the form "XX:XX:XX:XX:XX"

Local naming conventions:

- names starting with sock_ are socket object methods
- names starting with bt_ are module-level functions

*/

#include "btmodule.h"

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#include <sys/ioctl.h>
#include <sys/types.h>
#include <sys/socket.h>

#include <fcntl.h>
#include <errno.h>
#include <netdb.h>

#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/rfcomm.h>
#include <bluetooth/l2cap.h>
#include <bluetooth/sco.h>

#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include "btsdp.h"

/* Socket object documentation */
PyDoc_STRVAR(sock_doc,
"BluetoothSocket(proto=RFCOMM) -> bluetooth socket object\n\
\n\
Open a socket of the given protocol.  proto must be one of\n\
HCI, L2CAP, RFCOMM, or SCO.  SCO sockets have\n\
not been tested at all yet.\n\
\n\
A BluetoothSocket object represents one endpoint of a bluetooth connection.\n\
\n\
Methods of BluetoothSocket objects (keyword arguments not allowed):\n\
\n\
accept() -- accept a connection, returning new socket and client address\n\
bind(addr) -- bind the socket to a local address\n\
close() -- close the socket\n\
connect(addr) -- connect the socket to a remote address\n\
connect_ex(addr) -- connect, return an error code instead of an exception\n\
dup() -- return a new socket object identical to the current one\n\
fileno() -- return underlying file descriptor\n\
getpeername() -- return remote address\n\
getsockname() -- return local address\n\
getsockopt(level, optname[, buflen]) -- get socket options\n\
gettimeout() -- return timeout or None\n\
listen(n) -- start listening for incoming connections\n\
makefile([mode, [bufsize]]) -- return a file object for the socket\n\
recv(buflen[, flags]) -- receive data\n\
recvfrom(buflen[, flags]) -- receive data and sender's address\n\
sendall(data[, flags]) -- send all data\n\
send(data[, flags]) -- send data, may not send all of it\n\
sendto(data[, flags], addr) -- send data to a given address\n\
setblocking(0 | 1) -- set or clear the blocking I/O flag\n\
setsockopt(level, optname, value) -- set socket options\n\
settimeout(None | float) -- set or clear the timeout\n\
shutdown(how) -- shut down traffic in one or both directions");


/* Global variable holding the exception type for errors detected
   by this module (but not argument type or memory errors, etc.). */
PyObject *bluetooth_error;
static PyObject *socket_timeout;

/* A forward reference to the socket type object.
   The sock_type variable contains pointers to various functions,
   some of which call new_sockobject(), which uses sock_type, so
   there has to be a circular reference. */
PyTypeObject sock_type;

/* Convenience function to raise an error according to errno
   and return a NULL pointer from a function. */
PyObject *
set_error(void)
{
	return PyErr_SetFromErrno(bluetooth_error);
}


/* Function to perform the setting of socket blocking mode
   internally. block = (1 | 0). */
static int
internal_setblocking(PySocketSockObject *s, int block)
{
	int delay_flag;

	Py_BEGIN_ALLOW_THREADS
	delay_flag = fcntl(s->sock_fd, F_GETFL, 0);
	if (block)
		delay_flag &= (~O_NONBLOCK);
	else
		delay_flag |= O_NONBLOCK;
	fcntl(s->sock_fd, F_SETFL, delay_flag);
	Py_END_ALLOW_THREADS

	/* Since these don't return anything */
	return 1;
}

/* Do a select() on the socket, if necessary (sock_timeout > 0).
   The argument writing indicates the direction.
   This does not raise an exception; we'll let our caller do that
   after they've reacquired the interpreter lock.
   Returns 1 on timeout, 0 otherwise. */
static int
internal_select(PySocketSockObject *s, int writing)
{
	fd_set fds;
	struct timeval tv;
	int n;

	/* Nothing to do unless we're in timeout mode (not non-blocking) */
	if (s->sock_timeout <= 0.0)
		return 0;

	/* Guard against closed socket */
	if (s->sock_fd < 0)
		return 0;

	/* Construct the arguments to select */
	tv.tv_sec = (int)s->sock_timeout;
	tv.tv_usec = (int)((s->sock_timeout - tv.tv_sec) * 1e6);
	FD_ZERO(&fds);
	FD_SET(s->sock_fd, &fds);

	/* See if the socket is ready */
	if (writing)
		n = select(s->sock_fd+1, NULL, &fds, NULL, &tv);
	else
		n = select(s->sock_fd+1, &fds, NULL, NULL, &tv);
	if (n == 0)
		return 1;
	return 0;
}

/* Initialize a new socket object. */

static double defaulttimeout = -1.0; /* Default timeout for new sockets */

PyMODINIT_FUNC
init_sockobject(PySocketSockObject *s,
		int fd, int family, int type, int proto)
{
	s->sock_fd = fd;
	s->sock_family = family;
	s->sock_type = type;
	s->sock_proto = proto;
	s->sock_timeout = defaulttimeout;

	s->errorhandler = &set_error;

	if (defaulttimeout >= 0.0)
		internal_setblocking(s, 0);
}


/* Create a new socket object.
   This just creates the object and initializes it.
   If the creation fails, return NULL and set an exception (implicit
   in NEWOBJ()). */

static PySocketSockObject *
new_sockobject(int fd, int family, int type, int proto)
{
	PySocketSockObject *s;
	s = (PySocketSockObject *)
		PyType_GenericNew(&sock_type, NULL, NULL);
	if (s != NULL)
		init_sockobject(s, fd, family, type, proto);
	return s;
}


/* Create an object representing the given socket address,
   suitable for passing it back to bind(), connect() etc.
   The family field of the sockaddr structure is inspected
   to determine what kind of address it really is. */

/*ARGSUSED*/
static PyObject *
makesockaddr(PySocketSockObject *s, struct sockaddr *addr, int addrlen)
{
	if (addrlen == 0) {
		/* No address -- may be recvfrom() from known socket */
		Py_INCREF(Py_None);
		return Py_None;
	} else {
        char ba_name[18];

        switch(s->sock_proto) {
            case BTPROTO_HCI:
                {
                    return Py_BuildValue("H", 
                            ((struct sockaddr_hci*)(addr))->hci_dev );
                }
            case BTPROTO_L2CAP:
                {
                    struct sockaddr_l2 *a = (struct sockaddr_l2*)addr;
                    ba2str( &a->l2_bdaddr, ba_name );
                    return Py_BuildValue("sH", ba_name, btohs(a->l2_psm) );
                }
            case BTPROTO_RFCOMM:
                {
                    struct sockaddr_rc *a = (struct sockaddr_rc*)addr;
                    ba2str( &a->rc_bdaddr, ba_name );
                    return Py_BuildValue("sB", ba_name, a->rc_channel );
                }
            case BTPROTO_SCO:
                {
                    struct sockaddr_sco *a = (struct sockaddr_sco*)addr;
                    ba2str( &a->sco_bdaddr, ba_name );
                    return Py_BuildValue("s", ba_name);
                }
            default:
                PyErr_SetString(bluetooth_error, 
                        "getsockaddrarg: unknown Bluetooth protocol");
                    return 0;
        }
    }
}


/* Parse a socket address argument according to the socket object's
   address family.  Return 1 if the address was in the proper format,
   0 of not.  The address is returned through addr_ret, its length
   through len_ret. */

static int
getsockaddrarg(PySocketSockObject *s, PyObject *args,
	       struct sockaddr *addr_ret, int *len_ret)
{
    memset(addr_ret, 0, sizeof(struct sockaddr));
    addr_ret->sa_family = AF_BLUETOOTH;

    switch( s->sock_proto )
    {
        case BTPROTO_HCI:
            {
                struct sockaddr_hci *addr = (struct sockaddr_hci*) addr_ret;

                if ( !PyArg_ParseTuple(args, "H", &addr->hci_dev) ) {
                    return 0;
                }

                *len_ret = sizeof(struct sockaddr_hci);
                return 1;
            }
        case BTPROTO_L2CAP:
            {
                struct sockaddr_l2* addr = (struct sockaddr_l2*) addr_ret;
                char *ba_name = 0;

                if ( !PyArg_ParseTuple(args, "sH", &ba_name, &addr->l2_psm) )
                {
                    return 0;
                }

                str2ba( ba_name, &addr->l2_bdaddr );

                // check for a valid PSM
                if( ! ( 0x1 & addr->l2_psm ) ) {
                    PyErr_SetString( PyExc_ValueError, "Invalid PSM");
                    return 0;
                }

                addr->l2_psm = htobs(addr->l2_psm);

                *len_ret = sizeof *addr;
                return 1;
            }

        case BTPROTO_RFCOMM:
            {
                struct sockaddr_rc *addr = (struct sockaddr_rc*) addr_ret;
                char *ba_name = 0;

                if( !PyArg_ParseTuple(args, "sB", &ba_name, &addr->rc_channel) )
                {
                    return 0;
                }

                str2ba( ba_name, &addr->rc_bdaddr );
                *len_ret = sizeof *addr;
                return 1;
            }
        case BTPROTO_SCO:
            {
                struct sockaddr_sco *addr = (struct sockaddr_sco*) addr_ret;
                char *ba_name = 0;

                if( !PyArg_ParseTuple(args, "s", &ba_name) )
                {
                    return 0;
                }

                str2ba( ba_name, &addr->sco_bdaddr);
                *len_ret = sizeof *addr;
                return 1;
            }
        default:
            {
                PyErr_SetString(bluetooth_error, 
                        "getsockaddrarg: unknown Bluetooth protocol");
                return 0;
            }
    }
}


/* Get the address length according to the socket object's address family.
   Return 1 if the family is known, 0 otherwise.  The length is returned
   through len_ret. */

int
getsockaddrlen(PySocketSockObject *s, socklen_t *len_ret)
{
    switch(s->sock_proto)
    {
        case BTPROTO_L2CAP:
            *len_ret = sizeof (struct sockaddr_l2);
            return 1;
        case BTPROTO_RFCOMM:
            *len_ret = sizeof (struct sockaddr_rc);
            return 1;
        case BTPROTO_SCO:
            *len_ret = sizeof (struct sockaddr_sco);
            return 1;
        case BTPROTO_HCI:
            *len_ret = sizeof (struct sockaddr_hci);
            return 1;
        default:
            PyErr_SetString(bluetooth_error, 
                    "getsockaddrlen: unknown bluetooth protocol");
            return 0;
    }
}

int str2uuid( const char *uuid_str, uuid_t *uuid ) 
{
    uint32_t uuid_int[4];
    char *endptr;

    if( strlen( uuid_str ) == 36 ) {
        // Parse uuid128 standard format: 12345678-9012-3456-7890-123456789012
        char buf[9] = { 0 };

        if( uuid_str[8] != '-' && uuid_str[13] != '-' &&
            uuid_str[18] != '-'  && uuid_str[23] != '-' ) {
            return 0;
        }
        // first 8-bytes
        strncpy(buf, uuid_str, 8);
        uuid_int[0] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // second 8-bytes
        strncpy(buf, uuid_str+9, 4);
        strncpy(buf+4, uuid_str+14, 4);
        uuid_int[1] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // third 8-bytes
        strncpy(buf, uuid_str+19, 4);
        strncpy(buf+4, uuid_str+24, 4);
        uuid_int[2] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // fourth 8-bytes
        strncpy(buf, uuid_str+28, 8);
        uuid_int[3] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        if( uuid != NULL ) sdp_uuid128_create( uuid, uuid_int );
    } else if ( strlen( uuid_str ) == 8 ) {
        // 32-bit reserved UUID
        uint32_t i = strtoul( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 8 ) return 0;
        if( uuid != NULL ) sdp_uuid32_create( uuid, i );
    } else if( strlen( uuid_str ) == 4 ) {
        // 16-bit reserved UUID
        int i = strtol( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 4 ) return 0;
        if( uuid != NULL ) sdp_uuid16_create( uuid, i );
    } else {
        return 0;
    }

    return 1;
}

void uuid2str( const uuid_t *uuid, char *dest ) 
{
    if( uuid->type == SDP_UUID16 ) {
        sprintf(dest, "%04X", uuid->value.uuid16 );
    } else if( uuid->type == SDP_UUID32 ) {
        sprintf(dest, "%08X", uuid->value.uuid32 );
    } else if( uuid->type == SDP_UUID128 ) {
        uint32_t *data = (uint32_t*)(&uuid->value.uuid128);
        sprintf(dest, "%08X-%04X-%04X-%04X-%04X%08X",
                ntohl(data[0]), 
                ntohl(data[1])>>16, 
                (ntohl(data[1])<<16)>>16,
                ntohl(data[2])>>16, 
                (ntohl(data[2])<<16)>>16, 
                ntohl(data[3]));
    } 
}

// =================== socket methods ==================== //

/* s.accept() method */

    static PyObject *
sock_accept(PySocketSockObject *s)
{
    char addrbuf[256];
    int newfd;
    socklen_t addrlen;
    PyObject *sock = NULL;
    PyObject *addr = NULL;
    PyObject *res = NULL;
    int timeout;

    if (!getsockaddrlen(s, &addrlen))
        return NULL;
    memset(addrbuf, 0, addrlen);

    newfd = -1;

	Py_BEGIN_ALLOW_THREADS
	timeout = internal_select(s, 0);
	if (!timeout)
		newfd = accept(s->sock_fd, (struct sockaddr *) addrbuf,
			       &addrlen);
	Py_END_ALLOW_THREADS

	if (timeout) {
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}

	if (newfd < 0)
		return s->errorhandler();

	/* Create the new object with unspecified family,
	   to avoid calls to bind() etc. on it. */
	sock = (PyObject *) new_sockobject(newfd,
					   s->sock_family,
					   s->sock_type,
					   s->sock_proto);

	if (sock == NULL) {
		close(newfd);
		goto finally;
	}
	addr = makesockaddr(s, (struct sockaddr *)addrbuf, addrlen);
	if (addr == NULL)
		goto finally;

	res = Py_BuildValue("OO", sock, addr);

finally:
	Py_XDECREF(sock);
	Py_XDECREF(addr);
	return res;
}

PyDoc_STRVAR(accept_doc,
"accept() -> (socket object, address info)\n\
\n\
Wait for an incoming connection.  Return a new socket representing the\n\
connection, and the address of the client.  For L2CAP sockets, the address\n\
is a (host, psm) tuple.  For RFCOMM sockets, the address is a (host, channel)\n\
tuple.  For SCO sockets, the address is just a host.");

/* s.setblocking(flag) method.  Argument:
   False -- non-blocking mode; same as settimeout(0)
   True -- blocking mode; same as settimeout(None)
*/

static PyObject *
sock_setblocking(PySocketSockObject *s, PyObject *arg)
{
	int block;

	block = PyInt_AsLong(arg);
	if (block == -1 && PyErr_Occurred())
		return NULL;

	s->sock_timeout = block ? -1.0 : 0.0;
	internal_setblocking(s, block);

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(setblocking_doc,
"setblocking(flag)\n\
\n\
Set the socket to blocking (flag is true) or non-blocking (false).\n\
setblocking(True) is equivalent to settimeout(None);\n\
setblocking(False) is equivalent to settimeout(0.0).");

/* s.settimeout(timeout) method.  Argument:
   None -- no timeout, blocking mode; same as setblocking(True)
   0.0  -- non-blocking mode; same as setblocking(False)
   > 0  -- timeout mode; operations time out after timeout seconds
   < 0  -- illegal; raises an exception
*/
static PyObject *
sock_settimeout(PySocketSockObject *s, PyObject *arg)
{
	double timeout;

	if (arg == Py_None)
		timeout = -1.0;
	else {
		timeout = PyFloat_AsDouble(arg);
		if (timeout < 0.0) {
			if (!PyErr_Occurred())
				PyErr_SetString(PyExc_ValueError,
						"Timeout value out of range");
			return NULL;
		}
	}

	s->sock_timeout = timeout;
	internal_setblocking(s, timeout < 0.0);

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(settimeout_doc,
"settimeout(timeout)\n\
\n\
Set a timeout on socket operations.  'timeout' can be a float,\n\
giving in seconds, or None.  Setting a timeout of None disables\n\
the timeout feature and is equivalent to setblocking(1).\n\
Setting a timeout of zero is the same as setblocking(0).");

/* s.gettimeout() method.
   Returns the timeout associated with a socket. */
static PyObject *
sock_gettimeout(PySocketSockObject *s)
{
	if (s->sock_timeout < 0.0) {
		Py_INCREF(Py_None);
		return Py_None;
	}
	else
		return PyFloat_FromDouble(s->sock_timeout);
}

PyDoc_STRVAR(gettimeout_doc,
"gettimeout() -> timeout\n\
\n\
Returns the timeout in floating seconds associated with socket \n\
operations. A timeout of None indicates that timeouts on socket \n\
operations are disabled.");

/* s.setsockopt() method.
   With an integer third argument, sets an integer option.
   With a string third argument, sets an option from a buffer;
   use optional built-in module 'struct' to encode the string. */

static PyObject *
sock_setsockopt(PySocketSockObject *s, PyObject *args)
{
	int level;
	int optname;
	int res;
	char *buf;
	int buflen;
	int flag;

	if (PyArg_ParseTuple(args, "iii:setsockopt",
			     &level, &optname, &flag)) {
		buf = (char *) &flag;
		buflen = sizeof flag;
	}
	else {
		PyErr_Clear();
		if (!PyArg_ParseTuple(args, "iis#:setsockopt",
				      &level, &optname, &buf, &buflen))
			return NULL;
	}
	res = setsockopt(s->sock_fd, level, optname, (void *)buf, buflen);
	if (res < 0)
		return s->errorhandler();
	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(setsockopt_doc,
"setsockopt(level, option, value)\n\
\n\
Set a socket option.  See the Unix manual for level and option.\n\
The value argument can either be an integer or a string.");


/* s.getsockopt() method.
   With two arguments, retrieves an integer option.
   With a third integer argument, retrieves a string buffer of that size;
   use optional built-in module 'struct' to decode the string. */

static PyObject *
sock_getsockopt(PySocketSockObject *s, PyObject *args)
{
	int level;
	int optname;
	int res;
	PyObject *buf;
	socklen_t buflen = 0;


	if (!PyArg_ParseTuple(args, "ii|i:getsockopt",
			      &level, &optname, &buflen))
		return NULL;

	if (buflen == 0) {
		int flag = 0;
		socklen_t flagsize = sizeof flag;
		res = getsockopt(s->sock_fd, level, optname,
				 (void *)&flag, &flagsize);
		if (res < 0)
			return s->errorhandler();
		return PyInt_FromLong(flag);
	}
	if (buflen <= 0 || buflen > 1024) {
		PyErr_SetString(bluetooth_error,
				"getsockopt buflen out of range");
		return NULL;
	}
	buf = PyString_FromStringAndSize((char *)NULL, buflen);
	if (buf == NULL)
		return NULL;
	res = getsockopt(s->sock_fd, level, optname,
			 (void *)PyString_AS_STRING(buf), &buflen);
	if (res < 0) {
		Py_DECREF(buf);
		return s->errorhandler();
	}
	_PyString_Resize(&buf, buflen);
	return buf;
}

PyDoc_STRVAR(getsockopt_doc,
"getsockopt(level, option[, buffersize]) -> value\n\
\n\
Get a socket option.  See the Unix manual for level and option.\n\
If a nonzero buffersize argument is given, the return value is a\n\
string of that length; otherwise it is an integer.");

static PyObject *
sock_setl2capsecurity(PySocketSockObject *s, PyObject *args)
{
	int level;
        struct bt_security sec;

	if (! PyArg_ParseTuple(args, "i:setsockopt",
			     &level))
		return NULL;

        memset(&sec, 0, sizeof(sec));
        sec.level = level;

        if (setsockopt(s->sock_fd, SOL_BLUETOOTH, BT_SECURITY, &sec,
                                                        sizeof(sec)) == 0) {
		Py_INCREF(Py_None);
		return Py_None;
	}

        if (errno != ENOPROTOOPT)
		return s->errorhandler();

        int lm_map[] = {
                0,
                L2CAP_LM_AUTH,
                L2CAP_LM_AUTH | L2CAP_LM_ENCRYPT,
                L2CAP_LM_AUTH | L2CAP_LM_ENCRYPT | L2CAP_LM_SECURE,
        }, opt = lm_map[level];

        if (setsockopt(s->sock_fd, SOL_L2CAP, L2CAP_LM, &opt, sizeof(opt)) < 0)
		return s->errorhandler();

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(setl2capsecurity_doc,
"setl2capsecurity(BT_SECURITY_*) -> value\n\
\n\
Sets socket security. Levels are BT_SECURITY_SDP, LOW, MEDIUM\n\
and HIGH.");

/* s.bind(sockaddr) method */

static PyObject *
sock_bind(PySocketSockObject *s, PyObject *addro)
{
	struct sockaddr addr = { 0 };
	int addrlen;
	int res;

	if (!getsockaddrarg(s, addro, &addr, &addrlen))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	res = bind(s->sock_fd, &addr, addrlen);
	Py_END_ALLOW_THREADS
	if (res < 0)
		return s->errorhandler();
	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(bind_doc,
"bind(address)\n\
\n\
Bind the socket to a local address.  address must always be a tuple.\n\
  HCI sockets:    ( device number, )\n\
                  device number should be 0, 1, 2, etc.\n\
  L2CAP sockets:  ( host, psm )\n\
                  host should be an address e.g. \"01:23:45:67:89:ab\"\n\
                  psm should be an unsigned integer\n\
  RFCOMM sockets: ( host, channel )\n\
  SCO sockets:    ( host )\n\
");

/* s.close() method.
   Set the file descriptor to -1 so operations tried subsequently
   will surely fail. */

static PyObject *
sock_close(PySocketSockObject *s)
{
	int fd;

	if ((fd = s->sock_fd) != -1) {
		s->sock_fd = -1;
		Py_BEGIN_ALLOW_THREADS
		(void) close(fd);
		Py_END_ALLOW_THREADS
	}

    if( s->sdp_session ) {
        sdp_close( s->sdp_session );
        s->sdp_record_handle = 0;
        s->sdp_session = NULL;
    }

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(close_doc,
"close()\n\
\n\
Close the socket.  It cannot be used after this call.");

static int
internal_connect(PySocketSockObject *s, struct sockaddr *addr, int addrlen,
		 int *timeoutp)
{
	int res, timeout;

	timeout = 0;
	res = connect(s->sock_fd, addr, addrlen);

	if (s->sock_timeout > 0.0) {
		if (res < 0 && errno == EINPROGRESS) {
			timeout = internal_select(s, 1);
			res = connect(s->sock_fd, addr, addrlen);
			if (res < 0 && errno == EISCONN)
				res = 0;
		}
	}

	if (res < 0)
		res = errno;

	*timeoutp = timeout;

	return res;
}

/* s.connect(sockaddr) method */

static PyObject *
sock_connect(PySocketSockObject *s, PyObject *addro)
{
	struct sockaddr addr = { 0 };
	int addrlen;
	int res;
	int timeout;

	if (!getsockaddrarg(s, addro, &addr, &addrlen))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	res = internal_connect(s, &addr, addrlen, &timeout);
	Py_END_ALLOW_THREADS

	if (timeout) {
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (res != 0)
		return s->errorhandler();
	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(connect_doc,
"connect(address)\n\
\n\
Connect the socket to a remote address. For L2CAP sockets, the address is a \n\
(host,psm) tuple.  For RFCOMM sockets, the address is a (host,channel) tuple.\n\
For SCO sockets, the address is just the host.");


/* s.connect_ex(sockaddr) method */

static PyObject *
sock_connect_ex(PySocketSockObject *s, PyObject *addro)
{
	struct sockaddr addr = { 0 };
	int addrlen;
	int res;
	int timeout;

	if (!getsockaddrarg(s, addro, &addr, &addrlen))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	res = internal_connect(s, &addr, addrlen, &timeout);
	Py_END_ALLOW_THREADS

	return PyInt_FromLong((long) res);
}

PyDoc_STRVAR(connect_ex_doc,
"connect_ex(address) -> errno\n\
\n\
This is like connect(address), but returns an error code (the errno value)\n\
instead of raising an exception when an error occurs.");


/* s.fileno() method */

static PyObject *
sock_fileno(PySocketSockObject *s)
{
	return PyInt_FromLong((long) s->sock_fd);
}

PyDoc_STRVAR(fileno_doc,
"fileno() -> integer\n\
\n\
Return the integer file descriptor of the socket.");


#ifndef NO_DUP
/* s.dup() method */

static PyObject *
sock_dup(PySocketSockObject *s)
{
	int newfd;
	PyObject *sock;

	newfd = dup(s->sock_fd);
	if (newfd < 0)
		return s->errorhandler();
	sock = (PyObject *) new_sockobject(newfd,
					   s->sock_family,
					   s->sock_type,
					   s->sock_proto);
	if (sock == NULL)
		close(newfd);
	return sock;
}

PyDoc_STRVAR(dup_doc,
"dup() -> socket object\n\
\n\
Return a new socket object connected to the same system resource.");

#endif


/* s.getsockname() method */

static PyObject *
sock_getsockname(PySocketSockObject *s)
{
	char addrbuf[256];
	int res;
	socklen_t addrlen;

	if (!getsockaddrlen(s, &addrlen))
		return NULL;
	memset(addrbuf, 0, addrlen);
	Py_BEGIN_ALLOW_THREADS
	res = getsockname(s->sock_fd, (struct sockaddr *) addrbuf, &addrlen);
	Py_END_ALLOW_THREADS
	if (res < 0)
		return s->errorhandler();
	return makesockaddr(s, (struct sockaddr *) addrbuf, addrlen);
}

PyDoc_STRVAR(getsockname_doc,
"getsockname() -> address info\n\
\n\
Return the address of the local endpoint.");


/* s.getpeername() method */

static PyObject *
sock_getpeername(PySocketSockObject *s)
{
	char addrbuf[256];
	int res;
	socklen_t addrlen;

	if (!getsockaddrlen(s, &addrlen))
		return NULL;
	memset(addrbuf, 0, addrlen);
	Py_BEGIN_ALLOW_THREADS
	res = getpeername(s->sock_fd, (struct sockaddr *) addrbuf, &addrlen);
	Py_END_ALLOW_THREADS
	if (res < 0)
		return s->errorhandler();
	return makesockaddr(s, (struct sockaddr *) addrbuf, addrlen);
}

PyDoc_STRVAR(getpeername_doc,
"getpeername() -> address info\n\
\n\
Return the address of the remote endpoint.  For HCI sockets, the address is a\n\
device number (0, 1, 2, etc).  For L2CAP sockets, the address is a \n\
(host,psm) tuple.  For RFCOMM sockets, the address is a (host,channel) tuple.\n\
For SCO sockets, the address is just the host.");


/* s.listen(n) method */

static PyObject *
sock_listen(PySocketSockObject *s, PyObject *arg)
{
	int backlog;
	int res;

	backlog = PyInt_AsLong(arg);
	if (backlog == -1 && PyErr_Occurred())
		return NULL;
	Py_BEGIN_ALLOW_THREADS
	if (backlog < 1)
		backlog = 1;
	res = listen(s->sock_fd, backlog);
	Py_END_ALLOW_THREADS
	if (res < 0)
		return s->errorhandler();
	Py_INCREF(Py_None);

    s->is_listening_socket = 1;
	return Py_None;
}

PyDoc_STRVAR(listen_doc,
"listen(backlog)\n\
\n\
Enable a server to accept connections.  The backlog argument must be at\n\
least 1; it specifies the number of unaccepted connection that the system\n\
will allow before refusing new connections.");


#ifndef NO_DUP
/* s.makefile(mode) method.
   Create a new open file object referring to a dupped version of
   the socket's file descriptor.  (The dup() call is necessary so
   that the open file and socket objects may be closed independent
   of each other.)
   The mode argument specifies 'r' or 'w' passed to fdopen(). */

static PyObject *
sock_makefile(PySocketSockObject *s, PyObject *args)
{
	extern int fclose(FILE *);
	char *mode = "r";
	int bufsize = -1;
	int fd;
	FILE *fp;
	PyObject *f;

	if (!PyArg_ParseTuple(args, "|si:makefile", &mode, &bufsize))
		return NULL;
	if ((fd = dup(s->sock_fd)) < 0 || (fp = fdopen(fd, mode)) == NULL)
	{
		if (fd >= 0)
			close(fd);
		return s->errorhandler();
	}
	f = PyFile_FromFile(fp, "<socket>", mode, fclose);
	if (f != NULL)
		PyFile_SetBufSize(f, bufsize);
	return f;
}

PyDoc_STRVAR(makefile_doc,
"makefile([mode[, buffersize]]) -> file object\n\
\n\
Return a regular file object corresponding to the socket.\n\
The mode and buffersize arguments are as for the built-in open() function.");

#endif /* NO_DUP */


/* s.recv(nbytes [,flags]) method */

static PyObject *
sock_recv(PySocketSockObject *s, PyObject *args)
{
	int len, n = 0, flags = 0, timeout;
	PyObject *buf;

	if (!PyArg_ParseTuple(args, "i|i:recv", &len, &flags))
		return NULL;

	if (len < 0) {
		PyErr_SetString(PyExc_ValueError,
				"negative buffersize in recv");
		return NULL;
	}

	buf = PyString_FromStringAndSize((char *) 0, len);
	if (buf == NULL)
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	timeout = internal_select(s, 0);
	if (!timeout)
		n = recv(s->sock_fd, PyString_AS_STRING(buf), len, flags);
	Py_END_ALLOW_THREADS

	if (timeout) {
		Py_DECREF(buf);
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (n < 0) {
		Py_DECREF(buf);
		return s->errorhandler();
	}
	if (n != len)
		_PyString_Resize(&buf, n);
	return buf;
}

PyDoc_STRVAR(recv_doc,
"recv(buffersize[, flags]) -> data\n\
\n\
Receive up to buffersize bytes from the socket.  For the optional flags\n\
argument, see the Unix manual.  When no data is available, block until\n\
at least one byte is available or until the remote end is closed.  When\n\
the remote end is closed and all data is read, return the empty string.");


/* s.recvfrom(nbytes [,flags]) method */

static PyObject *
sock_recvfrom(PySocketSockObject *s, PyObject *args)
{
	char addrbuf[256];
	PyObject *buf = NULL;
	PyObject *addr = NULL;
	PyObject *ret = NULL;
	int len, n = 0, flags = 0, timeout;
	socklen_t addrlen;

	if (!PyArg_ParseTuple(args, "i|i:recvfrom", &len, &flags))
		return NULL;

	if (!getsockaddrlen(s, &addrlen))
		return NULL;
	buf = PyString_FromStringAndSize((char *) 0, len);
	if (buf == NULL)
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	memset(addrbuf, 0, addrlen);
	timeout = internal_select(s, 0);
	if (!timeout)
		n = recvfrom(s->sock_fd, PyString_AS_STRING(buf), len, flags,
			     (void *)addrbuf, &addrlen
			);
	Py_END_ALLOW_THREADS

	if (timeout) {
		Py_DECREF(buf);
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (n < 0) {
		Py_DECREF(buf);
		return s->errorhandler();
	}

	if (n != len && _PyString_Resize(&buf, n) < 0)
		return NULL;

	if (!(addr = makesockaddr(s, (struct sockaddr *)addrbuf,
				  addrlen)))
		goto finally;

	ret = Py_BuildValue("OO", buf, addr);

finally:
	Py_XDECREF(addr);
	Py_XDECREF(buf);
	return ret;
}

PyDoc_STRVAR(recvfrom_doc,
"recvfrom(buffersize[, flags]) -> (data, address info)\n\
\n\
Like recv(buffersize, flags) but also return the sender's address info.");

/* s.send(data [,flags]) method */

static PyObject *
sock_send(PySocketSockObject *s, PyObject *args)
{
	char *buf;
	int len, n = 0, flags = 0, timeout;

	if (!PyArg_ParseTuple(args, "s#|i:send", &buf, &len, &flags))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	timeout = internal_select(s, 1);
	if (!timeout)
		n = send(s->sock_fd, buf, len, flags);
	Py_END_ALLOW_THREADS

	if (timeout) {
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (n < 0)
		return s->errorhandler();
	return PyInt_FromLong((long)n);
}

PyDoc_STRVAR(send_doc,
"send(data[, flags]) -> count\n\
\n\
Send a data string to the socket.  For the optional flags\n\
argument, see the Unix manual.  Return the number of bytes\n\
sent; this may be less than len(data) if the network is busy.");


/* s.sendall(data [,flags]) method */

static PyObject *
sock_sendall(PySocketSockObject *s, PyObject *args)
{
	char *buf;
	int len, n = 0, flags = 0, timeout;

	if (!PyArg_ParseTuple(args, "s#|i:sendall", &buf, &len, &flags))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	do {
		timeout = internal_select(s, 1);
		if (timeout)
			break;
		n = send(s->sock_fd, buf, len, flags);
		if (n < 0)
			break;
		buf += n;
		len -= n;
	} while (len > 0);
	Py_END_ALLOW_THREADS

	if (timeout) {
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (n < 0)
		return s->errorhandler();

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(sendall_doc,
"sendall(data[, flags])\n\
\n\
Send a data string to the socket.  For the optional flags\n\
argument, see the Unix manual.  This calls send() repeatedly\n\
until all data is sent.  If an error occurs, it's impossible\n\
to tell how much data has been sent.");


/* s.sendto(data, [flags,] sockaddr) method */

static PyObject *
sock_sendto(PySocketSockObject *s, PyObject *args)
{
	PyObject *addro;
	char *buf;
	struct sockaddr addr = { 0 };
	int addrlen, len, n = 0, flags, timeout;

	flags = 0;
	if (!PyArg_ParseTuple(args, "s#O:sendto", &buf, &len, &addro)) {
		PyErr_Clear();
		if (!PyArg_ParseTuple(args, "s#iO:sendto",
				      &buf, &len, &flags, &addro))
			return NULL;
	}

	if (!getsockaddrarg(s, addro, &addr, &addrlen))
		return NULL;

	Py_BEGIN_ALLOW_THREADS
	timeout = internal_select(s, 1);
	if (!timeout)
		n = sendto(s->sock_fd, buf, len, flags, &addr, addrlen);
	Py_END_ALLOW_THREADS

	if (timeout) {
		PyErr_SetString(socket_timeout, "timed out");
		return NULL;
	}
	if (n < 0)
		return s->errorhandler();
	return PyInt_FromLong((long)n);
}

PyDoc_STRVAR(sendto_doc,
"sendto(data[, flags], address) -> count\n\
\n\
Like send(data, flags) but allows specifying the destination address.\n\
For IP sockets, the address is a pair (hostaddr, port).");


/* s.shutdown(how) method */

static PyObject *
sock_shutdown(PySocketSockObject *s, PyObject *arg)
{
	int how;
	int res;

	how = PyInt_AsLong(arg);
	if (how == -1 && PyErr_Occurred())
		return NULL;
	Py_BEGIN_ALLOW_THREADS
	res = shutdown(s->sock_fd, how);
	Py_END_ALLOW_THREADS
	if (res < 0)
		return s->errorhandler();
	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(shutdown_doc,
"shutdown(flag)\n\
\n\
Shut down the reading side of the socket (flag == 0), the writing side\n\
of the socket (flag == 1), or both ends (flag == 2).");

/* s.getsockid() method */

static PyObject *
sock_getsockid(PySocketSockObject *s, PyObject *arg)
{
    int dd;
    dd = s->sock_fd;
    return Py_BuildValue("i", dd);
}



/* List of methods for socket objects */

static PyMethodDef sock_methods[] = {
	{"accept",	(PyCFunction)sock_accept, METH_NOARGS,
			accept_doc},
	{"bind",	(PyCFunction)sock_bind, METH_O,
			bind_doc},
	{"close",	(PyCFunction)sock_close, METH_NOARGS,
			close_doc},
	{"connect",	(PyCFunction)sock_connect, METH_O,
			connect_doc},
	{"connect_ex",	(PyCFunction)sock_connect_ex, METH_O,
			connect_ex_doc},
#ifndef NO_DUP
	{"dup",		(PyCFunction)sock_dup, METH_NOARGS,
			dup_doc},
#endif
	{"fileno",	(PyCFunction)sock_fileno, METH_NOARGS,
			fileno_doc},
	{"getpeername",	(PyCFunction)sock_getpeername,
			METH_NOARGS, getpeername_doc},
    {"getsockid", (PyCFunction)sock_getsockid,
            METH_NOARGS, "Gets socket id."},
	{"getsockname",	(PyCFunction)sock_getsockname,
			METH_NOARGS, getsockname_doc},
	{"getsockopt",	(PyCFunction)sock_getsockopt, METH_VARARGS,
			getsockopt_doc},
	{"listen",	(PyCFunction)sock_listen, METH_O,
			listen_doc},
#ifndef NO_DUP
	{"makefile",	(PyCFunction)sock_makefile, METH_VARARGS,
			makefile_doc},
#endif
	{"recv",	(PyCFunction)sock_recv, METH_VARARGS,
			recv_doc},
	{"recvfrom",	(PyCFunction)sock_recvfrom, METH_VARARGS,
			recvfrom_doc},
	{"send",	(PyCFunction)sock_send, METH_VARARGS,
			send_doc},
	{"sendall",	(PyCFunction)sock_sendall, METH_VARARGS,
			sendall_doc},
	{"sendto",	(PyCFunction)sock_sendto, METH_VARARGS,
			sendto_doc},
	{"setblocking",	(PyCFunction)sock_setblocking, METH_O,
			setblocking_doc},
	{"settimeout", (PyCFunction)sock_settimeout, METH_O,
			settimeout_doc},
	{"gettimeout", (PyCFunction)sock_gettimeout, METH_NOARGS,
			gettimeout_doc},
	{"setsockopt",	(PyCFunction)sock_setsockopt, METH_VARARGS,
			setsockopt_doc},
	{"setl2capsecurity",	(PyCFunction)sock_setl2capsecurity, METH_VARARGS,
			setl2capsecurity_doc},
	{"shutdown",	(PyCFunction)sock_shutdown, METH_O,
			shutdown_doc},
	{NULL,			NULL}		/* sentinel */
};


/* Deallocate a socket object in response to the last Py_DECREF().
   First close the file description. */

static void
sock_dealloc(PySocketSockObject *s)
{
    // close the OS file descriptor
	if (s->sock_fd != -1) {
        Py_BEGIN_ALLOW_THREADS
		close(s->sock_fd);
        Py_END_ALLOW_THREADS
    }
    
    if( s->sdp_session ) {
        sdp_close( s->sdp_session );
        s->sdp_record_handle = 0;
        s->sdp_session = NULL;
    }

	s->ob_type->tp_free((PyObject *)s);
}


static PyObject *
sock_repr(PySocketSockObject *s)
{
	char buf[512];
#if SIZEOF_SOCKET_T > SIZEOF_LONG
	if (s->sock_fd > LONG_MAX) {
		/* this can occur on Win64, and actually there is a special
		   ugly printf formatter for decimal pointer length integer
		   printing, only bother if necessary*/
		PyErr_SetString(PyExc_OverflowError,
				"no printf formatter to display "
				"the socket descriptor in decimal");
		return NULL;
	}
#endif
	PyOS_snprintf(
		buf, sizeof(buf),
		"<socket object, fd=%ld, family=%d, type=%d, protocol=%d>",
		(long)s->sock_fd, s->sock_family,
		s->sock_type,
		s->sock_proto);
	return PyString_FromString(buf);
}


/* Create a new, uninitialized socket object. */

static PyObject *
sock_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *new;

	new = type->tp_alloc(type, 0);
	if (new != NULL) {
		((PySocketSockObject *)new)->sock_fd = -1;
		((PySocketSockObject *)new)->sock_timeout = -1.0;
		((PySocketSockObject *)new)->errorhandler = &set_error;
	}
	return new;
}


/* Initialize a new socket object. */

/*ARGSUSED*/
static int
sock_initobj(PyObject *self, PyObject *args, PyObject *kwds)
{
	PySocketSockObject *s = (PySocketSockObject *)self;
	int fd;
	int family = AF_BLUETOOTH, type = SOCK_STREAM, proto = BTPROTO_RFCOMM;
	static char *keywords[] = {"proto", 0};

	if (!PyArg_ParseTupleAndKeywords(args, kwds,
					 "|i:socket", keywords,
					 &proto))
		return -1;

    switch(proto) {
        case BTPROTO_HCI:
            type = SOCK_RAW;
            break;
        case BTPROTO_L2CAP:
            type = SOCK_SEQPACKET;
            break;
        case BTPROTO_RFCOMM:
            type = SOCK_STREAM;
            break;
        case BTPROTO_SCO:
            type = SOCK_SEQPACKET;
            break;
    }

	Py_BEGIN_ALLOW_THREADS
	fd = socket(family, type, proto);
	Py_END_ALLOW_THREADS

	if (fd < 0)
	{
		set_error();
		return -1;
	}
	init_sockobject(s, fd, family, type, proto);
	/* From now on, ignore SIGPIPE and let the error checking
	   do the work. */
#ifdef SIGPIPE
	(void) signal(SIGPIPE, SIG_IGN);
#endif

	return 0;

}


/* Type object for socket objects. */

PyTypeObject sock_type = {
	PyObject_HEAD_INIT(0)	/* Must fill in type value later */
	0,					/* ob_size */
	"_bluetooth.btsocket",			/* tp_name */
	sizeof(PySocketSockObject),		/* tp_basicsize */
	0,					/* tp_itemsize */
	(destructor)sock_dealloc,		/* tp_dealloc */
	0,					/* tp_print */
	0,					/* tp_getattr */
	0,					/* tp_setattr */
	0,					/* tp_compare */
	(reprfunc)sock_repr,			/* tp_repr */
	0,					/* tp_as_number */
	0,					/* tp_as_sequence */
	0,					/* tp_as_mapping */
	0,					/* tp_hash */
	0,					/* tp_call */
	0,					/* tp_str */
	PyObject_GenericGetAttr,		/* tp_getattro */
	0,					/* tp_setattro */
	0,					/* tp_as_buffer */
	Py_TPFLAGS_DEFAULT | Py_TPFLAGS_BASETYPE, /* tp_flags */
	sock_doc,				/* tp_doc */
	0,					/* tp_traverse */
	0,					/* tp_clear */
	0,					/* tp_richcompare */
	0,					/* tp_weaklistoffset */
	0,					/* tp_iter */
	0,					/* tp_iternext */
	sock_methods,				/* tp_methods */
	0,					/* tp_members */
	0,					/* tp_getset */
	0,					/* tp_base */
	0,					/* tp_dict */
	0,					/* tp_descr_get */
	0,					/* tp_descr_set */
	0,					/* tp_dictoffset */
	sock_initobj,				/* tp_init */
	PyType_GenericAlloc,			/* tp_alloc */
	sock_new,				/* tp_new */
	PyObject_Del,				/* tp_free */
};


#ifndef NO_DUP
/* Create a socket object from a numeric file description.
   Useful e.g. if stdin is a socket.
   Additional arguments as for socket(). */

/*ARGSUSED*/
static PyObject *
bt_fromfd(PyObject *self, PyObject *args)
{
	PySocketSockObject *s;
	int fd;
	int family, type, proto = 0;
	if (!PyArg_ParseTuple(args, "iii|i:fromfd",
			      &fd, &family, &type, &proto))
		return NULL;
	/* Dup the fd so it and the socket can be closed independently */
	fd = dup(fd);
	if (fd < 0)
		return set_error();
	s = new_sockobject(fd, family, type, proto);
	/* From now on, ignore SIGPIPE and let the error checking
	   do the work. */
#ifdef SIGPIPE
	(void) signal(SIGPIPE, SIG_IGN);
#endif
	return (PyObject *) s;
}

PyDoc_STRVAR(bt_fromfd_doc,
"fromfd(fd, family, type[, proto]) -> socket object\n\
\n\
Create a socket object from the given file descriptor.\n\
The remaining arguments are the same as for socket().");

#endif /* NO_DUP */


static PyObject *
bt_btohs(PyObject *self, PyObject *args)
{
	int x1, x2;

	if (!PyArg_ParseTuple(args, "i:btohs", &x1)) {
		return NULL;
	}
	x2 = (int)btohs((short)x1);
	return PyInt_FromLong(x2);
}

PyDoc_STRVAR(bt_btohs_doc,
"btohs(integer) -> integer\n\
\n\
Convert a 16-bit integer from bluetooth to host byte order.");


static PyObject *
bt_btohl(PyObject *self, PyObject *args)
{
	unsigned long x;
	PyObject *arg;
	
	if (!PyArg_ParseTuple(args, "O:btohl", &arg)) {
		return NULL;
	}

	if (PyInt_Check(arg)) {
		x = PyInt_AS_LONG(arg);
		if (x == (unsigned long) -1 && PyErr_Occurred())
			return NULL;
	}
	else if (PyLong_Check(arg)) {
		x = PyLong_AsUnsignedLong(arg);
		if (x == (unsigned long) -1 && PyErr_Occurred())
			return NULL;
#if SIZEOF_LONG > 4
		{
			unsigned long y;
			/* only want the trailing 32 bits */
			y = x & 0xFFFFFFFFUL;
			if (y ^ x)
				return PyErr_Format(PyExc_OverflowError,
					    "long int larger than 32 bits");
			x = y;
		}
#endif
	}
	else
		return PyErr_Format(PyExc_TypeError,
				    "expected int/long, %s found",
				    arg->ob_type->tp_name);
	if (x == (unsigned long) -1 && PyErr_Occurred())
		return NULL;
	return PyInt_FromLong(btohl(x));
}

PyDoc_STRVAR(bt_btohl_doc,
"btohl(integer) -> integer\n\
\n\
Convert a 32-bit integer from bluetooth to host byte order.");


static PyObject *
bt_htobs(PyObject *self, PyObject *args)
{
	unsigned long x1, x2;

	if (!PyArg_ParseTuple(args, "i:htobs", &x1)) {
		return NULL;
	}
	x2 = (int)htobs((short)x1);
	return PyInt_FromLong(x2);
}

PyDoc_STRVAR(bt_htobs_doc,
"htobs(integer) -> integer\n\
\n\
Convert a 16-bit integer from host to bluetooth byte order.");


static PyObject *
bt_htobl(PyObject *self, PyObject *args)
{
	unsigned long x;
	PyObject *arg;

	if (!PyArg_ParseTuple(args, "O:htobl", &arg)) {
		return NULL;
	}

	if (PyInt_Check(arg)) {
		x = PyInt_AS_LONG(arg);
		if (x == (unsigned long) -1 && PyErr_Occurred())
			return NULL;
	}
	else if (PyLong_Check(arg)) {
		x = PyLong_AsUnsignedLong(arg);
		if (x == (unsigned long) -1 && PyErr_Occurred())
			return NULL;
#if SIZEOF_LONG > 4
		{
			unsigned long y;
			/* only want the trailing 32 bits */
			y = x & 0xFFFFFFFFUL;
			if (y ^ x)
				return PyErr_Format(PyExc_OverflowError,
					    "long int larger than 32 bits");
			x = y;
		}
#endif
	}
	else
		return PyErr_Format(PyExc_TypeError,
				    "expected int/long, %s found",
				    arg->ob_type->tp_name);
	return PyInt_FromLong(htobl(x));
}

//static PyObject *
//bt_get_available_port_number( PyObject *self, PyObject *arg )
//{
//	int protocol = -1;
//    int s;
//
//	protocol = PyInt_AsLong(arg);
//
//	if (protocol == -1 && PyErr_Occurred())
//		return NULL;
//
//    switch(protocol) {
//        case BTPROTO_RFCOMM:
//            {
//                struct sockaddr_rc sockaddr = { 0 };
//                int s, psm;
//                s = socket( AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM );
//
//                sockaddr.rc_family = AF_BLUETOOTH;
//                bacppy( &sockaddr.rc_bdaddr, BDADDR_ANY 
//            }
//            break;
//        case BTPROTO_L2CAP:
//            {
//    loc_addr.l2_family = AF_BLUETOOTH;
//    bacpy( &loc_addr.l2_bdaddr, BDADDR_ANY );
//    loc_addr.l2_psm = htobs(0x1001);
//
//            }
//            break;
//        default:
//            {
//                PyErr_SetString( PyExc_ValueError, 
//                        "protocol must be either RFCOMM or L2CAP" );
//                return 0;
//            }
//            break;
//    }
//    Py_INCREF( Py_None );
//    return Py_None;
//}

PyDoc_STRVAR(bt_htobl_doc,
"htobl(integer) -> integer\n\
\n\
Convert a 32-bit integer from host to bluetooth byte order.");

/* Python API to getting and setting the default timeout value. */

static PyObject *
bt_getdefaulttimeout(PyObject *self)
{
	if (defaulttimeout < 0.0) {
		Py_INCREF(Py_None);
		return Py_None;
	}
	else
		return PyFloat_FromDouble(defaulttimeout);
}

PyDoc_STRVAR(bt_getdefaulttimeout_doc,
"getdefaulttimeout() -> timeout\n\
\n\
Returns the default timeout in floating seconds for new socket objects.\n\
A value of None indicates that new socket objects have no timeout.\n\
When the socket module is first imported, the default is None.");

static PyObject *
bt_setdefaulttimeout(PyObject *self, PyObject *arg)
{
	double timeout;

	if (arg == Py_None)
		timeout = -1.0;
	else {
		timeout = PyFloat_AsDouble(arg);
		if (timeout < 0.0) {
			if (!PyErr_Occurred())
				PyErr_SetString(PyExc_ValueError,
						"Timeout value out of range");
			return NULL;
		}
	}

	defaulttimeout = timeout;

	Py_INCREF(Py_None);
	return Py_None;
}

PyDoc_STRVAR(bt_setdefaulttimeout_doc,
"setdefaulttimeout(timeout)\n\
\n\
Set the default timeout in floating seconds for new socket objects.\n\
A value of None indicates that new socket objects have no timeout.\n\
When the socket module is first imported, the default is None.");

/*
 * ----------------------------------------------------------------------
 *  HCI Section   (Calvin)
 *  
 *  This section provides the socket methods for calling HCI commands.
 *  These commands may be called statically, and implementation is
 *  independent from the rest of the module (except for bt_methods[]).
 *
 * ----------------------------------------------------------------------
 *  
 */

/*
 * params:  (int) device number
 * effect: opens and binds a new HCI socket
 * return: a PySocketSockObject, or NULL on failure
 */
static PyObject *
bt_hci_open_dev(PyObject *self, PyObject *args)
{
    int dev = -1, fd;
	PySocketSockObject *s = NULL;
    
    if ( !PyArg_ParseTuple(args, "|i", &dev) )
    {
        return NULL;
    }

    // if the device was not specified, just use the first available bt device
    if (dev < 0) {
        dev = hci_get_route(NULL);
    }

    if (dev < 0) {
        PyErr_SetString(bluetooth_error, "no available bluetoot devices");
        return 0;
    }
    
    Py_BEGIN_ALLOW_THREADS
    fd = hci_open_dev(dev);
    Py_END_ALLOW_THREADS

	s = (PySocketSockObject *)PyType_GenericNew(&sock_type, NULL, NULL);
	if (s != NULL) init_sockobject(s, fd, AF_BLUETOOTH, SOCK_RAW, BTPROTO_HCI);

	return (PyObject*)s;
}

PyDoc_STRVAR(bt_hci_open_dev_doc, "hci_open_dev");

/*
 * params: (int) device number
 * effect: closes an HCI socket
 */
static PyObject *
bt_hci_close_dev(PyObject *self, PyObject *args)
{
    int dev, err;
    
    if ( !PyArg_ParseTuple(args, "i", &dev) )
    {
        return NULL;
    }

    Py_BEGIN_ALLOW_THREADS
    err = hci_close_dev(dev);
    Py_END_ALLOW_THREADS

    if( err < 0 ) return set_error();
    
    Py_INCREF(Py_None);
    return Py_None;
}

PyDoc_STRVAR(bt_hci_close_dev_doc, 
"hci_close_dev(dev_id)\n\
\n\
closes the specified device id.  Note:  device id is NOT a btoscket.\n\
You can also use btsocket.close() to close a specific socket.");

/*
 * params: (int) socket fd, (uint_16) ogf control bits
 *         (uint_16) ocf control bits, (struct) command params
 * effect: executes command described by the OGF and OCF bits
 *          (see bluetooth/hci.h)
 * return: (int) 0 on success, -1 on failure
 */
static PyObject *
bt_hci_send_cmd(PyObject *self, PyObject *args)
{
    PySocketSockObject *socko = NULL;
    int err, plen = 0;
    uint16_t ogf, ocf;
    char *param = NULL;
    int dd = 0;
    
    if ( !PyArg_ParseTuple(args, "OHH|s#", &socko, &ogf, &ocf, &param, &plen)) {
        return NULL;
    }

    dd = socko->sock_fd;

    Py_BEGIN_ALLOW_THREADS
    err = hci_send_cmd(dd, ogf, ocf, plen, (void*)param);
    Py_END_ALLOW_THREADS

    if( err ) return socko->errorhandler();

    return Py_BuildValue("i", err);
}

PyDoc_STRVAR(bt_hci_send_cmd_doc, 
"hci_send_command(sock, ogf, ocf, params)\n\
\n\
Transmits the specified HCI command to the socket.\n\
    sock     - the btoscket object to use\n\
    ogf, pcf - see bluetooth specification\n\
    params   - packed command parameters (use the struct module to do this)");

static PyObject *
bt_hci_send_req(PyObject *self, PyObject *args, PyObject *kwds)
{
    PySocketSockObject *socko = NULL;
    int err;
    int to=0;
    char rparam[256];
    struct hci_request req = { 0 };
    int dd = 0;

    static char *keywords[] = { "sock", "ogf", "ocf", "event", "rlen", "params",
        "timeout", 0 };

    if( !PyArg_ParseTupleAndKeywords(args, kwds, "OHHii|s#i", keywords,
                &socko, &req.ogf, &req.ocf, &req.event, &req.rlen, 
                &req.cparam, &req.clen, &to) )
        return 0;

    req.rparam = rparam;
    dd = socko->sock_fd;

    Py_BEGIN_ALLOW_THREADS
    err = hci_send_req( dd, &req, to );
    Py_END_ALLOW_THREADS

    if( err< 0 ) return socko->errorhandler();

    return Py_BuildValue("s#", rparam, req.rlen);
}
PyDoc_STRVAR(bt_hci_send_req_doc,
"hci_send_req(sock, ogf, ocf, event, rlen, params=None, timeout=0)\n\
\n\
Transmits a HCI cmomand to the socket and waits for the specified event.\n\
   sock      - the btsocket object\n\
   ogf, ocf  - see bluetooth specification\n\
   event     - the event to wait for.  Probably one of EVT_*\n\
   rlen      - the size of the returned packet to expect.  This must be\n\
               specified since bt won't know how much data to expect\n\
               otherwise\n\
    params   - the command parameters\n\
    timeout  - timeout, in milliseconds");


static PyObject*
bt_hci_inquiry(PyObject *self, PyObject *args, PyObject *kwds)
{
    int i, err;
    int dev_id = 0;
    int length = 8;
    int flush = 1;
    int flags = 0;
    char ba_name[19];
    inquiry_info *info = NULL;
    PySocketSockObject *socko = NULL;
    struct hci_inquiry_req *ir;
    char buf[sizeof(*ir) + sizeof(inquiry_info) * 250];

	PyObject *rtn_list = (PyObject *)NULL;

	static char *keywords[] = {"sock", "duration", "flush_cache", 0};

    if( !PyArg_ParseTupleAndKeywords(args, kwds, "O|ii", keywords,
                &socko, &length, &flush) )
    {
        return 0;
    }

    flags |= (flush)?IREQ_CACHE_FLUSH:0;


    ir = (struct hci_inquiry_req*)buf;
    ir->dev_id  = dev_id;
    ir->num_rsp = 250;
    ir->length  = length;
    ir->flags   = flags;

    ir->lap[0] = 0x33;
    ir->lap[1] = 0x8b;
    ir->lap[2] = 0x9e;

    Py_BEGIN_ALLOW_THREADS
    err = ioctl(socko->sock_fd, HCIINQUIRY, (unsigned long) buf);
    Py_END_ALLOW_THREADS

    if( err < 0 ) return socko->errorhandler();

    info = (inquiry_info*)(buf + sizeof(*ir));

    if( (rtn_list = PyList_New(0)) == NULL ) return 0;

    memset( ba_name, 0, sizeof(ba_name) );
    // fill in the list with the discovered bluetooth addresses
    for(i=0;i<ir->num_rsp;i++) {
        PyObject * list_entry = (PyObject *)NULL;
        int err;

        ba2str( &(info+i)->bdaddr, ba_name );
        
        list_entry = PyString_FromString( ba_name );
        err = PyList_Append( rtn_list, list_entry );
        Py_DECREF( list_entry );
        if (err) {
            Py_XDECREF( rtn_list );
            return NULL;
        }
    }

    return rtn_list;
}

PyDoc_STRVAR(bt_hci_inquiry_doc, 
"hci_inquiry(dev_id=0, duration=8, flush_cache=True\n\
\n\
Performs a device inquiry using the specified device (usually 0 or 1).\n\
The inquiry will last 1.28 * duration seconds.  If flush_cache is True, then\n\
previously discovered devices will not be returned in the inquiry.)");


static PyObject*
bt_hci_read_remote_name(PyObject *self, PyObject *args, PyObject *kwds)
{
    char *addr = NULL;
    bdaddr_t ba;
    int timeout = 5192;
    static char name[249];
    PySocketSockObject *socko = NULL;
    int dd = 0, err = 0;

	static char *keywords[] = {"dd", "bdaddr", "timeout", 0};

    if( !PyArg_ParseTupleAndKeywords(args, kwds, "Os|i", keywords,
                &socko, &addr, &timeout) )
    {
        return 0;
    }

    str2ba( addr, &ba );
    memset( name, 0, sizeof(name) );

    dd = socko->sock_fd;

    Py_BEGIN_ALLOW_THREADS
    err = hci_read_remote_name( socko->sock_fd, &ba, sizeof(name)-1, 
                name, timeout );
    Py_END_ALLOW_THREADS

    if( err < 0) 
        return PyErr_SetFromErrno(bluetooth_error);

    return PyString_FromString( name );
}
PyDoc_STRVAR(bt_hci_read_remote_name_doc,
"hci_read_remote_name(sock, bdaddr, timeout=5192)\n\
\n\
Performs a remote name request to the specified bluetooth device.\n\
   sock - the HCI socket object to use\n\
   bdaddr - the bluetooth address of the remote device\n\
   timeout - maximum amount of time, in milliseconds, to wait\n\
\n\
Returns the name of the device, or raises an error on failure");


/* HCI filter operations */

static PyObject *
bt_hci_filter_new(PyObject *self, PyObject *args)
{
    struct hci_filter flt;
    int len = sizeof(flt);
    hci_filter_clear( &flt );
    return Py_BuildValue("s#", (char*)&flt, len);
}
PyDoc_STRVAR(bt_hci_filter_new_doc,
"hci_filter_new()\n\
\n\
Returns a new HCI filter suitable for operating on with the hci_filter_*\n\
methods, and for passing to getsockopt and setsockopt.  The filter is\n\
initially cleared");

// lot of repetitive code... yay macros!!
#define DECL_HCI_FILTER_OP_1(name, docstring) \
static PyObject * bt_hci_filter_ ## name (PyObject *self, PyObject *args )\
{ \
    char *param; \
    int len, arg; \
    if( !PyArg_ParseTuple(args,"s#i", &param, &len, &arg) ) \
        return 0; \
    if( len != sizeof(struct hci_filter) ) { \
		PyErr_SetString(PyExc_ValueError, "bad filter"); \
        return 0; \
    } \
    hci_filter_ ## name ( arg, (struct hci_filter*)param ); \
    len = sizeof(struct hci_filter); \
    return Py_BuildValue("s#", param, len); \
} \
PyDoc_STRVAR(bt_hci_filter_ ## name ## _doc, docstring);

DECL_HCI_FILTER_OP_1(set_ptype, "set ptype!")
DECL_HCI_FILTER_OP_1(clear_ptype, "clear ptype!")
DECL_HCI_FILTER_OP_1(test_ptype, "test ptype!")

DECL_HCI_FILTER_OP_1(set_event, "set event!")
DECL_HCI_FILTER_OP_1(clear_event, "clear event!")
DECL_HCI_FILTER_OP_1(test_event, "test event!")

DECL_HCI_FILTER_OP_1(set_opcode, "set opcode!")
DECL_HCI_FILTER_OP_1(test_opcode, "test opcode!")

#undef DECL_HCI_FILTER_OP_1

#define DECL_HCI_FILTER_OP_2(name, docstring) \
static PyObject * bt_hci_filter_ ## name (PyObject *self, PyObject *args )\
{ \
    char *param; \
    int len; \
    if( !PyArg_ParseTuple(args,"s#", &param, &len) ) \
        return 0; \
    if( len != sizeof(struct hci_filter) ) { \
		PyErr_SetString(PyExc_ValueError, "bad filter"); \
        return 0; \
    } \
    hci_filter_ ## name ( (struct hci_filter*)param ); \
    len = sizeof(struct hci_filter); \
    return Py_BuildValue("s#", param, len); \
} \
PyDoc_STRVAR(bt_hci_filter_ ## name ## _doc, docstring);

DECL_HCI_FILTER_OP_2(all_events, "all events!");
DECL_HCI_FILTER_OP_2(clear, "clear filter");
DECL_HCI_FILTER_OP_2(all_ptypes, "all packet types!");
DECL_HCI_FILTER_OP_2(clear_opcode, "clear opcode!")

#undef DECL_HCI_FILTER_OP_2

static PyObject *
bt_cmd_opcode_pack(PyObject *self, PyObject *args ) 
{
    uint16_t opcode, ogf, ocf;
    if (!PyArg_ParseTuple(args, "HH", &ogf, &ocf )) return 0;
    opcode = cmd_opcode_pack(ogf, ocf);
    return Py_BuildValue("H", opcode);
}
PyDoc_STRVAR(bt_cmd_opcode_pack_doc,
"cmd_opcode_pack(ogf, ocf)\n\
\n\
packs an OCF and an OGF value together to form a opcode");

static PyObject *
bt_cmd_opcode_ogf(PyObject *self, PyObject *args )
{
    uint16_t opcode;
    if (!PyArg_ParseTuple(args, "H", &opcode)) return 0;
    return Py_BuildValue("H", cmd_opcode_ogf(opcode));
}
PyDoc_STRVAR(bt_cmd_opcode_ogf_doc,
"cmd_opcode_ogf(opcode)\n\
\n\
Convenience function to extract and return the OGF value from an opcode");

static PyObject *
bt_cmd_opcode_ocf(PyObject *self, PyObject *args )
{
    uint16_t opcode;
    if (!PyArg_ParseTuple(args, "H", &opcode)) return 0;
    return Py_BuildValue("H", cmd_opcode_ocf(opcode));
}
PyDoc_STRVAR(bt_cmd_opcode_ocf_doc,
"cmd_opcode_ocf(opcode)\n\
\n\
Convenience function to extract and return the OCF value from an opcode");


static PyObject *
bt_ba2str(PyObject *self, PyObject *args)
{
    char *data=NULL;
    int len=0;
    char ba_str[19] = {0};
    if (!PyArg_ParseTuple(args, "s#", &data, &len)) return 0;
    ba2str((bdaddr_t*)data, ba_str);
    return PyString_FromString( ba_str );
//    return Py_BuildValue("s#", ba_str, 18);
}
PyDoc_STRVAR(bt_ba2str_doc,
"ba2str(data)\n\
\n\
Converts a packed bluetooth address to a human readable string");
    
static PyObject *
bt_str2ba(PyObject *self, PyObject *args)
{
    char *ba_str=NULL;
    bdaddr_t ba;
    if (!PyArg_ParseTuple(args, "s", &ba_str)) return 0;
    str2ba( ba_str, &ba );
    return Py_BuildValue("s#", (char*)(&ba), sizeof(ba));
}
PyDoc_STRVAR(bt_str2ba_doc,
"str2ba(string)\n\
\n\
Converts a bluetooth address string into a packed bluetooth address.  The\n\
string should be of the form \"XX:XX:XX:XX:XX:XX\"");
    
/*
 * params:  (string) device address
 * effect: -
 * return: Device id
 */
static PyObject *
bt_hci_devid(PyObject *self, PyObject *args)
{
    char *devaddr=NULL;
    int devid;

    if ( !PyArg_ParseTuple(args, "|s", &devaddr) )
    {
        return NULL;
    }

	if (devaddr)
		devid=hci_devid(devaddr);

	else
		devid=hci_get_route(NULL);

    return Py_BuildValue("i",devid);
}
PyDoc_STRVAR( bt_hci_role_doc,
"hci_devid(address)\n\
\n\
get the device id for the local device with specified address.\n\
");

/*
 * params:  (string) device address
 * effect: -
 * return: Device id
 */
static PyObject *
bt_hci_role(PyObject *self, PyObject *args)
{
    int devid;
    int fd;
    int role;

    if ( !PyArg_ParseTuple(args, "ii", &fd, &devid) )
        return NULL;

    struct hci_dev_info di = {dev_id: devid};

    if (ioctl(fd, HCIGETDEVINFO, (void *) &di))
           return NULL;

    role = di.link_mode == HCI_LM_MASTER;

    return Py_BuildValue("i", role);
}
PyDoc_STRVAR( bt_hci_devid_doc,
"hci_role(hci_fd, dev_id)\n\
\n\
get the role (master or slave) of the device id.\n\
");

/*
 * params:  (string) device address
 * effect: -
 * return: Device id
 */
static PyObject *
bt_hci_read_clock(PyObject *self, PyObject *args)
{
    int fd;
    int handle;
    int which;
    int timeout;
    uint32_t btclock;
    uint16_t accuracy; 
    int res;

    if ( !PyArg_ParseTuple(args, "iiii", &fd, &handle, &which, &timeout) )
        return NULL;

    res = hci_read_clock(fd, handle, which, &btclock, &accuracy, timeout);
    if (res) {
    	Py_INCREF(Py_None);
    	return Py_None;
    }

    return Py_BuildValue("(ii)", btclock, accuracy);
}
PyDoc_STRVAR( bt_hci_read_clock_doc,
"hci_read_clock(hci_fd, acl_handle, which_clock, timeout_ms)\n\
\n\
Get the Bluetooth Clock (native or piconet).\n\
");

/*
 * params:  (string) device address
 * effect: -
 * return: Device id
 */
static PyObject *
bt_hci_get_route(PyObject *self, PyObject *args)
{
    char *devaddr=NULL;
    bdaddr_t binaddr;
    int devid;

    if ( !PyArg_ParseTuple(args, "|s", &devaddr) )
    {
        return NULL;
    }

	if (devaddr) {
    		str2ba(devaddr, &binaddr);
		devid=hci_get_route(&binaddr);
	} else {
		devid=hci_get_route(NULL);
	}

    return Py_BuildValue("i" ,devid);
}
PyDoc_STRVAR( bt_hci_get_route_doc,
"hci_get_route(address)\n\
\n\
get the device id through which remote specified addr can be reached.\n\
");

/*
 * params:  (string) device address
 * effect: -
 * return: Device id
 */
static PyObject *
bt_hci_acl_conn_handle(PyObject *self, PyObject *args)
{
    int fd;
    char *devaddr=NULL;
    bdaddr_t binaddr;
    struct hci_conn_info_req *cr;
    char buf[sizeof(struct hci_conn_info_req) + sizeof(struct hci_conn_info)];
    int handle = -1;

    if ( !PyArg_ParseTuple(args, "is", &fd, &devaddr) )
        return NULL;

    if (devaddr)
    	str2ba(devaddr, &binaddr);
    else
        str2ba("00:00:00:00:00:00", &binaddr);

    cr = (struct hci_conn_info_req*) &buf;
    bacpy(&cr->bdaddr, &binaddr);
    cr->type = ACL_LINK;

    if (ioctl(fd, HCIGETCONNINFO, (unsigned long) cr) == 0)
        handle = htobs(cr->conn_info->handle);

    return Py_BuildValue("i", handle);
}
PyDoc_STRVAR( bt_hci_acl_conn_handle_doc,
"hci_acl_conn_handle(hci_fd, address)\n\
\n\
get the ACL connection handle for the given remote device addr.\n\
");

/*
 * -------------------
 *  End of HCI section
 * -------------------
 */


/* ========= SDP specific bluetooth module methods ========== */

PyObject *
bt_sdp_advertise_service( PyObject *self, PyObject *args )
{
    PySocketSockObject *socko = NULL;
    char *name = NULL, 
         *service_id_str = NULL, 
         *provider = NULL, 
         *description = NULL;
    PyObject *service_classes, *profiles, *protocols;
    int namelen = 0, provlen = 0, desclen = 0;
    uuid_t svc_uuid = { 0 };
    int i;
    char addrbuf[256] = { 0 };
    int res;
    socklen_t addrlen;
    struct sockaddr *sockaddr;
    uuid_t root_uuid, l2cap_uuid, rfcomm_uuid;
    sdp_list_t *l2cap_list = 0, 
               *rfcomm_list = 0,
               *root_list = 0,
               *proto_list = 0, 
               *profile_list = 0,
               *svc_class_list = 0,
               *access_proto_list = 0;
    sdp_data_t *channel = 0, *psm = 0;

    sdp_record_t record;
    sdp_session_t *session = 0;
    int err = 0;

    if (!PyArg_ParseTuple(args, "O!s#sOOs#s#O", &sock_type, &socko, &name,
                &namelen, &service_id_str, &service_classes, 
                &profiles, &provider, &provlen, &description, &desclen,
                &protocols)) {
        return 0;
    }
    if( provlen == 0 ) provider = NULL;
    if( desclen == 0 ) description = NULL;

    if( socko->sdp_record_handle != 0 ) {
        PyErr_SetString(bluetooth_error,
                "SDP service record already registered with this socket!");
        return 0;
    }

    if( namelen == 0 ) {
        PyErr_SetString(bluetooth_error, "must specify name!");
        return 0;
    }

    // convert the service ID string into a uuid_t if it was specified
    if( strlen(service_id_str) && ! str2uuid( service_id_str, &svc_uuid ) ) {
        PyErr_SetString(PyExc_ValueError, "invalid service ID");
        return NULL;
    }

    // service_classes must be a list / sequence
    if (! PySequence_Check(service_classes)) {
        PyErr_SetString(PyExc_ValueError, 
                "service_classes must be a sequence");
        return 0;
    }
    // make sure each item in the list is a valid UUID
    for(i = 0; i < PySequence_Length(service_classes); ++i) {
        PyObject *item = PySequence_GetItem(service_classes, i);
        if( ! str2uuid( PyString_AsString( item ), NULL ) ) {
            PyErr_SetString(PyExc_ValueError, 
                    "service_classes must be a list of "
                    "strings, each either of the form XXXX or "
                    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
            return 0;
        }
    }

    // profiles must be a list / sequence
    if (! PySequence_Check(profiles)) {
	    PyErr_SetString(PyExc_ValueError, "profiles must be a sequence");
	    return 0;
    }
    // make sure each item in the list is a valid ( uuid, version ) pair
    for(i = 0; i < PySequence_Length(profiles); ++i) {
        char *profile_uuid_str = NULL;
        uint16_t version;
        PyObject *tuple = PySequence_GetItem(profiles, i);
        if ( ( ! PySequence_Check(tuple) ) || 
             ( ! PyArg_ParseTuple(tuple, "sH", 
                 &profile_uuid_str, &version)) || 
             ( ! str2uuid( profile_uuid_str, NULL ) ) 
             ) {
            PyErr_SetString(PyExc_ValueError, 
                    "Each profile must be a ('uuid', version) tuple");
            return 0;
        }
    }
    
    // protocols must be a list / sequence
    if (! PySequence_Check(protocols)) {
        PyErr_SetString(PyExc_ValueError, 
                "protocols must be a sequence");
        return 0;
    }
    // make sure each item in the list is a valid UUID
    for(i = 0; i < PySequence_Length(protocols); ++i) {
        PyObject *item = PySequence_GetItem(protocols, i);
        if( ! str2uuid( PyString_AsString( item ), NULL ) ) {
            PyErr_SetString(PyExc_ValueError, 
                    "protocols must be a list of "
                    "strings, each either of the form XXXX or "
                    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
            return 0;
        }
    }

    // verify that the socket is bound and listening
    if( ! socko->is_listening_socket ) {
        PyErr_SetString(bluetooth_error, 
                "must have already called socket.listen()");
        return 0;
    }

    // get the socket information
	if (!getsockaddrlen(socko, &addrlen)) {
        PyErr_SetString(bluetooth_error, "error getting socket information");
		return 0;
    }
	Py_BEGIN_ALLOW_THREADS
	res = getsockname(socko->sock_fd, (struct sockaddr *) addrbuf, &addrlen);
	Py_END_ALLOW_THREADS
	if (res < 0) {
        PyErr_SetString(bluetooth_error, "error getting socket information");
		return 0;
    }
    sockaddr = (struct sockaddr *)addrbuf;

    // can only deal with L2CAP and RFCOMM sockets
    if( socko->sock_proto != BTPROTO_L2CAP && 
            socko->sock_proto != BTPROTO_RFCOMM ) {
        PyErr_SetString(bluetooth_error, 
                "Sorry, can only advertise L2CAP and RFCOMM sockets for now");
        return 0;
    }

    // abort if this socket is already advertising a service
    if( socko->sdp_record_handle != 0 && socko->sdp_session != NULL ) {
        PyErr_SetString(bluetooth_error,
                "This socket is already being used to advertise a service!\n"
                "Use  stop_advertising first!\n");
        return 0;
    }

    // okay, now construct the SDP service record.
    memset( &record, 0, sizeof(sdp_record_t) );
    
    record.handle = 0xffffffff;

    // make the service record publicly browsable
    sdp_uuid16_create(&root_uuid, PUBLIC_BROWSE_GROUP);
    root_list = sdp_list_append(0, &root_uuid);
    sdp_set_browse_groups( &record, root_list );

    // set l2cap information (this will always go in)
    sdp_uuid16_create(&l2cap_uuid, L2CAP_UUID);
    l2cap_list = sdp_list_append( 0, &l2cap_uuid );
    proto_list = sdp_list_append( 0, l2cap_list );

    if( socko->sock_proto == BTPROTO_RFCOMM ) {
        // register the RFCOMM channel for RFCOMM sockets
        uint8_t rfcomm_channel = ((struct sockaddr_rc*)sockaddr)->rc_channel;

        sdp_uuid16_create(&rfcomm_uuid, RFCOMM_UUID);
        channel = sdp_data_alloc(SDP_UINT8, &rfcomm_channel);
        rfcomm_list = sdp_list_append( 0, &rfcomm_uuid );
        sdp_list_append( rfcomm_list, channel );
        sdp_list_append( proto_list, rfcomm_list );

    } else {
        // register the PSM for L2CAP sockets
        unsigned short l2cap_psm = ((struct sockaddr_l2*)sockaddr)->l2_psm;

        psm = sdp_data_alloc(SDP_UINT16, &l2cap_psm);
        sdp_list_append(l2cap_list, psm);
    }
    
    // add additional protocols, if any
    sdp_list_t *extra_protos_array[PySequence_Length(protocols)];
    if (PySequence_Length(protocols) > 0) {
        for(i = 0; i < PySequence_Length(protocols); i++) {
            uuid_t *proto_uuid = (uuid_t*) malloc( sizeof( uuid_t ) );
            PyObject *item = PySequence_GetItem(protocols, i);
            str2uuid( PyString_AsString( item ), proto_uuid );
            
            sdp_list_t *new_list;
            new_list = sdp_list_append( 0, proto_uuid );
            proto_list = sdp_list_append( proto_list, new_list );
            
            // keep track, to free the list later
            extra_protos_array[i] = new_list;
        }
    }
    
    access_proto_list = sdp_list_append( 0, proto_list );
    sdp_set_access_protos( &record, access_proto_list );

    // add service classes, if any
    for(i = 0; i < PySequence_Length(service_classes); i++) {
        uuid_t *svc_class_uuid = (uuid_t*) malloc( sizeof( uuid_t ) );
        PyObject *item = PySequence_GetItem(service_classes, i);
        str2uuid( PyString_AsString( item ), svc_class_uuid );
        svc_class_list = sdp_list_append(svc_class_list, 
                svc_class_uuid);
    }
    sdp_set_service_classes(&record, svc_class_list);

    // add profiles, if any
    for(i = 0; i < PySequence_Length(profiles); i++) {
        char *profile_uuid_str;
        sdp_profile_desc_t *profile_desc = 
            (sdp_profile_desc_t*)malloc(sizeof(sdp_profile_desc_t));
        PyObject *tuple = PySequence_GetItem(profiles, i);
        PyArg_ParseTuple(tuple, "sH", &profile_uuid_str, 
                &profile_desc->version);
        str2uuid( profile_uuid_str, &profile_desc->uuid );
        profile_list = sdp_list_append( profile_list, profile_desc );
    }
    sdp_set_profile_descs(&record, profile_list);

    // set the name, provider and description
    sdp_set_info_attr( &record, name, provider, description );

    // set the general service ID, if needed
    if( strlen(service_id_str) ) sdp_set_service_id( &record, svc_uuid );

    // connect to the local SDP server, register the service record, and 
    // disconnect
    Py_BEGIN_ALLOW_THREADS
    session = sdp_connect( BDADDR_ANY, BDADDR_LOCAL, 0 );
    Py_END_ALLOW_THREADS
    if (!session) {
        PyErr_SetFromErrno (bluetooth_error);
        return 0;
    }
    socko->sdp_session = session;
    Py_BEGIN_ALLOW_THREADS
    err = sdp_record_register(session, &record, 0);
    Py_END_ALLOW_THREADS

    // cleanup
    if( psm ) sdp_data_free( psm );
    if( channel ) sdp_data_free( channel );
    sdp_list_free( l2cap_list, 0 );
    sdp_list_free( rfcomm_list, 0 );
    for(i = 0; i < PySequence_Length(protocols); i++) {
        sdp_list_free( extra_protos_array[i], free );
    }
    sdp_list_free( root_list, 0 );
    sdp_list_free( access_proto_list, 0 );
    sdp_list_free( svc_class_list, free );
    sdp_list_free( profile_list, free );

    if( err ) {
        PyErr_SetFromErrno(bluetooth_error);
        return 0;
    }
    socko->sdp_record_handle = record.handle;

    Py_INCREF(Py_None);
    return Py_None;
}
PyDoc_STRVAR( bt_sdp_advertise_service_doc, 
"sdp_advertise_service( socket, name )\n\
\n\
Registers a service with the local SDP server.\n\
\n\
socket must be a bound, listening socket - you must have already\n\
called socket.listen().  Only L2CAP and RFCOMM sockets are supported.\n\
\n\
name is the name that you want to appear in the SDP record\n\
\n\
Registered services will be automatically unregistered when the socket is\n\
closed.\
");


PyObject *
bt_sdp_stop_advertising( PyObject *self, PyObject *args )
{
    PySocketSockObject *socko = NULL;

    if ( !PyArg_ParseTuple(args, "O!", &sock_type, &socko ) ) {
        return 0;
    }

    // verify that we got a real socket object
    if( ! socko || (socko->ob_type != &sock_type) ) {
        // TODO change this to a more accurate exception type
        PyErr_SetString(bluetooth_error, 
                "must pass in _bluetooth.socket object");
        return 0;
    }

    if( socko->sdp_session != NULL ) {
        Py_BEGIN_ALLOW_THREADS
        sdp_close( socko->sdp_session );
        Py_END_ALLOW_THREADS
        socko->sdp_session = NULL;
        socko->sdp_record_handle = 0;
    } else {
        PyErr_SetString( bluetooth_error, "not currently advertising!");
    }

    Py_INCREF(Py_None);
    return Py_None;
}
PyDoc_STRVAR( bt_sdp_stop_advertising_doc,
"sdp_stop_advertising( socket )\n\
\n\
stop advertising services associated with this socket\n\
");


/* List of functions exported by this module. */

#define DECL_BT_METHOD(name, argtype) \
{ #name, (PyCFunction)bt_ ##name, argtype, bt_ ## name ## _doc }

static PyMethodDef bt_methods[] = {
    DECL_BT_METHOD( hci_devid, METH_VARARGS ),
    DECL_BT_METHOD( hci_get_route, METH_VARARGS ),
    DECL_BT_METHOD( hci_role, METH_VARARGS ),
    DECL_BT_METHOD( hci_read_clock, METH_VARARGS ),
    DECL_BT_METHOD( hci_acl_conn_handle, METH_VARARGS ),
    DECL_BT_METHOD( hci_open_dev, METH_VARARGS ),
    DECL_BT_METHOD( hci_close_dev, METH_VARARGS ),
    DECL_BT_METHOD( hci_send_cmd, METH_VARARGS ),
    DECL_BT_METHOD( hci_send_req, METH_VARARGS | METH_KEYWORDS ),
    DECL_BT_METHOD( hci_inquiry, METH_VARARGS | METH_KEYWORDS ),
    DECL_BT_METHOD( hci_read_remote_name, METH_VARARGS | METH_KEYWORDS ),
    DECL_BT_METHOD( hci_filter_new, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_clear, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_all_events, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_all_ptypes, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_clear_opcode, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_set_ptype, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_clear_ptype, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_test_ptype, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_set_event, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_clear_event, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_test_event, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_set_opcode, METH_VARARGS ),
    DECL_BT_METHOD( hci_filter_test_opcode, METH_VARARGS ),
    DECL_BT_METHOD( cmd_opcode_pack, METH_VARARGS ),
    DECL_BT_METHOD( cmd_opcode_ogf, METH_VARARGS ),
    DECL_BT_METHOD( cmd_opcode_ocf, METH_VARARGS ),
    DECL_BT_METHOD( ba2str, METH_VARARGS ),
    DECL_BT_METHOD( str2ba, METH_VARARGS ),
#ifndef NO_DUP
    DECL_BT_METHOD( fromfd, METH_VARARGS ),
#endif
    DECL_BT_METHOD( btohs, METH_VARARGS ),
    DECL_BT_METHOD( btohl, METH_VARARGS ),
    DECL_BT_METHOD( htobs, METH_VARARGS ),
    DECL_BT_METHOD( htobl, METH_VARARGS ),
    DECL_BT_METHOD( getdefaulttimeout, METH_NOARGS ),
    DECL_BT_METHOD( setdefaulttimeout, METH_O ),
    DECL_BT_METHOD( sdp_advertise_service, METH_VARARGS ),
    DECL_BT_METHOD( sdp_stop_advertising, METH_VARARGS ),
//    DECL_BT_METHOD( advertise_service, METH_VARARGS | METH_KEYWORDS ),
	{NULL,			NULL}		 /* Sentinel */
};

#undef DECL_BT_METHOD

/* Initialize the bt module.
*/

PyDoc_STRVAR(socket_doc,
"Implementation module for bluetooth operations.\n\
\n\
See the bluetooth module for documentation.");

PyMODINIT_FUNC
init_bluetooth(void)
{
	PyObject *m;

	sock_type.ob_type = &PyType_Type;
    sdp_session_type.ob_type = &PyType_Type;

// Initialization steps for _bluetooth.
    m = Py_InitModule3("_bluetooth",
               bt_methods,
               socket_doc);
    bluetooth_error = PyErr_NewException("_bluetooth.error", NULL, NULL);
	if (bluetooth_error == NULL)
		return;
	Py_INCREF(bluetooth_error);
	PyModule_AddObject(m, "error", bluetooth_error);

    socket_timeout = PyErr_NewException("_bluetooth.timeout", bluetooth_error,
            NULL);
	if (socket_timeout == NULL)
		return;
	Py_INCREF(socket_timeout);
	PyModule_AddObject(m, "timeout", socket_timeout);

	Py_INCREF((PyObject *)&sock_type);
	if (PyModule_AddObject(m, "btsocket",
			       (PyObject *)&sock_type) != 0)
		return;

    Py_INCREF((PyObject *)&sdp_session_type);
    if (PyModule_AddObject(m, "SDPSession",
                (PyObject *)&sdp_session_type) != 0)
        return;


    // because we're lazy...
#define ADD_INT_CONST(m, a) PyModule_AddIntConstant(m, #a, a)


    // Global variables that can be accessible from Python.
//    ADD_INT_CONST(m, PF_BLUETOOTH);
//    ADD_INT_CONST(m, AF_BLUETOOTH);
    ADD_INT_CONST(m, SOL_HCI);
    ADD_INT_CONST(m, HCI_DATA_DIR);
    ADD_INT_CONST(m, HCI_TIME_STAMP);
    ADD_INT_CONST(m, HCI_FILTER);
    ADD_INT_CONST(m, HCI_MAX_EVENT_SIZE);
    ADD_INT_CONST(m, HCI_EVENT_HDR_SIZE);

    PyModule_AddIntConstant(m, "HCI", BTPROTO_HCI);
    PyModule_AddIntConstant(m, "L2CAP", BTPROTO_L2CAP);
    PyModule_AddIntConstant(m, "RFCOMM", BTPROTO_RFCOMM);
    PyModule_AddIntConstant(m, "SCO", BTPROTO_SCO);

//	/* Socket types */
//	ADD_INT_CONST(m, SOCK_STREAM);
//	ADD_INT_CONST(m, SOCK_DGRAM);
//	ADD_INT_CONST(m, SOCK_RAW);
//	ADD_INT_CONST(m, SOCK_SEQPACKET);
    
/* HCI Constants */

    /* HCI OGF values */
#ifdef OGF_LINK_CTL
    ADD_INT_CONST(m, OGF_LINK_CTL);
#endif
#ifdef OGF_LINK_POLICY
    ADD_INT_CONST(m, OGF_LINK_POLICY);
#endif
#ifdef OGF_HOST_CTL
    ADD_INT_CONST(m, OGF_HOST_CTL);
#endif
#ifdef OGF_INFO_PARAM
    ADD_INT_CONST(m, OGF_INFO_PARAM);
#endif
#ifdef OGF_STATUS_PARAM
    ADD_INT_CONST(m, OGF_STATUS_PARAM);
#endif
#ifdef OGF_TESTING_CMD
    ADD_INT_CONST(m, OGF_TESTING_CMD);
#endif
#ifdef OGF_VENDOR_CMD
    ADD_INT_CONST(m, OGF_VENDOR_CMD);
#endif

    /* HCI OCF values */
#ifdef OCF_INQUIRY
    ADD_INT_CONST(m, OCF_INQUIRY);
#endif
#ifdef OCF_INQUIRY_CANCEL
    ADD_INT_CONST(m, OCF_INQUIRY_CANCEL);
#endif
#ifdef OCF_PERIODIC_INQUIRY
    ADD_INT_CONST(m, OCF_PERIODIC_INQUIRY);
#endif
#ifdef OCF_EXIT_PERIODIC_INQUIRY
    ADD_INT_CONST(m, OCF_EXIT_PERIODIC_INQUIRY);
#endif
#ifdef OCF_CREATE_CONN
    ADD_INT_CONST(m, OCF_CREATE_CONN);
#endif
#ifdef OCF_DISCONNECT
    ADD_INT_CONST(m, OCF_DISCONNECT);
#endif
#ifdef OCF_ADD_SCO
    ADD_INT_CONST(m, OCF_ADD_SCO);
#endif
#ifdef OCF_ACCEPT_CONN_REQ
    ADD_INT_CONST(m, OCF_ACCEPT_CONN_REQ);
#endif
#ifdef OCF_REJECT_CONN_REQ
    ADD_INT_CONST(m, OCF_REJECT_CONN_REQ);
#endif
#ifdef OCF_LINK_KEY_REPLY
    ADD_INT_CONST(m, OCF_LINK_KEY_REPLY);
#endif
#ifdef OCF_LINK_KEY_NEG_REPLY
    ADD_INT_CONST(m, OCF_LINK_KEY_NEG_REPLY);
#endif
#ifdef OCF_PIN_CODE_REPLY
    ADD_INT_CONST(m, OCF_PIN_CODE_REPLY);
#endif
#ifdef OCF_PIN_CODE_NEG_REPLY
    ADD_INT_CONST(m, OCF_PIN_CODE_NEG_REPLY);
#endif
#ifdef OCF_SET_CONN_PTYPE
    ADD_INT_CONST(m, OCF_SET_CONN_PTYPE);
#endif
#ifdef OCF_AUTH_REQUESTED
    ADD_INT_CONST(m, OCF_AUTH_REQUESTED);
#endif
#ifdef OCF_SET_CONN_ENCRYPT
    ADD_INT_CONST(m, OCF_SET_CONN_ENCRYPT);
#endif
#ifdef OCF_REMOTE_NAME_REQ
    ADD_INT_CONST(m, OCF_REMOTE_NAME_REQ);
#endif
#ifdef OCF_READ_REMOTE_FEATURES
    ADD_INT_CONST(m, OCF_READ_REMOTE_FEATURES);
#endif
#ifdef OCF_READ_REMOTE_VERSION
    ADD_INT_CONST(m, OCF_READ_REMOTE_VERSION);
#endif
#ifdef OCF_READ_CLOCK_OFFSET
    ADD_INT_CONST(m, OCF_READ_CLOCK_OFFSET);
#endif
#ifdef OCF_READ_CLOCK_OFFSET
    ADD_INT_CONST(m, OCF_READ_CLOCK);
#endif
#ifdef OCF_HOLD_MODE
    ADD_INT_CONST(m, OCF_HOLD_MODE);
#endif
#ifdef OCF_SNIFF_MODE
    ADD_INT_CONST(m, OCF_SNIFF_MODE);
#endif
#ifdef OCF_EXIT_SNIFF_MODE
    ADD_INT_CONST(m, OCF_EXIT_SNIFF_MODE);
#endif
#ifdef OCF_PARK_MODE
    ADD_INT_CONST(m, OCF_PARK_MODE);
#endif
#ifdef OCF_EXIT_PARK_MODE
    ADD_INT_CONST(m, OCF_EXIT_PARK_MODE);
#endif
#ifdef OCF_QOS_SETUP
    ADD_INT_CONST(m, OCF_QOS_SETUP);
#endif
#ifdef OCF_ROLE_DISCOVERY
    ADD_INT_CONST(m, OCF_ROLE_DISCOVERY);
#endif
#ifdef OCF_SWITCH_ROLE
    ADD_INT_CONST(m, OCF_SWITCH_ROLE);
#endif
#ifdef OCF_READ_LINK_POLICY
    ADD_INT_CONST(m, OCF_READ_LINK_POLICY);
#endif
#ifdef OCF_WRITE_LINK_POLICY
    ADD_INT_CONST(m, OCF_WRITE_LINK_POLICY);
#endif
#ifdef OCF_RESET
    ADD_INT_CONST(m, OCF_RESET);
#endif
#ifdef OCF_SET_EVENT_FLT
    ADD_INT_CONST(m, OCF_SET_EVENT_FLT);
#endif
#ifdef OCF_CHANGE_LOCAL_NAME
    ADD_INT_CONST(m, OCF_CHANGE_LOCAL_NAME);
#endif
#ifdef OCF_READ_LOCAL_NAME
    ADD_INT_CONST(m, OCF_READ_LOCAL_NAME);
#endif
#ifdef OCF_WRITE_CA_TIMEOUT
    ADD_INT_CONST(m, OCF_WRITE_CA_TIMEOUT);
#endif
#ifdef OCF_WRITE_PG_TIMEOUT
    ADD_INT_CONST(m, OCF_WRITE_PG_TIMEOUT);
#endif
#ifdef OCF_READ_PAGE_TIMEOUT
    ADD_INT_CONST(m, OCF_READ_PAGE_TIMEOUT);
#endif
#ifdef OCF_WRITE_PAGE_TIMEOUT
    ADD_INT_CONST(m, OCF_WRITE_PAGE_TIMEOUT);
#endif
#ifdef OCF_WRITE_SCAN_ENABLE
    ADD_INT_CONST(m, OCF_WRITE_SCAN_ENABLE);
#endif
#ifdef OCF_READ_PAGE_ACTIVITY
    ADD_INT_CONST(m, OCF_READ_PAGE_ACTIVITY);
#endif
#ifdef OCF_WRITE_PAGE_ACTIVITY
    ADD_INT_CONST(m, OCF_WRITE_PAGE_ACTIVITY);
#endif
#ifdef OCF_READ_INQ_ACTIVITY
    ADD_INT_CONST(m, OCF_READ_INQ_ACTIVITY);
#endif
#ifdef OCF_WRITE_INQ_ACTIVITY
    ADD_INT_CONST(m, OCF_WRITE_INQ_ACTIVITY);
#endif
#ifdef OCF_READ_AUTH_ENABLE
    ADD_INT_CONST(m, OCF_READ_AUTH_ENABLE);
#endif
#ifdef OCF_WRITE_AUTH_ENABLE
    ADD_INT_CONST(m, OCF_WRITE_AUTH_ENABLE);
#endif
#ifdef OCF_READ_ENCRYPT_MODE
    ADD_INT_CONST(m, OCF_READ_ENCRYPT_MODE);
#endif
#ifdef OCF_WRITE_ENCRYPT_MODE
    ADD_INT_CONST(m, OCF_WRITE_ENCRYPT_MODE);
#endif
#ifdef OCF_READ_CLASS_OF_DEV
    ADD_INT_CONST(m, OCF_READ_CLASS_OF_DEV);
#endif
#ifdef OCF_WRITE_CLASS_OF_DEV
    ADD_INT_CONST(m, OCF_WRITE_CLASS_OF_DEV);
#endif
#ifdef OCF_READ_VOICE_SETTING
    ADD_INT_CONST(m, OCF_READ_VOICE_SETTING);
#endif
#ifdef OCF_WRITE_VOICE_SETTING
    ADD_INT_CONST(m, OCF_WRITE_VOICE_SETTING);
#endif
#ifdef OCF_READ_TRANSMIT_POWER_LEVEL
    ADD_INT_CONST(m, OCF_READ_TRANSMIT_POWER_LEVEL);
#endif
#ifdef OCF_HOST_BUFFER_SIZE
    ADD_INT_CONST(m, OCF_HOST_BUFFER_SIZE);
#endif
#ifdef OCF_READ_LINK_SUPERVISION_TIMEOUT
    ADD_INT_CONST(m, OCF_READ_LINK_SUPERVISION_TIMEOUT);
#endif
#ifdef OCF_WRITE_LINK_SUPERVISION_TIMEOUT
    ADD_INT_CONST(m, OCF_WRITE_LINK_SUPERVISION_TIMEOUT);
#endif
#ifdef OCF_READ_CURRENT_IAC_LAP
    ADD_INT_CONST(m, OCF_READ_CURRENT_IAC_LAP);
#endif
#ifdef OCF_WRITE_CURRENT_IAC_LAP
    ADD_INT_CONST(m, OCF_WRITE_CURRENT_IAC_LAP);
#endif
#ifdef OCF_READ_INQUIRY_MODE
    ADD_INT_CONST(m, OCF_READ_INQUIRY_MODE);
#endif
#ifdef OCF_WRITE_INQUIRY_MODE
    ADD_INT_CONST(m, OCF_WRITE_INQUIRY_MODE);
#endif
#ifdef OCF_READ_AFH_MODE
    ADD_INT_CONST(m, OCF_READ_AFH_MODE);
#endif
#ifdef OCF_WRITE_AFH_MODE
    ADD_INT_CONST(m, OCF_WRITE_AFH_MODE);
#endif
#ifdef OCF_READ_LOCAL_VERSION
    ADD_INT_CONST(m, OCF_READ_LOCAL_VERSION);
#endif
#ifdef OCF_READ_LOCAL_FEATURES
    ADD_INT_CONST(m, OCF_READ_LOCAL_FEATURES);
#endif
#ifdef OCF_READ_BUFFER_SIZE
    ADD_INT_CONST(m, OCF_READ_BUFFER_SIZE);
#endif
#ifdef OCF_READ_BD_ADDR
    ADD_INT_CONST(m, OCF_READ_BD_ADDR);
#endif
#ifdef OCF_READ_FAILED_CONTACT_COUNTER
    ADD_INT_CONST(m, OCF_READ_FAILED_CONTACT_COUNTER);
#endif
#ifdef OCF_RESET_FAILED_CONTACT_COUNTER
    ADD_INT_CONST(m, OCF_RESET_FAILED_CONTACT_COUNTER);
#endif
#ifdef OCF_GET_LINK_QUALITY
    ADD_INT_CONST(m, OCF_GET_LINK_QUALITY);
#endif
#ifdef OCF_READ_RSSI
    ADD_INT_CONST(m, OCF_READ_RSSI);
#endif
#ifdef OCF_READ_AFH_MAP
    ADD_INT_CONST(m, OCF_READ_AFH_MAP);
#endif

    /* HCI events */
#ifdef EVT_INQUIRY_COMPLETE
    ADD_INT_CONST(m, EVT_INQUIRY_COMPLETE);
#endif
#ifdef EVT_INQUIRY_RESULT
    ADD_INT_CONST(m, EVT_INQUIRY_RESULT);
#endif
#ifdef EVT_CONN_COMPLETE
    ADD_INT_CONST(m, EVT_CONN_COMPLETE);
#endif
#ifdef EVT_CONN_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_CONN_COMPLETE_SIZE);
#endif
#ifdef EVT_CONN_REQUEST
    ADD_INT_CONST(m, EVT_CONN_REQUEST);
#endif
#ifdef EVT_CONN_REQUEST_SIZE
    ADD_INT_CONST(m, EVT_CONN_REQUEST_SIZE);
#endif
#ifdef EVT_DISCONN_COMPLETE
    ADD_INT_CONST(m, EVT_DISCONN_COMPLETE);
#endif
#ifdef EVT_DISCONN_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_DISCONN_COMPLETE_SIZE);
#endif
#ifdef EVT_AUTH_COMPLETE
    ADD_INT_CONST(m, EVT_AUTH_COMPLETE);
#endif
#ifdef EVT_AUTH_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_AUTH_COMPLETE_SIZE);
#endif
#ifdef EVT_REMOTE_NAME_REQ_COMPLETE
    ADD_INT_CONST(m, EVT_REMOTE_NAME_REQ_COMPLETE);
#endif
#ifdef EVT_REMOTE_NAME_REQ_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_REMOTE_NAME_REQ_COMPLETE_SIZE);
#endif
#ifdef EVT_ENCRYPT_CHANGE
    ADD_INT_CONST(m, EVT_ENCRYPT_CHANGE);
#endif
#ifdef EVT_ENCRYPT_CHANGE_SIZE
    ADD_INT_CONST(m, EVT_ENCRYPT_CHANGE_SIZE);
#endif
#ifdef EVT_READ_REMOTE_FEATURES_COMPLETE
    ADD_INT_CONST(m, EVT_READ_REMOTE_FEATURES_COMPLETE);
#endif
#ifdef EVT_READ_REMOTE_FEATURES_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_READ_REMOTE_FEATURES_COMPLETE_SIZE);
#endif
#ifdef EVT_READ_REMOTE_VERSION_COMPLETE
    ADD_INT_CONST(m, EVT_READ_REMOTE_VERSION_COMPLETE);
#endif
#ifdef EVT_READ_REMOTE_VERSION_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_READ_REMOTE_VERSION_COMPLETE_SIZE);
#endif
#ifdef EVT_QOS_SETUP_COMPLETE
    ADD_INT_CONST(m, EVT_QOS_SETUP_COMPLETE);
#endif
#ifdef EVT_QOS_SETUP_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_QOS_SETUP_COMPLETE_SIZE);
#endif
#ifdef EVT_CMD_COMPLETE
    ADD_INT_CONST(m, EVT_CMD_COMPLETE);
#endif
#ifdef EVT_CMD_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_CMD_COMPLETE_SIZE);
#endif
#ifdef EVT_CMD_STATUS
    ADD_INT_CONST(m, EVT_CMD_STATUS);
#endif
#ifdef EVT_CMD_STATUS_SIZE
    ADD_INT_CONST(m, EVT_CMD_STATUS_SIZE);
#endif
#ifdef EVT_ROLE_CHANGE
    ADD_INT_CONST(m, EVT_ROLE_CHANGE);
#endif
#ifdef EVT_ROLE_CHANGE_SIZE
    ADD_INT_CONST(m, EVT_ROLE_CHANGE_SIZE);
#endif
#ifdef EVT_NUM_COMP_PKTS
    ADD_INT_CONST(m, EVT_NUM_COMP_PKTS);
#endif
#ifdef EVT_NUM_COMP_PKTS_SIZE
    ADD_INT_CONST(m, EVT_NUM_COMP_PKTS_SIZE);
#endif
#ifdef EVT_MODE_CHANGE
    ADD_INT_CONST(m, EVT_MODE_CHANGE);
#endif
#ifdef EVT_MODE_CHANGE_SIZE
    ADD_INT_CONST(m, EVT_MODE_CHANGE_SIZE);
#endif
#ifdef EVT_PIN_CODE_REQ
    ADD_INT_CONST(m, EVT_PIN_CODE_REQ);
#endif
#ifdef EVT_PIN_CODE_REQ_SIZE
    ADD_INT_CONST(m, EVT_PIN_CODE_REQ_SIZE);
#endif
#ifdef EVT_LINK_KEY_REQ
    ADD_INT_CONST(m, EVT_LINK_KEY_REQ);
#endif
#ifdef EVT_LINK_KEY_REQ_SIZE
    ADD_INT_CONST(m, EVT_LINK_KEY_REQ_SIZE);
#endif
#ifdef EVT_LINK_KEY_NOTIFY
    ADD_INT_CONST(m, EVT_LINK_KEY_NOTIFY);
#endif
#ifdef EVT_LINK_KEY_NOTIFY_SIZE
    ADD_INT_CONST(m, EVT_LINK_KEY_NOTIFY_SIZE);
#endif
#ifdef EVT_READ_CLOCK_OFFSET_COMPLETE
    ADD_INT_CONST(m, EVT_READ_CLOCK_OFFSET_COMPLETE);
#endif
#ifdef EVT_READ_CLOCK_OFFSET_COMPLETE_SIZE
    ADD_INT_CONST(m, EVT_READ_CLOCK_OFFSET_COMPLETE_SIZE);
#endif
#ifdef EVT_CONN_PTYPE_CHANGED
    ADD_INT_CONST(m, EVT_CONN_PTYPE_CHANGED);
#endif
#ifdef EVT_CONN_PTYPE_CHANGED_SIZE
    ADD_INT_CONST(m, EVT_CONN_PTYPE_CHANGED_SIZE);
#endif
#ifdef EVT_QOS_VIOLATION
    ADD_INT_CONST(m, EVT_QOS_VIOLATION);
#endif
#ifdef EVT_QOS_VIOLATION_SIZE
    ADD_INT_CONST(m, EVT_QOS_VIOLATION_SIZE);
#endif
#ifdef EVT_INQUIRY_RESULT_WITH_RSSI
    ADD_INT_CONST(m, EVT_INQUIRY_RESULT_WITH_RSSI);
#endif
#ifdef EVT_TESTING
    ADD_INT_CONST(m, EVT_TESTING);
#endif
#ifdef EVT_VENDOR
    ADD_INT_CONST(m, EVT_VENDOR);
#endif
#ifdef EVT_STACK_INTERNAL
    ADD_INT_CONST(m, EVT_STACK_INTERNAL);
#endif
#ifdef EVT_STACK_INTERNAL_SIZE
    ADD_INT_CONST(m, EVT_STACK_INTERNAL_SIZE);
#endif
#ifdef EVT_SI_DEVICE
    ADD_INT_CONST(m, EVT_SI_DEVICE);
#endif
#ifdef EVT_SI_DEVICE_SIZE
    ADD_INT_CONST(m, EVT_SI_DEVICE_SIZE);
#endif
#ifdef EVT_SI_SECURITY
    ADD_INT_CONST(m, EVT_SI_SECURITY);
#endif

    /* HCI packet types */
#ifdef HCI_COMMAND_PKT
    ADD_INT_CONST(m, HCI_COMMAND_PKT);
#endif
#ifdef HCI_ACLDATA_PKT
    ADD_INT_CONST(m, HCI_ACLDATA_PKT);
#endif
#ifdef HCI_SCODATA_PKT
    ADD_INT_CONST(m, HCI_SCODATA_PKT);
#endif
#ifdef HCI_EVENT_PKT
    ADD_INT_CONST(m, HCI_EVENT_PKT);
#endif
#ifdef HCI_UNKNOWN_PKT
    ADD_INT_CONST(m, HCI_UNKNOWN_PKT);
#endif

    /* socket options */
#ifdef	SO_DEBUG
	ADD_INT_CONST(m, SO_DEBUG);
#endif
#ifdef	SO_ACCEPTCONN
	ADD_INT_CONST(m, SO_ACCEPTCONN);
#endif
#ifdef	SO_REUSEADDR
	ADD_INT_CONST(m, SO_REUSEADDR);
#endif
#ifdef	SO_KEEPALIVE
	ADD_INT_CONST(m, SO_KEEPALIVE);
#endif
#ifdef	SO_DONTROUTE
	ADD_INT_CONST(m, SO_DONTROUTE);
#endif
#ifdef	SO_BROADCAST
	ADD_INT_CONST(m, SO_BROADCAST);
#endif
#ifdef	SO_USELOOPBACK
	ADD_INT_CONST(m, SO_USELOOPBACK);
#endif
#ifdef	SO_LINGER
	ADD_INT_CONST(m, SO_LINGER);
#endif
#ifdef	SO_OOBINLINE
	ADD_INT_CONST(m, SO_OOBINLINE);
#endif
#ifdef	SO_REUSEPORT
	ADD_INT_CONST(m, SO_REUSEPORT);
#endif
#ifdef	SO_SNDBUF
	ADD_INT_CONST(m, SO_SNDBUF);
#endif
#ifdef	SO_RCVBUF
	ADD_INT_CONST(m, SO_RCVBUF);
#endif
#ifdef	SO_SNDLOWAT
	ADD_INT_CONST(m, SO_SNDLOWAT);
#endif
#ifdef	SO_RCVLOWAT
	ADD_INT_CONST(m, SO_RCVLOWAT);
#endif
#ifdef	SO_SNDTIMEO
	ADD_INT_CONST(m, SO_SNDTIMEO);
#endif
#ifdef	SO_RCVTIMEO
	ADD_INT_CONST(m, SO_RCVTIMEO);
#endif
#ifdef	SO_ERROR
	ADD_INT_CONST(m, SO_ERROR);
#endif
#ifdef	SO_TYPE
	ADD_INT_CONST(m, SO_TYPE);
#endif

	/* Maximum number of connections for "listen" */
#ifdef	SOMAXCONN
	ADD_INT_CONST(m, SOMAXCONN);
#else
	ADD_INT_CONST(m, SOMAXCONN);
#endif

	/* Flags for send, recv */
#ifdef	MSG_OOB
	ADD_INT_CONST(m, MSG_OOB);
#endif
#ifdef	MSG_PEEK
	ADD_INT_CONST(m, MSG_PEEK);
#endif
#ifdef	MSG_DONTROUTE
	ADD_INT_CONST(m, MSG_DONTROUTE);
#endif
#ifdef	MSG_DONTWAIT
	ADD_INT_CONST(m, MSG_DONTWAIT);
#endif
#ifdef	MSG_EOR
	ADD_INT_CONST(m, MSG_EOR);
#endif
#ifdef	MSG_TRUNC
	ADD_INT_CONST(m, MSG_TRUNC);
#endif
#ifdef	MSG_CTRUNC
	ADD_INT_CONST(m, MSG_CTRUNC);
#endif
#ifdef	MSG_WAITALL
	ADD_INT_CONST(m, MSG_WAITALL);
#endif
#ifdef	MSG_BTAG
	ADD_INT_CONST(m, MSG_BTAG);
#endif
#ifdef	MSG_ETAG
	ADD_INT_CONST(m, MSG_ETAG);
#endif

	/* Protocol level and numbers, usable for [gs]etsockopt */
	ADD_INT_CONST(m, SOL_SOCKET);
	ADD_INT_CONST(m, SOL_L2CAP);
	ADD_INT_CONST(m, SOL_RFCOMM);
	ADD_INT_CONST(m, SOL_SCO);
	ADD_INT_CONST(m, SCO_OPTIONS);
	ADD_INT_CONST(m, L2CAP_OPTIONS);

    /* ioctl */
    ADD_INT_CONST(m, HCIDEVUP);
    ADD_INT_CONST(m, HCIDEVDOWN);
    ADD_INT_CONST(m, HCIDEVRESET);
    ADD_INT_CONST(m, HCIDEVRESTAT);
    ADD_INT_CONST(m, HCIGETDEVLIST);
    ADD_INT_CONST(m, HCIGETDEVINFO);
    ADD_INT_CONST(m, HCIGETCONNLIST);
    ADD_INT_CONST(m, HCIGETCONNINFO);
    ADD_INT_CONST(m, HCISETRAW);
    ADD_INT_CONST(m, HCISETSCAN);
    ADD_INT_CONST(m, HCISETAUTH);
    ADD_INT_CONST(m, HCISETENCRYPT);
    ADD_INT_CONST(m, HCISETPTYPE);
    ADD_INT_CONST(m, HCISETLINKPOL);
    ADD_INT_CONST(m, HCISETLINKMODE);
    ADD_INT_CONST(m, HCISETACLMTU);
    ADD_INT_CONST(m, HCISETSCOMTU);
    ADD_INT_CONST(m, HCIINQUIRY);

    ADD_INT_CONST(m, ACL_LINK);
    ADD_INT_CONST(m, SCO_LINK);

    /* RFCOMM */
    ADD_INT_CONST(m, RFCOMM_LM);
    ADD_INT_CONST(m, RFCOMM_LM_MASTER);
    ADD_INT_CONST(m, RFCOMM_LM_AUTH	);
    ADD_INT_CONST(m, RFCOMM_LM_ENCRYPT);
    ADD_INT_CONST(m, RFCOMM_LM_TRUSTED);
    ADD_INT_CONST(m, RFCOMM_LM_RELIABLE);
    ADD_INT_CONST(m, RFCOMM_LM_SECURE);

    /* L2CAP */
    ADD_INT_CONST(m, L2CAP_LM);
    ADD_INT_CONST(m, L2CAP_LM_MASTER);
    ADD_INT_CONST(m, L2CAP_LM_AUTH);
    ADD_INT_CONST(m, L2CAP_LM_ENCRYPT);
    ADD_INT_CONST(m, L2CAP_LM_TRUSTED);
    ADD_INT_CONST(m, L2CAP_LM_RELIABLE);
    ADD_INT_CONST(m, L2CAP_LM_SECURE);

    ADD_INT_CONST(m, L2CAP_COMMAND_REJ);
    ADD_INT_CONST(m, L2CAP_CONN_REQ	);
    ADD_INT_CONST(m, L2CAP_CONN_RSP	);
    ADD_INT_CONST(m, L2CAP_CONF_REQ	);
    ADD_INT_CONST(m, L2CAP_CONF_RSP	);
    ADD_INT_CONST(m, L2CAP_DISCONN_REQ);
    ADD_INT_CONST(m, L2CAP_DISCONN_RSP);
    ADD_INT_CONST(m, L2CAP_ECHO_REQ	);
    ADD_INT_CONST(m, L2CAP_ECHO_RSP	);
    ADD_INT_CONST(m, L2CAP_INFO_REQ	);
    ADD_INT_CONST(m, L2CAP_INFO_RSP	);

    ADD_INT_CONST(m, L2CAP_MODE_BASIC);
    ADD_INT_CONST(m, L2CAP_MODE_RETRANS);
    ADD_INT_CONST(m, L2CAP_MODE_FLOWCTL);
    ADD_INT_CONST(m, L2CAP_MODE_ERTM);
    ADD_INT_CONST(m, L2CAP_MODE_STREAMING);

    ADD_INT_CONST(m, BT_SECURITY);
    ADD_INT_CONST(m, BT_SECURITY_SDP);
    ADD_INT_CONST(m, BT_SECURITY_LOW);
    ADD_INT_CONST(m, BT_SECURITY_MEDIUM);
    ADD_INT_CONST(m, BT_SECURITY_HIGH);

#ifdef BT_DEFER_SETUP
    ADD_INT_CONST(m, BT_DEFER_SETUP);
#endif
    ADD_INT_CONST(m, SOL_BLUETOOTH);

#undef ADD_INT_CONST
}

/*
 * Affix socket module 
 * Socket module for python based in the original socket module for python
 * This code is a copy from socket.c source code from python2.2 with
 * updates/modifications to support affix socket interface * 
 *   AAA     FFFFFFF FFFFFFF IIIIIII X     X    
 * A     A   F       F          I     X   X
 * A     A   F       F      I      X X
 * AAAAAAA   FFFF    FFFF       I      X X
 * A     A   F       F      I     X   X
 * A     A   F       F       IIIIIII X     X
 * 
 * Any modifications of this sourcecode must keep this information !!!!!
 *
 * by Carlos Chinea
 * (C) Nokia Research Center, 2004
*/
