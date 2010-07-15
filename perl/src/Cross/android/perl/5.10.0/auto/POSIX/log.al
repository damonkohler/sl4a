# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 181 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/log.al)"
sub log {
    usage "log(x)" if @_ != 1;
    CORE::log($_[0]);
}

# end of POSIX::log
1;
