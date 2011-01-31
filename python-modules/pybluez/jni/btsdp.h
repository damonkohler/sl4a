#ifndef __pybluez_sdp_h__
#define __pybluez_sdp_h__

#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

typedef struct {
    PyObject_HEAD
    sdp_session_t *session;

	PyObject *(*errorhandler)(void); /* Error handler; checks
					    errno, returns NULL and
					    sets a Python exception */
} PySDPSessionObject;

extern PyTypeObject sdp_session_type;

#endif
