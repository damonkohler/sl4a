# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 136 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/errno.al)"
sub errno {
    usage "errno()" if @_ != 0;
    $! + 0;
}

# end of POSIX::errno
1;
