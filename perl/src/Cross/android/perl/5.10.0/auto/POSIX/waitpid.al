# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 611 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/waitpid.al)"
sub waitpid {
    usage "waitpid(pid, options)" if @_ != 2;
    CORE::waitpid($_[0], $_[1]);
}

# end of POSIX::waitpid
1;
