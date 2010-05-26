# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 489 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/system.al)"
sub system {
    usage "system(command)" if @_ != 1;
    CORE::system($_[0]);
}

# end of POSIX::system
1;
