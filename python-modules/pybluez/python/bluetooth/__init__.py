import sys
import os
from btcommon import *

__version__ = 0.19

def _dbg(*args):
    return
    sys.stderr.write(*args)
    sys.stderr.write("\n")

if sys.platform == "win32":
    _dbg("trying widcomm")
    have_widcomm = False
    dll = "wbtapi.dll"
    sysroot = os.getenv ("SystemRoot")
    if os.path.exists (dll) or \
       os.path.exists (os.path.join (sysroot, "system32", dll)) or \
       os.path.exists (os.path.join (sysroot, dll)):
        try:
            import widcomm
            if widcomm.inquirer.is_device_ready ():
                # if the Widcomm stack is active and a Bluetooth device on that
                # stack is detected, then use the Widcomm stack
                from widcomm import *
                have_widcomm = True
        except ImportError: 
            pass

    if not have_widcomm:
        # otherwise, fall back to the Microsoft stack
        _dbg("Widcomm not ready. falling back to MS stack")
        from msbt import *

elif sys.platform.startswith("linux"):
    from bluez import *
elif sys.platform == "darwin":
    from osx import *

discover_devices.__doc__ = \
    """
    performs a bluetooth device discovery using the first available bluetooth
    resource.

    if lookup_names is False, returns a list of bluetooth addresses.
    if lookup_names is True, returns a list of (address, name) tuples

    lookup_names=False
        if set to True, then discover_devices also attempts to lookup the
        display name of each detected device.
    """

lookup_name.__doc__ = \
    """
    Tries to determine the friendly name (human readable) of the device with
    the specified bluetooth address.  Returns the name on success, and None
    on failure.
    """

advertise_service.__doc__ = \
    """
    Advertises a service with the local SDP server.  sock must be a bound,
    listening socket.  name should be the name of the service, and service_id 
    (if specified) should be a string of the form 
    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX", where each 'X' is a hexadecimal
    digit.

    service_classes is a list of service classes whose this service belongs to.
    Each class service is a 16-bit UUID in the form "XXXX", where each 'X' is a
    hexadecimal digit, or a 128-bit UUID in the form
    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX". There are some constants for
    standard services, e.g. SERIAL_PORT_CLASS that equals to "1101". Some class
    constants:

    SERIAL_PORT_CLASS        LAN_ACCESS_CLASS         DIALUP_NET_CLASS 
    HEADSET_CLASS            CORDLESS_TELEPHONY_CLASS AUDIO_SOURCE_CLASS
    AUDIO_SINK_CLASS         PANU_CLASS               NAP_CLASS
    GN_CLASS

    profiles is a list of service profiles that thie service fulfills. Each
    profile is a tuple with ( uuid, version). Most standard profiles use
    standard classes as UUIDs. PyBluez offers a list of standard profiles,
    for example SERIAL_PORT_PROFILE. All standard profiles have the same
    name as the classes, except that _CLASS suffix is replaced by _PROFILE.

    provider is a text string specifying the provider of the service

    description is a text string describing the service

    A note on working with Symbian smartphones:
        bt_discover in Python for Series 60 will only detect service records
        with service class SERIAL_PORT_CLASS and profile SERIAL_PORT_PROFILE

    """

stop_advertising.__doc__ = \
    """
    Instructs the local SDP server to stop advertising the service associated
    with sock.  You should typically call this right before you close sock.
    """

find_service.__doc__ = \
    """
    find_service (name = None, uuid = None, address = None)

    Searches for SDP services that match the specified criteria and returns
    the search results.  If no criteria are specified, then returns a list of
    all nearby services detected.  If more than one is specified, then
    the search results will match all the criteria specified.  If uuid is
    specified, it must be either a 16-bit UUID in the form "XXXX", where each
    'X' is a hexadecimal digit, or as a 128-bit UUID in the form
    "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX".  A special case of address is
    "localhost", which will search for services on the local machine.

    The search results will be a list of dictionaries.  Each dictionary
    represents a search match and will have the following key/value pairs:

      host          - the bluetooth address of the device advertising the
                      service
      name          - the name of the service being advertised
      description   - a description of the service being advertised
      provider      - the name of the person/organization providing the service
      protocol      - either 'RFCOMM', 'L2CAP', None if the protocol was not
                      specified, or 'UNKNOWN' if the protocol was specified but
                      unrecognized
      port          - the L2CAP PSM # if the protocol is 'L2CAP', the RFCOMM
                      channel # if the protocol is 'RFCOMM', or None if it
                      wasn't specified
      service-classes - a list of service class IDs (UUID strings).  possibly
                        empty
      profiles        - a list of profiles - (UUID, version) pairs - the
                        service claims to support.  possibly empty.
      service-id      - the Service ID of the service.  None if it wasn't set
                        See the Bluetooth spec for the difference between
                        Service ID and Service Class ID List
    """
