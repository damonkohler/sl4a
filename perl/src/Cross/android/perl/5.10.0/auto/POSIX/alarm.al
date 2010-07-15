# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 631 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/alarm.al)"
sub alarm {
    usage "alarm(seconds)" if @_ != 1;
    CORE::alarm($_[0]);
}

# end of POSIX::alarm
1;
