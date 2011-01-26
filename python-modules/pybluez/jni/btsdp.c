#include "python2.6/Python.h"

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
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>

#include "btmodule.h"
#include "btsdp.h"

extern PyTypeObject sock_type;
extern int getsockaddrlen(PySocketSockObject *s, socklen_t *len_ret);
extern PyObject *set_error(void);
extern int str2uuid( const char *uuid_str, uuid_t *uuid );
extern void uuid2str( const uuid_t *uuid, char *dest );

// =================== utility functions =====================

static void 
dict_set_str_pyobj(PyObject *dict, const char *key, PyObject *valobj)
{
    PyObject *keyobj;
    keyobj = PyString_FromString( key );
    PyDict_SetItem( dict, keyobj, valobj );
    Py_DECREF( keyobj );
}

static void 
dict_set_strings(PyObject *dict, const char *key, const char *val)
{
    PyObject *keyobj, *valobj;
    keyobj = PyString_FromString( key );
    valobj = PyString_FromString( val );
    PyDict_SetItem( dict, keyobj, valobj );
    Py_DECREF( keyobj );
    Py_DECREF( valobj );
}

static void 
dict_set_str_long(PyObject *dict, const char *key, long val)
{
    PyObject *keyobj, *valobj;
    keyobj = PyString_FromString( key );
    valobj = PyInt_FromLong(val);
    PyDict_SetItem( dict, keyobj, valobj );
    Py_DECREF( keyobj );
    Py_DECREF( valobj );
}

PyDoc_STRVAR(sess_doc, 
"SDPSession()\n\
\n\
TODO\n\
");

/* 
 * utility function to perform an SDP search on a connected session.  Builds 
 * and returns a python list of dictionaries.  Each dictionary represents a 
 * service record match
 */
