# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 754 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/utime.al)"
sub utime {
    usage "utime(filename, atime, mtime)" if @_ != 3;
    CORE::utime($_[1], $_[2], $_[0]);
}

# end of POSIX::utime
1;
