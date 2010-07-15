# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 711 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/getppid.al)"
sub getppid {
    usage "getppid()" if @_ != 0;
    CORE::getppid;
}

# end of POSIX::getppid
1;
