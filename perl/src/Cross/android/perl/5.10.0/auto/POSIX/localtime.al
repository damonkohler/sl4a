# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 621 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/localtime.al)"
sub localtime {
    usage "localtime(time)" if @_ != 1;
    CORE::localtime($_[0]);
}

# end of POSIX::localtime
1;
