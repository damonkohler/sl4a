# NOTE: Derived from ../../lib/../xlib/arm/POSIX.pm.
# Changes made here will be lost when autosplit is run again.
# See AutoSplit.pm.
package POSIX;

#line 126 "../../lib/../xlib/arm/POSIX.pm (autosplit into ../../lib/../xlib/arm/auto/POSIX/readdir.al)"
sub readdir {
    usage "readdir(dirhandle)" if @_ != 1;
    CORE::readdir($_[0]);
}

# end of POSIX::readdir
1;
