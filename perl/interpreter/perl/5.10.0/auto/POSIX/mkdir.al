# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 591 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/mkdir.al)"
sub mkdir {
    usage "mkdir(directoryname, mode)" if @_ != 2;
    CORE::mkdir($_[0], $_[1]);
}

# end of POSIX::mkdir
1;
