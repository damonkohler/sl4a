# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 601 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/umask.al)"
sub umask {
    usage "umask(mask)" if @_ != 1;
    CORE::umask($_[0]);
}

# end of POSIX::umask
1;
