# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 744 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/sleep.al)"
sub sleep {
    usage "sleep(seconds)" if @_ != 1;
    $_[0] - CORE::sleep($_[0]);
}

# end of POSIX::sleep
1;
