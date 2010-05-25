# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 606 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/wait.al)"
sub wait {
    usage "wait()" if @_ != 0;
    CORE::wait();
}

# end of POSIX::wait
1;