static PyObject *
do_search( sdp_session_t *sess, uuid_t *uuid )
{
    sdp_list_t *response_list = NULL, *attrid_list, *search_list, *r;
    uint32_t range = 0x0000ffff;
    char buf[1024] = { 0 };
    int err = 0;
    PyObject *result = 0;

	PyObject *rtn_list = PyList_New(0);
    if( ! rtn_list ) return 0;
    search_list = sdp_list_append( 0, uuid );
    attrid_list = sdp_list_append( 0, &range );

    // perform the search
    Py_BEGIN_ALLOW_THREADS
    err = sdp_service_search_attr_req( sess, search_list, \
            SDP_ATTR_REQ_RANGE, attrid_list, &response_list);
    Py_END_ALLOW_THREADS
    if( err ) {
        PyErr_SetFromErrno( bluetooth_error );
        result = 0;
        goto cleanup;
    }

    // parse the results (ewww....)

    // go through each of the service records
    for (r = response_list; r; r = r->next ) {
        PyObject *dict = PyDict_New();
        sdp_record_t *rec = (sdp_record_t*) r->data;
        sdp_list_t *proto_list = NULL,
                   *svc_class_list = NULL,
                   *profile_list = NULL;
        PyObject *py_class_list = NULL, *py_profile_list = NULL; 
        uuid_t service_id = { 0 };

        if( ! dict ) return 0;

        // initialize service class list
        py_class_list = PyList_New(0);
        if( ! py_class_list ) return 0;
        dict_set_str_pyobj( dict, "service-classes", py_class_list );
        Py_DECREF( py_class_list );

        // initialize profile list
        py_profile_list = PyList_New(0);
        if( ! py_profile_list ) return 0;
        dict_set_str_pyobj( dict, "profiles", py_profile_list );
        Py_DECREF( py_profile_list );

        // set service name
        if( ! sdp_get_service_name( rec, buf, sizeof(buf) ) ) {
            dict_set_strings( dict, "name", buf );
            memset(buf, 0, sizeof( buf ) );
        } else {
            dict_set_str_pyobj( dict, "name", Py_None );
        }

        // set service description
        if( ! sdp_get_service_desc( rec, buf, sizeof(buf) ) ) {
            dict_set_strings( dict, "description", buf );
            memset(buf, 0, sizeof( buf ) );
        } else {
            dict_set_str_pyobj( dict, "description", Py_None );
        }

        // set service provider name
        if( ! sdp_get_provider_name( rec, buf, sizeof(buf) ) ) {
            dict_set_strings( dict, "provider", buf );
            memset(buf, 0, sizeof( buf ) );
        } else {
            dict_set_str_pyobj( dict, "provider", Py_None );
        }

        // set service id
        if( ! sdp_get_service_id( rec, &service_id ) ) {
            uuid2str( &service_id, buf );
            dict_set_strings( dict, "service-id", buf );
            memset(buf, 0, sizeof( buf ) );
        } else {
            dict_set_str_pyobj( dict, "service-id", Py_None );
        }
        
        // get a list of the protocol sequences
        if( sdp_get_access_protos( rec, &proto_list ) == 0 ) {
            sdp_list_t *p = proto_list;
            int port;

            if( ( port = sdp_get_proto_port( p, RFCOMM_UUID ) ) != 0 ) {
                dict_set_strings( dict, "protocol", "RFCOMM" );
                dict_set_str_long( dict, "port", port );
            } else if ( (port = sdp_get_proto_port( p, L2CAP_UUID ) ) != 0 ) {
                dict_set_strings( dict, "protocol", "L2CAP" );
                dict_set_str_long( dict, "port", port );
            } else {
                dict_set_strings( dict, "protocol", "UNKNOWN" );
                dict_set_str_pyobj( dict, "port", Py_None );
            }

            // sdp_get_access_protos allocates data on the heap for the
            // protocol list, so we need to free the results...
            for( ; p ; p = p->next ) {
                sdp_list_free( (sdp_list_t*)p->data, 0 );
            }
            sdp_list_free( proto_list, 0 );
        } else {
            dict_set_str_pyobj( dict, "protocol", Py_None );
            dict_set_str_pyobj( dict, "port", Py_None );
        }

        // get a list of the service classes
        if( sdp_get_service_classes( rec, &svc_class_list ) == 0 ) {
            sdp_list_t *iter;
            for( iter = svc_class_list; iter != NULL; iter = iter->next ) {
                PyObject *pystr;
                char uuid_str[40] = { 0 };

                uuid2str( (uuid_t*)iter->data, uuid_str );
                pystr = PyString_FromString( uuid_str );
                PyList_Append( py_class_list, pystr );
                Py_DECREF( pystr );
            }
            
            sdp_list_free( svc_class_list, free );
        }
        
        // get a list of the profiles
        if( sdp_get_profile_descs( rec, &profile_list ) == 0 ) {
            sdp_list_t *iter;
            for( iter = profile_list; iter != NULL; iter = iter->next ) {
                PyObject *tuple, *py_uuid, *py_version;
                sdp_profile_desc_t *desc = (sdp_profile_desc_t*)iter->data;
                char uuid_str[40] = { 0 };

                uuid2str( &desc->uuid, uuid_str );
                py_uuid = PyString_FromString( uuid_str );
                py_version = PyInt_FromLong( desc->version );

                tuple = PyTuple_New( 2 );
                PyList_Append( py_profile_list, tuple );
                Py_DECREF( tuple );

                PyTuple_SetItem( tuple, 0, py_uuid );
                PyTuple_SetItem( tuple, 1, py_version );
//                Py_DECREF( py_uuid );
//                Py_DECREF( py_version );
            }
            sdp_list_free( profile_list, free );
        }

        PyList_Append( rtn_list, dict );
        Py_DECREF( dict );

        sdp_record_free( rec );
    }

    result = rtn_list;

cleanup:
    sdp_list_free( response_list, 0 );
    sdp_list_free( search_list, 0 );
    sdp_list_free( attrid_list, 0 );
    return result;
}

