# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 227 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/kill.al)"
sub kill {
    usage "kill(pid, sig)" if @_ != 2;
    CORE::kill $_[1], $_[0];
}

# end of POSIX::kill
1;
