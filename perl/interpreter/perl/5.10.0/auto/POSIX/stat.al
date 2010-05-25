# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 596 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/stat.al)"
sub stat {
    usage "stat(filename)" if @_ != 1;
    CORE::stat($_[0]);
}

# end of POSIX::stat
1;
