#ifndef __btmodule_h__
#define __btmodule_h__

#include "python2.6/Python.h"
#include <bluetooth/bluetooth.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/* The object holding a socket.  It holds some extra information,
   like the address family, which is used to decode socket address
   arguments properly. */

typedef struct {
	PyObject_HEAD
	int sock_fd;	/* Socket file descriptor */
	int sock_family;	/* Address family, always AF_BLUETOOTH */
	int sock_type;		/* Socket type, e.g., SOCK_STREAM */
	int sock_proto;		/* Protocol type, e.g., BTPROTO_L2CAP */
	PyObject *(*errorhandler)(void); /* Error handler; checks
					    errno, returns NULL and
					    sets a Python exception */
	double sock_timeout;		 /* Operation timeout in seconds;
					    0.0 means non-blocking */

    int is_listening_socket;    // XXX this is a hack to make 
                                // sdp_advertise_service easier

    uint32_t sdp_record_handle; // if it's a listening socket and advertised 
                                // via SDP, this is the SDP handle
    sdp_session_t *sdp_session;
} PySocketSockObject;

#ifdef __cplusplus
}
#endif

extern PyObject *bluetooth_error;

#endif // __btmodule_h__
