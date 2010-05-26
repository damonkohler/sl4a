# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 716 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/getuid.al)"
sub getuid {
    usage "getuid()" if @_ != 0;
    $<;
}

# end of POSIX::getuid
1;
