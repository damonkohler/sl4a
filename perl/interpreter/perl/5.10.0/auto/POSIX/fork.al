# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 670 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/fork.al)"
sub fork {
    usage "fork()" if @_ != 0;
    CORE::fork;
}

# end of POSIX::fork
1;