// ==================== SDPSession methods ===========================

// connect
static PyObject *
sess_connect(PySDPSessionObject *s, PyObject *args, PyObject *kwds)
{
    bdaddr_t src; 
    bdaddr_t dst; 
    char *dst_buf = "localhost";
    uint32_t flags = SDP_RETRY_IF_BUSY;

	static char *keywords[] = {"target", 0};

    bacpy( &src, BDADDR_ANY );
    bacpy( &dst, BDADDR_LOCAL );

    if( s->session != NULL ) {
        sdp_close( s->session );
    }

	if (!PyArg_ParseTupleAndKeywords(args, kwds,
					 "|s", keywords,
					 &dst_buf))
		return NULL;

    if( strncmp( dst_buf, "localhost", 18 ) != 0 ) {
        str2ba( dst_buf, &dst );
    } else {
        // XXX
    }

	Py_BEGIN_ALLOW_THREADS
    s->session = sdp_connect( &src, &dst, flags );
	Py_END_ALLOW_THREADS
    if( s->session == NULL ) 
        return PyErr_SetFromErrno( bluetooth_error );

    Py_INCREF(Py_None);
    return Py_None;
}
PyDoc_STRVAR(sess_connect_doc,
"connect( dest = \"localhost\" )\n\
\n\
Connects the SDP session to the SDP server specified by dest.  If the\n\
session was already connected, it's closed first.\n\
\n\
dest specifies the bluetooth address of the server to connect to.  Special\n\
case is \"localhost\"\n\
\n\
raises _bluetooth.error if something goes wrong\n\
");

// close
static PyObject *
sess_close(PySDPSessionObject *s)
{
    if( s->session != NULL ) {
        Py_BEGIN_ALLOW_THREADS
        sdp_close( s->session );
        Py_END_ALLOW_THREADS
        s->session = NULL;
    }
    Py_INCREF(Py_None);
    return Py_None;
}
PyDoc_STRVAR(sess_close_doc,
"close()\n\
\n\
closes the connection with the SDP server.  No effect if a session is not open.\n\
");

// fileno
static PyObject *
sess_fileno(PySDPSessionObject *s)
{
	return PyInt_FromLong((long) s->session->sock);
}
PyDoc_STRVAR(sess_fileno_doc,
"fileno() -> integer\n\
\n\
Return the integer file descriptor of the socket.\n\
You can use this for direct communication with the SDP server.\n\
");

// search
static PyObject *
sess_search(PySDPSessionObject *s, PyObject *args, PyObject *kwds)
{
    char *uuid_str = 0;
    uuid_t uuid = { 0 };
    PyObject *result = 0;

    if (!PyArg_ParseTuple(args, "s", &uuid_str)) return NULL;

    // convert the UUID string into a uuid_t
    if( ! str2uuid( uuid_str, &uuid ) ) {
        PyErr_SetString(PyExc_ValueError, "invalid UUID!");
        return NULL;
    }

    // make sure the SDP session is open
    if( ! s->session ) {
        PyErr_SetString( bluetooth_error, "SDP session is not active!" );
        return 0;
     }

    // perform the search
    result = do_search( s->session, &uuid );

    return result;
}
PyDoc_STRVAR(sess_search_doc,
"search( UUID )\n\
\n\
Searches for a service record with the specified UUID.  If no match is found,\n\
returns None.  Otherwise, returns a dictionary\n\
\n\
UUID must be in the form \"XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX\", \n\
where each X is a hexadecimal digit.\n\
");

// browse
static PyObject *
sess_browse(PySDPSessionObject *s)
{
    uuid_t uuid = { 0 };
    PyObject *result = 0;

    // convert the UUID string into a uuid_t
    sdp_uuid16_create(&uuid, PUBLIC_BROWSE_GROUP);

    // make sure the SDP session is open
    if( ! s->session ) {
        PyErr_SetString( bluetooth_error, "SDP session is not active!" );
        return 0;
     }

    // perform the search
    result = do_search( s->session, &uuid );

    return result;
}
PyDoc_STRVAR(sess_browse_doc,
"browse()\n\
\n\
Browses all services advertised by connected SDP session\n\
");

static PyMethodDef sess_methods[] = {
    { "search", (PyCFunction) sess_search, METH_VARARGS, 
        sess_search_doc },
    { "browse", (PyCFunction) sess_browse, METH_NOARGS, 
        sess_browse_doc },
    { "fileno", (PyCFunction)sess_fileno, METH_NOARGS, 
        sess_fileno_doc },
    { "connect", (PyCFunction) sess_connect, METH_VARARGS | METH_KEYWORDS, 
        sess_connect_doc },
    { "close", (PyCFunction)sess_close, METH_NOARGS, 
        sess_close_doc },
    {NULL, NULL}
};


/* =============== object maintenance =============== */

/* Deallocate a socket object in response to the last Py_DECREF().
   First close the file description. */

static void
sess_dealloc(PySDPSessionObject *s)
{
    if(s->session != NULL) {
        sdp_close( s->session );
        s->session = NULL;
    }
	s->ob_type->tp_free((PyObject *)s);
}

static PyObject *
sess_repr(PySDPSessionObject *s)
{
	char buf[512];
    if (s->session != NULL) {
        PyOS_snprintf( buf, sizeof(buf), 
                "<SDP Session object - connected>");
    } else { 
        PyOS_snprintf( buf, sizeof(buf), 
                "<SDP Session object - unconnected>");
    }
	return PyString_FromString(buf);
}


/* Create a new, uninitialized socket object. */

static PyObject *
sess_new(PyTypeObject *type, PyObject *args, PyObject *kwds)
{
	PyObject *newsess;

	newsess = type->tp_alloc(type, 0);
	if (newsess != NULL) {
        ((PySDPSessionObject *)newsess)->session = NULL;
	}
	return newsess;
}


/* Initialize a new socket object. */

/*ARGSUSED*/
static int
sess_initobj(PyObject *self, PyObject *args, PyObject *kwds)
{
    PySDPSessionObject *s = (PySDPSessionObject *)self;
    s->errorhandler = &set_error;

	/* From now on, ignore SIGPIPE and let the error checking
	   do the work. */
#ifdef SIGPIPE
	(void) signal(SIGPIPE, SIG_IGN);
#endif

	return 0;

}


/* Type object for socket objects. */

PyTypeObject sdp_session_type = {
	PyObject_HEAD_INIT(0)	/* Must fill in type value later */
	0,					/* ob_size */
	"_bluetooth.SDPSession",			/* tp_name */
	sizeof(PySDPSessionObject),		/* tp_basicsize */
	0,					/* tp_itemsize */
	(destructor)sess_dealloc,		/* tp_dealloc */
	0,					/* tp_print */
	0,					/* tp_getattr */
	0,					/* tp_setattr */
	0,					/* tp_compare */
	(reprfunc)sess_repr,			/* tp_repr */
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
	sess_doc,				/* tp_doc */
	0,					/* tp_traverse */
	0,					/* tp_clear */
	0,					/* tp_richcompare */
	0,					/* tp_weaklistoffset */
	0,					/* tp_iter */
	0,					/* tp_iternext */
	sess_methods,				/* tp_methods */
	0,					/* tp_members */
	0,					/* tp_getset */
	0,					/* tp_base */
	0,					/* tp_dict */
	0,					/* tp_descr_get */
	0,					/* tp_descr_set */
	0,					/* tp_dictoffset */
	sess_initobj,				/* tp_init */
	PyType_GenericAlloc,			/* tp_alloc */
	sess_new,				/* tp_new */
	PyObject_Del,				/* tp_free */
};
